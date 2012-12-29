package com.maksl5.bl_hunt;



import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.activity.MainActivity.CustomSectionFragment;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText.OnBackKeyClickedListener;
import com.maksl5.bl_hunt.custom_ui.PatternProgressBar;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;
import com.maksl5.bl_hunt.storage.MacAddressAllocations;



/**
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */

public class FragmentLayoutManager {

	public static final int PAGE_DEVICE_DISCOVERY = 0;
	public static final int PAGE_LEADERBOARD = 1;
	public static final int PAGE_FOUND_DEVICES = 2;
	public static final int PAGE_ACHIEVEMENTS = 3;
	public static final int PAGE_STATISTICS = 4;

	public static View getSpecificView(	Bundle params,
										LayoutInflater parentInflater,
										ViewGroup rootContainer,
										Context context) {

		int sectionNumber = params.getInt(CustomSectionFragment.ARG_SECTION_NUMBER);

		switch (sectionNumber) {
		case PAGE_DEVICE_DISCOVERY:
			return parentInflater.inflate(R.layout.act_page_discovery, rootContainer, false);
		case PAGE_LEADERBOARD:
			return parentInflater.inflate(R.layout.act_page_leaderboard, rootContainer, false);
		case PAGE_FOUND_DEVICES:
			return parentInflater.inflate(R.layout.act_page_founddevices, rootContainer, false);
		case PAGE_ACHIEVEMENTS:
			break;
		case PAGE_STATISTICS:
			return parentInflater.inflate(R.layout.act_page_statistics, rootContainer, false);

		}

		return new View(context);
	}

	/**
	 * @author Maksl5
	 * 
	 */
	public static class FoundDevicesLayout {

		private static List<HashMap<String, String>> showedFdList =
				new ArrayList<HashMap<String, String>>();
		private static List<HashMap<String, String>> completeFdList =
				new ArrayList<HashMap<String, String>>();
		private static String[] from =
				{
					"macAddress", "manufacturer", "exp", "RSSI", "name", "time" };
		private static int[] to = {
									R.id.macTxtView, R.id.manufacturerTxtView, R.id.expTxtView,
									R.id.rssiTxtView, R.id.nameTxtView, R.id.timeTxtView };

		public static void refreshFoundDevicesList(final MainActivity mainActivity) {

			ListView lv =
					(ListView) mainActivity.mViewPager.getChildAt(PAGE_FOUND_DEVICES + 1).findViewById(R.id.listView2);

			List<HashMap<String, String>> devices =
					new DatabaseManager(mainActivity, mainActivity.versionCode).getAllDevices();
			List<HashMap<String, String>> listViewHashMaps =
					new ArrayList<HashMap<String, String>>();

			HashMap<String, String[]> macHashMap = MacAddressAllocations.getHashMap();
			HashMap<String, Integer> expHashMap = MacAddressAllocations.getExpHashMap();

			Set<String> keySet = macHashMap.keySet();
			for (HashMap<String, String> device : devices) {
				HashMap<String, String> tempDataHashMap = new HashMap<String, String>();
				
				String deviceMac = device.get(DatabaseHelper.COLUMN_MAC_ADDRESS);
				String manufacturer = device.get(DatabaseHelper.COLUMN_MANUFACTURER);
				String deviceTime = device.get(DatabaseHelper.COLUMN_TIME);

				tempDataHashMap.put("macAddress", deviceMac);
				tempDataHashMap.put("name", device.get(DatabaseHelper.COLUMN_NAME));
				tempDataHashMap.put("RSSI", "RSSI: " + device.get(DatabaseHelper.COLUMN_RSSI));
				
				if(manufacturer == null || manufacturer.equals("") || manufacturer.equals("Unkown")) {
					manufacturer = MacAddressAllocations.getManufacturer(deviceMac);
					new DatabaseManager(mainActivity, mainActivity.versionCode).addManufacturerToDevice(deviceMac, manufacturer);
				}
				
				tempDataHashMap.put("manufacturer", manufacturer);
				tempDataHashMap.put("exp", "" + MacAddressAllocations.getExp(manufacturer.replace(" ", "_")));

				Long time =
						(deviceTime == null || deviceTime.equals("null")) ? 0 : Long.parseLong(deviceTime);
				tempDataHashMap.put("time", DateFormat.getDateTimeInstance().format(new Date(time)));

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
			ListView lv =
					(ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);
			SimpleAdapter sAdapter;

			if (text.equalsIgnoreCase("[unknown]")) {

				for (HashMap<String, String> hashMap : completeFdList) {
					if (hashMap.get("manufacturer").equals("Unknown")) {
						searchedList.add(hashMap);
					}
				}
				sAdapter =
						new SimpleAdapter(mainActivity, searchedList, R.layout.act_page_founddevices_row, from, to);
			}
			else {
				sAdapter =
						new SimpleAdapter(mainActivity, completeFdList, R.layout.act_page_founddevices_row, from, to);
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
			PatternProgressBar progressBar =
					(PatternProgressBar) mainActivity.findViewById(R.id.progressBar1);

			int exp = LevelSystem.getUserExp(mainActivity);
			int level = LevelSystem.getLevel(exp);

			expTextView.setText(exp + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation) + " / " + LevelSystem.getLevelEndExp(level) + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));
			lvlTextView.setText("Level " + level);

			progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
			progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));
		}

	}

	/**
	 * @author Maksl5
	 * 
	 */
	public static class StatisticLayout {

		public static void initializeView(final MainActivity mainActivity) {

			View parentContainer = mainActivity.mViewPager.getChildAt(PAGE_STATISTICS + 1);

			final TextView nameTextView =
					(TextView) parentContainer.findViewById(R.id.nameTextView);
			final AdjustedEditText nameEditText =
					(AdjustedEditText) parentContainer.findViewById(R.id.nameEditText);

			// Listener
			nameTextView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {

					nameEditText.setText(nameTextView.getText());

					nameTextView.animate().setDuration(500).alpha(0f);
					nameTextView.setVisibility(TextView.GONE);

					nameEditText.setAlpha(0f);
					nameEditText.setVisibility(EditText.VISIBLE);
					nameEditText.animate().setDuration(500).alpha(1f);

					InputMethodManager imm =
							(InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

					return true;
				}
			});

			nameEditText.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(	TextView v,
												int actionId,
												KeyEvent event) {

					InputMethodManager imm =
							(InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

					if (actionId == EditorInfo.IME_ACTION_DONE && nameEditText.isShown()) {

						imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

						// submit();
						nameTextView.setText(nameEditText.getText());

						nameEditText.animate().setDuration(500).alpha(1f);
						nameEditText.setVisibility(EditText.GONE);

						nameTextView.setAlpha(0f);
						nameTextView.setVisibility(TextView.VISIBLE);
						nameTextView.animate().setDuration(500).alpha(1f);

						return true;
					}
					return false;
				}
			});

			nameEditText.setOnBackKeyClickListener(new OnBackKeyClickedListener() {

				@Override
				public void onBackKeyClicked() {

					InputMethodManager imm =
							(InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

					if (nameEditText.isShown() && imm.isActive(nameEditText)) {

						imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

						nameEditText.animate().setDuration(500).alpha(0f);
						nameEditText.setVisibility(EditText.GONE);

						nameTextView.setAlpha(0f);
						nameTextView.setVisibility(TextView.VISIBLE);
						nameTextView.animate().setDuration(500).alpha(1f);
					}
				}
			});

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
				((TableRow) child.findViewById(R.id.TableRow01)).setVisibility(View.GONE);

				TextWatcherClass txtWatcherClass = new TextWatcherClass(nameTxtView, child);
				nameTxtView.addTextChangedListener(txtWatcherClass);
				listenerList.add(txtWatcherClass);

			}
			else {
				((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(View.VISIBLE);
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
					((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(View.GONE);
				}
				else {
					((TableRow) child.findViewById(R.id.tableRow1)).setVisibility(View.VISIBLE);
				}

			}

		}
	}

}
