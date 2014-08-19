package com.maksl5.bl_hunt.custom_ui;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.R.integer;
import android.app.Activity;
import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.DiscoveryManager.DiscoveryState;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.activity.MainActivity.CustomSectionFragment;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText.OnBackKeyClickedListener;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.storage.Achievement;
import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.MacAddressAllocations;



/**
 * @author Maksl5[Markus Bensing]
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

			DeviceDiscoveryLayout.lv = null;
			DeviceDiscoveryLayout.dAdapter = null;

			return parentInflater.inflate(R.layout.act_page_discovery, rootContainer, false);
		case PAGE_LEADERBOARD:
			return parentInflater.inflate(R.layout.act_page_leaderboard, rootContainer, false);
		case PAGE_FOUND_DEVICES:
			return parentInflater.inflate(R.layout.act_page_founddevices, rootContainer, false);
		case PAGE_ACHIEVEMENTS:
			return parentInflater.inflate(R.layout.act_page_achievements, rootContainer, false);
		case PAGE_PROFILE:
			return parentInflater.inflate(R.layout.act_page_profile, rootContainer, false);

		}

		return new View(context);
	}

	/**
	 * @author Maksl5
	 */
	public static class FoundDevicesLayout {

		public static final int ARRAY_INDEX_MAC_ADDRESS = 0;
		public static final int ARRAY_INDEX_NAME = 1;
		public static final int ARRAY_INDEX_RSSI = 2;
		public static final int ARRAY_INDEX_MANUFACTURER = 3;
		public static final int ARRAY_INDEX_EXP = 4;
		public static final int ARRAY_INDEX_TIME = 5;

		private static ArrayList<FDAdapterData> showedFdList = new ArrayList<FDAdapterData>();
		private static ArrayList<FDAdapterData> completeFdList = new ArrayList<FDAdapterData>();

		private static ThreadManager threadManager = null;

		public static int selectedItem = -1;

		private static OnItemLongClickListener onLongClickListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(	AdapterView<?> parent,
											View view,
											int position,
											long id) {

				if (view.getContext() instanceof MainActivity) {

					MainActivity mainActivity = (MainActivity) view.getContext();

					selectedItem = position;

					mainActivity.startActionMode(mainActivity.getBlueHunter().actionBarHandler.actionModeCallback);

					ListView foundDevListView = (ListView) parent;

					// for (int i = 0; i < foundDevListView.getChildCount(); i++) {
					// View child = foundDevListView.getChildAt(i);
					// ((CheckBox) child.findViewById(R.id.selectCheckbox)).setVisibility(View.VISIBLE);
					// }

					foundDevListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					foundDevListView.setItemChecked(position, true);

					return true;
				}
				else {
					return false;
				}
			}

		};

		public static void refreshFoundDevicesList(final BlueHunter bhApp) {

			if (threadManager == null) {
				threadManager = new FoundDevicesLayout().new ThreadManager();
			}

			RefreshThread refreshThread =
					new FoundDevicesLayout().new RefreshThread(bhApp, threadManager);
			if (refreshThread.canRun()) {
				refreshThread.execute();
			}
			else {

				ListView listView;
				FoundDevicesAdapter fdAdapter;

				if (bhApp.mainActivity.mViewPager == null) {
					bhApp.mainActivity.mViewPager =
							(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
				}

				ViewPager pager = bhApp.mainActivity.mViewPager;

				if (pager == null) { return; }

				View pageView = pager.getChildAt(PAGE_FOUND_DEVICES + 1);

				if (pageView == null) {
					listView = (ListView) pager.findViewById(R.id.listView2);
				}
				else {
					listView = (ListView) pageView.findViewById(R.id.listView2);
				}

				if (listView == null) {
					listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
				}

				if (listView == null) return;

				fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
				if (fdAdapter == null || fdAdapter.isEmpty()) {
					fdAdapter =
							new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity, R.layout.act_page_founddevices_row, showedFdList);
					listView.setAdapter(fdAdapter);
				}

				fdAdapter.refreshList(showedFdList);

			}

		}

		public static void filterFoundDevices(	String text,
												BlueHunter bhApp) {

			if (threadManager.running) return;

			ArrayList<FDAdapterData> searchedList = new ArrayList<FDAdapterData>();

			ListView listView;
			FoundDevicesAdapter fdAdapter;

			if (bhApp.mainActivity.mViewPager == null) {
				bhApp.mainActivity.mViewPager =
						(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
			}

			ViewPager pager = bhApp.mainActivity.mViewPager;

			if (pager == null) { return; }

			View pageView = pager.getChildAt(PAGE_FOUND_DEVICES + 1);

			if (pageView == null) {
				listView = (ListView) pager.findViewById(R.id.listView2);
			}
			else {
				listView = (ListView) pageView.findViewById(R.id.listView2);
			}

			if (listView == null) {
				listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
			}

			if (listView == null) return;

			fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
			if (fdAdapter == null || fdAdapter.isEmpty()) {
				fdAdapter =
						new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity, R.layout.act_page_founddevices_row, showedFdList);
				listView.setAdapter(fdAdapter);
			}

			
			text = text.toLowerCase();
			
			
			if (text.equals("[unknown]")) {

				String unknownString = bhApp.getString(R.string.str_foundDevices_manu_unkown);

				for (FDAdapterData data : completeFdList) {

					if (data.getManufacturer().equals(unknownString)) {
						searchedList.add(data);
					}
				}
				showedFdList = searchedList;
				fdAdapter.refreshList(showedFdList);

			}
			else if (text.length() == 0) {
				if (!showedFdList.equals(completeFdList)) {
					showedFdList = new ArrayList<FDAdapterData>(completeFdList);
					fdAdapter.refreshList(showedFdList);

				}
			}
			else {

				ArrayList<FDAdapterData> filterList = new ArrayList<FDAdapterData>(completeFdList);

				final int count = filterList.size();
				final ArrayList<FDAdapterData> newValues = new ArrayList<FDAdapterData>();

				for (int i = 0; i < count; i++) {
					final FDAdapterData data = filterList.get(i);

					if (data.getMacAddress().toLowerCase().contains(text))
						if (!newValues.contains(data)) newValues.add(data);

					if (data.getName() != null && data.getName().toLowerCase().contains(text))
						if (!newValues.contains(data)) newValues.add(data);

					if (data.getTimeFormatted().toLowerCase().contains(text))
						if (!newValues.contains(data)) newValues.add(data);

					if (data.getManufacturer().toLowerCase().contains(text))
						if (!newValues.contains(data)) newValues.add(data);

					if (data.getExpString().toLowerCase().contains(text))
						if (!newValues.contains(data)) newValues.add(data);

				}

				showedFdList = newValues;
				fdAdapter.refreshList(showedFdList);

			}

		}

		/**
		 * @return
		 */
		public static String getSelectedMac() {

			if (selectedItem == -1) return null;

			return showedFdList.get(selectedItem).getMacAddress();

		}

		private class RefreshThread extends AsyncTask<Void, ArrayList<FDAdapterData>, ArrayList<FDAdapterData>> {

			private BlueHunter bhApp;
			private ListView listView;

			private FoundDevicesAdapter fdAdapter;

			private ThreadManager threadManager;

			private boolean canRun = true;

			private int scrollIndex;
			private int scrollTop;

			private RefreshThread(BlueHunter app,
					ThreadManager threadManager) {

				super();
				this.bhApp = app;

				if (bhApp.mainActivity.mViewPager == null) {
					bhApp.mainActivity.mViewPager =
							(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
				}

				ViewPager pager = bhApp.mainActivity.mViewPager;

				if (pager == null) {
					canRun = false;
					return;
				}

				View pageView = pager.getChildAt(PAGE_FOUND_DEVICES + 1);

				if (pageView == null) {
					listView = (ListView) pager.findViewById(R.id.listView2);
				}
				else {
					listView = (ListView) pageView.findViewById(R.id.listView2);
				}

				if (listView == null) {
					listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView2);
				}

				if (listView == null) {
					canRun = false;
					return;
				}

				listView.setOnItemLongClickListener(onLongClickListener);

				scrollIndex = listView.getFirstVisiblePosition();
				View v = listView.getChildAt(0);
				scrollTop = (v == null) ? 0 : v.getTop();

				this.fdAdapter = (FoundDevicesAdapter) listView.getAdapter();
				if (this.fdAdapter == null || this.fdAdapter.isEmpty()) {
					this.fdAdapter =
							new FoundDevicesAdapter(bhApp.mainActivity, R.layout.act_page_founddevices_row, showedFdList);
					this.listView.setAdapter(fdAdapter);
				}

				this.threadManager = threadManager;

				if (!this.threadManager.setThread(this)) {
					canRun = false;
				}

			}

			public boolean canRun() {

				return canRun;
			}

			@Override
			protected ArrayList<FDAdapterData> doInBackground(Void... params) {

				List<FoundDevice> devices = new DatabaseManager(bhApp).getAllDevices();
				ArrayList<FDAdapterData> listViewList = new ArrayList<FDAdapterData>();

				String expString = bhApp.getString(R.string.str_foundDevices_exp_abbreviation);
				DateFormat dateFormat = DateFormat.getDateTimeInstance();

				FDAdapterData adapterData;

				for (int i = 0; i < devices.size(); i++) {

					FoundDevice device = devices.get(i);

					String deviceMac = device.getMacAddress();
					String manufacturer = device.getManufacturer();
					Long time = device.getTime();
					float bonusExpMultiplier = device.getBonus();
					short rssi = device.getRssi();

					if (manufacturer == null || manufacturer.equals("Unknown") || manufacturer.equals("")) {
						manufacturer = MacAddressAllocations.getManufacturer(deviceMac);
						if (!manufacturer.equals("Unknown")) {
							new DatabaseManager(bhApp).addManufacturerToDevice(deviceMac, manufacturer);
						}
					}

					if (time == -1) time = (long) 0;

					if (bonusExpMultiplier == -1f) {
						FDAdapterData addBonus = new FDAdapterData();
						addBonus.setMacAddress(deviceMac);
						addBonus.setManufacturer("[ADDBONUS]");

						ArrayList<FDAdapterData> publishList = new ArrayList<FDAdapterData>();
						publishList.add(addBonus);

						publishProgress(publishList);
						bonusExpMultiplier = 0f;
					}

					// exp calc
					int bonusExp =
							(int) Math.floor(MacAddressAllocations.getExp(manufacturer) * bonusExpMultiplier);

					String completeExpString =
							(bonusExp == 0) ? "+" + MacAddressAllocations.getExp(manufacturer) + " " + expString : "+" + MacAddressAllocations.getExp(manufacturer) + " + " + bonusExp + " " + expString;

					if (manufacturer.equals("Unknown")) {
						manufacturer = bhApp.getString(R.string.str_foundDevices_manu_unkown);
					}

					// RSSI calculation
					int rssiRes = 0;

					// 0 bars
					if (rssi <= -102) rssiRes = 0;

					// 1 bar
					if (rssi >= -101 && rssi <= -93) rssiRes = R.drawable.rssi_1;

					// 2 bars
					if (rssi >= -92 && rssi <= -87) rssiRes = R.drawable.rssi_2;

					// 3 bars
					if (rssi >= -86 && rssi <= -78) rssiRes = R.drawable.rssi_3;

					// 4 bars
					if (rssi >= -77 && rssi <= -40) rssiRes = R.drawable.rssi_4;

					// 5 bars
					if (rssi >= -41) rssiRes = R.drawable.rssi_5;

					adapterData =
							new FDAdapterData(deviceMac, device.getName(), rssiRes, manufacturer, completeExpString, dateFormat.format(new Date(time)));

					listViewList.add(adapterData);

					publishProgress(listViewList);

				}

				return listViewList;

				// ListenerClass listenerClass = new FragmentLayoutManager().new ListenerClass();

				// lv.setOnHierarchyChangeListener(listenerClass);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(ArrayList<FDAdapterData> result) {

				if (!completeFdList.equals(result)) {
					completeFdList = new ArrayList<FDAdapterData>(result);
					showedFdList = new ArrayList<FDAdapterData>(result);
				}

				fdAdapter.refreshList(showedFdList);

				listView.setSelectionFromTop(scrollIndex, scrollTop);

				threadManager.finished(this);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
			 */
			@Override
			protected void onProgressUpdate(ArrayList<FDAdapterData>... values) {

				// showedFdList = values[0];

				// fdAdapter.refill(showedFdList);

				if (values[0].get(0).getManufacturer().equals("[ADDBONUS]"))
					new DatabaseManager(bhApp).addBonusToDevices(values[0].get(0).getMacAddress(), 0f);

				listView.setSelectionFromTop(scrollIndex, scrollTop);

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

			RefreshThread refreshThread;
			boolean running;

			/**
			 * @param refreshThread
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

		public class FDAdapterData {

			public FDAdapterData(String macAddress,
					String name,
					int rssiRes,
					String manufacturer,
					String expString,
					String timeFormatted) {

				this.macAddress = macAddress;
				this.name = name;
				this.rssiRes = rssiRes;
				this.manufacturer = manufacturer;
				this.expString = expString;
				this.timeFormatted = timeFormatted;
			}

			/**
			 * 
			 */
			public FDAdapterData() {

				// TODO Auto-generated constructor stub
			}

			private String macAddress;
			private String name;
			private int rssiRes;
			private String manufacturer;
			private String expString;
			private String timeFormatted;

			public String getMacAddress() {

				return macAddress;
			}

			public void setMacAddress(String macAddress) {

				this.macAddress = macAddress;
			}

			public String getName() {

				return name;
			}

			public void setName(String name) {

				this.name = name;
			}

			public int getRssiRes() {

				return rssiRes;
			}

			public void setRssiRes(int rssiRes) {

				this.rssiRes = rssiRes;
			}

			public String getManufacturer() {

				return manufacturer;
			}

			public void setManufacturer(String manufacturer) {

				this.manufacturer = manufacturer;
			}

			public String getExpString() {

				return expString;
			}

			public void setExpString(String expString) {

				this.expString = expString;
			}

			public String getTimeFormatted() {

				return timeFormatted;
			}

			public void setTimeFormatted(String timeFormatted) {

				this.timeFormatted = timeFormatted;
			}

			@Override
			public boolean equals(Object o) {

				return macAddress.equals(((FDAdapterData) o).macAddress);
			}

		}

		public class FoundDevicesAdapter extends ArrayAdapter<FDAdapterData> {

			private ArrayList<FDAdapterData> dataList;
			private ArrayList<FDAdapterData> originalDataList;
			public FoundDevicesAdapter(Context context,
					int textViewResourceId,
					ArrayList<FDAdapterData> objects) {

				super(context, textViewResourceId, objects);
				dataList = objects;
				originalDataList = objects;

			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater =
							(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					rowView = inflater.inflate(R.layout.act_page_founddevices_row, null);

					ViewHolder viewHolder = new ViewHolder();

					viewHolder.macAddress = (TextView) rowView.findViewById(R.id.macTxtView);
					viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
					viewHolder.manufacturer =
							(TextView) rowView.findViewById(R.id.manufacturerTxtView);
					viewHolder.rssi = (ImageView) rowView.findViewById(R.id.rssiView);
					viewHolder.time = (TextView) rowView.findViewById(R.id.timeTxtView);
					viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
					viewHolder.nameTableRow = (TableRow) rowView.findViewById(R.id.tableRow1);

					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				FDAdapterData data = dataList.get(position);

				if (holder != null && data != null) {

					String nameString = data.getName();
					if (nameString == null || nameString.equals("null")) {
						nameString = "";
						holder.nameTableRow.setVisibility(View.GONE);
					}
					else {
						holder.nameTableRow.setVisibility(View.VISIBLE);
					}

					holder.macAddress.setText(data.getMacAddress());
					holder.name.setText(nameString);
					holder.manufacturer.setText(data.getManufacturer());

					int rssiId = data.getRssiRes();
					if (rssiId == 0)
						holder.rssi.setImageResource(android.R.color.transparent);
					else
						holder.rssi.setImageResource(rssiId);

					holder.time.setText(data.getTimeFormatted());
					holder.exp.setText(data.getExpString());

				}
				return rowView;
			}

			public void refreshList(ArrayList<FDAdapterData> data) {

				clear();
				addAll(new ArrayList<FDAdapterData>(data));
				notifyDataSetChanged();

			}

		}

		static class ViewHolder {

			TextView macAddress;
			TextView name;
			TextView manufacturer;
			ImageView rssi;
			TextView time;
			TextView exp;
			TableRow nameTableRow;
			CheckBox selectCheckBox;
		}

	}

	/**
	 * @author Maksl5
	 */
	public static class DeviceDiscoveryLayout {

		private static DiscoveryAdapter dAdapter;
		private static ListView lv;

		private static ArrayList<FoundDevice> curList;

		public static void updateIndicatorViews(MainActivity mainActivity) {

			TextView expTextView = (TextView) mainActivity.findViewById(R.id.expIndicator);
			TextView lvlTextView = (TextView) mainActivity.findViewById(R.id.lvlIndicator);
			TextView devicesTextView = (TextView) mainActivity.findViewById(R.id.txt_devices);
			TextView devExpPerDayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpPerDay);
			TextView devExpTodayTxt = (TextView) mainActivity.findViewById(R.id.txt_devExpToday);

			PatternProgressBar progressBar =
					(PatternProgressBar) mainActivity.findViewById(R.id.progressBar1);

			int exp = LevelSystem.getUserExp(mainActivity.getBlueHunter());
			mainActivity.exp = exp;
			int level = LevelSystem.getLevel(exp);

			DecimalFormat df = new DecimalFormat(",###");

			expTextView.setText(String.format("%s %s / %s %s", df.format(exp), mainActivity.getString(R.string.str_foundDevices_exp_abbreviation), df.format(LevelSystem.getLevelEndExp(level)), mainActivity.getString(R.string.str_foundDevices_exp_abbreviation)));
			lvlTextView.setText(String.format("%d", level));

			progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
			progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));

			int deviceNum = new DatabaseManager(mainActivity.getBlueHunter()).getDeviceNum();
			long firstTime =
					new DatabaseManager(mainActivity.getBlueHunter()).getDevice(DatabaseManager.DatabaseHelper.COLUMN_TIME + " != 0", DatabaseManager.DatabaseHelper.COLUMN_TIME + " DESC").getTime();

			long now = System.currentTimeMillis();
			double devPerDay = (deviceNum / (double) (now - firstTime)) * 86400000;
			double expPerDay = (exp / (double) (now - firstTime)) * 86400000;

			devicesTextView.setText(df.format(deviceNum));
			devExpPerDayTxt.setText(String.format("%.2f / %.2f", devPerDay, expPerDay));

			List<FoundDevice> devicesToday =
					new DatabaseManager(mainActivity.getBlueHunter()).getDevices(DatabaseManager.DatabaseHelper.COLUMN_TIME + " < " + now + " AND " + DatabaseManager.DatabaseHelper.COLUMN_TIME + " > " + (now - 86400000), null);
			int devicesTodayNum = devicesToday.size();

			int expTodayNum = 0;

			for (Iterator iterator = devicesToday.iterator(); iterator.hasNext();) {
				FoundDevice deviceIt = (FoundDevice) iterator.next();
				String manufacturer = deviceIt.getManufacturer();

				float bonus = deviceIt.getBonus();

				expTodayNum += MacAddressAllocations.getExp(manufacturer) * (1 + bonus);
			}

			devExpTodayTxt.setText(String.format("%d / %d", devicesTodayNum, expTodayNum));

			curList =
					new ArrayList<FoundDevice>(mainActivity.getBlueHunter().disMan.getFDInCurDiscovery());

			TextView devInCurDisTxt =
					(TextView) mainActivity.findViewById(R.id.txt_discovery_devInCycle_value);
			devInCurDisTxt.setText("" + curList.size());

			if (dAdapter == null) {
				dAdapter = new DeviceDiscoveryLayout().new DiscoveryAdapter(mainActivity, curList);

				if (lv == null) lv = (ListView) mainActivity.findViewById(R.id.discoveryListView);

				lv.setAdapter(dAdapter);
			}

			if (lv != null && mainActivity != null && mainActivity.getBlueHunter() != null && mainActivity.getBlueHunter().disMan != null && mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != -2) {

				if (mainActivity.getBlueHunter().disMan.getCurDiscoveryState() == DiscoveryState.DISCOVERY_STATE_RUNNING && lv.getVisibility() != View.VISIBLE) {
					startShowLV(mainActivity);
				}
				else if (mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != DiscoveryState.DISCOVERY_STATE_RUNNING && mainActivity.getBlueHunter().disMan.getCurDiscoveryState() != DiscoveryState.DISCOVERY_STATE_FINISHED && lv.getVisibility() == View.VISIBLE) {
					stopShowLV(mainActivity);
				}
			}

			dAdapter.notifyDataSetChanged();
		}

		public static void startShowLV(MainActivity main) {

			AlphaAnimation animation = new AlphaAnimation(1f, 0f);
			animation.setDuration(250);

			TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);
			discoveryInfo.startAnimation(animation);

			lv.setVisibility(View.VISIBLE);

			animation = new AlphaAnimation(0f, 1f);

			animation.setDuration(250);
			discoveryInfo.startAnimation(animation);
			lv.startAnimation(animation);

		}

		public static void stopShowLV(MainActivity main) {

			AlphaAnimation animation = new AlphaAnimation(1f, 0f);
			animation.setDuration(250);

			TableLayout discoveryInfo = (TableLayout) main.findViewById(R.id.discoveryInfoTable);

			lv.startAnimation(animation);
			discoveryInfo.startAnimation(animation);

			lv.setVisibility(View.GONE);

			animation = new AlphaAnimation(0f, 1f);
			animation.setDuration(250);

			discoveryInfo.startAnimation(animation);

		}

		public class DiscoveryAdapter extends ArrayAdapter<FoundDevice> {

			private Activity context;
			private ArrayList<FoundDevice> values;

			public DiscoveryAdapter(Activity context,
					ArrayList<FoundDevice> curList) {

				super(context, R.layout.act_page_discovery_row, curList);
				this.values = curList;
				this.context = context;
			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater = context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.act_page_discovery_row, null);

					Holder holder = new Holder();
					holder.macAddress = (TextView) rowView.findViewById(R.id.macTxtView);
					holder.manufacturer = (TextView) rowView.findViewById(R.id.manufacturerTxtView);
					holder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
					holder.rssi = (ImageView) rowView.findViewById(R.id.rssiView);

					rowView.setTag(holder);

				}

				Holder vHolder = (Holder) rowView.getTag();

				FoundDevice device = values.get(position);

				String mac = device.getMacAddress();
				String manufacturer = device.getManufacturer();

				int exp = MacAddressAllocations.getExp(manufacturer);

				int bonusExp =
						(int) Math.floor(MacAddressAllocations.getExp(manufacturer) * device.getBonus());

				exp = bonusExp + exp;

				int rssi = device.getRssi();
				int rssiRes = 0;

				// 0 bars
				if (rssi <= -102) rssiRes = 0;

				// 1 bar
				if (rssi >= -101 && rssi <= -93) rssiRes = R.drawable.rssi_1;

				// 2 bars
				if (rssi >= -92 && rssi <= -87) rssiRes = R.drawable.rssi_2;

				// 3 bars
				if (rssi >= -86 && rssi <= -78) rssiRes = R.drawable.rssi_3;

				// 4 bars
				if (rssi >= -77 && rssi <= -40) rssiRes = R.drawable.rssi_4;

				// 5 bars
				if (rssi >= -41) rssiRes = R.drawable.rssi_5;

				if (manufacturer.equals("Unknown")) {
					manufacturer = context.getString(R.string.str_foundDevices_manu_unkown);
				}

				String expString = context.getString(R.string.str_foundDevices_exp_abbreviation);

				vHolder.macAddress.setText(mac);
				vHolder.manufacturer.setText(manufacturer);
				vHolder.exp.setText("+" + exp + " " + expString);
				vHolder.rssi.setImageResource(rssiRes);

				return rowView;
			}

		}

		static class Holder {

			TextView macAddress;
			TextView manufacturer;
			TextView exp;
			ImageView rssi;

		}

	}

	/**
	 * @author Maksl5
	 */
	public static class ProfileLayout {

		public static String userName = "";
		private static String backUpName = "";
		private static boolean isEditable = true;

		public static void initializeView(final MainActivity mainActivity) {

			View parentContainer = mainActivity.mViewPager.getChildAt(PAGE_PROFILE + 1);

			final TextView nameTextView =
					(TextView) parentContainer.findViewById(R.id.nameTextView);
			final AdjustedEditText nameEditText =
					(AdjustedEditText) parentContainer.findViewById(R.id.nameEditText);

			final QuickContactBadge contactImage =
					(QuickContactBadge) parentContainer.findViewById(R.id.contactBadge);

			// Listener
			nameTextView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {

					if (isEditable) {
						nameEditText.setText(nameTextView.getText());

						nameTextView.animate().setDuration(500).alpha(0f);
						nameTextView.setVisibility(TextView.GONE);

						nameEditText.setAlpha(0f);
						nameEditText.setVisibility(EditText.VISIBLE);
						nameEditText.animate().setDuration(500).alpha(1f);

						InputMethodManager imm =
								(InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					}
					return true;
				}
			});

			OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(	TextView v,
												int actionId,
												KeyEvent event) {

					InputMethodManager imm =
							(InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

					if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && nameEditText.isShown()) {

						imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);

						backUpName = userName;
						userName = nameEditText.getText().toString();

						nameTextView.setText("Applying...");
						submit(nameTextView);

						nameEditText.animate().setDuration(500).alpha(1f);
						nameEditText.setVisibility(EditText.GONE);

						nameTextView.setAlpha(0f);
						nameTextView.setVisibility(TextView.VISIBLE);
						nameTextView.animate().setDuration(500).alpha(1f);

						return true;
					}
					return false;
				}

				private void submit(TextView nameTextView) {

					isEditable = false;

					mainActivity.getBlueHunter().authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

						@Override
						public boolean onResult(int requestId,
												String resultString) {

							if (requestId == Authentification.NETRESULT_ID_APPLY_NAME) {

								Pattern pattern = Pattern.compile("Error=(\\d+)");
								Matcher matcher = pattern.matcher(resultString);

								if (matcher.find()) {
									int error = Integer.parseInt(matcher.group(1));
									String errorMsg =
											ErrorHandler.getErrorString(mainActivity, requestId, error);

									setName(mainActivity.getBlueHunter(), backUpName, true);

									Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_LONG).show();
								}
								else if (resultString.equals("<done />")) {
									setName(mainActivity.getBlueHunter(), userName, true);
								}
								if (mainActivity.getBlueHunter().loginManager.getLoginState())
									isEditable = true;
							}

							return true;
						}
					});

					NetworkThread applyName = new NetworkThread(mainActivity.getBlueHunter());
					applyName.execute(AuthentificationSecure.SERVER_APPLY_NAME, String.valueOf(Authentification.NETRESULT_ID_APPLY_NAME), "lt=" + mainActivity.getBlueHunter().authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + mainActivity.getBlueHunter().authentification.getStoredPass(), "n=" + userName);

				}
			};

			nameEditText.setOnEditorActionListener(onEditorActionListener);

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

			mainActivity.getBlueHunter().authentification.setOnLoginChangeListener(new OnLoginChangeListener() {

				@Override
				public void loginStateChange(boolean loggedIn) {

					if (loggedIn) {
						nameTextView.setText(userName);
						nameTextView.setTextColor(mainActivity.getResources().getColor(R.color.text_holo_light_blue));
						isEditable = true;

						contactImage.setEnabled(true);
						contactImage.setColorFilter(null);
					}
					else {
						nameTextView.setText("Not logged in.");
						nameTextView.setTextColor(Color.GRAY);
						isEditable = false;

						ColorMatrix cMatrix = new ColorMatrix();
						cMatrix.setSaturation(0);

						contactImage.setEnabled(false);
						contactImage.setColorFilter(new ColorMatrixColorFilter(cMatrix));
					}

				}
			});

			contactImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					mainActivity.startActivityForResult(Intent.createChooser(intent, "Select User Image"), MainActivity.REQ_PICK_USER_IMAGE);

				}
			});

		}

		/**
		 * @param nameString
		 */
		public static void setName(	BlueHunter blueHunter,
									String nameString,
									boolean forceSet) {

			userName = nameString;

			View parentContainer = blueHunter.mainActivity.mViewPager.getChildAt(PAGE_PROFILE + 1);

			TextView nameTextView;

			if (parentContainer == null) {
				nameTextView = (TextView) blueHunter.mainActivity.findViewById(R.id.nameTextView);
			}
			else {
				nameTextView = (TextView) parentContainer.findViewById(R.id.nameTextView);
			}

			if (nameTextView == null) return;

			if (blueHunter.loginManager.getLoginState()) {
				nameTextView.setText(userName);
				nameTextView.setTextColor(blueHunter.getResources().getColor(R.color.text_holo_light_blue));
				isEditable = true;
			}
			else {
				nameTextView.setText("Not logged in.");
				nameTextView.setTextColor(Color.GRAY);
				isEditable = false;
			}

		}

		public static void passPickedImage(	MainActivity mainActivity,
											Intent intent) {

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			try {
				BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(intent.getData()), null, o);
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			final int REQUIRED_SIZE = 200;

			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
					break;
				}
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inDither = true;

			try {
				Bitmap scaledUserImage =
						BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(intent.getData()), null, o2);

				View parentContainer = mainActivity.mViewPager.getChildAt(PAGE_PROFILE + 1);
				QuickContactBadge contactBadge =
						(QuickContactBadge) parentContainer.findViewById(R.id.contactBadge);

				contactBadge.setImageBitmap(scaledUserImage);

			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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

	/**
	 * @author Maksl5
	 */
	public static class LeaderboardLayout {

		public static final int ARRAY_INDEX_NAME = 0;
		public static final int ARRAY_INDEX_LEVEL = 1;
		public static final int ARRAY_INDEX_PROGRESS_MAX = 2;
		public static final int ARRAY_INDEX_PROGRESS_VALUE = 3;
		public static final int ARRAY_INDEX_DEV_NUMBER = 4;
		public static final int ARRAY_INDEX_EXP = 5;
		public static final int ARRAY_INDEX_ID = 6;

		private volatile static ArrayList<LBAdapterData> showedFdList =
				new ArrayList<LBAdapterData>();
		public volatile static ArrayList<LBAdapterData> completeFdList =
				new ArrayList<LBAdapterData>();

		private static ThreadManager threadManager = null;

		public static HashMap<Integer, Integer> changeList = new HashMap<Integer, Integer>();

		public static void refreshLeaderboard(final BlueHunter bhApp) {

			refreshLeaderboard(bhApp, false);
		}

		public static void refreshLeaderboard(	final BlueHunter bhApp,
												boolean orientationChanged) {

			if (orientationChanged) {

				ListView listView;
				LeaderboardAdapter ldAdapter;

				if (bhApp.mainActivity.mViewPager == null) {
					bhApp.mainActivity.mViewPager =
							(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
				}

				ViewPager pager = bhApp.mainActivity.mViewPager;
				View pageView = pager.getChildAt(PAGE_LEADERBOARD + 1);

				if (pageView == null) {
					listView = (ListView) pager.findViewById(R.id.listView1);
				}
				else {
					listView = (ListView) pageView.findViewById(R.id.listView1);
				}

				if (listView == null) {
					listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView1);
				}

				if (listView == null) { return; }

				ldAdapter = (LeaderboardAdapter) listView.getAdapter();
				if (ldAdapter == null || ldAdapter.isEmpty()) {
					ldAdapter =
							new LeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity, R.layout.act_page_leaderboard_row, showedFdList);
					listView.setAdapter(ldAdapter);
				}

				ldAdapter.notifyDataSetChanged();
				return;

			}

			if (threadManager == null) {
				threadManager = new LeaderboardLayout().new ThreadManager();
			}

			RefreshThread refreshThread =
					new LeaderboardLayout().new RefreshThread(bhApp, threadManager);
			if (refreshThread.canRun()) {
				showedFdList.clear();
				bhApp.actionBarHandler.getMenuItem(R.id.menu_search).collapseActionView();
				refreshThread.execute(1, 5);
			}

		}

		public static void filterLeaderboard(	String text,
												BlueHunter bhApp) {

			if (threadManager.running) return;

			List<String> searchedList = new ArrayList<String>();

			if (bhApp.mainActivity.mViewPager == null) {
				bhApp.mainActivity.mViewPager =
						(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
			}

			ViewPager pager = bhApp.mainActivity.mViewPager;
			View pageView = pager.getChildAt(PAGE_LEADERBOARD + 1);
			ListView lv;
			
			
			if (pageView == null) {
				lv = (ListView) pager.findViewById(R.id.listView1);
			}
			else {
				lv = (ListView) pageView.findViewById(R.id.listView1);
			}

			if (lv == null) {
				lv = (ListView) bhApp.mainActivity.findViewById(R.id.listView1);
			}

			if (lv == null) {
				return;
			}
			
			LeaderboardAdapter lbAdapter = (LeaderboardAdapter) lv.getAdapter();
			
			if (lbAdapter == null || lbAdapter.isEmpty()) {
				lbAdapter =
						new LeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity, R.layout.act_page_leaderboard_row, showedFdList);
				lv.setAdapter(lbAdapter);
			}
			
			text = text.toLowerCase();
			
			if (text.length() == 0) {
				if (!showedFdList.equals(completeFdList)) {
					showedFdList = new ArrayList<LBAdapterData>(completeFdList);
					lbAdapter.refreshList(showedFdList);
				}
			}
			else {

					ArrayList<LBAdapterData> filterList =
							new ArrayList<LBAdapterData>(completeFdList);

					final int count = filterList.size();
					final ArrayList<LBAdapterData> newValues = new ArrayList<LBAdapterData>();

					for (int i = 0; i < count; i++) {
						final LBAdapterData data = filterList.get(i);

						if (data.getName().toLowerCase().contains(text))
							if (!newValues.contains(data)) newValues.add(data);

						if (("" + data.getDevNum()).toLowerCase().contains(text))
							if (!newValues.contains(data)) newValues.add(data);

						if (("" + data.getExp()).toLowerCase().contains(text))
							if (!newValues.contains(data)) newValues.add(data);

						if (("" + data.getLevel()).toLowerCase().contains(text))
							if (!newValues.contains(data)) newValues.add(data);

					}

					showedFdList = newValues;
					lbAdapter.refreshList(showedFdList);

			}

		}

		private class RefreshThread extends AsyncTask<Integer, Void, String> {

			private BlueHunter bhApp;
			private ListView listView;

			private LeaderboardAdapter ldAdapter;

			private ThreadManager threadManager;

			private boolean canRun = true;

			private int scrollIndex;
			private int scrollTop;

			private int startIndex;
			private int length;

			private RefreshThread(BlueHunter app,
					ThreadManager threadManager) {

				super();
				this.bhApp = app;

				if (bhApp.mainActivity.mViewPager == null) {
					bhApp.mainActivity.mViewPager =
							(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
				}

				ViewPager pager = bhApp.mainActivity.mViewPager;
				View pageView = pager.getChildAt(PAGE_LEADERBOARD + 1);

				if (pageView == null) {
					listView = (ListView) pager.findViewById(R.id.listView1);
				}
				else {
					listView = (ListView) pageView.findViewById(R.id.listView1);
				}

				if (listView == null) {
					listView = (ListView) bhApp.mainActivity.findViewById(R.id.listView1);
				}

				if (listView == null) {
					canRun = false;
					return;
				}

				scrollIndex = listView.getFirstVisiblePosition();
				View v = listView.getChildAt(0);
				scrollTop = (v == null) ? 0 : v.getTop();

				this.ldAdapter = (LeaderboardAdapter) listView.getAdapter();
				if (this.ldAdapter == null || this.ldAdapter.isEmpty()) {
					this.ldAdapter =
							new LeaderboardAdapter(bhApp.mainActivity, R.layout.act_page_leaderboard_row, showedFdList);
					this.listView.setAdapter(ldAdapter);
				}

				this.threadManager = threadManager;

				if (!this.threadManager.setThread(this)) {
					canRun = false;
				}

			}

			public boolean canRun() {

				return canRun;
			}

			@Override
			protected String doInBackground(Integer... params) {

				startIndex = params[0];
				length = params[1];

				try {

					List<NameValuePair> postValues = new ArrayList<NameValuePair>();

					
					
					URI httpUri =
							URI.create(AuthentificationSecure.SERVER_GET_LEADERBOARD + "?s=" + startIndex + "&l=" + length);

					HttpClient httpClient;

					httpClient = new DefaultHttpClient();

					HttpPost postRequest = new HttpPost(httpUri);

					postRequest.setEntity(new UrlEncodedFormEntity(postValues));

					HttpResponse httpResponse = httpClient.execute(postRequest);

					String result = EntityUtils.toString(httpResponse.getEntity());

					if (!String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("2")) { return "Error=" + httpResponse.getStatusLine().getStatusCode(); }

					return result;
				}
				catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "Error=5\n" + e.getMessage();
				}
				catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "Error=4\n" + e.getMessage();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "Error=1\n" + e.getMessage();
				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(String result) {

				try {

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(result);
					if (matcher.find()) {
						int errorCode = Integer.parseInt(matcher.group(1));

						LBAdapterData data =
								new LBAdapterData("Error " + errorCode, 0, 100, 0, 0, 0, 0);
						showedFdList.add(data);

						if (ldAdapter != null) ldAdapter.refreshList(showedFdList);

						listView.setSelectionFromTop(scrollIndex, scrollTop);

						threadManager.finished(this);

						return;
					}

					DocumentBuilder docBuilder;
					Document document = null;

					try {
						docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						document = docBuilder.parse(new InputSource(new StringReader(result)));
					}
					catch (Exception e) {

					}

					document.getDocumentElement().normalize();

					NodeList nodes = document.getElementsByTagName("user");

					boolean last = false;

					for (int i = 0; i < nodes.getLength(); i++) {

						Node node = nodes.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {

							Element element = (Element) node;

							int id = Integer.parseInt(element.getAttribute("id"));
							int rank = Integer.parseInt(element.getAttribute("rank"));

							String name =
									element.getElementsByTagName("name").item(0).getTextContent();
							int exp =
									Integer.parseInt(element.getElementsByTagName("exp").item(0).getTextContent());
							int num =
									Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());

							int level = LevelSystem.getLevel(exp);
							int progressMax =
									LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level);
							int progressValue = exp - LevelSystem.getLevelStartExp(level);

							last = element.getAttribute("last").equals("1");

							LBAdapterData data =
									new LBAdapterData(name, level, progressMax, progressValue, num, exp, id);

							completeFdList.add(data);
							completeFdList.set(rank - 1, data);
							showedFdList = completeFdList;

						}

					}

					threadManager.finished(this);

					if (last) {
						showedFdList = completeFdList;
						ldAdapter.notifyDataSetChanged();

						listView.setSelectionFromTop(scrollIndex, scrollTop);
					}
					else {

						ldAdapter.notifyDataSetChanged();
						new RefreshThread(bhApp, threadManager).execute(startIndex + length, length);

					}

				}
				catch (NullPointerException e) {
					if (bhApp != null && threadManager != null) {
						new RefreshThread(bhApp, threadManager).execute(startIndex, length);
					}

				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
			 */
			@Override
			protected void onProgressUpdate(Void... values) {

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

			RefreshThread refreshThread;
			boolean running;

			/**
			 * @param refreshThread2
			 * @return
			 */
			public boolean setThread(RefreshThread refreshThread) {

				if (running) { return false; }

				this.refreshThread = refreshThread;
				setRunning(true);
				return true;
			}

			public boolean finished(RefreshThread refreshThread) {

				if (this.refreshThread.equals(refreshThread)) {
					setRunning(false);
					return true;
				}
				return false;
			}

			private void setRunning(boolean running) {

				this.running = running;

				if (!running) {
					if (!refreshThread.bhApp.netMananger.areThreadsRunning()) {
						MenuItem progressBar =
								refreshThread.bhApp.actionBarHandler.getMenuItem(R.id.menu_progress);
						progressBar.setVisible(false);
					}
				}
				else {
					MenuItem progressBar =
							refreshThread.bhApp.actionBarHandler.getMenuItem(R.id.menu_progress);
					progressBar.setVisible(true);
				}

			}
		}

		public class LBAdapterData {

			private String name;
			private int level;
			private int progressMax;
			private int progressValue;
			private int devNum;
			private int exp;
			private int id;

			public LBAdapterData(String name,
					int level,
					int progressMax,
					int progressValue,
					int devNum,
					int exp,
					int id) {

				this.name = name;
				this.level = level;
				this.progressMax = progressMax;
				this.progressValue = progressValue;
				this.devNum = devNum;
				this.exp = exp;
				this.id = id;
			}

			public String getName() {

				return name;
			}

			public void setName(String name) {

				this.name = name;
			}

			public int getLevel() {

				return level;
			}

			public void setLevel(int level) {

				this.level = level;
			}

			public int getProgressMax() {

				return progressMax;
			}

			public void setProgressMax(int progressMax) {

				this.progressMax = progressMax;
			}

			public int getProgressValue() {

				return progressValue;
			}

			public void setProgressValue(int progressValue) {

				this.progressValue = progressValue;
			}

			public int getDevNum() {

				return devNum;
			}

			public void setDevNum(int devNum) {

				this.devNum = devNum;
			}

			public int getExp() {

				return exp;
			}

			public void setExp(int exp) {

				this.exp = exp;
			}

			public int getId() {

				return id;
			}

			public void setId(int id) {

				this.id = id;
			}

			@Override
			public boolean equals(Object o) {

				// TODO Auto-generated method stub
				return id == ((LBAdapterData) (o)).id;
			}

		}

		public class LeaderboardAdapter extends ArrayAdapter<LBAdapterData> {

			private ArrayList<LBAdapterData> dataList;
			private ArrayList<LBAdapterData> originalDataList;

			public LeaderboardAdapter(Context context,
					int textViewResourceId,
					ArrayList<LBAdapterData> newLbData) {

				super(context, textViewResourceId, showedFdList);

				dataList = newLbData;
				originalDataList = newLbData;

			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater =
							(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					rowView = inflater.inflate(R.layout.act_page_leaderboard_row, parent, false);

					ViewHolder viewHolder = new ViewHolder();

					viewHolder.rank = (TextView) rowView.findViewById(R.id.rankTxtView);
					viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
					viewHolder.level = (TextView) rowView.findViewById(R.id.levelTxtView);
					viewHolder.levelPrg = (ProgressBar) rowView.findViewById(R.id.levelPrgBar);
					viewHolder.devices = (TextView) rowView.findViewById(R.id.devTxtView);
					viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);
					viewHolder.changeImg = (ImageView) rowView.findViewById(R.id.changeImgView);
					viewHolder.changeTxt = (TextView) rowView.findViewById(R.id.changeTxtView);

					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				if (holder != null && dataList != null && dataList.size() > position) {

					LBAdapterData user = dataList.get(position);

					String nameString = user.getName();

					holder.rank.setText("" + (completeFdList.indexOf(user) + 1) + ".");
					holder.name.setText(nameString);
					holder.name.setTag(user.getId());
					holder.level.setText("" + user.getLevel());
					holder.levelPrg.setMax(user.getProgressMax());
					holder.levelPrg.setProgress(user.getProgressValue());
					holder.devices.setText(new DecimalFormat(",###").format(user.getDevNum()) + " Devices");
					holder.exp.setText(new DecimalFormat(",###").format(user.getExp()) + " " + getContext().getString(R.string.str_foundDevices_exp_abbreviation));

					int rankNow = position + 1;
					Integer rankBefore = changeList.get(user.getId());

					if (rankBefore == null) rankBefore = position + 1;

					holder.changeTxt.setText("" + Math.abs(rankBefore - rankNow));

					if ((rankBefore - rankNow) > 0) {

						holder.changeImg.setImageResource(R.drawable.ic_change_up);

					}
					else if ((rankBefore - rankNow) < 0) {

						holder.changeImg.setImageResource(R.drawable.ic_change_down);

					}
					else if ((rankBefore - rankNow) == 0) {

						holder.changeImg.setImageResource(0);
						holder.changeTxt.setText("");
					}

				}
				return rowView;
			}

			public void refreshList(ArrayList<LBAdapterData> data) {
				
				clear();
				addAll(data);
				notifyDataSetChanged();
				
			}

		}

		static class ViewHolder {

			TextView rank;
			TextView name;
			TextView level;
			ProgressBar levelPrg;
			TextView devices;
			TextView exp;
			ImageView changeImg;
			TextView changeTxt;
		}

	}

	/**
	 * @author Maksl5[Markus Bensing]
	 */
	public static class AchievementsLayout {

		public static void initializeAchievements(BlueHunter bhApp) {

			AchievementSystem.checkAchievements(bhApp, true);

			ListView lv = (ListView) bhApp.mainActivity.findViewById(R.id.listView_ach);

			int[] to = new int[] { R.id.txtName, R.id.txtDescription, R.id.txtBoost, R.id.chkBox };
			String[] from = new String[] { "name", "description", "boost", "accomplished" };

			ViewBinder viewBinder = new ViewBinder() {

				@Override
				public boolean setViewValue(View view,
											Object data,
											String textRepresentation) {

					if (view instanceof CheckBox) {

						if (((String) data).equals("true")) {
							((CheckBox) view).setChecked(true);
						}
						else if (((String) data).equals("false")) {
							((CheckBox) view).setChecked(false);
						}

						return true;

					}
					return false;
				}
			};

			List<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();

			for (Achievement achievement : AchievementSystem.achievements) {

				HashMap<String, String> dataHashMap = new HashMap<String, String>();

				dataHashMap.put("name", achievement.getName(bhApp));
				dataHashMap.put("description", achievement.getDescription(bhApp));
				dataHashMap.put("boost", String.format("Boost: + %s", NumberFormat.getPercentInstance().format(achievement.getBoost())));

				boolean accomplished = false;

				if (AchievementSystem.achievementStates.containsKey(achievement.getId())) {
					accomplished = AchievementSystem.achievementStates.get(achievement.getId());
				}

				dataHashMap.put("accomplished", String.valueOf(accomplished));

				rows.add(dataHashMap);

			}

			SimpleAdapter simpleAdapter =
					new SimpleAdapter(bhApp.mainActivity, rows, R.layout.act_page_achievements_row, from, to);
			simpleAdapter.setViewBinder(viewBinder);
			lv.setAdapter(simpleAdapter);

		}

	}

}
