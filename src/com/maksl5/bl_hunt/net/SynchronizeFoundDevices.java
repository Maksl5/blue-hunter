/**
 *  SynchronizeFoundDevices.java in com.maksl5.bl_hunt.net
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.net;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class SynchronizeFoundDevices implements OnNetworkResultAvailableListener, OnLoginChangeListener {

	private static int exp = 0;
	private static int deviceNum = 0;
	private BlueHunter blueHunter;

	private List<String> changesToSync = new ArrayList<String>();
	private List<String> backupList = new ArrayList<String>();

	public static final int MODE_ADD = 1;
	public static final int MODE_REMOVE = -1;
	public static final int MODE_CHANGE = 2;

	public SynchronizeFoundDevices(BlueHunter blHunt) {

		blueHunter = blHunt;
		changesToSync =
				new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getAllChanges();

	}

	public void addNewChange(	int mode,
								SparseArray<String> rules) {

		if (rules == null || rules.get(DatabaseManager.INDEX_MAC_ADDRESS) == null) return;

		String macString = rules.get(DatabaseManager.INDEX_MAC_ADDRESS);
		String nameString =
				(rules.get(DatabaseManager.INDEX_NAME) == null) ? (char) 30 + "null" + (char) 30 : rules.get(DatabaseManager.INDEX_NAME);
		String rssiString =
				(rules.get(DatabaseManager.INDEX_RSSI) == null) ? (char) 30 + "null" + (char) 30 : rules.get(DatabaseManager.INDEX_RSSI);
		String timeString =
				(rules.get(DatabaseManager.INDEX_TIME) == null) ? (char) 30 + "null" + (char) 30 : rules.get(DatabaseManager.INDEX_TIME);

		String rulesString = "";
		try {
			rulesString =
					String.format("[(%s),(%s),(%s),(%s)]", macString, URLEncoder.encode(nameString, "UTF-8"), rssiString, timeString);
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		switch (mode) {
		case MODE_ADD:
			changesToSync.add("[+]" + rulesString);
			new DatabaseManager(blueHunter, blueHunter.getVersionCode()).addChange("[+]" + rulesString);
			break;
		case MODE_REMOVE:
			changesToSync.add("[-]" + rulesString);
			new DatabaseManager(blueHunter, blueHunter.getVersionCode()).addChange("[-]" + rulesString);
			break;
		case MODE_CHANGE:
			changesToSync.add("[+-]" + rulesString);
			new DatabaseManager(blueHunter, blueHunter.getVersionCode()).addChange("[+-]" + rulesString);
			break;

		default:
			break;
		}

		int syncInterval = PreferenceManager.getPref(blueHunter, "pref_syncInterval", 5);
		if (changesToSync.size() >= syncInterval) {
			start();
		}

	}

	public void start() {

		exp = LevelSystem.getUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getDeviceNum();

		Log.d("exp", String.valueOf(exp));
		Log.d("deviceNum", String.valueOf(deviceNum));

		if (PreferenceManager.getPref(blueHunter, "pref_syncingActivated", false)) {

			blueHunter.authentification.setOnNetworkResultAvailableListener(this);

			long lastModified =
					new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getLastModifiedTime();

			NetworkThread checkSync = new NetworkThread(blueHunter);
			checkSync.execute(AuthentificationSecure.SERVER_SYNC_FD_CHECK, String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_CHECK), "lt=" + blueHunter.authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + blueHunter.authentification.getStoredPass(), "e=" + exp, "n=" + deviceNum, "t=" + lastModified);

		}

	}

	public void startSyncing(	int mode,
								boolean forceSync) {

		if (mode == 0) return;

		exp = LevelSystem.getUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getDeviceNum();

		long lastModified =
				new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getLastModifiedTime();

		blueHunter.authentification.setOnNetworkResultAvailableListener(this);

		if (mode == 1 && (changesToSync.size() != 0 || forceSync)) {

			if (!PreferenceManager.getPref(blueHunter, "pref_syncingActivated", false) && !forceSync)
				return;

			StringBuilder builder = new StringBuilder();
			for (String change : changesToSync) {
				builder.append(change + ";");
			}

			String rules = "[UP]";

			if (builder.length() != 0) {
				builder.deleteCharAt(builder.lastIndexOf(";"));
				rules = builder.toString();
			}

			NetworkThread applySync = new NetworkThread(blueHunter);

			backupList = changesToSync;
			changesToSync.clear();
			new DatabaseManager(blueHunter, blueHunter.getVersionCode()).resetChanges();

			applySync.execute(AuthentificationSecure.SERVER_SYNC_FD_APPLY, String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY), "lt=" + blueHunter.authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + blueHunter.authentification.getStoredPass(), "e=" + exp, "n=" + deviceNum, "t=" + lastModified, "r=" + rules, "m=" + mode);

		}
		else if (mode == 2) {

			NetworkThread applySync = new NetworkThread(blueHunter);
			applySync.execute(AuthentificationSecure.SERVER_SYNC_FD_APPLY, String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY), "lt=" + blueHunter.authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + blueHunter.authentification.getStoredPass(), "e=" + exp, "n=" + deviceNum, "t=" + lastModified, "r=[not-required]", "m=" + mode);

		}
		else if (mode == 3) {

			if (!PreferenceManager.getPref(blueHunter, "pref_syncingActivated", false) && !forceSync)
				return;

			List<SparseArray<String>> devices =
					new DatabaseManager(blueHunter, blueHunter.getVersionCode()).getAllDevices();

			String rulesString = "";

			for (SparseArray<String> device : devices) {
				String macString = device.get(DatabaseManager.INDEX_MAC_ADDRESS);
				String nameString =
						(device.get(DatabaseManager.INDEX_NAME) == null) ? (char) 30 + "null" + (char) 30 : device.get(DatabaseManager.INDEX_NAME);
				String rssiString =
						(device.get(DatabaseManager.INDEX_RSSI) == null) ? (char) 30 + "null" + (char) 30 : device.get(DatabaseManager.INDEX_RSSI);
				String timeString =
						(device.get(DatabaseManager.INDEX_TIME) == null) ? (char) 30 + "null" + (char) 30 : device.get(DatabaseManager.INDEX_TIME);

				try {
					rulesString +=
							String.format("[(%s),(%s),(%s),(%s)]", macString, URLEncoder.encode(nameString, "UTF-8"), rssiString, timeString);
				}
				catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				rulesString += ";";
			}

			rulesString = rulesString.substring(0, rulesString.lastIndexOf(';'));

			NetworkThread applySync = new NetworkThread(blueHunter);
			applySync.execute(AuthentificationSecure.SERVER_SYNC_FD_APPLY, String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY), "lt=" + blueHunter.authentification.getStoredLoginToken(), "s=" + Authentification.getSerialNumber(), "p=" + blueHunter.authentification.getStoredPass(), "e=" + exp, "n=" + deviceNum, "t=" + lastModified, "r=" + rulesString, "m=" + mode);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
	 */
	@Override
	public boolean onResult(int requestId,
							String resultString) {

		switch (requestId) {
		case Authentification.NETRESULT_ID_SYNC_FD_CHECK:

			Pattern pattern = Pattern.compile("Error=(\\d+)");
			Matcher matcher = pattern.matcher(resultString);

			if (matcher.find()) {
				int error = Integer.parseInt(matcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(blueHunter, requestId, error);

				Toast.makeText(blueHunter, errorMsg, Toast.LENGTH_LONG).show();

				return false;
			}

			Pattern checkSyncPattern = Pattern.compile("<needsSync>([0-3])</needsSync>");
			Matcher checkSyncMatcher = checkSyncPattern.matcher(resultString);

			if (checkSyncMatcher.find()) {
				int needsSync = Integer.parseInt(checkSyncMatcher.group(1));

				String tempMsg = "";

				if (needsSync == 1) {
					tempMsg = "Needs sync [UP].";
				}
				else if (needsSync == 2) {
					tempMsg = "Needs sync [DOWN].";
				}
				else if (needsSync == 3) {
					tempMsg = "Needs init [UP].";
				}
				else if (needsSync == 0) {
					tempMsg = "Doesn't need sync.";
				}

				Toast.makeText(blueHunter, tempMsg, Toast.LENGTH_LONG).show();

				if (needsSync == 1 && changesToSync.isEmpty()) {
					startSyncing(needsSync, true);
				}
				else {
					startSyncing(needsSync, false);
				}
			}
			return false;

		case Authentification.NETRESULT_ID_SYNC_FD_APPLY:

			Pattern errorPattern = Pattern.compile("Error=(\\d+)");
			Matcher errorMatcher = errorPattern.matcher(resultString);

			if (errorMatcher.find()) {
				int error = Integer.parseInt(errorMatcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(blueHunter, requestId, error);

				Toast.makeText(blueHunter, errorMsg, Toast.LENGTH_LONG).show();

				if (changesToSync.isEmpty()) {
					changesToSync = backupList;
					new DatabaseManager(blueHunter, blueHunter.getVersionCode()).resetChanges();
					new DatabaseManager(blueHunter, blueHunter.getVersionCode()).addChanges(changesToSync);
				}
				return false;
			}

			Pattern applySyncPattern = Pattern.compile("<done mode=\"([1-3])\" />");
			Matcher applySyncMatcher = applySyncPattern.matcher(resultString);

			if (applySyncMatcher.find()) {
				int syncMode = Integer.parseInt(applySyncMatcher.group(1));

				String resultMsg = "";

				if (syncMode == 1) {
					resultMsg = "Successfully synced up.";
				}
				else if (syncMode == 2) {
					resultString = resultString.replace("<done mode=\"2\" />", "");

					List<SparseArray<String>> devices = new ArrayList<SparseArray<String>>();
					String[] deviceStrings = resultString.split(";");

					Pattern parsePattern =
							Pattern.compile("\\[\\(((?:[0-9a-fA-F][0-9a-fA-F]:){5}(?:[0-9a-fA-F][0-9a-fA-F]){1})\\),\\((.*?)\\),\\(([-]{0,1}\\d{0,3})\\),\\((.*?)\\)\\]");
					for (String string : deviceStrings) {
						Matcher parseMatcher = parsePattern.matcher(string);
						if (parseMatcher.find()) {
							SparseArray<String> device = new SparseArray<String>();

							String nameString = parseMatcher.group(2);
							if (nameString.equals("%1Enull%1E")) {
								nameString = null;
							}
							else {
								try {
									nameString = URLDecoder.decode(nameString, "UTF-8");
								}
								catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							String timeString = parseMatcher.group(4);
							if (!timeString.matches("\\d+")) {
								timeString = "0";
							}

							device.put(DatabaseManager.INDEX_MAC_ADDRESS, parseMatcher.group(1));
							device.put(DatabaseManager.INDEX_NAME, nameString);
							device.put(DatabaseManager.INDEX_RSSI, parseMatcher.group(3));
							device.put(DatabaseManager.INDEX_TIME, timeString);

							devices.add(device);

						}
					}

					new DatabaseManager(blueHunter, blueHunter.getVersionCode()).newSyncedDatabase(devices);

					FragmentLayoutManager.DeviceDiscoveryLayout.updateIndicatorViews(blueHunter.mainActivity);
					FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(blueHunter);

					resultMsg = "Successfully synced down database.";
				}
				else if (syncMode == 3) {
					resultMsg = "Successfully initiated database syncing.";
				}

				Toast.makeText(blueHunter, resultMsg, Toast.LENGTH_LONG).show();

			}
			return false;

		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener#loginStateChange(boolean)
	 */
	@Override
	public void loginStateChange(boolean loggedIn) {

		if (loggedIn) {
			start();
		}

	}

	public void saveChanges() {

		new DatabaseManager(blueHunter, blueHunter.getVersionCode()).resetChanges();
		new DatabaseManager(blueHunter, blueHunter.getVersionCode()).addChanges(changesToSync);
	}

}
