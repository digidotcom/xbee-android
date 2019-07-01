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

import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.models.XBee64BitAddress;

public class ReceivedIOSamplePacket extends AbstractReceivedPacket {

	// Constants.
	private final static String ANALOG_VALUES = "Analog values:\n";
	private final static String ANALOG_VALUES_SHORT = " Analog - ";
	private final static String DIGITAL_VALUES = "Digital values:\n";
	private final static String DIGITAL_VALUES_SHORT = " Digital";
	private final static String BULLET = "    - ";
	private final static String VALUE_SEPARATOR = ": ";
	private final static String END_LINE = "\n";
	
	// Variables.
	private IOSample ioSample;
	
	/**
	 * Class constructor. Instantiates a new {@code ReceivedIOSamplePacket}
	 * object with the given parameters.
	 * 
	 * @param sourceAddress 64-bit address of the device that sent this packet.
	 * @param ioSample Received IO sample.
	 */
	public ReceivedIOSamplePacket(XBee64BitAddress sourceAddress, IOSample ioSample) {
		super(sourceAddress, PacketType.TYPE_IO_SAMPLE);
		this.ioSample = ioSample;
	}

	@Override
	public String getShortPacketData() {
		StringBuilder sb = new StringBuilder();
		if (ioSample.hasAnalogValues())
			sb.append(ioSample.getAnalogValues().size()).append(ANALOG_VALUES_SHORT);
		else
			sb.append("0").append(ANALOG_VALUES_SHORT);
		if (ioSample.hasDigitalValues())
			sb.append(ioSample.getDigitalValues().size()).append(DIGITAL_VALUES_SHORT);
		else 
			sb.append("0").append(DIGITAL_VALUES_SHORT);
		return sb.toString();
	}
	
	@Override
	public String getPacketData() {
		StringBuilder sb = new StringBuilder();
		if (ioSample.hasAnalogValues()) {
			sb.append(ANALOG_VALUES);
			for (IOLine ioLine:ioSample.getAnalogValues().keySet()) {
				sb.append(BULLET);
				sb.append(ioLine.name()).append(VALUE_SEPARATOR);
				sb.append(ioSample.getAnalogValues().get(ioLine));
				sb.append(END_LINE);
			}
			sb.append(END_LINE);
		}
		if (ioSample.hasDigitalValues()) {
			sb.append(DIGITAL_VALUES);
			for (IOLine ioLine:ioSample.getDigitalValues().keySet()) {
				sb.append(BULLET);
				sb.append(ioLine.name()).append(VALUE_SEPARATOR);
				sb.append(ioSample.getDigitalValues().get(ioLine).getName());
				sb.append(END_LINE);
			}
		}
		return sb.toString();
	}
}
