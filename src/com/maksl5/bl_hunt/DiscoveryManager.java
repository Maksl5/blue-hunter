package com.maksl5.bl_hunt;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.widget.TextView;



/**
 * 
 * 
 * 
 * Manages the complete device discovery. Easily construct the class with
 * {@code DiscoveryManager disMan = new DiscoveryManager(Activity, TextView);}
 * 
 * @author Maksl5[Markus Bensing]
 */

public class DiscoveryManager {

	private Activity parentActivity;
	private static TextView stateTextView;
	private BluetoothDiscoveryHandler btHandler;

	public DiscoveryManager(Activity activity) {

		parentActivity = activity;

	}

	public boolean startDiscoveryManager() {

		if (stateTextView == null) return false;

		if (btHandler == null) {
			btHandler = new BluetoothDiscoveryHandler(new DiscoveryState(stateTextView, parentActivity));
		}
		else {
			btHandler.forceSetStateText();
		}
		return true;
	}

	public boolean supplyTextView(TextView txtView) {

		if (txtView == null) return false;

		stateTextView = txtView;
		return true;

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
		public static final int DISCOVERY_STATE_ERROR = 10;
		public static final int DISCOVERY_STATE_OFF = 0;

		private int curDiscoveryState = DISCOVERY_STATE_OFF;
		private Context context;

		/**
		 * 
		 * @param stateTextView
		 *            The {@link TextView}, in which the discovery state should be shown.
		 * @param con
		 *            The {@link Context} of the base package or activity.
		 */

		public DiscoveryState(TextView stateTextView,
				Context con) {

			context = con;
			setDiscoveryStateTextView();
		}

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

		public int getCurDiscoveryState() {

			return curDiscoveryState;
		}

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

		public boolean setDiscoveryStateTextView(int state) {

			if (stateTextView == null) return false;

			stateTextView.setText(getDiscoveryState(state, context));
			stateTextView.invalidate();
			return true;
		}

		public boolean setDiscoveryStateTextView() {

			if (stateTextView == null) return false;

			stateTextView.setText(formatStateText(getDiscoveryState()));
			return true;
		}

		public boolean setDiscoveryState(int state) {

			if (state != (DISCOVERY_STATE_BT_OFF | DISCOVERY_STATE_ERROR | DISCOVERY_STATE_FINISHED | DISCOVERY_STATE_OFF | DISCOVERY_STATE_RUNNING | DISCOVERY_STATE_STOPPED))
				return false;

			curDiscoveryState = state;
			return true;
		}

	}

	/**
	 * Handles the native Bluetooth events and manages Bluetooth related user input.
	 * 
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	private class BluetoothDiscoveryHandler {

		private DiscoveryState disState;
		private Activity parentActivity;
		private BluetoothAdapter btAdapter;

		private BluetoothDiscoveryHandler(DiscoveryState state) {

			this.parentActivity = DiscoveryManager.this.parentActivity;
			disState = state;
			btAdapter = BluetoothAdapter.getDefaultAdapter();

		}

		private boolean isBluetoothSupported() {

			if (btAdapter == null) {
				return false;
			}
			else {
				return true;
			}
		}

		private boolean isBluetoothEnabled() {

			if (isBluetoothSupported()) if (btAdapter.isEnabled()) return true;

			return false;

		}

		private boolean forceSetStateText() {

			if (disState.setDiscoveryStateTextView()) {
				return true;
			}
			else {
				return false;
			}
		}

	}

}
