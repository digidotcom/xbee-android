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

package com.digi.xbee.sample.android.xbeemanager;

import com.digi.xbee.sample.android.xbeemanager.managers.XBeeManager;
import com.digi.android.serial.SerialPortManager;
import com.digi.xbee.api.exceptions.XBeeException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class XBeeConnectActivity extends Activity {

	// Constants.
	private static final int ACTION_CLEAR_ERROR_MESSAGE = 0;
	private static final int ACTION_SET_ERROR_MESSAGE = 1;
	private static final int ACTION_SHOW_PROGRESS_DIALOG = 2;
	private static final int ACTION_HIDE_PROGRESS_DIALOG = 3;
	private static final int ACTION_ENABLE_CONNECT_BUTTON = 4;
	private static final int ACTION_DISABLE_CONNECT_BUTTON = 5;
	
	// Variables.
	private RadioButton useUSBHostButton;
	private RadioButton useSerialButton;

	private Spinner serialPortSpinner;
	private Spinner baudRateSpinner;

	private Button connectButton;

	private TextView errorText;
	private TextView serialPortLabel;

	private ProgressDialog progressDialog;
	
	private XBeeManager xbeeManager;

	private IncomingHandler handler = new IncomingHandler(this);

	// Handler used to perform actions in the UI thread.
	static class IncomingHandler extends Handler {
		private final WeakReference<XBeeConnectActivity> wActivity;

		IncomingHandler(XBeeConnectActivity activity) {
			wActivity = new WeakReference<XBeeConnectActivity>(activity);
	}

		@Override
		public void handleMessage(Message msg) {

			XBeeConnectActivity xBeeConnectActivity = wActivity.get();
			if (xBeeConnectActivity == null)
				return;

			switch (msg.what) {
				case ACTION_CLEAR_ERROR_MESSAGE:
					xBeeConnectActivity.errorText.setText("");
					break;
				case ACTION_SET_ERROR_MESSAGE:
					xBeeConnectActivity.errorText.setText((String)msg.obj);
					break;
				case ACTION_SHOW_PROGRESS_DIALOG:
					xBeeConnectActivity.progressDialog = new ProgressDialog(xBeeConnectActivity);
					xBeeConnectActivity.progressDialog.setCancelable(false);
					xBeeConnectActivity.progressDialog.setTitle(xBeeConnectActivity.getResources().getString(R.string.connect_dialog_title));
					xBeeConnectActivity.progressDialog.setMessage(xBeeConnectActivity.getResources().getString(R.string.connect_dialog_text));
					xBeeConnectActivity.progressDialog.show();
					break;
				case ACTION_HIDE_PROGRESS_DIALOG:
					if (xBeeConnectActivity.progressDialog != null)
						xBeeConnectActivity.progressDialog.dismiss();
					break;
				case ACTION_ENABLE_CONNECT_BUTTON:
					xBeeConnectActivity.connectButton.setEnabled(true);
					break;
				case ACTION_DISABLE_CONNECT_BUTTON:
					xBeeConnectActivity.connectButton.setEnabled(false);
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xbee_connect_activity);
		
		// Retrieve the application's XBee Manager.
		xbeeManager = XBeeManagerApplication.getInstance().getXBeeManager();
		
		// Initialize UI Components.
		initializeUIComponents();

		// Fill Serial Ports
		fillSerialPorts();
		
		// Fill Baud Rates
		fillBaudRates();
	}
	
	/**
	 * Initializes all the required UI components and sets the corresponding
	 * listeners.
	 */
	private void initializeUIComponents() {
		useUSBHostButton = (RadioButton)findViewById(R.id.usb_host_button);
		useUSBHostButton.setChecked(true);
		useUSBHostButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleUseUSBHostButtonPressed();
			}
		});
		useSerialButton = (RadioButton)findViewById(R.id.usb_serial_button);
		useSerialButton.setChecked(false);
		useSerialButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleUseSerialButtonPressed();
			}
		});
		serialPortSpinner = (Spinner)findViewById(R.id.serial_port_spinner);
		serialPortSpinner.setEnabled(false);
		baudRateSpinner = (Spinner)findViewById(R.id.baud_rate_spinner);
		connectButton = (Button)findViewById(R.id.connect_button);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleConnectButtonPressed();
			}
		});
		errorText = (TextView)findViewById(R.id.error_text);
		serialPortLabel = (TextView)findViewById(R.id.serial_port_label);
	}
	
	/**
	 * Handles what happens when the use USB Host interface button is pressed.
	 */
	private void handleUseUSBHostButtonPressed() {
		enableSerialPortSpinner(false);
		useSerialButton.setChecked(false);
	}
	
	/**
	 * Handles what happens when the use Serial interface button is pressed.
	 */
	private void handleUseSerialButtonPressed() {
		enableSerialPortSpinner(true);
		useUSBHostButton.setChecked(false);
	}
	
	/**
	 * Handles what happens when the connect button is pressed.
	 */
	private void handleConnectButtonPressed() {
		// Configure the XBee Manager.
		int baudRate = Integer.valueOf(baudRateSpinner.getSelectedItem().toString());
		if (useUSBHostButton.isChecked())
			xbeeManager.createXBeeDevice(baudRate);
		else
			xbeeManager.createXBeeDevice(serialPortSpinner.getSelectedItem().toString(), baudRate);
		// Create the connection thread.
		Thread connectThread = new Thread(new Runnable() {
			@Override
			public void run() {
				clearErrorMessage();
				disableConnectButton();
				showProgressDialog();
				try {
					xbeeManager.openConnection();
					startActivity(new Intent(XBeeConnectActivity.this, XBeeTabsActivity.class));
				} catch (XBeeException e) {
					setErrorMessage("Error opening connection > " + e.getMessage());
				} finally {
					enableConnectButton();
				}
				hideProgressDialog();
			}
		});
		connectThread.start();
	}

	/**
	 * Fills the serial port spinner list.
	 */
	private void fillSerialPorts() {
		String[] serialPorts;
		ArrayAdapter<String> serialPortsAdapter;
		try {
			SerialPortManager serialPortManager = new SerialPortManager(this);
			serialPorts = serialPortManager.listSerialPorts();
			if (serialPorts == null || serialPorts.length == 0) {
				serialPorts = new String[] {getResources().getString(R.string.no_ports_available)};
				useSerialButton.setEnabled(false);
			}
		} catch (NoClassDefFoundError e) {
			serialPorts = new String[] {getResources().getString(R.string.no_ports_available)};
			useSerialButton.setEnabled(false);
		}
		serialPortsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
		serialPortsAdapter.addAll(serialPorts);
		serialPortSpinner.setAdapter(serialPortsAdapter);
		enableSerialPortSpinner(false);
	}
	
	/**
	 * Fills the baud rate spinner list.
	 */
	private void fillBaudRates() {
		ArrayAdapter<String> baudRatesAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
		baudRatesAdapter.addAll(getResources().getStringArray(R.array.baud_rates));
		baudRateSpinner.setAdapter(baudRatesAdapter);
		baudRateSpinner.setSelection(3);
	}
	
	/**
	 * Changes the enablement state of the serial port spinner.
	 * 
	 * @param enabled {@code true} to enable the serial port spinner,
	 *                {@code false} to disable it.
	 */
	private void enableSerialPortSpinner(boolean enabled) {
		serialPortSpinner.setEnabled(enabled);
		if (enabled && serialPortLabel != null)
			serialPortLabel.setTextColor(getResources().getColor(R.color.dark_gray));
		else if (serialPortLabel != null)
			serialPortLabel.setTextColor(getResources().getColor(R.color.disabled_gray));
	}
	
	/**
	 * Displays a 'Connecting...' progress dialog.
	 */
	private void showProgressDialog() {
		handler.sendEmptyMessage(ACTION_SHOW_PROGRESS_DIALOG);
	}
	
	/**
	 * Hides the progress dialog it is is open.
	 */
	private void hideProgressDialog() {
		handler.sendEmptyMessage(ACTION_HIDE_PROGRESS_DIALOG);
	}
	
	/**
	 * Removes the error message.
	 */
	private void clearErrorMessage() {
		handler.sendEmptyMessage(ACTION_CLEAR_ERROR_MESSAGE);
	}
	
	/**
	 * Enables the connect button.
	 */
	private void enableConnectButton() {
		handler.sendEmptyMessage(ACTION_ENABLE_CONNECT_BUTTON);
	}
	
	/**
	 * Disables the connect button.
	 */
	private void disableConnectButton() {
		handler.sendEmptyMessage(ACTION_DISABLE_CONNECT_BUTTON);
	}
	
	/**
	 * Sets the given error message in the activity.
	 * 
	 * @param message Error message to show.
	 */
	private void setErrorMessage(String message) {
		if (message == null)
			return;
		Message msg = handler.obtainMessage(ACTION_SET_ERROR_MESSAGE);
		msg.obj = message;
		handler.sendMessage(msg);
	}
}
