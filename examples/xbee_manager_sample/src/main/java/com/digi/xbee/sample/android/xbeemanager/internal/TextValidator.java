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

package com.digi.xbee.sample.android.xbeemanager.internal;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Helper class used to validate text from Edit Text widgets wrapping the
 * {@code TextWatcher} class.
 */
public abstract class TextValidator implements TextWatcher {

	// Variables.
	private final EditText editText;

	/**
	 * Class constructor. Instantiates a new {@code TextValidator} object
	 * with the given parameters.
	 * 
	 * @param editText Edit Text widget this validator will be for.
	 */
	public TextValidator(EditText editText) {
		this.editText = editText;
	}

	/**
	 * Validates the given text.
	 * 
	 * @param textView Edit Text widget this validator will be for.
	 * @param text Text to validate.
	 */
	public abstract void validate(EditText textView, String text);

	@Override
	final public void afterTextChanged(Editable s) {
		String text = editText.getText().toString();
		validate(editText, text);
	}

	@Override
	final public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// Do nothing.
	}

	@Override
	final public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Do nothing.
	}
}