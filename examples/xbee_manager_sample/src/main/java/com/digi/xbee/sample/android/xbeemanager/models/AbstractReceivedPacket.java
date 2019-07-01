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

import java.util.Calendar;
import java.util.Date;

import com.digi.xbee.api.models.XBee64BitAddress;

public abstract class AbstractReceivedPacket {
	
	// Variables.
	protected Date receivedDate;
	
	protected XBee64BitAddress sourceAddress;
	
	private PacketType type;
	
	/**
	 * Class constructor. Instantiates a new {@code AbstractReceivedXBeePacket}
	 * object with the given parameters.
	 * 
	 * @param sourceAddress 64-bit address of the device that sent this packet.
	 * @param type Packet type.
	 */
	public AbstractReceivedPacket(XBee64BitAddress sourceAddress, PacketType type) {
		this.sourceAddress = sourceAddress;
		this.type = type;
		receivedDate = new Date();
	}
	
	/**
	 * Returns the date at which packet was received.
	 * 
	 * @return The date at which packet was received.
	 */
	public Date getDate() {
		return receivedDate;
	}
	
	/**
	 * Returns the received time in format HH:mm:ss.
	 * 
	 * @return Received time in format HH:mm:ss.
	 */
	public String getTimeString() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(receivedDate);
		return completeNumber(cal.get(Calendar.HOUR_OF_DAY)) + ':'
				+ completeNumber(cal.get(Calendar.MINUTE)) + ':'
				+ completeNumber(cal.get(Calendar.SECOND)) + '.'
				+ completeMilliseconds(cal.get(Calendar.MILLISECOND));
	}
	
	/**
	 * Returns the received date and time in format yyyy-MM-dd HH:mm:ss.
	 * 
	 * @return Received date and time in format yyyy-MM-dd HH:mm:ss.
	 */
	public String getDateAndTimeString() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(receivedDate);

		String dateString = String.valueOf(cal.get(Calendar.YEAR)) + '-'
				+ (cal.get(Calendar.MONTH) + 1) + '-'
				+ cal.get(Calendar.DAY_OF_MONTH) + " ";

		String timeString = completeNumber(cal.get(Calendar.HOUR_OF_DAY)) + ':'
				+ completeNumber(cal.get(Calendar.MINUTE)) + ':'
				+ completeNumber(cal.get(Calendar.SECOND)) + '.'
				+ completeMilliseconds(cal.get(Calendar.MILLISECOND));

		return dateString + timeString;
	}
	
	/**
	 * Returns the received packet source address.
	 * 
	 * @return The received packet 64-bit source address.
	 */
	public XBee64BitAddress getSourceAddress() {
		return sourceAddress;
	}
	
	/**
	 * Returns the packet type.
	 * 
	 * @return The packet type.
	 */
	public PacketType getType() {
		return type;
	}
	
	/**
	 * Returns the packet data in a readable format.
	 * 
	 * @return The packet data in a readable format.
	 */
	public abstract String getPacketData();
	
	/**
	 * Returns the packet data (short format) in a readable format.
	 * 
	 * @return The packet data (short formal) in a readable format.
	 */
	public abstract String getShortPacketData();
	
	/**
	 * Completes the given number by adding a preceding zero if necessary.
	 * 
	 * @param number Number to complete.
	 * @return Completed number.
	 */
	private String completeNumber(int number) {
		if (number < 10)
			return "0" + number;
		return "" + number;
	}
	
	/**
	 * Completes the given milliseconds value by adding '0' until reaching 3
	 * floating point decimals.
	 * 
	 * @param milliseconds Milliseconds number to complete.
	 * @return Completed milliseconds number.
	 */
	private String completeMilliseconds(int milliseconds) {
		if (("" + milliseconds).length() == 3)
			return "" + milliseconds;
		int diff = 3 - ("" + milliseconds).length();
		StringBuilder sb = new StringBuilder();
		sb.append(milliseconds);
		for (int i = 0; i < diff; i++){
			sb.append("0");
		}
		return sb.toString();
	}
}
