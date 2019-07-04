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

/**
 * This class represents a ZigBee Bluetooth Low Energy (BLE) device.
 *
 * @see CellularBLEDevice
 * @see DigiMeshBLEDevice
 * @see Raw802BLEDevice
 * @see XBeeBLEDevice
 */
public class ZigBeeBLEDevice extends XBeeBLEDevice {
    /**
     * Class constructor. Instantiates a new {@code ZigBeeBLEDevice} object for
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
     * @see #ZigBeeBLEDevice(Context, String, String)
     * @see BluetoothDevice
     */
    public ZigBeeBLEDevice(Context context, BluetoothDevice bleDevice, String password) {
        super(context, bleDevice, password);
    }

    /**
     * Class constructor. Instantiates a new {@code ZigBeeBLEDevice} object for
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
     * @see #ZigBeeBLEDevice(Context, BluetoothDevice, String)
     */
    public ZigBeeBLEDevice(Context context, String deviceAddress, String password) {
        super(context, deviceAddress, password);
    }
}
