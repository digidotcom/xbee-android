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

package com.digi.xbee.sample.android.xbeemanager.fragments;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.digi.xbee.sample.android.xbeemanager.R;
import com.digi.xbee.sample.android.xbeemanager.XBeeConstants;
import com.digi.xbee.sample.android.xbeemanager.XBeeTabsActivity;
import com.digi.xbee.sample.android.xbeemanager.dialogs.ChangeOtherParameterDialog;
import com.digi.xbee.sample.android.xbeemanager.dialogs.ChangeParameterDialog;
import com.digi.xbee.sample.android.xbeemanager.dialogs.ReadOtherParameterDialog;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class XBeeDeviceInfoFragment extends AbstractXBeeDeviceFragment {
	
	// Variables.
	private XBeeTabsActivity xbeeTabsActivity;

	private Button refreshButton;
	
	private TextView errorText;
	private TextView serialPortText;
	private TextView baudRateText;
	private TextView firmwareVersionText;
	private TextView hardwareVersionText;
	private TextView protocolText;
	private TextView macAddressText;
	private TextView nodeIdentifierText;
	private TextView panIDText;
	private TextView destAddressHighText;
	private TextView destAddressLowText;
	private TextView nodeDiscoveryTimeText;
	private TextView ioSamplingRateTimeText;
	
	private ProgressDialog progressDialog;

	private IncomingHandler handler = new IncomingHandler(this);

	// Handler used to perform actions in the UI thread.
	static class IncomingHandler extends Handler {

		private final WeakReference<XBeeDeviceInfoFragment> wActivity;

		IncomingHandler(XBeeDeviceInfoFragment activity) {
			wActivity = new WeakReference<XBeeDeviceInfoFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			XBeeDeviceInfoFragment devInfoFragment = wActivity.get();
			if (devInfoFragment == null)
				return;

			switch (msg.what) {
				case ACTION_CLEAR_ERROR_MESSAGE:
					devInfoFragment.errorText.setText("");
					break;
				case ACTION_SET_ERROR_MESSAGE:
					devInfoFragment.errorText.setTextColor(devInfoFragment.getResources().getColor(R.color.red));
					devInfoFragment.errorText.setText((String)msg.obj);
					break;
				case ACTION_SET_SUCCESS_MESSAGE:
					devInfoFragment.errorText.setTextColor(devInfoFragment.getResources().getColor(R.color.green));
					devInfoFragment.errorText.setText((String)msg.obj);
					break;
				case ACTION_SHOW_READ_PROGRESS_DIALOG:
					devInfoFragment.progressDialog = new ProgressDialog(devInfoFragment.getActivity());
					devInfoFragment.progressDialog.setCancelable(false);
					devInfoFragment.progressDialog.setTitle(devInfoFragment.getResources().getString(R.string.reading_dialog_title));
					devInfoFragment.progressDialog.setMessage(devInfoFragment.getResources().getString(R.string.reading_dialog_text));
					devInfoFragment.progressDialog.show();
					break;
				case ACTION_SHOW_WRITE_PROGRESS_DIALOG:
					devInfoFragment.progressDialog = new ProgressDialog(devInfoFragment.getActivity());
					devInfoFragment.progressDialog.setCancelable(false);
					devInfoFragment.progressDialog.setTitle(devInfoFragment.getResources().getString(R.string.writing_dialog_title));
					devInfoFragment.progressDialog.setMessage(devInfoFragment.getResources().getString(R.string.writing_dialog_text));
					devInfoFragment.progressDialog.show();
					break;
				case ACTION_HIDE_PROGRESS_DIALOG:
					if (devInfoFragment.progressDialog != null)
						devInfoFragment.progressDialog.dismiss();
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.xbee_device_info, container, false);
		
		// Initialize all required UI elements.
		initializeUIElements(view);
		
		// Read all device parameters.
		readDeviceParameters();
		
		return view;
	}

	@Override
	public String getFragmentName() {
		return getResources().getString(R.string.device_info_fragment_title);
	}

	/**
	 * Sets the XBee tabs activity that holds this fragment.
	 *
	 * @param activity XBee tabs activity that holds this fragment.
	 */
	public void setXBeeTabsActivity(XBeeTabsActivity activity) {
		xbeeTabsActivity = activity;
	}

	/**
	 * Initializes all the required graphic elements of this fragment and sets
	 * the corresponding listeners. 
	 * 
	 * @param view View to search elements in.
	 */
	private void initializeUIElements(View view) {
		// Buttons.
		Button disconnectButton = (Button)view.findViewById(R.id.disconnect_button);
		disconnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleDisconnectButtonPressed();
			}
		});

		refreshButton = (Button)view.findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleRefreshButtonPressed();
			}
		});

		Button readOtherParameterButton = (Button)view.findViewById(R.id.read_other_param_button);
		readOtherParameterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleReadOtherParameterButtonPressed();
			}
		});

		Button changeOtherParameterButton = (Button)view.findViewById(R.id.change_other_param_button);
		changeOtherParameterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleChangeOtherParameterButtonPressed();
			}
		});

		// Change parameters buttons.
		Button nodeIdentifierButton = (Button)view.findViewById(R.id.node_identifier_button);
		nodeIdentifierButton.setOnClickListener(parametersChangeButtonListener);
		Button panIDButton = (Button)view.findViewById(R.id.pan_id_button);
		panIDButton.setOnClickListener(parametersChangeButtonListener);
		Button destAddressHighButton = (Button)view.findViewById(R.id.dest_address_high_button);
		destAddressHighButton.setOnClickListener(parametersChangeButtonListener);
		Button destAddressLowButton = (Button)view.findViewById(R.id.dest_address_low_button);
		destAddressLowButton.setOnClickListener(parametersChangeButtonListener);
		Button nodeDiscoveryTimeButton = (Button)view.findViewById(R.id.node_discovery_time_button);
		nodeDiscoveryTimeButton.setOnClickListener(parametersChangeButtonListener);
		Button ioSamplingRateTimeTimeButton = (Button)view.findViewById(R.id.io_sampling_rate_button);
		ioSamplingRateTimeTimeButton.setOnClickListener(parametersChangeButtonListener);

		// Texts.
		errorText = (TextView)view.findViewById(R.id.error_text);
		serialPortText = (TextView)view.findViewById(R.id.port_text);
		baudRateText = (TextView)view.findViewById(R.id.baud_rate_text);
		firmwareVersionText = (TextView)view.findViewById(R.id.firmware_text);
		hardwareVersionText = (TextView)view.findViewById(R.id.hardware_text);
		protocolText = (TextView)view.findViewById(R.id.protocol_text);
		macAddressText = (TextView)view.findViewById(R.id.mac_text);
		nodeIdentifierText = (TextView)view.findViewById(R.id.node_identifier_text);
		panIDText = (TextView)view.findViewById(R.id.pan_id_text);
		destAddressHighText = (TextView)view.findViewById(R.id.dest_address_high_text);
		destAddressLowText = (TextView)view.findViewById(R.id.dest_address_low_text);
		nodeDiscoveryTimeText = (TextView)view.findViewById(R.id.node_discovery_time_text);
		ioSamplingRateTimeText = (TextView)view.findViewById(R.id.io_sampling_rate_text);
	}
	
	/**
	 * Listener used to wait for clicks on the "Change..." parameter buttons.
	 */
	private OnClickListener parametersChangeButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Remove error message.
			clearErrorMessage();
			// Initialize variables.
			String oldValue = null;
			String parameter = null;
			int type = 0;

			switch (v.getId()) {
				case R.id.node_identifier_button:
					parameter = XBeeConstants.PARAM_NODE_IDENTIFIER;
					oldValue = nodeIdentifierText.getText().toString();
					type = ChangeParameterDialog.TYPE_TEXT;
					break;
				case R.id.pan_id_button:
					parameter = XBeeConstants.PARAM_PAN_ID;
					oldValue = panIDText.getText().toString();
					type = ChangeParameterDialog.TYPE_HEXADECIMAL;
					break;
				case R.id.dest_address_high_button:
					parameter = XBeeConstants.PARAM_DEST_ADDRESS_H;
					oldValue = destAddressHighText.getText().toString();
					type = ChangeParameterDialog.TYPE_HEXADECIMAL;
					break;
				case R.id.dest_address_low_button:
					parameter = XBeeConstants.PARAM_DEST_ADDRESS_L;
					oldValue = destAddressLowText.getText().toString();
					type = ChangeParameterDialog.TYPE_HEXADECIMAL;
					break;
				case R.id.node_discovery_time_button:
					parameter = XBeeConstants.PARAM_NODE_DISCOVERY_TIME;
					oldValue = nodeDiscoveryTimeText.getText().toString();
					type = ChangeParameterDialog.TYPE_NUMERIC;
					break;
				case R.id.io_sampling_rate_button:
					parameter = XBeeConstants.PARAM_IO_SAMPLING_RATE;
					oldValue = ioSamplingRateTimeText.getText().toString();
					type = ChangeParameterDialog.TYPE_NUMERIC;
					break;
				default:
					break;
			}

			final String parameterFinal = parameter;
			final ChangeParameterDialog changeParameterDialog = new ChangeParameterDialog(XBeeDeviceInfoFragment.this.getActivity(), type, oldValue);
			changeParameterDialog.show();
			// This thread waits until dialog is closed, dialog is notified itself when that happens.
			Thread waitThread = new Thread() {
				@Override
				public void run() {
					synchronized (changeParameterDialog) {
						try {
							changeParameterDialog.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					String newValue = changeParameterDialog.getTextValue();
					if (newValue != null)
						writeParameter(parameterFinal, newValue);
				}
			};
			waitThread.start();
		}
	};
	
	/**
	 * Reads XBee device parameters and fills the page with the read values.
	 */
	private void readDeviceParameters() {
		// Disable refresh button.
		refreshButton.setEnabled(false);

		// Create the read thread.
		Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				clearErrorMessage();
				showReadProgressDialog();
				try {
					HashMap<String, String> parameters = xbeeManager.readBasicLocalParameters();
					fillDeviceParameters(parameters);
				} catch (XBeeException e) {
					setErrorMessage("Error reading parameters > " + e.getMessage());
				}
				hideProgressDialog();

				// Enable refresh button.
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						refreshButton.setEnabled(true);
					}
				});
			}
		});
		readThread.start();
	}
	
	/**
	 * Fills the page fields with the read device parameters.
	 * 
	 * @param parameters Read device parameters.
	 */
	private void fillDeviceParameters(final HashMap<String, String> parameters) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				serialPortText.setText(parameters.get(XBeeConstants.PARAM_SERIAL_PORT));
				baudRateText.setText(parameters.get(XBeeConstants.PARAM_BAUD_RATE));
				firmwareVersionText.setText(parameters.get(XBeeConstants.PARAM_FIRMWARE_VERSION));
				hardwareVersionText.setText(parameters.get(XBeeConstants.PARAM_HARDWARE_VERSION));
				protocolText.setText(parameters.get(XBeeConstants.PARAM_XBEE_PROTOCOL));
				macAddressText.setText(parameters.get(XBeeConstants.PARAM_MAC_ADDRESS));
				nodeIdentifierText.setText(parameters.get(XBeeConstants.PARAM_NODE_IDENTIFIER));
				panIDText.setText(parameters.get(XBeeConstants.PARAM_PAN_ID));
				destAddressHighText.setText(parameters.get(XBeeConstants.PARAM_DEST_ADDRESS_H));
				destAddressLowText.setText(parameters.get(XBeeConstants.PARAM_DEST_ADDRESS_L));
				nodeDiscoveryTimeText.setText(parameters.get(XBeeConstants.PARAM_NODE_DISCOVERY_TIME));
				ioSamplingRateTimeText.setText(parameters.get(XBeeConstants.PARAM_IO_SAMPLING_RATE));
			}
		});
	}
	
	/**
	 * Reads the given parameter from the local XBee Device.
	 * 
	 * @param parameter Parameter to read.
	 */
	private void readParameter(final String parameter) {
		// Create the read thread.
		Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				clearErrorMessage();
				showReadProgressDialog();
				try {
					String value = xbeeManager.getLocalParameter(parameter);
					setSuccessMessage("Value of " + parameter + " > " + value);
				} catch (XBeeException e) {
					setErrorMessage("Error reading parameter " + parameter + " > " + e.getMessage());
				}
				hideProgressDialog();
			}
		});
		readThread.start();
	}
	
	/**
	 * Writes the given parameter to the local XBee Device.
	 * 
	 * @param parameter Parameter to write.
	 * @param newValue Parameter value given by the user.
	 */
	private void writeParameter(final String parameter, final String newValue) {
		final String atParam = calculateATParam(parameter);
		final byte[] value = calculateParameterValue(parameter, newValue);
		if (value == null) {
			setErrorMessage("Invalid value for parameter " + atParam + " > " + newValue);
			return;
		}
		// Create the write thread.
		Thread writeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				clearErrorMessage();
				showWriteProgressDialog();
				try {
					xbeeManager.setLocalParameter(atParam, value);
					xbeeManager.saveChanges();
					updateParameterValue(parameter, newValue);
					setSuccessMessage("Parameter successfully written.");
				} catch (XBeeException e) {
					setErrorMessage("Error writing parameter " + atParam + " > " + e.getMessage());
				}
				hideProgressDialog();
			}
		});
		writeThread.start();
	}
	
	/**
	 * Returns the corresponding AT command for the given parameter.
	 * 
	 * @param parameter Parameter to get its AT command.
	 * 
	 * @return The parameter's AT command.
	 */
	private String calculateATParam(String parameter) {
		if (parameter.equals(XBeeConstants.PARAM_NODE_IDENTIFIER))
			return XBeeConstants.AT_COMMAND_NI;
		if (parameter.equals(XBeeConstants.PARAM_PAN_ID))
			return XBeeConstants.AT_COMMAND_ID;
		if (parameter.equals(XBeeConstants.PARAM_DEST_ADDRESS_H))
			return XBeeConstants.AT_COMMAND_DH;
		if (parameter.equals(XBeeConstants.PARAM_DEST_ADDRESS_L))
			return XBeeConstants.AT_COMMAND_DL;
		else if (parameter.equals(XBeeConstants.PARAM_NODE_DISCOVERY_TIME))
			return XBeeConstants.AT_COMMAND_NT;
		else if (parameter.equals(XBeeConstants.PARAM_IO_SAMPLING_RATE))
			return XBeeConstants.AT_COMMAND_IR;
		else
			return parameter;
	}
	
	/**
	 * Calculates the real value to set for the given parameter.
	 * 
	 * @param parameter Parameter to calculate its value.
	 * @param newValue The parameter value given by the user.
	 * 
	 * @return The calculated parameter value based on the given user value.
	 */
	private byte[] calculateParameterValue(String parameter, String newValue) {
		byte[] value;
		try {
			if (parameter.equals(XBeeConstants.PARAM_NODE_DISCOVERY_TIME))
				value = ByteUtils.longToByteArray((Long.parseLong(newValue)) / 100);
			else if (parameter.equals(XBeeConstants.PARAM_IO_SAMPLING_RATE))
				value = ByteUtils.longToByteArray(Long.parseLong(newValue));
			else if (parameter.equals(XBeeConstants.PARAM_NODE_IDENTIFIER))
				value = newValue.getBytes();
			else
				value = HexUtils.hexStringToByteArray(newValue);
			return value;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Updates the given parameter to to the given value.
	 * 
	 * @param parameter Parameter to restore its value.
	 * @param value Parameter value.
	 */
	private void updateParameterValue(final String parameter, final String value) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (parameter.equals(XBeeConstants.PARAM_NODE_IDENTIFIER))
					nodeIdentifierText.setText(value);
				else if (parameter.equals(XBeeConstants.PARAM_PAN_ID))
					panIDText.setText(value);
				else if (parameter.equals(XBeeConstants.PARAM_DEST_ADDRESS_H))
					destAddressHighText.setText(value);
				else if (parameter.equals(XBeeConstants.PARAM_DEST_ADDRESS_L))
					destAddressLowText.setText(value);
				else if (parameter.equals(XBeeConstants.PARAM_NODE_DISCOVERY_TIME))
					nodeDiscoveryTimeText.setText(value);
				else if (parameter.equals(XBeeConstants.PARAM_IO_SAMPLING_RATE))
					ioSamplingRateTimeText.setText(value);
			}
		});
	}
	
	/**
	 * Handles what happens when the disconnect button is pressed.
	 */
	private void handleDisconnectButtonPressed() {
		xbeeTabsActivity.finish();
	}
	
	/**
	 * Handles what happens when the refresh button is pressed.
	 */
	private void handleRefreshButtonPressed() {
		readDeviceParameters();
	}

	/**
	 * Handles what happens when the 'Read Other Parameter' button is pressed.
	 */
	private void handleReadOtherParameterButtonPressed() {
		// Remove error message.
		clearErrorMessage();
		final ReadOtherParameterDialog readOtherParamDialog = new ReadOtherParameterDialog(XBeeDeviceInfoFragment.this.getActivity());
		readOtherParamDialog.show();
		// This thread waits until dialog is closed, dialog is notified itself when that happens.
		Thread waitThread = new Thread() {
			@Override
			public void run() {
				synchronized (readOtherParamDialog) {
					try {
						readOtherParamDialog.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				String parameter = readOtherParamDialog.getParameter();

				// Sanity checks.
				if (parameter == null)
					return;
				if (parameter.length() != 2) {
					setErrorMessage("Invalid Parameter > " + parameter);
					return;
				}

				// Perform read action.
				readParameter(parameter);
			}
		};
		waitThread.start();
	}
	
	/**
	 * Handles what happens when the 'Change Other Parameter' button is pressed.
	 */
	private void handleChangeOtherParameterButtonPressed() {
		// Remove error message.
		clearErrorMessage();
		final ChangeOtherParameterDialog changeOtherParamDialog = new ChangeOtherParameterDialog(XBeeDeviceInfoFragment.this.getActivity());
		changeOtherParamDialog.show();
		// This thread waits until dialog is closed, dialog is notified itself when that happens.
		Thread waitThread = new Thread() {
			@Override
			public void run() {
				synchronized (changeOtherParamDialog) {
					try {
						changeOtherParamDialog.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				String parameter = changeOtherParamDialog.getParameter();
				String value = changeOtherParamDialog.getValue();

				// Sanity checks.
				if (parameter == null)
					return;
				if (parameter.length() != 2) {
					setErrorMessage("Invalid Parameter > " + parameter);
					return;
				}
				if (value == null || value.length() == 0) {
					setErrorMessage("Invalid Parameter Value");
					return;
				}

				// Perform write action.
				writeParameter(parameter, value);
			}
		};
		waitThread.start();
	}
	
	/**
	 * Displays a 'Reading...' progress dialog.
	 */
	private void showReadProgressDialog() {
		handler.sendEmptyMessage(ACTION_SHOW_READ_PROGRESS_DIALOG);
	}

	/**
	 * Displays a 'Writing...' progress dialog.
	 */
	private void showWriteProgressDialog() {
		handler.sendEmptyMessage(ACTION_SHOW_WRITE_PROGRESS_DIALOG);
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
	
	/**
	 * Sets the given error message in the activity.
	 * 
	 * @param message Error message to show.
	 */
	private void setSuccessMessage(String message) {
		if (message == null)
			return;

		Message msg = handler.obtainMessage(ACTION_SET_SUCCESS_MESSAGE);
		msg.obj = message;
		handler.sendMessage(msg);
	}
}