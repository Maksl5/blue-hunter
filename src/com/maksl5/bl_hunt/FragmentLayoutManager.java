package com.maksl5.bl_hunt;



import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.maksl5.bl_hunt.DatabaseManager.DatabaseHelper;
import com.maksl5.bl_hunt.MainActivity.CustomSectionFragment;
import com.maksl5.bl_hunt.CustomUI.PatternProgressBar;



/**
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */

public class FragmentLayoutManager {

	
	
	public static View getSpecificView(	Bundle params,
										LayoutInflater parentInflater,
										ViewGroup rootContainer,
										Context context) {

		int sectionNumber = params.getInt(CustomSectionFragment.ARG_SECTION_NUMBER);

		switch (sectionNumber) {
		case 1:
			return parentInflater.inflate(R.layout.act_page_discovery, rootContainer, false);
		case 2:
			return parentInflater.inflate(R.layout.act_page_leaderboard, rootContainer, false);
		case 3:
			return parentInflater.inflate(R.layout.act_page_founddevices, rootContainer, false);
		case 4:

		}

		return new View(context);
	}

	/**
	 * @author Maksl5
	 *
	 */
	public static class FoundDevicesLayout {

		private static List<HashMap<String, String>> showedFdList = new ArrayList<HashMap<String, String>>();
		private static List<HashMap<String, String>> completeFdList = new ArrayList<HashMap<String, String>>();
		private static String[] from = { "macAddress",
		"manufacturer",
		"exp",
		"RSSI",
		"name",
		"time" };
		private static int[] to = { R.id.macTxtView,
		R.id.manufacturerTxtView,
		R.id.expTxtView,
		R.id.rssiTxtView,
		R.id.nameTxtView,
		R.id.timeTxtView };
		public static void refreshFoundDevicesList(final MainActivity mainActivity) {
		
			ListView lv = (ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);
		
			List<HashMap<String, String>> devices =
					new DatabaseManager(mainActivity, mainActivity.versionCode).getAllDevices();
			List<HashMap<String, String>> listViewHashMaps = new ArrayList<HashMap<String, String>>();
		
			HashMap<String, String[]> macHashMap = MacAdressAllocations.getHashMap();
			HashMap<String, Integer> expHashMap = MacAdressAllocations.getExpHashMap();
		
			Set<String> keySet = macHashMap.keySet();
			for (HashMap<String, String> device : devices) {
				HashMap<String, String> tempDataHashMap = new HashMap<String, String>();
		
				tempDataHashMap.put("macAddress", device.get(DatabaseHelper.COLUMN_MAC_ADDRESS));
				tempDataHashMap.put("name", device.get(DatabaseHelper.COLUMN_NAME));
				tempDataHashMap.put("RSSI", "RSSI: " + device.get(DatabaseHelper.COLUMN_RSSI));
		
				Long time =
						(device.get(DatabaseHelper.COLUMN_TIME) == null || device.get(DatabaseHelper.COLUMN_TIME).equals("null")) ? 0 : Long.parseLong(device.get(DatabaseHelper.COLUMN_TIME));
				tempDataHashMap.put("time", DateFormat.getDateTimeInstance().format(new Date(time)));
		
				boolean foundManufacturer = false;
		
				for (String key : keySet) {
					String[] specificMacs = macHashMap.get(key);
		
					for (String mac : specificMacs) {
						if (device.get(DatabaseHelper.COLUMN_MAC_ADDRESS).startsWith(mac)) {
							foundManufacturer = true;
							tempDataHashMap.put("manufacturer", key);
							tempDataHashMap.put("exp", "+" + expHashMap.get(key.replace(" ", "_") + "_exp") + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));
						}
					}
		
				}
		
				if (!foundManufacturer) {
					tempDataHashMap.put("manufacturer", "Unknown");
					tempDataHashMap.put("exp", "+" + expHashMap.get("Unknown_exp") + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));
				}
		
				listViewHashMaps.add(tempDataHashMap);
		
			}
		
			SimpleAdapter sAdapter =
					new SimpleAdapter(mainActivity, listViewHashMaps, R.layout.act_page_founddevices_row, from, to);
			showedFdList = listViewHashMaps;
		
			if (!completeFdList.equals(listViewHashMaps)) {
				completeFdList = listViewHashMaps;
			}
		
			ListenerClass listenerClass = new FragmentLayoutManager().new ListenerClass();
		
			// lv.setOnHierarchyChangeListener(listenerClass);
		
			int scroll = lv.getFirstVisiblePosition();
			lv.setAdapter(sAdapter);
			lv.setSelection(scroll);
		}

		public static void filterFoundDevices(	String text,
												MainActivity mainActivity) {
		
			List<HashMap<String, String>> searchedList = new ArrayList<HashMap<String, String>>();
			ListView lv = (ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);
			SimpleAdapter sAdapter;
		
			if (text.equalsIgnoreCase("[unknown]")) {
		
				for (HashMap<String, String> hashMap : completeFdList) {
					if (hashMap.get("manufacturer").equals("Unknown")) {
						searchedList.add(hashMap);
					}
				}
				sAdapter = new SimpleAdapter(mainActivity, searchedList, R.layout.act_page_founddevices_row, from, to);
			}
			else {
				sAdapter = new SimpleAdapter(mainActivity, completeFdList, R.layout.act_page_founddevices_row, from, to);
			}
		
			lv.setAdapter(sAdapter);
		
		}
	
	}

	/**
	 * @author Maksl5
	 *
	 */
	public static class DeviceDiscoveryLayout {

		public static void updateIndicatorViews(MainActivity mainActivity) {
		
			TextView expTextView = (TextView) mainActivity.findViewById(R.id.expIndicator);
			TextView lvlTextView = (TextView) mainActivity.findViewById(R.id.lvlIndicator);
			PatternProgressBar progressBar = (PatternProgressBar) mainActivity.findViewById(R.id.progressBar1);
		
			int exp = LevelSystem.getUserExp(mainActivity);
			int level = LevelSystem.getLevel(exp);
		
			expTextView.setText(exp + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation) + " / " + LevelSystem.getLevelEndExp(level) + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));
			lvlTextView.setText("Level " + level);
		
			progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
			progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));
		}
	
	}

	private class ListenerClass implements OnHierarchyChangeListener {

		List<TextWatcherClass> listenerList;

		private ListenerClass() {

			listenerList = new ArrayList<TextWatcherClass>();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.ViewGroup.OnHierarchyChangeListener#onChildViewAdded(android.view.View, android.view.View)
		 */
		@Override
		public void onChildViewAdded(	View parent,
										View child) {

			TextView nameTxtView = (TextView) child.findViewById(R.id.nameTxtView);
			if (nameTxtView.getText() == null || nameTxtView.getText().toString().equals("") || nameTxtView.toString().trim().equals("")) {
				((TableRow) child.findViewById(R.id.TableRow01)).setVisibility(TableRow.GONE);

				TextWatcherClass txtWatcherClass = new TextWatcherClass(nameTxtView, child);
				nameTxtView.addTextChangedListener(txtWatcherClass);
				listenerList.add(txtWatcherClass);

			}
			else {
				((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(TableRow.VISIBLE);
			}

			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.ViewGroup.OnHierarchyChangeListener#onChildViewRemoved(android.view.View,
		 * android.view.View)
		 */
		@Override
		public void onChildViewRemoved(	View parent,
										View child) {

			TextView nameTxtView = (TextView) child.findViewById(R.id.nameTxtView);
			for (TextWatcherClass textWatcher : new ArrayList<TextWatcherClass>(listenerList)) {
				if (textWatcher.child.equals(child)) {
					textWatcher.nameTxtView.removeTextChangedListener(textWatcher);
					listenerList.remove(textWatcher);
				}
			}

		}

		private class TextWatcherClass implements TextWatcher {

			public TextView nameTxtView;
			View child;

			private TextWatcherClass(TextView nameTxtView,
					View child) {

				this.nameTxtView = nameTxtView;
				this.child = child;

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
			 */
			@Override
			public void afterTextChanged(Editable s) {

				// TODO Auto-generated method stub

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
			 */
			@Override
			public void beforeTextChanged(	CharSequence s,
											int start,
											int count,
											int after) {

				// TODO Auto-generated method stub

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
			 */
			@Override
			public void onTextChanged(	CharSequence s,
										int start,
										int before,
										int count) {

				if (nameTxtView.getText() == null || nameTxtView.getText().toString().equals("") || nameTxtView.toString().trim().equals("")) {
					((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(TableRow.GONE);
				}
				else {
					((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(TableRow.VISIBLE);
				}

			}

		}
	}

}
