package com.maksl5.bl_hunt;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
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

	private MainActivity mainActivity;
	private TextView stateTextView;
	private BluetoothDiscoveryHandler btHandler;

	public DiscoveryManager(MainActivity activity) {

		mainActivity = activity;

	}

	/**
	 * Starts the {@link DiscoveryManager}, constructing all its subclasses and initializing the BluetoothDiscovery.
	 * 
	 * @return <b>false</b> - if the given TextView is null.
	 */
	public boolean startDiscoveryManager() {

		if (stateTextView == null) return false;

		if (btHandler == null) {
			btHandler = new BluetoothDiscoveryHandler(new DiscoveryState(stateTextView, mainActivity));
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

	public boolean passEnableBTActivityResult(int result) {

		if (btHandler == null) return false;

		btHandler.enableBluetoothResult(result);
		return true;
	}

	public void registerReceiver() {

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		mainActivity.registerReceiver(btHandler, filter);
	}

	public void unregisterReceiver() {

		mainActivity.unregisterReceiver(btHandler);

	}
	
	public void stopDiscoveryManager () {
		btHandler.stopDiscovery();
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

		private List<BluetoothDevice> foundDevices;
		private List<HashMap<String, String>> listViewMaps;

		private BluetoothDiscoveryHandler(DiscoveryState state) {

			disState = state;
			btAdapter = BluetoothAdapter.getDefaultAdapter();

			foundDevices = new ArrayList<BluetoothDevice>();
			listViewMaps = new ArrayList<HashMap<String, String>>();

			discoveryButton = (CompoundButton) mainActivity.actionBarHandler.getActionView(R.id.menu_switch);

			requestBtEnable();

			discoveryButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(	CompoundButton buttonView,
												boolean isChecked) {

					if (isChecked) {
						runDiscovery();
					}
					else {
						btAdapter.cancelDiscovery();
						disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_STOPPED);
					}

				}
			});

		}

		private void requestBtEnable() {

			if (isBluetoothSupported()) {
				if (!isBluetoothEnabled()) {
					mainActivity.startActivityForResult(new Intent(mainActivity, EnableBluetoothActivity.class), 64);
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

		private void enableBluetoothResult(int result) {

			switch (result) {
			case EnableBluetoothActivity.BT_ENABLE_RESULT_ENABLE:
				enableBluetooth();
				break;
			case EnableBluetoothActivity.BT_ENABLE_RESULT_NOT_ENABLE:
				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_OFF);
				break;
			default:
				break;
			}
		}

		private boolean enableBluetooth() {

			if (btAdapter.enable()) return true;

			return false;
		}

		private boolean runDiscovery() {

			return btAdapter.startDiscovery();
		}
		
		private boolean stopDiscovery() {
			return btAdapter.cancelDiscovery();
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
				int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

				switch (newState) {
				case BluetoothAdapter.STATE_OFF:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_OFF);
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_ENABLING);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_BT_DISABLING);
					break;
				case BluetoothAdapter.STATE_ON:
					disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_OFF);
					break;
				default:
					break;
				}

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_RUNNING);

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

				disState.setDiscoveryState(DiscoveryState.DISCOVERY_STATE_FINISHED);
				runDiscovery();

			}
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice tempDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				ListView lv = (ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);

				if (!foundDevices.contains(tempDevice)) {
					foundDevices.add(tempDevice);

					HashMap<String, String> tempDataHashMap = new HashMap<String, String>();

					if (tempDevice.getAddress() != null) {
						tempDataHashMap.put("macAddress", tempDevice.getAddress());
					}
					else {
						tempDataHashMap.put("macAddress", "--:--:--:--:--:--");
					}

					listViewMaps.add(tempDataHashMap);
					
					String[] from = { "macAddress" };
					int[] to = { R.id.macTxtView };
					
					SimpleAdapter sAdapter = new SimpleAdapter(context, listViewMaps,R.layout.act_page_founddevices_row, from, to);
					lv.setAdapter(sAdapter);

				}
				
				
				
				
				

			}
			
			
			

		}
	}

}
