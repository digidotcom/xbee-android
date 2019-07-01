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

package com.digi.xbee.sample.android.bleconfiguration;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.digi.xbee.api.android.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.HexUtils;

public class ConfigurationActivity extends AppCompatActivity {

    // Variables.
    private XBeeDevice device;

    private EditText shText;
    private EditText slText;
    private EditText blText;
    private EditText niText;
    private EditText vrText;
    private EditText hvText;
    private Spinner apSpinner;
    private Spinner d9Spinner;

    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // Get the selected XBee device.
        device = MainActivity.getXBeeDevice();

        // Initialize layout components.
        shText = findViewById(R.id.shText);
        slText = findViewById(R.id.slText);
        blText = findViewById(R.id.blText);
        niText = findViewById(R.id.niText);
        vrText = findViewById(R.id.vrText);
        hvText = findViewById(R.id.hvText);

        apSpinner = findViewById(R.id.apSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ap_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        apSpinner.setAdapter(adapter);

        d9Spinner = findViewById(R.id.d9Spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.d9_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        d9Spinner.setAdapter(adapter);

        Button readButton = findViewById(R.id.readButton);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readSettings();
            }
        });

        Button writeButton = findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeSettings();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register a receiver to be notified when the Bluetooth connection is lost.
        registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the the Bluetooth connection lost receiver.
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        // Ask the user if wants to close the connection.
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.disconnect_device_title))
                .setMessage(getResources().getString(R.string.disconnect_device_description))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeAndBack();
                    }
                }).setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Reads all the settings and updates their values in the UI.
     */
    private void readSettings() {
        startOperation(true);
    }

    /**
     * Writes all the settings with their new values.
     */
    private void writeSettings() {
        startOperation(false);
    }

    /**
     * Starts the read or write operation.
     *
     * @param read {@code true} to start the read operation, {@code false} to
     *             start the write operation.
     */
    private void startOperation(final boolean read) {
        // Clear the focus of all components.
        clearFocus();

        // Show a progress dialog while performing the operation.
        final ProgressDialog dialog = ProgressDialog.show(this, getResources().getString(read ? R.string.reading_settings_title : R.string.writing_settings_title),
                getResources().getString(read ? R.string.reading_settings_description : R.string.writing_settings_description), true);

        // The read/write process blocks the UI interface, so it must be done in a different thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (read) {
                        // Read the values.
                        final byte[] shValue = device.getParameter("SH");
                        final byte[] slValue = device.getParameter("SL");
                        final byte[] blValue = device.getParameter("BL");
                        final byte[] niValue = device.getParameter("NI");
                        final byte[] apValue = device.getParameter("AP");
                        final byte[] d9Value = device.getParameter("D9");
                        final byte[] vrValue = device.getParameter("VR");
                        final byte[] hvValue = device.getParameter("HV");

                        // Set the values in the UI.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shText.setText(HexUtils.byteArrayToHexString(shValue));
                                slText.setText(HexUtils.byteArrayToHexString(slValue));
                                blText.setText(HexUtils.byteArrayToHexString(blValue));
                                niText.setText(new String(niValue));
                                apSpinner.setSelection(Integer.parseInt(HexUtils.byteArrayToHexString(apValue)));
                                d9Spinner.setSelection(Integer.parseInt(HexUtils.byteArrayToHexString(d9Value)));
                                vrText.setText(HexUtils.byteArrayToHexString(vrValue));
                                hvText.setText(HexUtils.byteArrayToHexString(hvValue));
                            }
                        });
                    } else {
                        // Write the values.
                        device.setParameter("NI", niText.getText().toString().getBytes());
                        device.setParameter("AP", HexUtils.hexStringToByteArray(apSpinner.getSelectedItem().toString().split(" ")[0]));
                        device.setParameter("D9", HexUtils.hexStringToByteArray(d9Spinner.getSelectedItem().toString().split(" ")[0]));
                    }
                } catch (final XBeeException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(ConfigurationActivity.this).setTitle(getResources().getString(R.string.error_performing_operation))
                                    .setMessage(e.getMessage())
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }
                    });
                }
                // Close the dialog.
                dialog.dismiss();
            }
        }).start();
    }

    /**
     * Clears the focus of all elements.
     */
    private void clearFocus() {
        niText.clearFocus();
        apSpinner.clearFocus();
        d9Spinner.clearFocus();
    }

    /**
     * Closes the connection with the device and goes to the previous activity.
     */
    private void closeAndBack() {
        device.close();
        super.onBackPressed();
    }

    /**
     * Class to handle the Bluetooth connection lost.
     */
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
                // Show a dialog to the user.
                new AlertDialog.Builder(ConfigurationActivity.this).setTitle(getResources().getString(R.string.connection_lost_title))
                        .setMessage(getResources().getString(R.string.connection_lost_description))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                closeAndBack();
                            }
                        }).show();
            }
        }
    }
}
