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

import java.util.ArrayList;

import com.digi.xbee.sample.android.xbeemanager.R;
import com.digi.xbee.sample.android.xbeemanager.models.AbstractReceivedPacket;
import com.digi.xbee.api.utils.ByteUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class ReceivedXBeePacketsAdapter extends BaseAdapter {

	public static final int NOTHING_SELECTED = -1;
	
	// Variables.
	private ArrayList<AbstractReceivedPacket> receivedPackets;
	
	private LayoutInflater layoutInflater;
	
	private Context context;
	
	private int selectedItem = NOTHING_SELECTED;
	
	/**
	 * Class constructor. Instantiates a new {@code RemoteXBeeDevicesAdapter}
	 * object with the given parameters.
	 * 
	 * @param context Application context.
	 * @param receivedPackets List of received XBee packets.
	 */
	public ReceivedXBeePacketsAdapter(Context context, ArrayList<AbstractReceivedPacket> receivedPackets) {
		this.context = context;
		this.receivedPackets = receivedPackets;
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Sets the selected item position.
	 * 
	 * @param position Selected item position.
	 */
	public void setSelection(int position) {
		selectedItem = position;
	}
	
	/**
	 * Retrieves the selected item position.
	 * 
	 * @return The selected item position.
	 */
	public int getSelection() {
		return selectedItem;
	}

	@Override
	public int getCount() {
		return receivedPackets.size();
	}

	@Override
	public Object getItem(int position) {
		return receivedPackets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return ByteUtils.byteArrayToLong(receivedPackets.get(position).getSourceAddress().getValue());
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Inflate the view if required.
		View view = convertView;
		if (convertView == null)
			view = layoutInflater.inflate(R.layout.received_frame_list_item, null);

		// Find view fields.
		RelativeLayout rootLayout = (RelativeLayout)view.findViewById(R.id.root_layout);
		TextView dateText = (TextView)view.findViewById(R.id.date_text);
		TextView typeText = (TextView)view.findViewById(R.id.packet_type_text);
		TextView sourceAddressText = (TextView)view.findViewById(R.id.source_address_text);
		TextView packetDataText = (TextView)view.findViewById(R.id.packet_data_text);
		
		// Retrieve selected packet.
		final AbstractReceivedPacket receivedPacket = receivedPackets.get(position);
		
		// Set background.
		if (position == selectedItem)
			rootLayout.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
		else
			rootLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
		
		// Fill in all fields.
		dateText.setText(receivedPacket.getTimeString());
		typeText.setText(receivedPacket.getType().getName());
		sourceAddressText.setText(receivedPacket.getSourceAddress().toString());
		packetDataText.setText(receivedPacket.getShortPacketData());
		
		return view;
	}
}
