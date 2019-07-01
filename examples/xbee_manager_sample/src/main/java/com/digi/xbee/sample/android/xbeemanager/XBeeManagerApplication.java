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

import android.app.Application;

public class XBeeManagerApplication extends Application {

	// Variables.
	private XBeeManager xbeeManager;
	
	private static XBeeManagerApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// Save application instance.
		instance = this;
		// Initialize Managers.
		initializeManagers();
	}
	
	/**
	 * Returns the application instance.
	 * 
	 * @return The application instance.
	 */
	public static XBeeManagerApplication getInstance() {
		return instance;
	}
	
	/**
	 * Initializes the managers used by this application.
	 */
	private void initializeManagers() {
		xbeeManager = new XBeeManager(this);
	}
	
	/**
	 * Returns the XBee Manager.
	 * 
	 * @return The application XBee Manager.
	 */
	public XBeeManager getXBeeManager() {
		return xbeeManager;
	}
}
