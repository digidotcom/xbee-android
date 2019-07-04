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

package com.digi.xbee.sample.android.blemicropython;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.digi.xbee.api.android.XBeeBLEDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.relay.IMicroPythonDataReceiveListener;

public class TemperatureActivity extends AppCompatActivity implements IMicroPythonDataReceiveListener {

    // Constants.
    private static final String SEPARATOR = "@@@";

    private static final String MSG_ACK = "OK";

    private static final int ACK_TIMEOUT = 5000;

    // Variables.
    private XBeeBLEDevice device;

    private TableLayout tableLayout;
    private TextView temperatureText;
    private TextView humidityText;
    private Spinner rateSpinner;
    private ToggleButton startButton;

    private boolean ackReceived = false;

    private final Object lock = new Object();

    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        // Get the selected XBee device.
        device = MainActivity.getXBeeDevice();

        // Initialize layout components.
        tableLayout = findViewById(R.id.tableLayout);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);

        rateSpinner = findViewById(R.id.rateSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.refresh_rate_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rateSpinner.setAdapter(adapter);
        rateSpinner.setSelection(2);

        startButton = findViewById(R.id.startButton);
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean checked) {
                rateSpinner.setEnabled(!checked);
                // Send a message to the MicroPython interface with the action and refresh time.
                final String data = checked ? ("ON" + SEPARATOR + ((String) rateSpinner.getSelectedItem()).split(" ")[0]) : "OFF";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean ackReceived = sendDataAndWaitResponse(data.getBytes());
                            if (ackReceived) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tableLayout.setAlpha(checked ? 1 : (float) 0.2);
                                    }
                                });
                            }
                        } catch (XBeeException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register a MicroPython data listener.
        device.addMicroPythonDataListener(this);

        // Register a receiver to be notified when the Bluetooth connection is lost.
        registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister a MicroPython data listener.
        device.removeMicroPythonDataListener(this);

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

    @Override
    public void dataReceived(byte[] data) {
        // If the response is "OK", notify the lock to continue the process.
        if (new String(data).equals(MSG_ACK)) {
            ackReceived = true;
            synchronized (lock) {
                lock.notify();
            }
        } else {
            // If the process is stopped, do nothing.
            if (!startButton.isChecked())
                return;

            // Get the temperature and humidity from the received data.
            String[] dataString = new String(data).split(SEPARATOR);
            if (dataString.length != 2)
                return;

            final String temperature = dataString[0];
            final String humidity = dataString[1];
            final ColorStateList oldColors = temperatureText.getTextColors();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update the values of the temperature and humidity.
                    temperatureText.setText(temperature);
                    humidityText.setText(humidity);
                    // Make the texts blink for a short time.
                    temperatureText.setTextColor(getResources().getColor(R.color.colorAccent));
                    humidityText.setTextColor(getResources().getColor(R.color.colorAccent));
                }
            });

            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    temperatureText.setTextColor(oldColors);
                    humidityText.setTextColor(oldColors);
                }
            });
        }
    }

    /**
     * Sends the given data and waits for an ACK response during the configured
     * timeout.
     *
     * @param data Data to send.
     *
     * @return {@code true} if the ACK was received, {@code false} otherwise.
     *
     * @throws XBeeException if there is any problem sending the data.
     */
    private boolean sendDataAndWaitResponse(byte[] data) throws XBeeException {
        ackReceived = false;
        // Send the data.
        device.sendMicroPythonData(data);
        // Wait until the ACK is received to send the next block.
        try {
            synchronized (lock) {
                lock.wait(ACK_TIMEOUT);
            }
        } catch (InterruptedException ignore) {}
        // If the ACK was not received, show an error.
        if (!ackReceived) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(TemperatureActivity.this).setTitle(getResources().getString(R.string.error_waiting_response_title))
                            .setMessage(getResources().getString(R.string.error_waiting_response_description))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });
            return false;
        }
        return true;
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
                new AlertDialog.Builder(TemperatureActivity.this).setTitle(getResources().getString(R.string.connection_lost_title))
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
