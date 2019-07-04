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

import com.digi.xbee.api.AbstractXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.listeners.IUserDataRelayReceiveListener;
import com.digi.xbee.api.listeners.relay.IMicroPythonDataReceiveListener;
import com.digi.xbee.api.listeners.relay.ISerialDataReceiveListener;
import com.digi.xbee.api.models.XBeeLocalInterface;
import com.digi.xbee.api.packet.XBeePacket;

/**
 * This class represents an XBee Bluetooth Low Energy (BLE) device.
 *
 * @see CellularBLEDevice
 * @see DigiMeshBLEDevice
 * @see Raw802BLEDevice
 * @see ZigBeeBLEDevice
 */
public class XBeeBLEDevice extends AbstractXBeeDevice {

    /**
     * Class constructor. Instantiates a new {@code XBeeBLEDevice} object for
     * Android with the given parameters.
     *
     * <p>This constructor uses the Android Bluetooth Low Energy API to
     * communicate with the devices.</p>
     *
     * <p>The Bluetooth password must be provided before calling the
     * {@link #open()} method, either through this constructor or the
     * {@link #setBluetoothPassword(String)} method.</p>
     *
     * @param context The Android application context.
     * @param bleDevice Bluetooth device.
     * @param password Bluetooth password (can be {@code null}).
     *
     * @see #XBeeBLEDevice(Context, String, String)
     * @see BluetoothDevice
     */
    public XBeeBLEDevice(Context context, BluetoothDevice bleDevice, String password) {
        super(XBee.createConnectionInterface(context, bleDevice));

        this.bluetoothPassword = password;
    }

    /**
     * Class constructor. Instantiates a new {@code XBeeBLEDevice} object for
     * Android with the given parameters.
     *
     * <p>This constructor uses the Android Bluetooth Low Energy API to
     * communicate with the devices.</p>
     *
     * <p>The Bluetooth password must be provided before calling the
     * {@link #open()} method, either through this constructor or the
     * {@link #setBluetoothPassword(String)} method.</p>
     *
     * @param context The Android application context.
     * @param deviceAddress Address of the Bluetooth device.
     * @param password Bluetooth password (can be {@code null}).
     *
     * @see #XBeeBLEDevice(Context, BluetoothDevice, String)
     */
    public XBeeBLEDevice(Context context, String deviceAddress, String password) {
        super(XBee.createConnectionInterface(context, deviceAddress));

        this.bluetoothPassword = password;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public void reset() throws TimeoutException, XBeeException {
        softwareReset();
    }

    @Override
    public void open() throws XBeeException {
        super.open();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean isOpen() {
        return super.isOpen();
    }

    @Override
    public int getNextFrameID() {
        return super.getNextFrameID();
    }

    @Override
    public int getReceiveTimeout() {
        return super.getReceiveTimeout();
    }

    @Override
    public void setReceiveTimeout(int receiveTimeout) {
        super.setReceiveTimeout(receiveTimeout);
    }

    @Override
    public void addPacketListener(IPacketReceiveListener listener) {
        super.addPacketListener(listener);
    }

    @Override
    public void removePacketListener(IPacketReceiveListener listener) {
        super.removePacketListener(listener);
    }

    @Override
    public void addUserDataRelayListener(IUserDataRelayReceiveListener listener) {
        super.addUserDataRelayListener(listener);
    }

    @Override
    public void removeUserDataRelayListener(IUserDataRelayReceiveListener listener) {
        super.removeUserDataRelayListener(listener);
    }

    @Override
    public void addMicroPythonDataListener(IMicroPythonDataReceiveListener listener) {
        super.addMicroPythonDataListener(listener);
    }

    @Override
    public void removeMicroPythonDataListener(IMicroPythonDataReceiveListener listener) {
        super.removeMicroPythonDataListener(listener);
    }

    @Override
    public void addSerialDataListener(ISerialDataReceiveListener listener) {
        super.addSerialDataListener(listener);
    }

    @Override
    public void removeSerialDataListener(ISerialDataReceiveListener listener) {
        super.removeSerialDataListener(listener);
    }

    @Override
    public void sendUserDataRelay(XBeeLocalInterface destInterface, byte[] data) throws XBeeException {
        super.sendUserDataRelay(destInterface, data);
    }

    @Override
    public void sendMicroPythonData(byte[] data) throws XBeeException {
        super.sendMicroPythonData(data);
    }

    @Override
    public void sendSerialData(byte[] data) throws XBeeException {
        super.sendSerialData(data);
    }

    @Override
    public void sendPacket(XBeePacket packet, IPacketReceiveListener packetReceiveListener) throws XBeeException {
        super.sendPacket(packet, packetReceiveListener);
    }

    @Override
    public void sendPacketAsync(XBeePacket packet) throws XBeeException {
        super.sendPacketAsync(packet);
    }

    @Override
    public XBeePacket sendPacket(XBeePacket packet) throws TimeoutException, XBeeException {
        return super.sendPacket(packet);
    }

    @Override
    public void setBluetoothPassword(String password) {
        super.setBluetoothPassword(password);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
