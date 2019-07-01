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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.digi.xbee.api.android.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IUserDataRelayReceiveListener;
import com.digi.xbee.api.models.XBeeLocalInterface;
import com.digi.xbee.api.models.UserDataRelayMessage;

import java.util.ArrayList;

public class RelayConsoleActivity extends AppCompatActivity {

    // Variables.
    private XBeeDevice device;

    private UserDataRelayListener relayListener;

    private ListView relayMessagesListView;
    private RelayMessageAdapter relayMessageAdapter;
    private ArrayList<UserDataRelayMessage> relayMessages = new ArrayList<>();

    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay_console);

        // Get the selected XBee device.
        device = MainActivity.getXBeeDevice();
        relayListener = new UserDataRelayListener();

        // Initialize the list view.
        relayMessagesListView = findViewById(R.id.relayMessagesListView);
        relayMessageAdapter = new RelayMessageAdapter(this, relayMessages);
        relayMessagesListView.setAdapter(relayMessageAdapter);

        // Initialize the send button.
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRelayMessage();
            }
        });

        // Initialize the clear image/button.
        ImageView clearImageView = findViewById(R.id.clearImageView);
        clearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relayMessageAdapter.clear();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the User Data Relay receive listener.
        device.addUserDataRelayListener(relayListener);

        // Register a receiver to be notified when the Bluetooth connection is lost.
        registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the User Data Relay receive listener.
        device.removeUserDataRelayListener(relayListener);

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
     * Opens a dialog to send a User Data Relay message.
     */
    private void sendRelayMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.send_udr));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 20);

        TextView destInterfaceText = new TextView(this);
        destInterfaceText.setText(getResources().getString(R.string.dest_interface));
        layout.addView(destInterfaceText);

        final Spinner interfaceSpinner = new Spinner(this);
        ArrayList<XBeeLocalInterface> values = new ArrayList<>();
        for (XBeeLocalInterface relayInterface : XBeeLocalInterface.values()) {
            if (relayInterface != XBeeLocalInterface.UNKNOWN)
                values.add(relayInterface);
        }
        ArrayAdapter<XBeeLocalInterface> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
        interfaceSpinner.setAdapter(adapter);
        layout.addView(interfaceSpinner);

        TextView dataLabel = new TextView(this);
        dataLabel.setText(getResources().getString(R.string.data));
        layout.addView(dataLabel);

        final EditText dataText = new EditText(this);
        dataText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        dataText.setSingleLine(false);
        dataText.setMinLines(2);
        layout.addView(dataText);

        builder.setView(layout);

        builder.setPositiveButton(getResources().getString(R.string.send), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    // Send the User Data Relay message.
                    device.sendUserDataRelay((XBeeLocalInterface) interfaceSpinner.getSelectedItem(), dataText.getText().toString().getBytes());
                    Toast.makeText(RelayConsoleActivity.this, getResources().getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                } catch (XBeeException e) {
                    e.printStackTrace();
                    Toast.makeText(RelayConsoleActivity.this, getResources().getString(R.string.send_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.show();
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
                new AlertDialog.Builder(RelayConsoleActivity.this).setTitle(getResources().getString(R.string.connection_lost_title))
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

    /**
     * Listener to be notified when new User Data Relay messages are received.
     */
    private class UserDataRelayListener implements IUserDataRelayReceiveListener {
        @Override
        public void userDataRelayReceived(final UserDataRelayMessage userDataRelayMessage) {
            RelayConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Add the message to the list.
                    relayMessageAdapter.add(userDataRelayMessage);
                    relayMessagesListView.setSelection(relayMessageAdapter.getCount() - 1);
                }
            });
        }
    }

    /**
     * Custom adapter class to represent the received User Data Relay messages
     * in the list.
     */
    private class RelayMessageAdapter extends ArrayAdapter<UserDataRelayMessage> {

        private Context context;

        RelayMessageAdapter(@NonNull Context context, ArrayList<UserDataRelayMessage> relayMessages) {
            super(context, -1, relayMessages);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            UserDataRelayMessage message = relayMessages.get(position);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 30, 40, 30);

            TextView messageText = new TextView(context);
            messageText.setText(getResources().getString(R.string.udr_format,  message.getSourceInterface().getDescription(), message.getDataString()));
            layout.addView(messageText);

            return layout;
        }
    }
}
