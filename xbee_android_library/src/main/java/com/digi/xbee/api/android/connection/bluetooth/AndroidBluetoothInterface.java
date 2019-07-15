/*
 * Copyright 2019, Digi International Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.digi.xbee.api.android.connection.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.digi.xbee.api.connection.bluetooth.AbstractBluetoothInterface;
import com.digi.xbee.api.exceptions.InvalidInterfaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class represents a communication interface with XBee devices over
 * Bluetooth Low Energy.
 */
public class AndroidBluetoothInterface extends AbstractBluetoothInterface {

	// Constants.
	private static final int CHAR_PROP_INDICATE = 32;

	private static final int CONNECTION_TIMEOUT = 20000;
	private static final int DISCONNECTION_TIMEOUT = 10000;
	private static final int SERVICES_TIMEOUT = 10000;
	private static final int WRITE_TIMEOUT = 2000;

	private static final int RETRIES_CONNECT = 3;

	// Variables.
	private Context context;

	private BluetoothDevice device;
	private BluetoothGatt bluetoothGatt;
	private BluetoothGattCharacteristic txCharacteristic;
	private BluetoothGattCharacteristic rxCharacteristic;

	private BlCircularByteBuffer inputByteBuffer;
	private BlCircularByteBuffer outputByteBuffer;

	private boolean isOpen = false;
	private boolean writeTaskRunning = false;
	private boolean dataWritten = false;

	private WriteTask writeTask;

	private final Object connectionLock = new Object();
	private final Object disconnectionLock = new Object();
	private final Object servicesLock = new Object();
	private final Object descriptorLock = new Object();
	private final Object writeCharLock = new Object();

	private BLEGattCallback bleGattCallback;

	private Logger logger;

	/**
	 * Class constructor. Instantiates a new {@code AndroidBluetoothInterface}
	 * with the given parameters.
	 *
	 * @param context The Android application context.
	 * @param device The Bluetooth device to connect to.
	 *
	 * @throws NullPointerException if {@code context == null} or
	 *                              if {@code device == null}.
	 */
	public AndroidBluetoothInterface(Context context, BluetoothDevice device) {
		if (context == null)
			throw new NullPointerException("Android context cannot be null.");
		if (device == null)
			throw new NullPointerException("Bluetooth device cannot be null.");

		this.context = context;
		this.device = device;

		logger = LoggerFactory.getLogger(AndroidBluetoothInterface.class);
		bleGattCallback = new BLEGattCallback();
	}

	/**
	 * Class constructor. Instantiates a new {@code BluetoothInterface}
	 * with the given parameters.
	 *
	 * @param context The Android application context.
	 * @param deviceAddress The address of the Bluetooth device to connect to.
	 *
	 * @throws IllegalArgumentException if the device address does not follow
	 *                                  the format "00:11:22:33:AA:BB".
	 * @throws NullPointerException if {@code context == null} or
	 *                              if {@code deviceAddress == null}.
	 */
	public AndroidBluetoothInterface(Context context, String deviceAddress) {
		if (context == null)
			throw new NullPointerException("Android context cannot be null.");
		if (deviceAddress == null)
			throw new NullPointerException("Bluetooth address cannot be null.");
		if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress.toUpperCase()))
			throw new IllegalArgumentException("Invalid Bluetooth address.");

		BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

		this.context = context;
		this.device = bluetoothAdapter.getRemoteDevice(deviceAddress.toUpperCase());

		logger = LoggerFactory.getLogger(AndroidBluetoothInterface.class);
		bleGattCallback = new BLEGattCallback();
	}

	@Override
	public void open() throws InvalidInterfaceException {
		// Do nothing if the device is already open.
		if (isOpen)
			return;

		// Connect the device. Try up to 3 times.
		int retries = RETRIES_CONNECT;
		while (!isOpen && retries > 0) {
			bluetoothGatt = device.connectGatt(context, false, bleGattCallback);
			// Wait until the device is connected.
			synchronized (connectionLock) {
				try {
					connectionLock.wait(CONNECTION_TIMEOUT);
				} catch (InterruptedException ignore) {
				}
			}
			retries -= 1;
		}

		// Check if the device is connected.
		if (!isOpen)
			throw new InvalidInterfaceException();

		// Discover the services.
		bluetoothGatt.discoverServices();
		// Wait until the services are discovered.
		synchronized (servicesLock) {
			try {
				servicesLock.wait(SERVICES_TIMEOUT);
			} catch (InterruptedException ignore) {}
		}

		// Get the TX and RX characteristics.
		BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_GUID));
		if (service == null)
			throw new InvalidInterfaceException("Could not get the communication service");
		txCharacteristic = service.getCharacteristic(UUID.fromString(TX_CHAR_GUID));
		rxCharacteristic = service.getCharacteristic(UUID.fromString(RX_CHAR_GUID));
		if (txCharacteristic == null || rxCharacteristic == null)
			throw new InvalidInterfaceException("Could not get the communication characteristics");

		// Subscribe to the RX characteristic.
		bluetoothGatt.setCharacteristicNotification(rxCharacteristic, true);
		byte[] descValue = (rxCharacteristic.getProperties() & CHAR_PROP_INDICATE) == CHAR_PROP_INDICATE ?
				BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
		for (BluetoothGattDescriptor descriptor : rxCharacteristic.getDescriptors()) {
			descriptor.setValue(descValue);
			bluetoothGatt.writeDescriptor(descriptor);
			// Wait until the descriptor is written.
			synchronized (descriptorLock) {
				try {
					descriptorLock.wait(WRITE_TIMEOUT);
				} catch (InterruptedException ignore) {}
			}
		}

		// Initialize the input and output streams.
		inputByteBuffer = new BlCircularByteBuffer();
		outputByteBuffer = new BlCircularByteBuffer();

		writeTask = new WriteTask(this, outputByteBuffer.getInputStream());
		writeTaskRunning = true;
		writeTask.start();

		encrypt = false;
	}

	@Override
	public void close() {
		if (!isOpen() || bluetoothGatt == null)
			return;

		// Unsubscribe from the RX characteristic.
		bluetoothGatt.setCharacteristicNotification(rxCharacteristic, false);
		for (BluetoothGattDescriptor descriptor : rxCharacteristic.getDescriptors()) {
			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			bluetoothGatt.writeDescriptor(descriptor);
			// Wait until the descriptor is written.
			synchronized (descriptorLock) {
				try {
					descriptorLock.wait(WRITE_TIMEOUT);
				} catch (InterruptedException ignore) {}
			}
		}

		// Close the streams.
		if (writeTask != null) {
			writeTaskRunning = false;
			writeTask = null;
		}
		if (inputByteBuffer != null) {
			try {
				inputByteBuffer.getOutputStream().close();
			} catch (IOException ignore) {}
			try {
				inputByteBuffer.getInputStream().close();
			} catch (IOException ignore) {}
			inputByteBuffer = null;
		}
		if (outputByteBuffer != null) {
			try {
				outputByteBuffer.getOutputStream().close();
			} catch (IOException ignore) {}
			try {
				outputByteBuffer.getInputStream().close();
			} catch (IOException ignore) {}
			outputByteBuffer = null;
		}

		// Disconnect the device.
		bluetoothGatt.disconnect();

		// Wait until the device is disconnected.
		synchronized (disconnectionLock) {
			try {
				disconnectionLock.wait(DISCONNECTION_TIMEOUT);
			} catch (InterruptedException ignore) {}
		}
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public InputStream getInputStream() {
		if (inputByteBuffer != null)
			return inputByteBuffer.getInputStream();
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		if (outputByteBuffer != null)
			return outputByteBuffer.getOutputStream();
		return null;
	}

	@Override
	public void writeData(byte[] data) {
		writeData(data, 0, data.length);
	}

	@Override
	public synchronized void writeData(byte[] data, int offset, int length) {
		byte[] dataToWrite = new byte[length];

		// Write the data in the TX characteristic.
		dataWritten = false;
		try {
			if (encrypt)
				cipherEnc.update(data, offset, length, dataToWrite, 0);
			else
				System.arraycopy(data, offset, dataToWrite, 0, length);

			txCharacteristic.setValue(dataToWrite);
			bluetoothGatt.writeCharacteristic(txCharacteristic);

			if (!dataWritten) {
				// Wait until the data is written.
				synchronized (writeCharLock) {
					writeCharLock.wait(WRITE_TIMEOUT);
				}
			}
		} catch (InterruptedException | ShortBufferException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public int readData(byte[] data) throws IOException {
		return readData(data, 0, data.length);
	}

	@Override
	public int readData(byte[] data, int offset, int length) throws IOException {
		int readBytes = 0;
		if (getInputStream() != null)
			readBytes = getInputStream().read(data, offset, length);
		return readBytes;
	}

	@Override
	public String toString() {
		return String.format("[%s] ", device.getAddress());
	}

	/**
	 * Class used to write user's data into the Bluetooth characteristic.
	 */
	class WriteTask extends Thread {
		private final AndroidBluetoothInterface iface;
		private final InputStream input;

		WriteTask(AndroidBluetoothInterface iface, InputStream input) {
			this.iface = iface;
			this.input = input;
		}

		@Override
		public void run() {
			try {
				while (writeTaskRunning) {
					int available = input.available();
					if (available > 0) {
						byte[] data = new byte[available];
						if (input.read(data) > 0)
							iface.writeData(data);
					} else {
						Thread.sleep(50);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Class used to handle the bluetooth interface events.
	 */
	class BLEGattCallback extends BluetoothGattCallback {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				isOpen = true;
				// Notify the connection lock.
				synchronized (connectionLock) {
					connectionLock.notify();
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				isOpen = false;
				bluetoothGatt.close();
				bluetoothGatt = null;
				// Notify the disconnection lock.
				synchronized (disconnectionLock) {
					disconnectionLock.notify();
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// Notify that the services has been discovered.
			synchronized (servicesLock) {
				servicesLock.notify();
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			// Notify that the descriptor has been written.
			synchronized (descriptorLock) {
				descriptorLock.notify();
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte[] value = characteristic.getValue();

			// If the communication is encrypted, decrypt the received data.
			if (encrypt) {
				try {
					cipherDec.update(value, 0, value.length, value, 0);
				} catch (ShortBufferException e) {
					logger.error(e.getMessage(), e);
				}
			}

			if (inputByteBuffer == null)
				return;

			try {
				inputByteBuffer.getOutputStream().write(value);
				inputByteBuffer.getOutputStream().flush();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			// Notify that data has been received.
			synchronized (AndroidBluetoothInterface.this) {
				AndroidBluetoothInterface.this.notify();
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			dataWritten = status == BluetoothGatt.GATT_SUCCESS;

			// Notify that the write operation has finished.
			synchronized (writeCharLock) {
				writeCharLock.notify();
			}
		}
	}
}
