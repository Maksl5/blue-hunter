package com.maksl5.bl_hunt.custom_ui;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.activity.MainActivity.CustomSectionFragment;
import com.maksl5.bl_hunt.custom_ui.AdjustedEditText.OnBackKeyClickedListener;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.storage.DatabaseManager;
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

		private volatile static List<String> showedFdList = new ArrayList<String>();
		private volatile static List<String> completeFdList = new ArrayList<String>();

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

		}

		public static void filterFoundDevices(	String text,
												BlueHunter bhApp) {

			List<String> searchedList = new ArrayList<String>();

			if (bhApp.mainActivity.mViewPager == null) {
				bhApp.mainActivity.mViewPager =
						(ViewPager) bhApp.mainActivity.findViewById(R.id.pager);
			}

			ListView lv =
					(ListView) bhApp.mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);
			FoundDevicesAdapter fdAdapter = (FoundDevicesAdapter) lv.getAdapter();
			if (fdAdapter == null || fdAdapter.isEmpty()) {
				fdAdapter =
						new FoundDevicesLayout().new FoundDevicesAdapter(bhApp.mainActivity, R.layout.act_page_founddevices_row, showedFdList);
				lv.setAdapter(fdAdapter);
			}

			if (text.equalsIgnoreCase("[unknown]")) {

				String unknownString = bhApp.getString(R.string.str_foundDevices_manu_unkown);

				for (String deviceAsString : completeFdList) {

					String[] device = deviceAsString.split(String.valueOf((char) 30));

					if (device[ARRAY_INDEX_MANUFACTURER].equals(unknownString)) {
						searchedList.add(deviceAsString);
					}
				}
				showedFdList = searchedList;
				fdAdapter.refill(showedFdList);

			}
			else if (text.length() == 0) {
				if (!showedFdList.equals(completeFdList)) {
					showedFdList = completeFdList;
					fdAdapter.refill(showedFdList);
				}
			}
			else {
				fdAdapter.getFilter().filter(text);
			}

		}

		/**
		 * @param mainActivity
		 * @return
		 */
		public static String getSelectedMac() {

			if (selectedItem == -1) return null;

			String macString =
					showedFdList.get(selectedItem).split(String.valueOf((char) 30))[ARRAY_INDEX_MAC_ADDRESS];

			return macString;

		}

		private class RefreshThread extends AsyncTask<Void, List<String>, List<String>> {

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

				this.listView =
						(ListView) bhApp.mainActivity.mViewPager.getChildAt(PAGE_FOUND_DEVICES + 1).findViewById(R.id.listView2);

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
			protected List<String> doInBackground(Void... params) {

				List<SparseArray<String>> devices =
						new DatabaseManager(bhApp, bhApp.getVersionCode()).getAllDevices();
				List<String> listViewList = new ArrayList<String>();

				String expString = bhApp.getString(R.string.str_foundDevices_exp_abbreviation);
				DateFormat dateFormat = DateFormat.getDateTimeInstance();

				String tempString;

				for (int i = 0; i < devices.size(); i++) {

					SparseArray<String> device = devices.get(i);

					String deviceMac = device.get(DatabaseManager.INDEX_MAC_ADDRESS);
					String manufacturer = device.get(DatabaseManager.INDEX_MANUFACTURER);
					String deviceTime = device.get(DatabaseManager.INDEX_TIME);

					if (manufacturer == null || manufacturer.equals("Unknown") || manufacturer.equals("")) {
						manufacturer = MacAddressAllocations.getManufacturer(deviceMac);
						if (manufacturer.equals("Unknown")) {
							manufacturer = bhApp.getString(R.string.str_foundDevices_manu_unkown);
						}
						else {
							new DatabaseManager(bhApp, bhApp.getVersionCode()).addManufacturerToDevice(deviceMac, manufacturer);
						}
					}

					Long time =
							(deviceTime == null || deviceTime.equals("null")) ? 0 : Long.parseLong(deviceTime);

					tempString =
							deviceMac + (char) 30 + device.get(DatabaseManager.INDEX_NAME) + (char) 30 + "RSSI: " + device.get(DatabaseManager.INDEX_RSSI) + (char) 30 + manufacturer + (char) 30 + "+" + MacAddressAllocations.getExp(manufacturer.replace(" ", "_")) + " " + expString + (char) 30 + dateFormat.format(new Date(time));

					listViewList.add(tempString);

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
			protected void onPostExecute(List<String> result) {

				if (!completeFdList.equals(result)) {
					completeFdList = result;
				}

				fdAdapter.refill(showedFdList);

				listView.setSelectionFromTop(scrollIndex, scrollTop);

				threadManager.finished(this);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
			 */
			@Override
			protected void onProgressUpdate(List<String>... values) {

				showedFdList = values[0];

				// fdAdapter.refill(showedFdList);

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

			private final Object lock = new Object();

			private Context context;

			private FoundDevicesFilter filter;

			private List<String> devices;

			private ArrayList<String> originalValues;

			private boolean notifyOnChange = true;

			private LayoutInflater inflater;

			public FoundDevicesAdapter(Context context,
					int textViewResourceId,
					List<String> objects) {

				super(context, textViewResourceId, objects);
				init(context, textViewResourceId, 0, objects);

				devices = objects;

			}

			private void init(	Context context,
								int resource,
								int textViewResourceId,
								List<String> objects) {

				this.context = context;
				inflater =
						(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				this.devices = objects;

			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				if (devices == null || devices.get(position) == null) { return new View(context); }

				View rowView = convertView;
				if (rowView == null) {
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

				if (holder != null) {

					String deviceAsString = devices.get(position);
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

				}
				return rowView;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getFilter()
			 */
			@Override
			public Filter getFilter() {

				if (filter == null) {
					filter = new FoundDevicesFilter();
				}

				return filter;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#add(java.lang.Object)
			 */
			@Override
			public void add(String object) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.add(object);
					}
					else {
						devices.add(object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#addAll(java.util.Collection)
			 */
			@Override
			public void addAll(Collection<? extends String> collection) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.addAll(collection);
					}
					else {
						devices.addAll(collection);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#addAll(T[])
			 */
			@Override
			public void addAll(String... items) {

				synchronized (lock) {
					if (originalValues != null) {
						Collections.addAll(originalValues, items);
					}
					else {
						Collections.addAll(devices, items);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#clear()
			 */
			@Override
			public void clear() {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.clear();
					}
					else {
						devices.clear();
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getCount()
			 */
			@Override
			public int getCount() {

				return devices.size();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getItem(int)
			 */
			@Override
			public String getItem(int position) {

				return devices.get(position);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getPosition(java.lang.Object)
			 */
			@Override
			public int getPosition(String item) {

				return devices.indexOf(item);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#insert(java.lang.Object, int)
			 */
			@Override
			public void insert(	String object,
								int index) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.add(index, object);
					}
					else {
						devices.add(index, object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#remove(java.lang.Object)
			 */
			@Override
			public void remove(String object) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.remove(object);
					}
					else {
						devices.remove(object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#sort(java.util.Comparator)
			 */
			@Override
			public void sort(Comparator<? super String> comparator) {

				synchronized (lock) {
					if (originalValues != null) {
						Collections.sort(originalValues, comparator);
					}
					else {
						Collections.sort(devices, comparator);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			@Override
			public void notifyDataSetChanged() {

				super.notifyDataSetChanged();
				notifyOnChange = true;
			}

			@Override
			public void setNotifyOnChange(boolean notifyOnChange) {

				this.notifyOnChange = notifyOnChange;
			}

			public void refill(List<String> devices) {

				this.devices.clear();
				this.devices.addAll(devices);
				notifyDataSetChanged();
			}

			/**
			 * @author Maksl5[Markus Bensing]
			 * 
			 */
			private class FoundDevicesFilter extends Filter {

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
				 */
				@Override
				protected FilterResults performFiltering(CharSequence filterSequence) {

					FilterResults results = new FilterResults();

					if (originalValues == null) {
						synchronized (lock) {
							originalValues = new ArrayList<String>(devices);
						}
					}

					if (filterSequence == null || filterSequence.length() == 0) {
						ArrayList<String> list;
						synchronized (lock) {
							list = new ArrayList<String>(originalValues);
						}
						results.values = list;
						results.count = list.size();
					}
					else {
						String filterString = filterSequence.toString().toLowerCase();

						ArrayList<String> devicesList;
						synchronized (lock) {
							devicesList = new ArrayList<String>(originalValues);
						}

						final int count = devicesList.size();
						final ArrayList<String> newValues = new ArrayList<String>();

						for (int i = 0; i < count; i++) {
							final String device = devicesList.get(i);
							final String deviceString = device.toString().toLowerCase();

							final String[] deviceAsArray =
									deviceString.split(String.valueOf((char) 30));

							for (String property : deviceAsArray) {
								if (property.contains(filterString)) {
									if (!newValues.contains(device)) newValues.add(device);
								}
							}

						}

						results.values = newValues;
						results.count = newValues.size();
					}

					return results;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.widget.Filter#publishResults(java.lang.CharSequence,
				 * android.widget.Filter.FilterResults)
				 */
				@Override
				protected void publishResults(	CharSequence constraint,
												FilterResults results) {

					devices = (List<String>) results.values;
					showedFdList = devices;
					if (results.count > 0) {
						notifyDataSetChanged();
					}
					else {
						notifyDataSetInvalidated();
					}

				}

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
			CheckBox selectCheckBox;
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

			int exp = LevelSystem.getUserExp(mainActivity.getBlueHunter());
			mainActivity.exp = exp;
			int level = LevelSystem.getLevel(exp);

			expTextView.setText(String.format("%d %s / %d %s", exp, mainActivity.getString(R.string.str_foundDevices_exp_abbreviation), LevelSystem.getLevelEndExp(level), mainActivity.getString(R.string.str_foundDevices_exp_abbreviation)));
			lvlTextView.setText(String.format("%d", level));

			progressBar.setMax(LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level));
			progressBar.setProgress(exp - LevelSystem.getLevelStartExp(level));
		}

	}

	/**
	 * @author Maksl5
	 * 
	 */
	public static class ProfileLayout {

		private static String userName = "";
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
	 * 
	 */
	public static class LeaderboardLayout {

		public static final int ARRAY_INDEX_NAME = 0;
		public static final int ARRAY_INDEX_LEVEL = 1;
		public static final int ARRAY_INDEX_PROGRESS_MAX = 2;
		public static final int ARRAY_INDEX_PROGRESS_VALUE = 3;
		public static final int ARRAY_INDEX_DEV_NUMBER = 4;
		public static final int ARRAY_INDEX_EXP = 5;

		private volatile static List<String> showedFdList = new ArrayList<String>();
		private volatile static List<String> completeFdList = new ArrayList<String>();

		private static ThreadManager threadManager = null;

		public static void refreshLeaderboard(final BlueHunter bhApp) {

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

		public static void filterFoundDevices(	String text,
												BlueHunter bhApp) {

			List<String> searchedList = new ArrayList<String>();

			ListView lv =
					(ListView) bhApp.mainActivity.mViewPager.getChildAt(PAGE_LEADERBOARD + 1).findViewById(R.id.listView1);
			LeaderboardAdapter lbAdapter = (LeaderboardAdapter) lv.getAdapter();
			if (lbAdapter == null || lbAdapter.isEmpty()) {
				lbAdapter =
						new LeaderboardLayout().new LeaderboardAdapter(bhApp.mainActivity, R.layout.act_page_leaderboard_row, showedFdList);
				lv.setAdapter(lbAdapter);
			}

			else if (text.length() == 0) {
				if (!showedFdList.equals(completeFdList)) {
					showedFdList = completeFdList;
					lbAdapter.refill(showedFdList);
				}
			}
			else {
				lbAdapter.getFilter().filter(text);
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

				this.listView =
						(ListView) bhApp.mainActivity.mViewPager.getChildAt(PAGE_LEADERBOARD + 1).findViewById(R.id.listView1);

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
							URI.create("http://maks.mph-p.de/blueHunter/getLeaderboard.php?s=" + startIndex + "&l=" + length);

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

				Pattern pattern = Pattern.compile("Error=(\\d+)");
				Matcher matcher = pattern.matcher(result);
				if (matcher.find()) {
					int errorCode = Integer.parseInt(matcher.group(1));
					String array =
							"Error " + errorCode + (char) 30 + "" + (char) 30 + 100 + (char) 30 + 0 + (char) 30 + 0 + (char) 30 + 0;

					showedFdList.add(array);

					ldAdapter.notifyDataSetChanged();

					listView.setSelectionFromTop(scrollIndex, scrollTop);

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

						int rank = Integer.parseInt(element.getAttribute("rank"));

						String name = element.getElementsByTagName("name").item(0).getTextContent();
						int exp =
								Integer.parseInt(element.getElementsByTagName("exp").item(0).getTextContent());
						int num =
								Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());

						int level = LevelSystem.getLevel(exp);
						int progressMax =
								LevelSystem.getLevelEndExp(level) - LevelSystem.getLevelStartExp(level);
						int progressValue = exp - LevelSystem.getLevelStartExp(level);

						last = element.getAttribute("last").equals("1");

						String array =
								name + (char) 30 + level + (char) 30 + progressMax + (char) 30 + progressValue + (char) 30 + num + (char) 30 + exp;

						showedFdList.add(array);
						showedFdList.set(rank - 1, array);

					}

				}

				threadManager.finished(this);

				if (last) {
					completeFdList = showedFdList;
					ldAdapter.notifyDataSetChanged();

					listView.setSelectionFromTop(scrollIndex, scrollTop);
				}
				else {

					ldAdapter.notifyDataSetChanged();
					new RefreshThread(bhApp, threadManager).execute(startIndex + length, length);

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
					if (refreshThread.bhApp.netMananger.areThreadsRunning()) {
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

		public class LeaderboardAdapter extends ArrayAdapter<String> {

			private final Object lock = new Object();

			private Context context;

			private LeaderboardFilter filter;

			private List<String> users;

			private ArrayList<String> originalValues;

			private boolean notifyOnChange = true;

			private LayoutInflater inflater;

			public LeaderboardAdapter(Context context,
					int textViewResourceId,
					List<String> objects) {

				super(context, textViewResourceId, objects);
				init(context, textViewResourceId, 0, objects);

				users = objects;

			}

			private void init(	Context context,
								int resource,
								int textViewResourceId,
								List<String> objects) {

				this.context = context;
				inflater =
						(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				this.users = objects;

			}

			@Override
			public View getView(int position,
								View convertView,
								ViewGroup parent) {

				if (users == null || users.get(position) == null) { return new View(context); }

				View rowView = convertView;
				if (rowView == null) {
					rowView = inflater.inflate(R.layout.act_page_leaderboard_row, parent, false);

					ViewHolder viewHolder = new ViewHolder();

					viewHolder.rank = (TextView) rowView.findViewById(R.id.rankTxtView);
					viewHolder.name = (TextView) rowView.findViewById(R.id.nameTxtView);
					viewHolder.level = (TextView) rowView.findViewById(R.id.levelTxtView);
					viewHolder.levelPrg = (ProgressBar) rowView.findViewById(R.id.levelPrgBar);
					viewHolder.devices = (TextView) rowView.findViewById(R.id.devTxtView);
					viewHolder.exp = (TextView) rowView.findViewById(R.id.expTxtView);

					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				if (holder != null) {

					String userAsString = users.get(position);
					String[] user = userAsString.split(String.valueOf((char) 30));

					String nameString = user[ARRAY_INDEX_NAME];

					holder.rank.setText("" + (position + 1) + ".");
					holder.name.setText(nameString);
					holder.level.setText(user[ARRAY_INDEX_LEVEL]);
					holder.levelPrg.setMax(Integer.parseInt(user[ARRAY_INDEX_PROGRESS_MAX]));
					holder.levelPrg.setProgress(Integer.parseInt(user[ARRAY_INDEX_PROGRESS_VALUE]));
					holder.devices.setText(user[ARRAY_INDEX_DEV_NUMBER] + " Devices");
					holder.exp.setText(user[ARRAY_INDEX_EXP] + " " + context.getString(R.string.str_foundDevices_exp_abbreviation));

				}
				return rowView;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getFilter()
			 */
			@Override
			public Filter getFilter() {

				if (filter == null) {
					filter = new LeaderboardFilter();
				}

				return filter;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#add(java.lang.Object)
			 */
			@Override
			public void add(String object) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.add(object);
					}
					else {
						users.add(object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#addAll(java.util.Collection)
			 */
			@Override
			public void addAll(Collection<? extends String> collection) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.addAll(collection);
					}
					else {
						users.addAll(collection);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#addAll(T[])
			 */
			@Override
			public void addAll(String... items) {

				synchronized (lock) {
					if (originalValues != null) {
						Collections.addAll(originalValues, items);
					}
					else {
						Collections.addAll(users, items);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#clear()
			 */
			@Override
			public void clear() {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.clear();
					}
					else {
						users.clear();
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getCount()
			 */
			@Override
			public int getCount() {

				return users.size();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getItem(int)
			 */
			@Override
			public String getItem(int position) {

				return users.get(position);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#getPosition(java.lang.Object)
			 */
			@Override
			public int getPosition(String item) {

				return users.indexOf(item);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#insert(java.lang.Object, int)
			 */
			@Override
			public void insert(	String object,
								int index) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.add(index, object);
					}
					else {
						users.add(index, object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#remove(java.lang.Object)
			 */
			@Override
			public void remove(String object) {

				synchronized (lock) {
					if (originalValues != null) {
						originalValues.remove(object);
					}
					else {
						users.remove(object);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.widget.ArrayAdapter#sort(java.util.Comparator)
			 */
			@Override
			public void sort(Comparator<? super String> comparator) {

				synchronized (lock) {
					if (originalValues != null) {
						Collections.sort(originalValues, comparator);
					}
					else {
						Collections.sort(users, comparator);
					}
				}
				if (notifyOnChange) notifyDataSetChanged();
			}

			@Override
			public void notifyDataSetChanged() {

				super.notifyDataSetChanged();
				notifyOnChange = true;
			}

			@Override
			public void setNotifyOnChange(boolean notifyOnChange) {

				this.notifyOnChange = notifyOnChange;
			}

			public void refill(List<String> devices) {

				this.users.clear();
				this.users.addAll(devices);
				notifyDataSetChanged();
			}

			/**
			 * @author Maksl5[Markus Bensing]
			 * 
			 */
			private class LeaderboardFilter extends Filter {

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
				 */
				@Override
				protected FilterResults performFiltering(CharSequence filterSequence) {

					FilterResults results = new FilterResults();

					if (originalValues == null) {
						synchronized (lock) {
							originalValues = new ArrayList<String>(users);
						}
					}

					if (filterSequence == null || filterSequence.length() == 0) {
						ArrayList<String> list;
						synchronized (lock) {
							list = new ArrayList<String>(originalValues);
						}
						results.values = list;
						results.count = list.size();
					}
					else {
						String filterString = filterSequence.toString().toLowerCase();

						ArrayList<String> devicesList;
						synchronized (lock) {
							devicesList = new ArrayList<String>(originalValues);
						}

						final int count = devicesList.size();
						final ArrayList<String> newValues = new ArrayList<String>();

						for (int i = 0; i < count; i++) {
							final String device = devicesList.get(i);
							final String deviceString = device.toString().toLowerCase();

							final String[] deviceAsArray =
									deviceString.split(String.valueOf((char) 30));

							for (String property : deviceAsArray) {
								if (property.contains(filterString)) {
									if (!newValues.contains(device)) newValues.add(device);
								}
							}

						}

						results.values = newValues;
						results.count = newValues.size();
					}

					return results;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see android.widget.Filter#publishResults(java.lang.CharSequence,
				 * android.widget.Filter.FilterResults)
				 */
				@Override
				protected void publishResults(	CharSequence constraint,
												FilterResults results) {

					users = (List<String>) results.values;
					showedFdList = users;
					if (results.count > 0) {
						notifyDataSetChanged();
					}
					else {
						notifyDataSetInvalidated();
					}

				}

			}

		}

		static class ViewHolder {

			TextView rank;
			TextView name;
			TextView level;
			ProgressBar levelPrg;
			TextView devices;
			TextView exp;
		}

	}

}
