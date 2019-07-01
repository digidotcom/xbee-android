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

package com.digi.xbee.sample.android.xbeemanager.dialogs;

import java.util.regex.Pattern;

import com.digi.xbee.sample.android.xbeemanager.R;
import com.digi.xbee.sample.android.xbeemanager.internal.TextValidator;
import com.digi.xbee.api.utils.HexUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class SendDataDialog {

	// Constants.
	public final static int TYPE_TEXT = 0;
	public final static int TYPE_HEXADECIMAL = 1;
	
	private final static String HEXADECIMAL_PATTERN = "[0-9a-fA-F]+";
	
	private final static String ERROR_DATA_EMPTY = "Data cannot be empty.";
	private final static String ERROR_INVALID_HEX_DATA = "Invalid hexadecimal data.";
	
	// Variables.
	private Context context;
	
	private EditText inputText;
	
	private View inputTextDialogView;
	
	private AlertDialog inputTextDialog;
	
	private String textValue;
	
	private int type;
	
	/**
	 * Class constructor. Instantiates a new {@code SendDataDialog} object
	 * with the given parameters.
	 * 
	 * @param context Application context.
	 * @param type Type of data to send.
	 */
	public SendDataDialog(Context context, int type) {
		this.context = context;
		this.type = type;
		
		// Setup the layout.
		setupLayout();
	}

	/**
	 * Displays the send data dialog.
	 */
	public void show() {
		// Reset the value.
		textValue = null;
		createDialog();
		inputTextDialog.show();
		inputTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}

	/**
	 * Returns the send data dialog value.
	 *
	 * @return The send data dialog value, {@code null} if dialog was
	 *         cancelled or no text was entered.
	 */
	public byte[] getValue() {
		if (textValue == null)
			return null;
		byte[] data = null;
		switch (type) {
			case TYPE_TEXT:
				data = textValue.getBytes();
				break;
			case TYPE_HEXADECIMAL:
				data = HexUtils.hexStringToByteArray(textValue);
				break;
			default:
				break;
		}
		return data;
	}

	/**
	 * Configures the layout of the send data dialog.
	 */
	@SuppressLint("InflateParams")
	private void setupLayout() {
		// Create the layout.
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		if (type == TYPE_TEXT)
			inputTextDialogView = layoutInflater.inflate(R.layout.send_data_text_dialog, null);
		else
			inputTextDialogView = layoutInflater.inflate(R.layout.send_data_hex_dialog, null);

		// Configure the input text.
		inputText = (EditText) inputTextDialogView.findViewById(R.id.input_text);
		inputText.addTextChangedListener(new TextValidator(inputText) {
			@Override
			public void validate(EditText textView, String text) {
				if (text.length() == 0) {
					inputText.setError(ERROR_DATA_EMPTY);
					inputTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					return;
				}

				if (type == TYPE_HEXADECIMAL && !Pattern.matches(HEXADECIMAL_PATTERN, text)) {
					inputText.setError(ERROR_INVALID_HEX_DATA);
					inputTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					return;
				}

				inputText.setError(null);
				inputTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			}
		});
	}
	
	/**
	 * Creates the alert dialog that will be displayed.
	 */
	private void createDialog() {
		// Setup the dialog window.
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(inputTextDialogView);
		alertDialogBuilder.setTitle(R.string.send_data_dialog_title);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				textValue = inputText.getText().toString();
				synchronized (SendDataDialog.this) {
					SendDataDialog.this.notify();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int id) {
				dialog.cancel();
				synchronized (SendDataDialog.this) {
					SendDataDialog.this.notify();
				}
			}
		});
		// Create the dialog.
		inputTextDialog = alertDialogBuilder.create();
	}
}
