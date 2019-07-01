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

package com.digi.xbee.sample.android.blemicrocontroller;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digi.xbee.api.android.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.relay.ISerialDataReceiveListener;
import com.digi.xbee.api.packet.XBeeChecksum;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SendFileActivity extends AppCompatActivity implements ISerialDataReceiveListener {

    // Constants.
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private static final String MSG_START = "START@@@%s";
    private static final String MSG_END = "END";
    private static final String MSG_ACK = "OK";

    private static final int BLOCK_SIZE = 64;
    private static final int ACK_TIMEOUT = 5000;

    private static final String PERCENTAGE_FORMAT = "%d %%";

    // Variables.
    private XBeeDevice device;

    private Button sendFileButton;
    private LinearLayout progressLayout;
    private ProgressBar progressBar;
    private TextView percentageText;
    private TextView sendingFileText;

    private boolean ackReceived = false;

    private final Object lock = new Object();

    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        // Get the selected XBee device.
        device = MainActivity.getXBeeDevice();

        // Initialize layout components.
        sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

        progressLayout = findViewById(R.id.progressLayout);
        progressBar = findViewById(R.id.progressBar);
        percentageText = findViewById(R.id.percentageText);
        sendingFileText = findViewById(R.id.sendingFileText);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register a Serial data listener.
        device.addSerialDataListener(this);

        // Register a receiver to be notified when the Bluetooth connection is lost.
        registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister a Serial data listener.
        device.removeSerialDataListener(this);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != PICKFILE_RESULT_CODE || data == null || data.getData() == null)
            return;

        final String filePath = data.getData().getPath();
        if (filePath == null)
            return;

        // Create a thread to send the file.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Pre-configure the UI.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendFileButton.setEnabled(false);
                        progressLayout.setAlpha(1);
                        sendingFileText.setText(filePath);
                    }
                });

                // Start the send process.
                sendFile(new File(filePath));

                // Post-configure the UI.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendFileButton.setEnabled(true);
                        progressLayout.setAlpha(0.2F);
                    }
                });
            }
        }).start();
    }

    @Override
    public void dataReceived(byte[] data) {
        // If the response is "OK", notify the lock to continue the process.
        if (new String(data).equals(MSG_ACK)) {
            ackReceived = true;
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    /**
     * Sends the given file split in blocks to the XBee serial interface.
     *
     * @param file File to send.
     */
    private void sendFile(File file) {
        try {
            // Send the 'START' message.
            if (!sendDataAndWaitResponse(String.format(MSG_START, file.getName()).getBytes()))
                return;

            ArrayList<byte[]> fileBlocks = getFileBlocks(file);
            for (int i = 0; i < fileBlocks.size(); i++) {
                byte[] block = fileBlocks.get(i);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // Append the checksum to the block payload.
                baos.write(block);
                baos.write(getChecksum(block));
                // Update the progress.
                updateProgress(100 * (i + 1) / fileBlocks.size());
                // Send the block.
                if (!sendDataAndWaitResponse(baos.toByteArray()))
                    return;
            }

            // Send the 'END' message.
            if (!sendDataAndWaitResponse(MSG_END.getBytes()))
                return;

            // Show a message to notify the completion.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SendFileActivity.this, R.string.file_sent_successfully, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException | XBeeException e) {
            e.printStackTrace();
            showError(e.getMessage());
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
        device.sendSerialData(data);
        // Wait until the ACK is received to send the next block.
        try {
            synchronized (lock) {
                lock.wait(ACK_TIMEOUT);
            }
        } catch (InterruptedException ignore) {}
        // If the ACK was not received, stop the process.
        if (!ackReceived) {
            showError(getResources().getString(R.string.error_sending_file_ack));
            return false;
        }
        return true;
    }

    /**
     * Splits the given file in blocks.
     *
     * @param file File to split.
     *
     * @return List of blocks.
     */
    private ArrayList<byte[]> getFileBlocks(File file) {
        ArrayList<byte[]> fileBlocks = new ArrayList<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] blockBuffer = new byte[BLOCK_SIZE];
            int readBytes = inputStream.read(blockBuffer, 0, blockBuffer.length);
            while (readBytes > 0) {
                byte[] blockToAdd = new byte[readBytes];
                System.arraycopy(blockBuffer, 0, blockToAdd, 0, readBytes);
                fileBlocks.add(blockToAdd);
                readBytes = inputStream.read(blockBuffer, 0, blockBuffer.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBlocks;
    }

    /**
     * Calculates and returns the checksum of the given block.
     *
     * @param block Block to calculate the checksum.
     *
     * @return Checksum of the block.
     */
    private int getChecksum(byte[] block) {
        XBeeChecksum checksum = new XBeeChecksum();
        checksum.add(block);
        return checksum.generate();
    }

    /**
     * Updates the progress in the UI.
     *
     * @param percentage Percentage of the send process.
     */
    private void updateProgress(final int percentage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(percentage);
                percentageText.setText(String.format(Locale.getDefault(), PERCENTAGE_FORMAT, percentage));
            }
        });
    }

    /**
     * Shows an error dialog with the given message.
     *
     * @param message Error message.
     */
    private void showError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SendFileActivity.this).setTitle(getResources().getString(R.string.error_sending_file_title))
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    /**
     * Requests the storage permission to the user and opens the file chooser.
     */
    @AfterPermissionGranted(REQUEST_STORAGE_PERMISSION)
    private void requestStoragePermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Open the file chooser.
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.storage_permission_needed),
                    REQUEST_STORAGE_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
                new AlertDialog.Builder(SendFileActivity.this).setTitle(getResources().getString(R.string.connection_lost_title))
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
