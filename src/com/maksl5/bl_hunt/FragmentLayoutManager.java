package com.maksl5.bl_hunt;



import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.R.bool;
import android.R.integer;
import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
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
	public static final int PAGE_PROFILE = 4;

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
		case PAGE_PROFILE:
			return parentInflater.inflate(R.layout.act_page_profile, rootContainer, false);

		}

		return new View(context);
	}

	/**
	 * @author Maksl5
	 * 
	 */
	public static class FoundDevicesLayout {

		public static final int ARRAY_INDEX_MAC_ADDRESS = 0;
		public static final int ARRAY_INDEX_NAME = 1;
		public static final int ARRAY_INDEX_RSSI = 2;
		public static final int ARRAY_INDEX_MANUFACTURER = 3;
		public static final int ARRAY_INDEX_EXP = 4;
		public static final int ARRAY_INDEX_TIME = 5;

		private volatile static String[] showedFdArray = {};
		private volatile static String[] completeFdList = {};
		private static String[] from =
				{
					"macAddress", "manufacturer", "exp", "RSSI", "name", "time" };
		private static int[] to = {
									R.id.macTxtView, R.id.manufacturerTxtView, R.id.expTxtView,
									R.id.rssiTxtView, R.id.nameTxtView, R.id.timeTxtView };

		private static ThreadManager threadManager = null;

		public static void refreshFoundDevicesList(final MainActivity mainActivity) {

			if (threadManager == null) {
				threadManager = new FoundDevicesLayout().new ThreadManager(mainActivity);
			}

			RefreshThread refreshThread =
					new FoundDevicesLayout().new RefreshThread(mainActivity, threadManager);
			if (refreshThread.canRun()) {
				refreshThread.execute();
			}

		}

		public static void filterFoundDevices(	String text,
												MainActivity mainActivity) {

			List<String> searchedList = new ArrayList<String>();

			ListView lv =
					(ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);
			FoundDevicesAdapter fdAdapter;

			if (text.equalsIgnoreCase("[unknown]")) {

				String unknownString =
						mainActivity.getString(R.string.str_foundDevices_manu_unkown);

				for (String deviceAsString : completeFdList) {

					String[] device = deviceAsString.split(String.valueOf((char) 30));

					if (device[ARRAY_INDEX_MANUFACTURER].equals(unknownString)) {
						searchedList.add(deviceAsString);
					}
				}

				showedFdArray = searchedList.toArray(new String[searchedList.size()]);
				fdAdapter =
						new FoundDevicesLayout().new FoundDevicesAdapter(mainActivity, R.layout.act_page_founddevices_row, showedFdArray);
				lv.setAdapter(fdAdapter);

			}
			else {
				showedFdArray = completeFdList;
				fdAdapter =
						new FoundDevicesLayout().new FoundDevicesAdapter(mainActivity, R.layout.act_page_founddevices_row, showedFdArray);
				lv.setAdapter(fdAdapter);
			}

		}

		private class RefreshThread extends AsyncTask<Void, Void, Void> {

			private MainActivity mainActivity;
			private ListView listView;

			private FoundDevicesAdapter fdAdapter;

			private ThreadManager threadManager;

			private boolean canRun = true;

			private RefreshThread(MainActivity mainActivity,
					ThreadManager threadManager) {

				super();
				this.mainActivity = mainActivity;
				this.listView =
						(ListView) mainActivity.mViewPager.getChildAt(PAGE_FOUND_DEVICES + 1).findViewById(R.id.listView2);
				this.fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
				if (this.fdAdapter == null || this.fdAdapter.isEmpty()) {
					this.fdAdapter =
							new FoundDevicesAdapter(mainActivity, R.layout.act_page_founddevices_row, showedFdArray);
				}
				this.listView.setAdapter(fdAdapter);

				this.threadManager = threadManager;

				if (!this.threadManager.setThread(this)) {
					canRun = false;
				}

			}

			public boolean canRun() {

				return canRun;
			}

			@Override
			protected Void doInBackground(Void... params) {

				List<HashMap<String, String>> devices =
						new DatabaseManager(mainActivity, mainActivity.versionCode).getAllDevices();
				String[] listViewArray = new String[devices.size()];

				String expString =
						mainActivity.getString(R.string.str_foundDevices_exp_abbreviation);
				DateFormat dateFormat = DateFormat.getDateTimeInstance();

				String tempString;

				for (int i = 0; i < devices.size(); i++) {

					HashMap<String, String> device = devices.get(i);

					String deviceMac = device.get(DatabaseHelper.COLUMN_MAC_ADDRESS);
					String manufacturer = device.get(DatabaseHelper.COLUMN_MANUFACTURER);
					String deviceTime = device.get(DatabaseHelper.COLUMN_TIME);

					if (manufacturer == null || manufacturer.equals("Unknown") || manufacturer.equals("")) {
						manufacturer = MacAddressAllocations.getManufacturer(deviceMac);
						new DatabaseManager(mainActivity, mainActivity.versionCode).addManufacturerToDevice(deviceMac, manufacturer);

						if (manufacturer.equals("Unknown")) {
							manufacturer =
									mainActivity.getString(R.string.str_foundDevices_manu_unkown);
						}
					}

					Long time =
							(deviceTime == null || deviceTime.equals("null")) ? 0 : Long.parseLong(deviceTime);

					tempString =
							deviceMac + (char) 30 + device.get(DatabaseHelper.COLUMN_NAME) + (char) 30 + "RSSI: " + device.get(DatabaseHelper.COLUMN_RSSI) + (char) 30 + manufacturer + (char) 30 + "+" + MacAddressAllocations.getExp(manufacturer.replace(" ", "_")) + " " + expString + (char) 30 + dateFormat.format(new Date(time));

					listViewArray[i] = tempString;

					showedFdArray = listViewArray;

					publishProgress();

				}

				if (!completeFdList.equals(listViewArray)) {
					completeFdList = listViewArray;
				}

				return null;

				// ListenerClass listenerClass = new FragmentLayoutManager().new ListenerClass();

				// lv.setOnHierarchyChangeListener(listenerClass);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Void result) {

				int scroll = listView.getFirstVisiblePosition();
				this.fdAdapter =
						new FoundDevicesAdapter(mainActivity, R.layout.act_page_founddevices_row, showedFdArray);
				listView.setAdapter(fdAdapter);
				listView.setSelection(scroll);

				threadManager.finished(this);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
			 */
			@Override
			protected void onProgressUpdate(Void... values) {

				int scroll = listView.getFirstVisiblePosition();
				this.fdAdapter =
						new FoundDevicesAdapter(mainActivity, R.layout.act_page_founddevices_row, showedFdArray);
				listView.setAdapter(fdAdapter);
				listView.setSelection(scroll);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPreExecute()
			 */
			@Override
			protected void onPreExecute() {

			}
		}

		private class ThreadManager {

			MainActivity mainActivity;
			RefreshThread refreshThread;
			boolean running;

			public ThreadManager(MainActivity mainActivity) {

				this.mainActivity = mainActivity;
			}

			/**
			 * @param refreshThread2
			 * @return
			 */
			public boolean setThread(RefreshThread refreshThread) {

				if (running) { return false; }

				this.refreshThread = refreshThread;
				running = true;
				return true;
			}

			public boolean finished(RefreshThread refreshThread) {

				if (this.refreshThread.equals(refreshThread)) {
					running = false;
					return true;
				}
				return false;
			}
		}

		public class FoundDevicesAdapter extends ArrayAdapter<String> {

			String[] devices;
			Context context;

			public FoundDevicesAdapter(Context context,
					int textViewResourceId,
					String[] objects) {

				super(context, textViewResourceId, objects);
				this.context = context;
				devices = objects;

			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				if (devices == null || devices[position] == null) { return null; }

				View rowView = convertView;
				if (rowView == null) {

					LayoutInflater inflater =
							(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					rowView = inflater.inflate(R.layout.act_page_founddevices_row, parent, false);

					ViewHolder viewHolder = new ViewHolder();

					viewHolder.macAddress = (TextView) rowView.findViewById(R.id.macTxtView);
					viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
					viewHolder.manufacturer =
							(TextView) rowView.findViewById(R.id.manufacturerTxtView);
					viewHolder.rssi = (TextView) rowView.findViewById(R.id.rssiTxtView);
					viewHolder.time = (TextView) rowView.findViewById(R.id.timeTxtView);
					viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
					viewHolder.nameTableRow = (TableRow) rowView.findViewById(R.id.tableRow1);

					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				String deviceAsString = devices[position];
				String[] device = deviceAsString.split(String.valueOf((char) 30));

				String nameString = device[ARRAY_INDEX_NAME];
				if (nameString == null || nameString.equals("null")) {
					nameString = "";
					holder.nameTableRow.setVisibility(View.GONE);
				}
				else {
					holder.nameTableRow.setVisibility(View.VISIBLE);
				}

				holder.macAddress.setText(device[ARRAY_INDEX_MAC_ADDRESS]);
				holder.name.setText(nameString);
				holder.manufacturer.setText(device[ARRAY_INDEX_MANUFACTURER]);
				holder.rssi.setText(device[ARRAY_INDEX_RSSI]);
				holder.time.setText(device[ARRAY_INDEX_TIME]);
				holder.exp.setText(device[ARRAY_INDEX_EXP]);

				return rowView;
			}

		}

		static class ViewHolder {

			TextView macAddress;
			TextView name;
			TextView manufacturer;
			TextView rssi;
			TextView time;
			TextView exp;
			TableRow nameTableRow;
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
			mainActivity.exp = exp;
			int level = LevelSystem.getLevel(exp);

			expTextView.setText(String.format("%d %s / %d %s", exp, mainActivity.getString(R.string.str_foundDevices_exp_abbreviation), LevelSystem.getLevelEndExp(level), mainActivity.getString(R.string.str_foundDevices_exp_abbreviation)));
			lvlTextView.setText(String.format("%s %d", mainActivity.getString(R.string.str_foundDevices_level), level));

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

			View parentContainer = mainActivity.mViewPager.getChildAt(PAGE_PROFILE + 1);

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
