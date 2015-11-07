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

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.custom_ui.fragment.AchievementsLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;
import com.maksl5.bl_hunt.util.FoundDevice;

import android.util.Log;
import android.widget.Toast;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class SynchronizeFoundDevices implements
		OnNetworkResultAvailableListener, OnLoginChangeListener {

	private static int exp = 0;
	private static int deviceNum = 0;
	private BlueHunter blueHunter;

	private List<String> changesToSync = new ArrayList<String>();
	private List<String> backupList = new ArrayList<String>();

	public boolean needForceOverrideUp = false;

	public static final int MODE_ADD = 1;
	public static final int MODE_REMOVE = -1;
	public static final int MODE_CHANGE = 2;

	public SynchronizeFoundDevices(BlueHunter blHunt) {

		blueHunter = blHunt;
		changesToSync = new DatabaseManager(blueHunter).getAllChanges();

	}

	public void addNewChange(int mode, FoundDevice device, boolean makeSync) {

		if (device == null || device.getMacAddress() == null)
			return;

		String macString = device.getMacAddressString();
		String nameString = (device.getName() == null) ? "" : device.getName();
		String rssiString = ""
				+ ((device.getRssi() == 1) ? "" : device.getRssi());
		String timeString = ""
				+ ((device.getTime() == -1) ? "" : device.getTime());
		String bonusString = ""
				+ ((device.getBoost() == -1f) ? "" : device.getBoost());

		String rulesString = "";
		try {
			rulesString = String.format("[(%s),(%s),(%s),(%s),(%s)]",
					macString, URLEncoder.encode(nameString, "UTF-8"),
					rssiString, timeString, bonusString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		switch (mode) {
		case MODE_ADD:
			changesToSync.add("[+]" + rulesString);
			new DatabaseManager(blueHunter).addChange("[+]" + rulesString);
			break;
		case MODE_REMOVE:
			changesToSync.add("[-]" + rulesString);
			new DatabaseManager(blueHunter).addChange("[-]" + rulesString);
			break;
		case MODE_CHANGE:
			changesToSync.add("[+-]" + rulesString);
			new DatabaseManager(blueHunter).addChange("[+-]" + rulesString);
			break;

		default:
			break;
		}

		if (makeSync) {
			checkAndStart();
		}

	}

	public void checkAndStart() {
		
		int syncInterval = PreferenceManager.getPref(blueHunter,
				"pref_syncInterval", 5);
		if (PreferenceManager.getPref(blueHunter, "pref_syncingActivated",
				false) && changesToSync.size() >= syncInterval) {
			start();
		}
		
		
	}
	
	public void start() {

		exp = LevelSystem.getCachedUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter).getDeviceNum();

		Log.d("exp", String.valueOf(exp));
		Log.d("deviceNum", String.valueOf(deviceNum));

		if (PreferenceManager.getPref(blueHunter, "pref_syncingActivated",
				false)) {

			blueHunter.authentification
					.setOnNetworkResultAvailableListener(this);

			long lastModified = new DatabaseManager(blueHunter)
					.getLastModifiedTime();

			NetworkThread checkSync = new NetworkThread(blueHunter);
			checkSync
					.execute(
							AuthentificationSecure.SERVER_SYNC_FD_CHECK,
							String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_CHECK),
							"lt="
									+ blueHunter.authentification
											.getStoredLoginToken(), "s="
									+ Authentification.getSerialNumber(),
							"p=" + blueHunter.authentification.getStoredPass(),
							"e=" + exp, "n=" + deviceNum, "t=" + lastModified);

		}

	}

	public void startSyncing(int mode, boolean forceSync) {

		if (mode == 0)
			return;

		exp = LevelSystem.getCachedUserExp(blueHunter);
		deviceNum = new DatabaseManager(blueHunter).getDeviceNum();

		long lastModified = new DatabaseManager(blueHunter)
				.getLastModifiedTime();

		blueHunter.authentification.setOnNetworkResultAvailableListener(this);

		if (mode == 1 && (changesToSync.size() != 0 || forceSync)) {

			if (!PreferenceManager.getPref(blueHunter, "pref_syncingActivated",
					false) && !forceSync)
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
			new DatabaseManager(blueHunter).resetChanges();

			applySync
					.execute(
							AuthentificationSecure.SERVER_SYNC_FD_APPLY,
							String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY),
							"lt="
									+ blueHunter.authentification
											.getStoredLoginToken(), "s="
									+ Authentification.getSerialNumber(),
							"p=" + blueHunter.authentification.getStoredPass(),
							"e=" + exp, "n=" + deviceNum, "t=" + lastModified,
							"r=" + rules, "m=" + mode);

		} else if (mode == 2) {

			NetworkThread applySync = new NetworkThread(blueHunter);
			applySync
					.execute(
							AuthentificationSecure.SERVER_SYNC_FD_APPLY,
							String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY),
							"lt="
									+ blueHunter.authentification
											.getStoredLoginToken(), "s="
									+ Authentification.getSerialNumber(),
							"p=" + blueHunter.authentification.getStoredPass(),
							"e=" + exp, "n=" + deviceNum, "t=" + lastModified,
							"r=[not-required]", "m=" + mode);

		} else if (mode == 3) {

			if (!PreferenceManager.getPref(blueHunter, "pref_syncingActivated",
					false) && !forceSync)
				return;

			List<FoundDevice> allDevices = DatabaseManager.getCachedList();

			if (allDevices == null) {

				new DatabaseManager(blueHunter).loadAllDevices(true);
				return;

			}

			String rulesString = "";

			for (FoundDevice device : allDevices) {
				String macString = device.getMacAddressString();
				String nameString = (device.getName() == null) ? "" : device
						.getName();
				String rssiString = ""
						+ ((device.getRssi() == 1) ? "" : device.getRssi());
				String timeString = ""
						+ ((device.getTime() == -1) ? "" : device.getTime());
				String bonusString = ""
						+ ((device.getBoost() == -1f) ? "" : device.getBoost());

				try {
					rulesString += String.format("[(%s),(%s),(%s),(%s),(%s)]",
							macString, URLEncoder.encode(nameString, "UTF-8"),
							rssiString, timeString, bonusString);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				rulesString += ";";
			}

			if (!(rulesString.length() == 0 || rulesString.lastIndexOf(';') == -1)) {

				rulesString = rulesString.substring(0,
						rulesString.lastIndexOf(';'));
			}

			NetworkThread applySync = new NetworkThread(blueHunter);
			applySync
					.execute(
							AuthentificationSecure.SERVER_SYNC_FD_APPLY,
							String.valueOf(Authentification.NETRESULT_ID_SYNC_FD_APPLY),
							"lt="
									+ blueHunter.authentification
											.getStoredLoginToken(), "s="
									+ Authentification.getSerialNumber(),
							"p=" + blueHunter.authentification.getStoredPass(),
							"e=" + exp, "n=" + deviceNum, "t=" + lastModified,
							"r=" + rulesString, "m=" + mode);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener
	 * #onResult(int, java.lang.String)
	 */
	@Override
	public boolean onResult(int requestId, String resultString) {

		switch (requestId) {
		case Authentification.NETRESULT_ID_SYNC_FD_CHECK:

			Pattern pattern = Pattern.compile("Error=(\\d+)");
			Matcher matcher = pattern.matcher(resultString);

			if (matcher.find()) {
				int error = Integer.parseInt(matcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(blueHunter,
						requestId, error);

				Toast.makeText(blueHunter, errorMsg, Toast.LENGTH_LONG).show();

				return false;
			}

			Pattern checkSyncPattern = Pattern
					.compile("<needsSync>([0-3])</needsSync>");
			Matcher checkSyncMatcher = checkSyncPattern.matcher(resultString);

			if (checkSyncMatcher.find()) {
				int needsSync = Integer.parseInt(checkSyncMatcher.group(1));

				String tempMsg = "";

				if (needsSync == 1) {
					tempMsg = "Needs sync [UP].";
				} else if (needsSync == 2) {
					tempMsg = "Needs sync [DOWN].";
				} else if (needsSync == 3) {
					tempMsg = "Needs init [UP].";
				} else if (needsSync == 0) {
					tempMsg = "Doesn't need sync.";
				}

				Toast.makeText(blueHunter, tempMsg, Toast.LENGTH_LONG).show();

				if (needsSync == 1 && changesToSync.isEmpty()) {
					startSyncing(needsSync, true);
				} else {
					startSyncing(needsSync, false);
				}
			}
			return false;

		case Authentification.NETRESULT_ID_SYNC_FD_APPLY:

			Pattern errorPattern = Pattern.compile("Error=(\\d+)");
			Matcher errorMatcher = errorPattern.matcher(resultString);

			if (errorMatcher.find()) {
				int error = Integer.parseInt(errorMatcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(blueHunter,
						requestId, error);

				Toast.makeText(blueHunter, errorMsg, Toast.LENGTH_LONG).show();

				if (changesToSync.isEmpty()) {
					changesToSync = backupList;
					new DatabaseManager(blueHunter).resetChanges();
					new DatabaseManager(blueHunter).addChanges(changesToSync);
				}
				return false;
			}

			Pattern applySyncPattern = Pattern
					.compile("<done mode=\"([1-3])\" />");
			Matcher applySyncMatcher = applySyncPattern.matcher(resultString);

			if (applySyncMatcher.find()) {
				int syncMode = Integer.parseInt(applySyncMatcher.group(1));

				String resultMsg = "";

				if (syncMode == 1) {
					resultMsg = "Successfully synced up.";
				} else if (syncMode == 2) {

					long startTime = System.currentTimeMillis();

					// Debug.startMethodTracing();

					resultString = resultString.replace("<done mode=\"2\" />",
							"");

					Log.d("SyncMode 2 [DOWN]", resultString);

					List<FoundDevice> devices = new ArrayList<FoundDevice>();
					String[] deviceStrings = resultString.split(";");

					Pattern parsePattern = Pattern
							.compile("\\[\\(((?:[0-9a-fA-F][0-9a-fA-F]:){5}(?:[0-9a-fA-F][0-9a-fA-F]){1})\\),\\((.*?)\\),\\(((?:[-]?\\d*)?)\\),\\((\\d*?)\\),\\(((?:\\d+[.])?\\d+)\\)\\]");
					for (String string : deviceStrings) {
						Matcher parseMatcher = parsePattern.matcher(string);
						if (parseMatcher.find()) {
							FoundDevice device = new FoundDevice();

							String nameString = parseMatcher.group(2);
							if (nameString == null || nameString.equals("")) {
								nameString = null;
							} else {
								try {
									nameString = URLDecoder.decode(nameString,
											"UTF-8");
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							String timeString = parseMatcher.group(4);
							if (!timeString.matches("\\d+")) {
								timeString = "0";
							}

							String bonusString = parseMatcher.group(5);

							device.setMac(parseMatcher.group(1));
							device.setName(nameString);
							device.setRssi(parseMatcher.group(3));
							device.setTime(timeString);
							device.setBoost(bonusString);
							device.setManu(0);

							devices.add(device);

						}
					}

					new DatabaseManager(blueHunter).newSyncedDatabase(devices);

					// Debug.stopMethodTracing();

					long endTime = System.currentTimeMillis();

					long diff = endTime - startTime;
					int num = deviceStrings.length;

					float timePerDev = (float) (diff / (float) num);

					Log.d("SyncMode 2 [DOWN]", "Time: " + diff + "ms");
					Log.d("SyncMode 2 [DOWN]", "Time per device: " + timePerDev
							+ "ms");

					PreferenceManager.setPref(blueHunter, "requireManuCheck", true);
					
					FoundDevicesLayout.refreshFoundDevicesList(blueHunter,
							false);
					AchievementsLayout.initializeAchievements(blueHunter);
					
					DeviceDiscoveryLayout
					.updateIndicatorViews(blueHunter.mainActivity);

					resultMsg = "Successfully synced down database.";
				} else if (syncMode == 3) {
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
	 * @see com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener#
	 * loginStateChange(boolean)
	 */
	@Override
	public void loginStateChange(boolean loggedIn) {

		if (loggedIn) {
			if (!needForceOverrideUp) {
				start();
			} else {
				startSyncing(3, true);
			}
		}

	}

	public void saveChanges() {

		new DatabaseManager(blueHunter).resetChanges();
		new DatabaseManager(blueHunter).addChanges(changesToSync);
	}

}
