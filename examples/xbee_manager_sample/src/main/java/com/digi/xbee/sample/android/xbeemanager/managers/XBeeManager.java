/*
 * Copyright 2017-2019, Digi International Inc.
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

package com.digi.xbee.sample.android.xbeemanager.managers;

import java.util.HashMap;

import android.content.Context;

import com.digi.xbee.sample.android.xbeemanager.XBeeConstants;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.android.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;

public class XBeeManager {
	
	// Constants.
	private final static String USB_HOST_API = "USB Host API";
	
	// Variables.
	private String port;
	
	private int baudRate;
	
	private XBeeDevice localDevice;
	
	private Context context;
	
	/**
	 * Class constructor. Instances a new {@code XBeeManager} object with the
	 * given parameters.
	 * 
	 * @param context The Android context.
	 */
	public XBeeManager(Context context) {
		this.context = context;
	}
	
	/**
	 * Creates the local XBee Device using the Android USB Host API with the
	 * given baud rate.
	 * 
	 * @param baudRate Baud rate to use in the XBee Connection.
	 */
	public void createXBeeDevice(int baudRate) {
		this.baudRate = baudRate;
		this.port = null;
		localDevice = new XBeeDevice(context, baudRate);
	}
	
	/**
	 * Creates the local XBee Device using the given serial port and baud rate.
	 * 
	 * @param port Local serial port to use in the XBee Connection.
	 * @param baudRate Baud rate to use in the XBee Connection.
	 */
	public void createXBeeDevice(String port, int baudRate) {
		this.port = port;
		this.baudRate = baudRate;
		localDevice = new XBeeDevice(context, port, baudRate);
	}
	
	/**
	 * Returns the local XBee device.
	 * 
	 * @return The local XBee device.
	 */
	public XBeeDevice getLocalXBeeDevice() {
		return localDevice;
	}
	
	/**
	 * Returns the port used for the XBee device connection.
	 * 
	 * @return The port used for the XBee device connection.
	 */
	public String getSerialPort() {
		return port;
	}
	
	/**
	 * Returns the baud rate used for the XBee device connection.
	 * 
	 * @return The baud rate used for the XBee device connection.
	 */
	public int getBaudRate() {
		return baudRate;
	}
	
	/**
	 * Sets the given parameter to the given value in the local device.
	 * 
	 * @param parameter AT parameter to set.
	 * @param value Value of the parameter.
	 *
	 * @throws XBeeException if there is a timeout or
	 *                       any other error executing the request.
	 */
	public void setLocalParameter(String parameter, byte[] value) throws XBeeException {
		localDevice.setParameter(parameter, value);
	}
	
	/**
	 * Reads the given parameter from the local device.
	 * 
	 * @param parameter AT parameter to read.
	 * 
	 * @return The value of the given parameter.
	 *
	 * @throws XBeeException if there is a timeout or
	 *                       any other error executing the request.
	 */
	public String getLocalParameter(String parameter) throws XBeeException {
		byte[] value = localDevice.getParameter(parameter);
		return HexUtils.byteArrayToHexString(value);
	}
	
	/**
	 * Returns a map with the basic read parameters of the local device.
	 * 
	 * @return Map with the basic read parameters of the local device.
	 * 
	 * @throws XBeeException if there is a timeout
	 */
	public HashMap<String, String> readBasicLocalParameters() throws XBeeException {
		localDevice.readDeviceInfo();
		HashMap<String, String> readParams = new HashMap<String, String>();
		readParams.put(XBeeConstants.PARAM_NODE_IDENTIFIER, localDevice.getNodeID());
		readParams.put(XBeeConstants.PARAM_MAC_ADDRESS, localDevice.get64BitAddress().toString());
		readParams.put(XBeeConstants.PARAM_FIRMWARE_VERSION, localDevice.getFirmwareVersion());
		readParams.put(XBeeConstants.PARAM_HARDWARE_VERSION, localDevice.getHardwareVersion().toString());
		readParams.put(XBeeConstants.PARAM_XBEE_PROTOCOL, localDevice.getXBeeProtocol().toString());
		readParams.put(XBeeConstants.PARAM_PAN_ID, "" + HexUtils.byteArrayToHexString(localDevice.getPANID()));
		readParams.put(XBeeConstants.PARAM_DEST_ADDRESS_H, HexUtils.byteArrayToHexString(localDevice.getParameter(XBeeConstants.AT_COMMAND_DH)));
		readParams.put(XBeeConstants.PARAM_DEST_ADDRESS_L, HexUtils.byteArrayToHexString(localDevice.getParameter(XBeeConstants.AT_COMMAND_DL)));
		byte[] ntValue = localDevice.getParameter(XBeeConstants.AT_COMMAND_NT);
		long nodeDiscoveryTime = ByteUtils.byteArrayToLong(ntValue) * 100;
		readParams.put(XBeeConstants.PARAM_NODE_DISCOVERY_TIME, "" + nodeDiscoveryTime);
		byte[] irValue = localDevice.getParameter(XBeeConstants.AT_COMMAND_IR);
		long samplingRateTime = ByteUtils.byteArrayToLong(irValue);
		readParams.put(XBeeConstants.PARAM_IO_SAMPLING_RATE, "" + samplingRateTime);
		if (port == null)
			readParams.put(XBeeConstants.PARAM_SERIAL_PORT, USB_HOST_API);
		else
			readParams.put(XBeeConstants.PARAM_SERIAL_PORT, port);
		readParams.put(XBeeConstants.PARAM_BAUD_RATE, "" + baudRate);
		return readParams;
	}
	
	/**
	 * Returns a map with the basic read parameters of the given remote device.
	 * 
	 * @param remoteDevice Remote device to read its parameters.
	 * 
	 * @return Map with the basic read parameters of the local device.
	 * 
	 * @throws XBeeException if there is a timeout
	 *                       or any other error executing the request.
	 */
	public HashMap<String, String> readBasicRemoteParameters(RemoteXBeeDevice remoteDevice) throws XBeeException {
		remoteDevice.readDeviceInfo();
		HashMap<String, String> readParams = new HashMap<String, String>();
		readParams.put(XBeeConstants.PARAM_NODE_IDENTIFIER, remoteDevice.getNodeID());
		readParams.put(XBeeConstants.PARAM_MAC_ADDRESS, remoteDevice.get64BitAddress().toString());
		readParams.put(XBeeConstants.PARAM_FIRMWARE_VERSION, remoteDevice.getFirmwareVersion());
		readParams.put(XBeeConstants.PARAM_HARDWARE_VERSION, remoteDevice.getHardwareVersion().toString());
		readParams.put(XBeeConstants.PARAM_XBEE_PROTOCOL, remoteDevice.getXBeeProtocol().toString());
		return readParams;
	}
	
	/**
	 * Sends the given data to the given remote device.
	 * 
	 * @param data Data to send.
	 * @param remoteDevice Remote XBee device to send data to.
	 * 
	 * @throws XBeeException if there is a timeout or
	 *                       any other error executing the request.
	 */
	public void sendDataToRemote(byte[] data, RemoteXBeeDevice remoteDevice) throws XBeeException {
		localDevice.sendData(remoteDevice, data);
	}
	
	/**
	 * Retrieves the local XBee device 64-bit address.
	 * 
	 * @return The local XBee device 64-bit address.
	 */
	public XBee64BitAddress getLocalXBee64BitAddress() {
		return localDevice.get64BitAddress();
	}
	
	/**
	 * Adds the given listener to the list of listeners that will be notified
	 * on device discovery events.
	 * 
	 * @param listener Discovery listener to add.
	 */
	public void addDiscoveryListener(IDiscoveryListener listener) {
		localDevice.getNetwork().addDiscoveryListener(listener);
	}
	
	/**
	 * Starts the device discovery process.
	 */
	public void startDiscoveryProcess() {
		localDevice.getNetwork().startDiscoveryProcess();
	}
	
	/**
	 * Returns whether the device discovery process is running or not.
	 * 
	 * @return {@code true} if device discovery process is running, 
	 *         {@code false} otherwise.
	 */
	public boolean isDiscoveryRunning() {
		return localDevice.getNetwork().isDiscoveryRunning();
	}
	
	/**
	 * Saves changes to flash.
	 * 
	 * @throws XBeeException if there is a timeout or
	 *                          any other error during the operation.
	 */
	public void saveChanges() throws XBeeException {
		localDevice.writeChanges();
	}
	
	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when XBee data packets are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeDataPacketListener(IDataReceiveListener listener) {
		localDevice.addDataListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of data packet listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeDataPacketListener(IDataReceiveListener listener) {
		localDevice.removeDataListener(listener);
	}
	
	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when XBee IO packets are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeIOPacketListener(IIOSampleReceiveListener listener) {
		localDevice.addIOSampleListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of IO packet listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeIOPacketListener(IIOSampleReceiveListener listener) {
		localDevice.removeIOSampleListener(listener);
	}
	
	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when Modem Status events are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeModemStatusPacketListener(IModemStatusReceiveListener listener) {
		localDevice.addModemStatusListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of Modem Status packet
	 * listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeModemStatusPacketListener(IModemStatusReceiveListener listener) {
		localDevice.removeModemStatusListener(listener);
	}
	
	/**
	 * Attempts to open the local XBee Device connection.
	 * 
	 * @throws XBeeException if any error occurs during the process.
	 */
	public void openConnection() throws XBeeException {
		if (!localDevice.isOpen())
			localDevice.open();
	}
	
	/**
	 * Attempts to close the local XBee Device connection.
	 */
	public void closeConnection() {
		if (localDevice.isOpen())
			localDevice.close();
	}
}
