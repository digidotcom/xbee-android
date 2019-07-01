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

package com.digi.xbee.sample.android.relayconsole;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digi.xbee.api.android.XBeeDevice;
import com.digi.xbee.api.exceptions.BluetoothAuthenticationException;
import com.digi.xbee.api.exceptions.XBeeException;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    // Constants.
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Variables.
    private ArrayList<BluetoothDevice> bleDevices = new ArrayList<>();
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback scanCallback;

    private BluetoothDevice selectedDevice = null;

    private ProgressBar scanProgress;

    private static XBeeDevice xbeeDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Bluetooth stuff.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            bluetoothAdapter = bluetoothManager.getAdapter();
        scanCallback = new BleScanCallback();

        // Initialize the list view.
        ListView devicesListView = findViewById(R.id.devicesListView);
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, bleDevices);
        devicesListView.setAdapter(bluetoothDeviceAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                stopScan();
                selectedDevice = bluetoothDeviceAdapter.getItem(i);
                askForPassword(false);
            }
        });

        scanProgress = findViewById(R.id.scanProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bluetoothDeviceAdapter.clear();
        requestLocationPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop the Bluetooth scan.
        stopScan();
    }

    /**
     * Requests the location permission to the user and starts the Bluetooth
     * scan when done.
     */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Start the Bluetooth scan.
            startScan();
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.location_permission_needed),
                    REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Starts the Bluetooth scan process.
     */
    private void startScan() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.startLeScan(scanCallback);
        scanProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Stops the Bluetooth scan process.
     */
    private void stopScan() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.stopLeScan(scanCallback);
        scanProgress.setVisibility(View.INVISIBLE);
    }

    /**
     * Returns the selected and open XBee device.
     *
     * @return The selected and open XBee device.
     */
    public static XBeeDevice getXBeeDevice() {
        return xbeeDevice;
    }

    /**
     * Asks the user for the Bluetooth password and tries to connect to the
     * selected device.
     *
     * @param authFailed {@code true} if the first authentication attempt
     *                   failed, {@code false} otherwise.
     */
    private void askForPassword(boolean authFailed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.enter_password));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText passwordText = new EditText(this);
        passwordText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordText);

        // If the first authentication attempt failed, show an error message.
        if (authFailed) {
            TextView invalidPwdText = new TextView(this);
            invalidPwdText.setText(getResources().getString(R.string.invalid_password));
            layout.addView(invalidPwdText);
        }

        builder.setView(layout);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Hide keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);

                // Connect to the selected device.
                connectToDevice(selectedDevice, passwordText.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Restart the Bluetooth scan.
                startScan();
            }
        });

        builder.show();
    }

    /**
     * Connects to the given Bluetooth device with the given password.
     *
     * @param device Bluetooth device to connect to.
     * @param password Bluetooth password.
     */
    private void connectToDevice(final BluetoothDevice device, final String password) {
        // Show a progress dialog while connecting to the device.
        final ProgressDialog dialog = ProgressDialog.show(this, getResources().getString(R.string.connecting_device_title),
                getResources().getString(R.string.connecting_device_description), true);

        // The connection process blocks the UI interface, so it must be done in a different thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate an XBee device with the Bluetooth device and password.
                xbeeDevice = new XBeeDevice(MainActivity.this, device, password);
                try {
                    // Open the connection with the device.
                    xbeeDevice.open();

                    // If the open method did not throw an exception, the connection is open.
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            // Start the Relay Console activity.
                            Intent intent = new Intent(MainActivity.this, RelayConsoleActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (BluetoothAuthenticationException e) {
                    // There was a problem in the Bluetooth authentication process, so ask for the password again.
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            askForPassword(true);
                        }
                    });
                } catch (final XBeeException e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            new AlertDialog.Builder(MainActivity.this).setTitle(getResources().getString(R.string.error_connecting_title))
                                    .setMessage(getResources().getString(R.string.error_connecting_description, e.getMessage()))
                                    .setPositiveButton(android.R.string.ok, null).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Custom adapter class to represent the Bluetooth device items in the list.
     */
    private class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

        private Context context;

        BluetoothDeviceAdapter(@NonNull Context context, ArrayList<BluetoothDevice> devices) {
            super(context, -1, devices);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BluetoothDevice device = bleDevices.get(position);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 30, 40, 30);

            TextView nameText = new TextView(context);
            nameText.setText(device.getName() == null ? getResources().getString(R.string.unknown_name) : device.getName());
            nameText.setTypeface(nameText.getTypeface(), Typeface.BOLD);
            nameText.setTextSize(18);
            layout.addView(nameText);

            TextView macText = new TextView(context);
            macText.setText(getResources().getString(R.string.mac_addr, device.getAddress()));
            layout.addView(macText);

            return layout;
        }
    }

    /**
     * Custom Bluetooth scan callback.
     */
    private class BleScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            // If the Bluetooth device is not in the list yet, add it.
            if (!bleDevices.contains(bluetoothDevice))
                bluetoothDeviceAdapter.add(bluetoothDevice);
        }
    }
}
