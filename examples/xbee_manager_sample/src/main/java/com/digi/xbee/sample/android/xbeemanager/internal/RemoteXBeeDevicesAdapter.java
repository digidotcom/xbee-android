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
import com.digi.xbee.sample.android.xbeemanager.fragments.XBeeDeviceDiscoveryFragment;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.utils.ByteUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class RemoteXBeeDevicesAdapter extends BaseAdapter {

	public static final int NOTHING_SELECTED = -1;
	
	// Variables.
	private ArrayList<RemoteXBeeDevice> remoteDevices;
	
	private LayoutInflater layoutInflater;
	
	private XBeeDeviceDiscoveryFragment deviceDiscoveryFragment;
	
	private int selectedItem = NOTHING_SELECTED;
	
	/**
	 * Class constructor. Instantiates a new {@code RemoteXBeeDevicesAdapter}
	 * object with the given parameters.
	 * 
	 * @param deviceDiscoveryFragment Fragment that will use this adapter.
	 * @param remoteDevices List of remote XBee devices.
	 */
	public RemoteXBeeDevicesAdapter(XBeeDeviceDiscoveryFragment deviceDiscoveryFragment, ArrayList<RemoteXBeeDevice> remoteDevices) {
		this.deviceDiscoveryFragment = deviceDiscoveryFragment;
		this.remoteDevices = remoteDevices;
		layoutInflater = (LayoutInflater)deviceDiscoveryFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Sets the selected item position.
	 * 
	 * @param position Selected item position.
	 */
	public void setSelection(int position) {
		selectedItem = position;
		notifyDataSetChanged();
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
		return remoteDevices.size();
	}

	@Override
	public Object getItem(int position) {
		return remoteDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return ByteUtils.byteArrayToLong(remoteDevices.get(position).get64BitAddress().getValue());
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Inflate the view if required.
		View view = convertView;
		if (convertView == null)
			view = layoutInflater.inflate(R.layout.remote_device_list_item, null);

		// Find view fields.
		TextView macAddressText = (TextView)view.findViewById(R.id.mac_address_text);
		TextView nodeIdentifierText = (TextView)view.findViewById(R.id.node_identifier_text);
		TextView address16Text = (TextView)view.findViewById(R.id.address_16_text);
		ImageButton removeButton = (ImageButton)view.findViewById(R.id.remove_button);
		
		// Retrieve selected Remote Device.
		final RemoteXBeeDevice remoteDevice = remoteDevices.get(position);
		
		// Set background.
		if (position == selectedItem)
			view.setBackgroundColor(deviceDiscoveryFragment.getResources().getColor(R.color.light_yellow));
		else
			view.setBackgroundColor(deviceDiscoveryFragment.getResources().getColor(R.color.white));
		
		// Fill in all fields.
		macAddressText.setText(remoteDevice.get64BitAddress().toString());
		nodeIdentifierText.setText(remoteDevice.getNodeID());
		if (remoteDevice.get16BitAddress() != null)
			address16Text.setText(remoteDevice.get16BitAddress().toString());
		else
			address16Text.setText("");
		
		// Add listener to the remove button.
		removeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				remoteDevices.remove(position);
				deviceDiscoveryFragment.updateListView();
				if (selectedItem == position) {
					setSelection(NOTHING_SELECTED);
					deviceDiscoveryFragment.handleRemoteDeviceSelected(null);
				}
			}
		});
		
		return view;
	}
}
