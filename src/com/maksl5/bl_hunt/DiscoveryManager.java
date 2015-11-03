package com.maksl5.bl_hunt;



import java.util.ArrayList;
import java.util.List;

import com.maksl5.bl_hunt.activity.EnableBluetoothActivity;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout;
import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;
import com.maksl5.bl_hunt.util.FoundDevice;
import com.maksl5.bl_hunt.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Vibrator;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;



/**
 * 
 * 
 * 
 * Manages the complete device discovery. Easily construct the class with
 * <p>
 * {@code DiscoveryManager disMan = new DiscoveryManager(Activity);}
 * </p>
 * 
 * Supply the discovery state {@link TextView} with the {@link #supplyTextView(TextView)} method.
 * 
 * @author Maksl5[Markus Bensing]
 */

public class DiscoveryManager {

	private BlueHunter bhApp;
	private TextView stateTextView;
	private BluetoothDiscoveryHandler btHandler;

	public DiscoveryManager(BlueHunter application) {

		bhApp = application;

	}

	/**
	 * Starts the {@link DiscoveryManager}, constructing all its subclasses and initializing the BluetoothDiscovery.
	 * 
	 * @return <b>false</b> - if the given TextView is null.
	 */
	public boolean startDiscoveryManager() {

		if (stateTextView == null) return false;

		if (btHandler == null) {
			btHandler = new BluetoothDiscoveryHandler(new DiscoveryState(stateTextView, bhApp));
			registerReceiver();
		}
		else {
			btHandler.forceSetStateText();
		}
		return true;
	}

	/**
	 * Supplies a new {@link TextView} to use for the discovery state.
	 * 
	 * @param txtView
	 *            - Discovery state {@link TextView}
	 * @return <b>false</b> - if the {@link TextView} is null.
	 */
	public boolean supplyTextView(TextView txtView) {

		if (txtView == null) return false;

		stateTextView = txtView;
		return true;

	}

	public void supplyNewTextView(TextView txtView) {

		int curState = btHandler.disState.curDiscoveryState;
		DiscoveryState discoveryState = new DiscoveryState(txtView, bhApp);
		discoveryState.setDiscoveryState(curState);
		btHandler.supplyNewDiscoveryState(discoveryState);

	}

	public boolean passEnableBTActivityResult(	int result,
												int request) {

		if (btHandler == null) return false;

		btHandler.enableBluetoothResult(result, request);
		return true;
	}

	public void registerReceiver() {

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		bhApp.registerReceiver(btHandler, filter);
	}

	public void unregisterReceiver() {

		try {
			bhApp.unregisterReceiver(btHandler);
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void disableBluetooth() {

		if (btHandler == null) return;

		btHandler.disableBluetooth();
	}

	public void stopDiscoveryManager() {

		btHandler.stopDiscovery();
	}

	public List<FoundDevice> getFDInCurDiscovery() {

		return btHandler.fDListCurDiscovery;

	}

	public int getCurDiscoveryState() {

		if (btHandler != null && btHandler.disState != null)
			return btHandler.disState.getCurDiscoveryState();

		return -2;
		
	}

	/**
	 * 
	 * This class handles the discovery state of the device discovery. Construct with
	 * {@code new DiscoveryState(TextView, Context);}
	 * 
	 * @author Maksl5[Markus Bensing]
	 * 
	 */

	public static class DiscoveryState {

		public static final int DISCOVERY_STATE_RUNNING = 1;
		public static final int DISCOVERY_STATE_STOPPED = 3;
		public static final int DISCOVERY_STATE_BT_OFF = -1;
		public static final int DISCOVERY_STATE_FINISHED = 2;
		public static final int DISCOVERY_STATE_BT_ENABLING = 4;
		public static final int DISCOVERY_STATE_ERROR = 10;
		public static final int DISCOVERY_STATE_OFF = 0;
		public static final int DISCOVERY_STATE_BT_DISABLING = 5;

		private int curDiscoveryState = DISCOVERY_STATE_OFF;
		private Context context;
		private TextView stateTextView;

		/**
		 * 
		 * @param stateTextView
		 *            The {@link TextView}, in which the discovery state should be shown.
		 * @param con
		 *            The {@link Context} of the base package or activity.
		 */

		public DiscoveryState(TextView stateTextView,
				Context con) {

			this.stateTextView = stateTextView;
			context = con;

			setDiscoveryState(DISCOVERY_STATE_OFF);
		}

		/**
		 * @param state
		 * @param context
		 * @return
		 */
		public static String getDiscoveryState(	int state,
												Context context) {

			switch (state) {
			case DISCOVERY_STATE_RUNNING:
				return formatStateText(context.getString(R.string.str_discoveryState_running));
			case DISCOVERY_STATE_STOPPED:
				return formatStateText(context.getString(R.string.str_discoveryState_stopped));
			case DISCOVERY_STATE_BT_OFF:
				return formatStateText(context.getString(R.string.str_discoveryState_btOff));
			case DISCOVERY_STATE_FINISHED:
				return formatStateText(context.getString(R.string.str_discoveryState_finished));
			case DISCOVERY_STATE_BT_ENABLING:
				return formatStateText(context.getString(R.string.str_discoveryState_btEnabling));
			case DISCOVERY_STATE_BT_DISABLING:
				return formatStateText(context.getString(R.string.str_discoveryState_btDisabling));
			case DISCOVERY_STATE_ERROR:
				return formatStateText(context.getString(R.string.str_discoveryState_error));
			case DISCOVERY_STATE_OFF:
				return formatStateText(context.getString(R.string.str_discoveryState_off));
			}

			return "";
		}

		public static String getUnformatedDiscoveryState(	int state,
															Context context) {

			switch (state) {
			case DISCOVERY_STATE_RUNNING:
				return context.getString(R.string.str_discoveryState_running);
			case DISCOVERY_STATE_STOPPED:
				return context.getString(R.string.str_discoveryState_stopped);
			case DISCOVERY_STATE_BT_OFF:
				return context.getString(R.string.str_discoveryState_btOff);
			case DISCOVERY_STATE_FINISHED:
				return context.getString(R.string.str_discoveryState_finished);
			case DISCOVERY_STATE_BT_ENABLING:
				return context.getString(R.string.str_discoveryState_btEnabling);
			case DISCOVERY_STATE_BT_DISABLING:
				return context.getString(R.string.str_discoveryState_btDisabling);
			case DISCOVERY_STATE_ERROR:
				return context.getString(R.string.str_discoveryState_error);
			case DISCOVERY_STATE_OFF:
				return context.getString(R.string.str_discoveryState_off);
			}

			return "";
		}

		private String getDiscoveryState() {

			return getDiscoveryState(curDiscoveryState, context);
		}

		/**
		 * @return
		 */
		public int getCurDiscoveryState() {

			return curDiscoveryState;
		}

		/**
		 * @return
		 */
		public String getCurDiscoveryStateText() {

			return getDiscoveryState();
		}

		private static String formatStateText(String stateText) {

			char[] splittedText = stateText.toCharArray();

			String newText = "";

			for (int i = 0; i < splittedText.length; i++) {
				newText = newText + String.valueOf(splittedText[i]) + " ";
			}

			return newText.trim().toUpperCase();
		}

		/**
		 * @return
		 */
		private boolean setDiscoveryStateTextView() {

			if (stateTextView == null) return false;

			stateTextView.setText(getDiscoveryState());
			return true;
		}

		/**
		 * @param state
		 * @return
		 */
		public boolean setDiscoveryState(int state) {

			// if (state != (DISCOVERY_STATE_BT_OFF | DISCOVERY_STATE_ERROR | DISCOVERY_STATE_FINISHED |
			// DISCOVERY_STATE_OFF | DISCOVERY_STATE_RUNNING | DISCOVERY_STATE_STOPPED | DISCOVERY_STATE_BT_ENABLING |
			// DISCOVERY_STATE_BT_DISABLING))
			// return false;

			curDiscoveryState = state;
			if (!setDiscoveryStateTextView()) return false;
			return true;
		}

	}

	/**
	 * Handles the native Bluetooth events and manages Bluetooth related user input.
	 * 
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	private class BluetoothDiscoveryHandler extends BroadcastReceiver {

		private DiscoveryState disState;
		private BluetoothAdapter btAdapter;

		private CompoundButton discoveryButton;

		private List<FoundDevice> foundDevices;
		private List<BluetoothDevice> foundDevicesInCurDiscovery;
		private List<FoundDevice> fDListCurDiscovery;
		private int requestId = 0;

		private BluetoothDiscoveryHandler(DiscoveryState state) {

			disState = state;
			btAdapter = BluetoothAdapter.getDefaultAdapter();

			foundDevices = new DatabaseManager(bhApp).getAllDevices();
			foundDevicesInCurDiscovery = new ArrayList<BluetoothDevice>();
			fDListCurDiscovery = new ArrayList<FoundDevice>();

			discoveryButton =
					(CompoundButton) bhApp.actionBarHandler.getActionView(R.id.menu_switch);

			final ListView discoveryListView =
					(ListView) bhApp.mainActivity.findViewById(R.id.discoveryListView);

			requestBtEnable(false);

			discoveryButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(	CompoundButton buttonView,
												boolean isChecked) {

					if (isChecked) {
						if (isBluetoothEnabled()) {
							DeviceDiscoveryLayout.startShowLV(bhApp.mainActivity);
							runDiscovery();
						}
						else {
							requestBtEnable(true);
						}
					}
					else {
						if (isBluetoothEnabled()) {

							stopDiscovery();
							DeviceDiscoveryLayout.stopShowLV(bhApp.mainActivity);

							if (PreferenceManager.getPref(bhApp, "pref_disBtSrchOff", false)) {
								disableBluetooth();
							}
						}

					}

				}
			});

		}

		private void requestBtEnable(boolean startDiscovery) {

			if (isBluetoothSupported()) {
				if (!isBluetoothEnabled()) {
					if (startDiscovery) {

						switch (PreferenceManager.getPref(bhApp, "pref_enableBT_remember", 0)) {
						case 1:
							enableBluetoothResult(EnableBluetoothActivity.BT_ENABLE_RESULT_ENABLE, 128);
							break;
						case 2:
							bhApp.mainActivity.startActivityForResult(new Intent(bhApp, EnableBluetoothActivity.class), 128);
							break;
						case 0:
							bhApp.mainActivity.startActivityForResult(new Intent(bhApp, EnableBluetoothActivity.class), 128);
							break;
						}
					}
					else {
						switch (PreferenceManager.getPref(bhApp, "pref_enableBT_remember", 0)) {
						case 1:
							enableBluetoothResult(EnableBluetoothActivity.BT_ENABLE_RESULT_ENABLE, 64);
							break;
						case 2:
							enableBluetoothResult(EnableBluetoothActivity.BT_ENABLE_RESULT_ENABLE, 64);
							break;
						case 0:
							bhApp.mainActivity.startActivityForResult(new Intent(bhApp, EnableBluetoothActivity.class), 64);
							break;
						}

					}

				}
			}
		}

		private boolean isBluetoothSupported() {

			if (btAdapter == null) {
				discoveryButton.setEnabled(false);
				discoveryButton.setChecked(false);
				return false;
			}
			else {
				return true;
			}
		}

		private boolean isBluetoothEnabled() {

			if (isBluetoothSupported()) if (btAdapter.isEnabled()) return true;

			disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_OFF);

			return false;

		}

		private boolean forceSetStateText() {

			if (disState.setDiscoveryState(disState.getCurDiscoveryState())) {
				return true;
			}
			else {
				return false;
			}
		}

		private void enableBluetoothResult(	int result,
											int request) {

			switch (result) {
			case EnableBluetoothActivity.BT_ENABLE_RESULT_ENABLE:
				enableBluetooth();
				requestId = request;
				break;
			case EnableBluetoothActivity.BT_ENABLE_RESULT_NOT_ENABLE:
				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_OFF);
				discoveryButton.setChecked(false);
				break;
			default:
				break;
			}
		}

		private boolean enableBluetooth() {

			if (btAdapter.enable()) return true;

			return false;
		}

		private boolean disableBluetooth() {

			if (isBluetoothEnabled()) {
				if (btAdapter.disable()) return true;

				return false;
			}

			return true;

		}

		private boolean runDiscovery() {

			if (isBluetoothSupported()) return btAdapter.startDiscovery();

			return false;
		}

		private boolean stopDiscovery() {

			if (isBluetoothSupported()) return btAdapter.cancelDiscovery();

			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(	Context context,
								Intent intent) {

			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int newState =
						intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

				switch (newState) {
				case BluetoothAdapter.STATE_OFF:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_OFF);
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_ENABLING);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					discoveryButton.setChecked(false);
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_DISABLING);
					break;
				case BluetoothAdapter.STATE_ON:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_OFF);
					if (requestId == 128) {
						DeviceDiscoveryLayout.startShowLV(bhApp.mainActivity);
						runDiscovery();
					}
					break;
				default:
					break;
				}

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_RUNNING);
				bhApp.mainActivity.stateNotificationBuilder.setContentTitle(DiscoveryState.getUnformatedDiscoveryState(DiscoveryState.DISCOVERY_STATE_RUNNING, bhApp));
				bhApp.mainActivity.updateNotification();

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

				
				
				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_FINISHED);

				for (BluetoothDevice btDevice : foundDevicesInCurDiscovery) {
					new DatabaseManager(bhApp).addNameToDevice(btDevice.getAddress(), btDevice.getName());
				}
				
				bhApp.synchronizeFoundDevices.checkAndStart();

				foundDevicesInCurDiscovery = null;
				foundDevicesInCurDiscovery = new ArrayList<BluetoothDevice>();

				fDListCurDiscovery = null;
				fDListCurDiscovery = new ArrayList<FoundDevice>();

				DeviceDiscoveryLayout.onlyCurListUpdate(bhApp.mainActivity);

				if (discoveryButton.isChecked()) {
					runDiscovery();
				}
				else {
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_STOPPED);
					bhApp.mainActivity.stateNotificationBuilder.setContentTitle(DiscoveryState.getUnformatedDiscoveryState(DiscoveryState.DISCOVERY_STATE_STOPPED, bhApp));
					bhApp.mainActivity.updateNotification();
				}

			}
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice tempDevice =
						intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
				onDeviceFound(tempDevice, RSSI);

			}

		}

		public void onDeviceFound(	BluetoothDevice btDevice,
									short rssi) {

			//Debug.startMethodTracing("onDeviceFound");
			
			foundDevices = new DatabaseManager(bhApp).getAllDevices();

			//Compare Object
			FoundDevice compare = new FoundDevice();
			compare.setMac(btDevice.getAddress());
			
			if (!foundDevices.contains(compare)) {
				foundDevicesInCurDiscovery.add(btDevice);

				FoundDevice device = new FoundDevice();
				device.setMac(btDevice.getAddress().toUpperCase());
				device.setRssi(rssi);
				device.setTime(System.currentTimeMillis());
				device.setBoost(AchievementSystem.getBoost(bhApp));

				fDListCurDiscovery.add(device);
				attemptVibration();
				new DatabaseManager(bhApp).addNewDevice(device);

				FoundDevicesLayout.refreshFoundDevicesList(bhApp, false);
				DeviceDiscoveryLayout.updateIndicatorViews(bhApp.mainActivity);

				bhApp.mainActivity.updateNotification();

			}

			//Debug.stopMethodTracing();
						
		}

		public void attemptVibration() {

			boolean bVibrate = PreferenceManager.getPref(bhApp, "pref_vibrate", false);
			if (bVibrate) {
				Vibrator vibrator = (Vibrator) bhApp.getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(500);
			}

		}

		public void supplyNewDiscoveryState(DiscoveryState discoveryState) {

			this.disState = discoveryState;
		}

	}

}
