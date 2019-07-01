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

import java.util.ArrayList;

import com.digi.xbee.sample.android.xbeemanager.fragments.AbstractXBeeDeviceFragment;
import com.digi.xbee.sample.android.xbeemanager.fragments.XBeeDeviceDiscoveryFragment;
import com.digi.xbee.sample.android.xbeemanager.fragments.XBeeDeviceInfoFragment;
import com.digi.xbee.sample.android.xbeemanager.fragments.XBeeReceivedPacketsFragment;
import com.digi.xbee.sample.android.xbeemanager.managers.XBeeManager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class XBeeTabsActivity extends FragmentActivity {
	
	// Variables.
	private ViewPager viewPager;
    private XBeeManager xbeeManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xbee_tabs_activity);
		
		// Retrieve the XBee Manager.
		xbeeManager = XBeeManagerApplication.getInstance().getXBeeManager();
		
		// Setup view pager.
		setupViewPager();
		
		// Configure Action Bar.
		configureTabs();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Disconnect the device.
		xbeeManager.closeConnection();
	}
	
	/**
	 * Configures the view pager that will be used to display the different
	 * activity fragments.
	 */
	private void setupViewPager() {
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			/*
			 * (non-Javadoc)
			 * @see android.support.v4.view.ViewPager.SimpleOnPageChangeListener#onPageSelected(int)
			 */
			public void onPageSelected(int position) {
				// When swiping between pages, select the corresponding tab.
				ActionBar actionBar = getActionBar();
				if (actionBar != null)
					actionBar.setSelectedNavigationItem(position);
			}
		});
		viewPager.setOffscreenPageLimit(2);
		XBeeDevicePagerAdapter xbeePagerAdapter = new XBeeDevicePagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(xbeePagerAdapter);
	}
	
	/**
	 * Configures the activity tabs of the action bar.
	 */
	private void configureTabs() {
		ActionBar actionBar = getActionBar();
		if (actionBar == null)
			return;

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		XBeeDeviceTabListener xbeeDeviceTabListener = new XBeeDeviceTabListener();

		Tab infoTab = actionBar.newTab();
		infoTab.setText(getResources().getString(R.string.device_info_fragment_title));
		infoTab.setTabListener(xbeeDeviceTabListener);

		Tab discoverTab;discoverTab = actionBar.newTab();
		discoverTab.setText(getResources().getString(R.string.device_discovery_fragment_title));
		discoverTab.setTabListener(xbeeDeviceTabListener);

		Tab framesTab = actionBar.newTab();
		framesTab.setText(getResources().getString(R.string.frames_fragment_title));
		framesTab.setTabListener(xbeeDeviceTabListener);
		
		actionBar.addTab(infoTab);
		actionBar.addTab(discoverTab);
		actionBar.addTab(framesTab);
	}
	
	/**
	 * Helper class used to handle the events of the different tab items.
	 */
	private class XBeeDeviceTabListener implements TabListener {

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// Do nothing.
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// When the tab is selected, switch to the corresponding page in the ViewPager.
			viewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// Do nothing.
		}
	}
	
	// Since this is an object collection, use a FragmentStatePagerAdapter,
	// and NOT a FragmentPagerAdapter.
	private class XBeeDevicePagerAdapter extends FragmentPagerAdapter {
		
		// Variables.
		private AbstractXBeeDeviceFragment infoFragment;
		private AbstractXBeeDeviceFragment discoverFragment;
		private AbstractXBeeDeviceFragment framesFragment;
		
		private ArrayList<AbstractXBeeDeviceFragment> fragments;
		
		public XBeeDevicePagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<AbstractXBeeDeviceFragment>();
			// Create device information fragment.
			infoFragment = new XBeeDeviceInfoFragment();
			infoFragment.setXBeeManager(xbeeManager);
			((XBeeDeviceInfoFragment)infoFragment).setXBeeTabsActivity(XBeeTabsActivity.this);
			fragments.add(infoFragment);
			// Create device discovery fragment.
			discoverFragment = new XBeeDeviceDiscoveryFragment();
			discoverFragment.setXBeeManager(xbeeManager);
			fragments.add(discoverFragment);
			// Create received frames fragment.
			framesFragment = new XBeeReceivedPacketsFragment();
			framesFragment.setXBeeManager(xbeeManager);
			fragments.add(framesFragment);
		}

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragments.get(position).getFragmentName();
		}
	}
}
