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

package com.digi.xbee.sample.android.xbeemanager.models;

public enum PacketType {
	
	// Enumeration entries.
	TYPE_DATA(0, "Data"),
	TYPE_IO_SAMPLE(1, "IO Sample"),
	TYPE_MODEM_STATUS(2, "Modem Status");
	
	// Variables.
	private int id;
	
	private String name;
	
	/**
	 * Class constructor. Instantiates a new {@code PacketType} enumeration
	 * entry with the given parameters.
	 * 
	 * @param id ID of the enumeration entry.
	 * @param name Name of the enumeration entry.
	 */
	PacketType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the enumeration ID.
	 * 
	 * @return The enumeration ID.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Returns the enumeration name.
	 * 
	 * @return The enumeration name.
	 */
	public String getName() {
		return name;
	}
}
