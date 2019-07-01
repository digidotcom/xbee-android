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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.digi.xbee.sample.android.xbeemanager.R;

public class ChangeOtherParameterDialog {
	
	// Variables.
	private Context context;
	
	private EditText parameterText;
	private EditText valueText;
	
	private View inputTextDialogView;
	
	private AlertDialog inputTextDialog;
	
	private String parameter;
	private String value;
	
	public ChangeOtherParameterDialog(Context context) {
		this.context = context;
		
		// Setup the layout.
		setupLayout();
	}

	/**
	 * Displays the input text dialog.
	 */
	public void show() {
		// Reset the dialog values.
		parameter = null;
		value = null;
		createDialog();
		inputTextDialog.show();
	}

	/**
	 * Returns the input text dialog value.
	 *
	 * @return The input text dialog value, {@code null} if dialog was
	 *         cancelled or no text was entered.
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * Returns the input text dialog value.
	 *
	 * @return The input text dialog value, {@code null} if dialog was
	 *         cancelled or no text was entered.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Configures the layout of the input text dialog.
	 */
	@SuppressLint("InflateParams")
	private void setupLayout() {
		// Create the layout.
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		inputTextDialogView = layoutInflater.inflate(R.layout.change_other_param_dialog, null);
		// Configure the input texts.
		parameterText = (EditText) inputTextDialogView.findViewById(R.id.param_text);
		valueText = (EditText) inputTextDialogView.findViewById(R.id.value_text);
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
				parameter = parameterText.getText().toString();
				value = valueText.getText().toString();
				synchronized (ChangeOtherParameterDialog.this) {
					ChangeOtherParameterDialog.this.notify();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int id) {
				dialog.cancel();
				synchronized (ChangeOtherParameterDialog.this) {
					ChangeOtherParameterDialog.this.notify();
				}
			}
		});

		// Create the dialog.
		inputTextDialog = alertDialogBuilder.create();
	}
}
