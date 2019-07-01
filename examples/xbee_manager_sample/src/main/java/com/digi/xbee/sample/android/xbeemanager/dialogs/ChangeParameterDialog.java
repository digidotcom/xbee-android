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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ChangeParameterDialog {

	// Constants.
	public final static int TYPE_TEXT = 0;
	public final static int TYPE_NUMERIC = 1;
	public final static int TYPE_HEXADECIMAL = 2;
	
	private final static String HEXADECIMAL_PATTERN = "[0-9a-fA-F]+";
	
	private final static String ERROR_VALUE_EMPTY = "Value cannot be empty.";
	private final static String ERROR_INVALID_HEX_VALUE = "Invalid hexadecimal value.";
	
	// Variables.
	private Context context;
	
	private String oldText;
	
	private EditText inputText;
	
	private View inputTextDialogView;
	
	private AlertDialog inputTextDialog;
	
	private String textValue;
	
	private int type;
	
	public ChangeParameterDialog(Context context, int type, String oldText) {
		this.context = context;
		this.type = type;
		this.oldText = oldText;
		
		// Setup the layout.
		setupLayout();
	}


	/**
	 * Displays the input text dialog.
	 */
	public void show() {
		// Reset the value.
		textValue = null;
		createDialog();
		inputTextDialog.show();
	}

	/**
	 * Returns the input text dialog value.
	 *
	 * @return The input text dialog value, {@code null} if dialog was
	 *         cancelled or no text was entered.
	 */
	public String getTextValue() {
		return textValue;
	}
	
	/**
	 * Configures the layout of the input text dialog.
	 */
	@SuppressLint("InflateParams")
	private void setupLayout() {
		// Create the layout.
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		switch (type) {
			case TYPE_TEXT:
				inputTextDialogView = layoutInflater.inflate(R.layout.change_text_param_dialog, null);
				break;
			case TYPE_NUMERIC:
				inputTextDialogView = layoutInflater.inflate(R.layout.change_numeric_param_dialog, null);
				break;
			case TYPE_HEXADECIMAL:
				inputTextDialogView = layoutInflater.inflate(R.layout.change_hexadecimal_param_dialog, null);
				break;
			default:
				break;
		}
		// Configure the input text.
		inputText = (EditText) inputTextDialogView.findViewById(R.id.input_text);
		if (oldText != null)
			inputText.setText(oldText);

		inputText.addTextChangedListener(new TextValidator(inputText) {
			@Override
			public void validate(EditText textView, String text) {
				if (text.length() == 0) {
					inputText.setError(ERROR_VALUE_EMPTY);
					inputTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					return;
				}

				if (type == TYPE_HEXADECIMAL && !Pattern.matches(HEXADECIMAL_PATTERN, text)) {
					inputText.setError(ERROR_INVALID_HEX_VALUE);
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
		alertDialogBuilder.setTitle(R.string.edit_dialog_title);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				textValue = inputText.getText().toString();
				synchronized (ChangeParameterDialog.this) {
					ChangeParameterDialog.this.notify();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int id) {
				dialog.cancel();
				synchronized (ChangeParameterDialog.this) {
					ChangeParameterDialog.this.notify();
				}
			}
		});
		// Create the dialog.
		inputTextDialog = alertDialogBuilder.create();
	}
}
