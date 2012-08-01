package com.maksl5.bl_hunt;



import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

/**
 * 
 * 
 *
 * Manages the complete device discovery.
 * Easily construct the class with 
 * {@code DiscoveryManager disMan = new DiscoveryManager(Activity, TextView);}
 * 
 * @author Maksl5[Markus Bensing]
 */

public class DiscoveryManager {

	
	Activity parentActivity;
	TextView stateTextView;
	BluetoothDiscoveryHandler btHandler;

	public DiscoveryManager(Activity activity,
			TextView stateTextView) {

		parentActivity = activity;
		this.stateTextView = stateTextView;

		
	}
	
	public void startDiscoveryManager() {
		btHandler = new BluetoothDiscoveryHandler(new DiscoveryState(stateTextView, parentActivity));
	}

	/**
	 * 
	 * This class handles the discovery state of the device discovery. Construct with {@code new DiscoveryState(TextView, Context);}
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

		private TextView stateTxtView;
		private int curDiscoveryState = DISCOVERY_STATE_OFF;
		private Context context;

		/**
		 * 
		 * @param stateTextView  The {@link TextView}, in which the discovery state should be shown.
		 * @param con            The {@link Context} of the base package or activity.
		 */
		
		public DiscoveryState(TextView stateTextView,
				Context con) {

			stateTxtView = stateTextView;
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

			return formatStateText(getDiscoveryState());
		}

		private static String formatStateText(String stateText) {

			char[] splittedText = stateText.toCharArray();

			String newText = "";

			for (int i = 0; i < splittedText.length; i++) {
				newText = newText + String.valueOf(splittedText[i]) + " ";
			}

			return newText.trim().toUpperCase();
		}

		public void setDiscoveryStateTextView(int state) {

			stateTxtView.setText(formatStateText(getDiscoveryState(state, context)));
		}

		public void setDiscoveryStateTextView() {

			stateTxtView.setText(formatStateText(getDiscoveryState()));
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
		
		public BluetoothDiscoveryHandler(DiscoveryState state) {
			
			this.parentActivity = DiscoveryManager.this.parentActivity;
			disState = state;
			
		}
	
	}

}
