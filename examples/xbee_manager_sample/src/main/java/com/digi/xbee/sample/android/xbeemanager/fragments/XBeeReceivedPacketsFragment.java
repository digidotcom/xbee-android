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

package com.digi.xbee.sample.android.xbeemanager.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.digi.xbee.sample.android.xbeemanager.R;
import com.digi.xbee.sample.android.xbeemanager.internal.ReceivedXBeePacketsAdapter;
import com.digi.xbee.sample.android.xbeemanager.internal.RemoteXBeeDevicesAdapter;
import com.digi.xbee.sample.android.xbeemanager.models.AbstractReceivedPacket;
import com.digi.xbee.sample.android.xbeemanager.models.ReceivedDataPacket;
import com.digi.xbee.sample.android.xbeemanager.models.ReceivedIOSamplePacket;
import com.digi.xbee.sample.android.xbeemanager.models.ReceivedModemStatusPacket;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.models.ModemStatusEvent;
import com.digi.xbee.api.models.XBeeMessage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class XBeeReceivedPacketsFragment extends AbstractXBeeDeviceFragment
		implements IDataReceiveListener, IIOSampleReceiveListener, IModemStatusReceiveListener {

	// Variables.
	private ArrayList<AbstractReceivedPacket> receivedPackets;
	private ReceivedXBeePacketsAdapter receivedPacketsAdapter;

	private TextView receivedPacketsText;
	private TextView dateText;
	private TextView typeText;
	private TextView sourceAddressText;
	private TextView packetDataText;
	
	private final Object receivedPacketsLock = new Object();

	private IncomingHandler handler = new IncomingHandler(this);

	// Handler used to perform actions in the UI thread.
	static class IncomingHandler extends Handler {

		private final WeakReference<XBeeReceivedPacketsFragment> wActivity;

		IncomingHandler(XBeeReceivedPacketsFragment activity) {
			wActivity = new WeakReference<XBeeReceivedPacketsFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			XBeeReceivedPacketsFragment recPacketsFragment = wActivity.get();
			if (recPacketsFragment == null)
				return;

			switch (msg.what) {
				case ACTION_UPDATE_LIST_VIEW:
					recPacketsFragment.receivedPacketsAdapter.notifyDataSetChanged();
					sendEmptyMessage(ACTION_UPDATE_LIST_TEXT);
					break;
				case ACTION_UPDATE_LIST_TEXT:
					recPacketsFragment.receivedPacketsText.setText(
							String.format("%s %s",
									recPacketsFragment.receivedPackets.size(),
									recPacketsFragment.getResources().getString(R.string.packets_received)));
					break;
				case ACTION_CLEAR_VALUES:
					recPacketsFragment.dateText.setText("");
					recPacketsFragment.typeText.setText("");
					recPacketsFragment.sourceAddressText.setText("");
					recPacketsFragment.packetDataText.setText("");
					break;
				case ACTION_ADD_PACKET_TO_LIST:
					synchronized (recPacketsFragment.receivedPacketsLock) {
						recPacketsFragment.receivedPackets.add(0, (AbstractReceivedPacket)msg.obj);
						recPacketsFragment.updateListView();

						int sel = recPacketsFragment.receivedPacketsAdapter.getSelection();
						if (sel != ReceivedXBeePacketsAdapter.NOTHING_SELECTED)
							recPacketsFragment.receivedPacketsAdapter.setSelection(sel + 1);

						recPacketsFragment.updateListView();
					}
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment.
		View view = inflater.inflate(R.layout.xbee_received_data, container, false);
		
		// Check if we have to initialize the received packets variables.
		if (receivedPackets == null) {
			receivedPackets = new ArrayList<AbstractReceivedPacket>();
			receivedPacketsAdapter = new ReceivedXBeePacketsAdapter(getActivity(), receivedPackets);
		}
		
		// Initialize all required UI elements.
		initializeUIElements(view);
		
		// Render initial remote devices list.
		updateListView();
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Subscribe listeners.
		xbeeManager.subscribeDataPacketListener(this);
		xbeeManager.subscribeIOPacketListener(this);
		xbeeManager.subscribeModemStatusPacketListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Unsubscribe listeners.
		xbeeManager.unsubscribeDataPacketListener(this);
		xbeeManager.unsubscribeIOPacketListener(this);
		xbeeManager.unsubscribeModemStatusPacketListener(this);
	}

	@Override
	public String getFragmentName() {
		return getResources().getString(R.string.frames_fragment_title);
	}

	@Override
	public void dataReceived(XBeeMessage xbeeMessage) {
		ReceivedDataPacket p = new ReceivedDataPacket(xbeeMessage.getDevice().get64BitAddress(), xbeeMessage.getData());
		addPacketToList(p);
	}

	@Override
	public void ioSampleReceived(RemoteXBeeDevice remoteDevice, IOSample ioSample) {
		ReceivedIOSamplePacket p = new ReceivedIOSamplePacket(remoteDevice.get64BitAddress(), ioSample);
		addPacketToList(p);
	}

	@Override
	public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent) {
		ReceivedModemStatusPacket p = new ReceivedModemStatusPacket(xbeeManager.getLocalXBee64BitAddress(), modemStatusEvent);
		addPacketToList(p);
	}

	/**
	 * Updates the list view.
	 */
	public void updateListView() {
		handler.sendEmptyMessage(ACTION_UPDATE_LIST_VIEW);
	}
	
	/**
	 * Initializes all the required graphic elements of this fragment. 
	 * 
	 * @param view View to search elements in.
	 */
	private void initializeUIElements(View view) {
		// XBee packet list.
        ListView receivedPacketsList = (ListView)view.findViewById(R.id.received_packets_list);
		receivedPacketsList.setAdapter(receivedPacketsAdapter);
		receivedPacketsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				receivedPacketsAdapter.setSelection(i);
				updateListView();
				handlePacketSelected(receivedPackets.get(i));
			}
		});

		// Buttons.
        Button clearButton = (Button)view.findViewById(R.id.clear_button);
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClearButtonPressed();
			}
		});

		// Texts.
		receivedPacketsText = (TextView)view.findViewById(R.id.received_packets_text);
		dateText = (TextView)view.findViewById(R.id.date_text);
		typeText = (TextView)view.findViewById(R.id.packet_type_text);
		sourceAddressText = (TextView)view.findViewById(R.id.source_address_text);
		packetDataText = (TextView)view.findViewById(R.id.packet_data_text);
	}

	/**
	 * Handles what happens when a received packet is selected from the list.
	 * 
	 * @param selectedPacket Selected received packet.
	 */
	private void handlePacketSelected(AbstractReceivedPacket selectedPacket) {
		if (selectedPacket == null) {
			clearValues();
			return;
		}
		dateText.setText(selectedPacket.getDateAndTimeString());
		typeText.setText(String.format("%s %s",
				selectedPacket.getType().getName(),
				getResources().getString(R.string.packet_suffix)));
		sourceAddressText.setText(selectedPacket.getSourceAddress().toString());
		packetDataText.setText(selectedPacket.getPacketData());
	}
	
	/**
	 * Handles what happens when the clear button is pressed.
	 */
	private void handleClearButtonPressed() {
		synchronized (receivedPacketsLock) {
			receivedPackets.clear();
		}
		receivedPacketsAdapter.setSelection(RemoteXBeeDevicesAdapter.NOTHING_SELECTED);
		updateListView();
		handlePacketSelected(null);
	}
	
	/**
	 * Clears the text values.
	 */
	private void clearValues() {
		handler.sendEmptyMessage(ACTION_CLEAR_VALUES);
	}
	
	/**
	 * Adds the given packet to the list of packets.
	 * 
	 * @param receivedPacket Packet to add to the list.
	 */
	private void addPacketToList(AbstractReceivedPacket receivedPacket) {
		Message msg = handler.obtainMessage(ACTION_ADD_PACKET_TO_LIST);
		msg.obj = receivedPacket;
		handler.sendMessage(msg);
	}
}
