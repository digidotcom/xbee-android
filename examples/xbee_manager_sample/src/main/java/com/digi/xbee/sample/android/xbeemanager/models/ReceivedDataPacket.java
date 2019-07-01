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

import com.digi.xbee.api.models.XBee64BitAddress;

public class ReceivedDataPacket extends AbstractReceivedPacket {

	// Variables.
	private byte[] receivedData;
	
	/**
	 * Class constructor. Instantiates a new {@code ReceivedDataPacket} object
	 * with the given parameters.
	 * 
	 * @param sourceAddress 64-bit address of the device that sent this packet.
	 * @param receivedData Received packet data.
	 */
	public ReceivedDataPacket(XBee64BitAddress sourceAddress, byte[] receivedData) {
		super(sourceAddress, PacketType.TYPE_DATA);
		this.receivedData = receivedData;
	}

	@Override
	public String getShortPacketData() {
		return new String(receivedData);
	}
	
	@Override
	public String getPacketData() {
		return new String(receivedData);
	}
}
