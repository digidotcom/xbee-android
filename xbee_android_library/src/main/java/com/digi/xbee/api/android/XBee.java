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
package com.digi.xbee.api.android;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.digi.xbee.api.android.connection.usb.AndroidUSBInterface;
import com.digi.xbee.api.android.connection.usb.AndroidUSBPermissionListener;
import com.digi.xbee.api.android.connection.bluetooth.AndroidBluetoothInterface;
import com.digi.xbee.api.android.connection.serial.SerialPortDigiAndroid;
import com.digi.xbee.api.connection.IConnectionInterface;
import com.digi.xbee.api.connection.bluetooth.AbstractBluetoothInterface;
import com.digi.xbee.api.connection.serial.SerialPortParameters;

public class XBee {
    /**
     * Returns an XBee Android connection interface for the given context and
     * baud rate.
     *
     * <p>This constructor uses the Android USB host interface API to
     * communicate with the devices.</p>
     *
     * @param context The Android application context.
     * @param baudRate The USB connection baud rate.
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null}.
     * @throws IllegalArgumentException if {@code baudRate < 1}.
     *
     * @see #createConnectiontionInterface(Context, int, AndroidUSBPermissionListener)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     */
    public static IConnectionInterface createConnectiontionInterface(Context context, int baudRate) {
        return createConnectiontionInterface(context, baudRate, null);
    }

    /**
     * Returns an XBee Android connection interface for the given context and
     * baud rate.
     *
     * <p>This constructor uses the Android USB host interface API to
     * communicate with the devices.</p>
     *
     * @param context The Android application context.
     * @param baudRate The USB connection baud rate.
     * @param usbDevice The explicit UsbDevice handle to use
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null}.
     * @throws IllegalArgumentException if {@code baudRate < 1}.
     *
     * @see #createConnectiontionInterface(Context, int, AndroidUSBPermissionListener)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     */
    public static IConnectionInterface createConnectionInterface(Context context, int baudRate, UsbDevice usbDevice) {
        return createConnectionInterface(context, baudRate, usbDevice, null);
    }

    /**
     * Returns an XBee Android connection interface for the given context and
     * baud rate.
     *
     * <p>This constructor uses the Android USB host interface API to
     * communicate with the devices.</p>
     *
     * @param context The Android context.
     * @param baudRate The USB connection baud rate.
     * @param permissionListener The USB permission listener that will be
     *                           notified when user grants USB permissions.
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null}.
     * @throws IllegalArgumentException if {@code baudRate < 1}.
     *
     * @see #createConnectiontionInterface(Context, int)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     * @see AndroidUSBPermissionListener
     */
    public static IConnectionInterface createConnectiontionInterface(Context context, int baudRate, AndroidUSBPermissionListener permissionListener) {
        return new AndroidUSBInterface(context, baudRate, permissionListener);
    }

    /**
     * Returns an XBee Android connection interface for the given context and
     * baud rate.
     *
     * <p>This constructor uses the Android USB host interface API to
     * communicate with the devices.</p>
     *
     * @param context The Android context.
     * @param baudRate The USB connection baud rate.
     * @param usbDevice The explicit USBDevice handle to use
     * @param permissionListener The USB permission listener that will be
     *                           notified when user grants USB permissions.
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null}.
     * @throws IllegalArgumentException if {@code baudRate < 1}.
     *
     * @see #createConnectiontionInterface(Context, int)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     * @see AndroidUSBPermissionListener
     */
    public static IConnectionInterface createConnectionInterface(Context context, int baudRate, UsbDevice usbDevice, AndroidUSBPermissionListener permissionListener) {
        return new AndroidUSBInterface(context, baudRate, usbDevice, permissionListener);
    }

    /**
     * Retrieves an XBee Android connection interface for the given context,
     * port and baud rate.
     *
     * <p>This constructor uses the Digi Android Serial Port API based on the
     * RxTx library to communicate with the devices.</p>
     *
     * @param context The Android application context.
     * @param port The Android COM port.
     * @param baudRate The serial port connection baud rate.
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null} or
     *                              if {@code port == null}.
     * @throws IllegalArgumentException if {@code baudRate < 1}.
     *
     * @see #createConnectiontionInterface(Context, String, SerialPortParameters)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     */
    public static IConnectionInterface createConnectiontionInterface(Context context, String port, int baudRate) {
        return new SerialPortDigiAndroid(context, port, baudRate);
    }

    /**
     * Retrieves an XBee Android connection interface for the given context,
     * port and parameters.
     *
     * <p>This constructor uses the Digi Android Serial Port API based on the
     * RxTx library to communicate with the devices.</p>
     *
     * @param context The Android application context.
     * @param port The Android COM port.
     * @param serialPortParameters The serial port parameters.
     *
     * @return The XBee Android connection interface.
     *
     * @throws NullPointerException if {@code context == null} or
     *                              if {@code port == null} or
     *                              if {@code serialPortParameters == null}.
     *
     * @see #createConnectiontionInterface(Context, String, int)
     * @see com.digi.xbee.api.connection.IConnectionInterface
     * @see com.digi.xbee.api.connection.serial.SerialPortParameters
     */
    public static IConnectionInterface createConnectiontionInterface(Context context, String port, SerialPortParameters serialPortParameters) {
        return new SerialPortDigiAndroid(context, port, serialPortParameters);
    }

    /**
     * Retrieves an XBee Android Bluetooth connection interface for the given
     * context and Bluetooth device.
     *
     * @param context The Android application context.
     * @param bleDevice The Bluetooth device.
     *
     * @return The XBee Android Bluetooth connection interface.
     *
     * @throws NullPointerException if {@code context == null} or
     *                              if {@code bleDevice == null}.
     *
     * @see #createConnectionInterface(Context, String)
     * @see com.digi.xbee.api.connection.bluetooth.AbstractBluetoothInterface
     * @see BluetoothDevice
     */
    public static AbstractBluetoothInterface createConnectionInterface(Context context, BluetoothDevice bleDevice) {
        return new AndroidBluetoothInterface(context, bleDevice);
    }

    /**
     * Retrieves an XBee Android Bluetooth connection interface for the given
     * context and Bluetooth device address.
     *
     * @param context The Android application context.
     * @param deviceAddress The address of the Bluetooth device.
     *
     * @return The XBee Android Bluetooth connection interface.
     *
     * @throws IllegalArgumentException if the device address does not follow
     *                                  the format "00:11:22:33:AA:BB".
     * @throws NullPointerException if {@code context == null} or
     *                              if {@code deviceAddress == null}.
     *
     * @see #createConnectionInterface(Context, BluetoothDevice)
     * @see com.digi.xbee.api.connection.bluetooth.AbstractBluetoothInterface
     */
    public static AbstractBluetoothInterface createConnectionInterface(Context context, String deviceAddress) {
        return new AndroidBluetoothInterface(context, deviceAddress);
    }
}
