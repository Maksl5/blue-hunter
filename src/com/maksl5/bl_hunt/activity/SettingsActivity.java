/**
 *  PreferenceActivity.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout.FDAdapterData;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.NetworkThread;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;
import com.maksl5.bl_hunt.util.FoundDevice;
import com.maksl5.bl_hunt.util.MacAddress;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class SettingsActivity extends android.preference.PreferenceActivity implements OnNetworkResultAvailableListener {

	Menu menu;
	ProgressBar progressBar;
	MenuItem progressBarItem;

	private String newPass = null;

	BlueHunter bhApp;
	PrefNetManager netManager;

	List<Header> headers;
	private boolean isDestroyed = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		bhApp = (BlueHunter) getApplication();
		bhApp.currentActivity = this;

		netManager = new PrefNetManager(this);

		ActionBar actionBar = this.getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onBuildHeaders(List<Header> target) {

		headers = new ArrayList<Header>();
		loadHeadersFromResource(R.xml.preference_headers, target);
		headers = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();

		bhApp.currentActivity = this;

		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.menu = menu;

		getMenuInflater().inflate(R.menu.act_settings, this.menu);

		progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
		this.menu.findItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);
		progressBar.setPadding(5, 0, 5, 0);

		boolean goToInfoPref = getIntent().getBooleanExtra("goToInfoPref", false);

		if (goToInfoPref) {

			// switchToHeader(bhApp.getPackageName() +
			// ".activity.SettingsActivity$InfoFragment", null);
			if (headers == null) {

				onCreate(null);
				finish();

			}
			for (Header header : headers) {
				if (header.fragment.equals(bhApp.getPackageName() + ".activity.SettingsActivity$InfoFragment")) {
					switchToHeader(header);
					break;
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:

			finish();

			break;

		default:
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 0) {
			return;
		}

		switch (requestCode) {
		case ChangePasswordActivity.MODE_CHANGE_ONLINE_PASS:
			if (resultCode == 1) {

				String oldPass = data.getStringExtra("oldPass");
				newPass = data.getStringExtra("newPass");

				bhApp.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, bhApp, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE),
						"h=" + bhApp.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(),
						"v=" + bhApp.getVersionCode(), "op=" + oldPass, "np=" + newPass,
						"lt=" + bhApp.authentification.getStoredLoginToken());
			}
			else if (resultCode == 2) {

				newPass = data.getStringExtra("newPass");

				bhApp.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, bhApp, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE),
						"h=" + bhApp.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(),
						"v=" + bhApp.getVersionCode(), "np=" + newPass, "lt=" + bhApp.authentification.getStoredLoginToken());

			}
			break;
		case ChangePasswordActivity.MODE_CHANGE_LOGIN_PASS:
			if (resultCode == 1) {

				String newLoginPass = data.getStringExtra("newLoginPass");
				bhApp.authentification.storePass(newLoginPass);

				bhApp.loginManager.login();

			}
			break;
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

		if (requestId == Authentification.NETRESULT_ID_PASS_CHANGE) {

			Pattern pattern = Pattern.compile("Error=(\\d+)");
			Matcher matcher = pattern.matcher(resultString);

			if (matcher.find()) {
				int error = Integer.parseInt(matcher.group(1));

				String errorMsg = ErrorHandler.getErrorString(bhApp, requestId, error);

				switch (error) {
				case 1012:
					bhApp.loginManager.login();
					break;
				case 1015:
					bhApp.loginManager.login();
					break;
				}

				Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
			}

			if (resultString.equals("<SUCCESS>")) {
				Toast.makeText(bhApp, getString(R.string.str_Preferences_changePass_success), Toast.LENGTH_LONG).show();

				if (ProfileFragment.changeLoginPass != null) {
					ProfileFragment.changeLoginPass.setEnabled(true);
				}

				if (newPass != null) {
					bhApp.authentification.storePass(newPass);
				}

			}

		}

		return true;
	}

	@Override
	protected void onDestroy() {

		if (netManager != null) netManager.cancelAllTasks();

		isDestroyed = true;

		super.onDestroy();
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public static class InfoFragment extends PreferenceFragment {

		BlueHunter bhApp;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			bhApp = (BlueHunter) getActivity().getApplication();

			addPreferencesFromResource(R.xml.info_preference);

			if (!Authentification.newUpdateAvailable || BlueHunter.isPlayStore) {
				Preference newUpdatePref = findPreference("pref_newUpdateAvailable");
				PreferenceScreen infoScreen = getPreferenceScreen();
				infoScreen.removePreference(newUpdatePref);

				if (BlueHunter.isPlayStore) {

					Preference checkPref = findPreference("pref_checkUpdate");
					checkPref.setEnabled(false);
					infoScreen.removePreference(checkPref);

				}

			}

			initializeStaticPrefs();
			registerListeners();
		}

		/**
		 * 
		 */
		private void registerListeners() {

			// New Update Available - Click
			Preference newUpdatePref = findPreference("pref_newUpdateAvailable");
			if (newUpdatePref != null) {
				newUpdatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("https://github.com/Maksl5/blue-hunter/raw/master/bin/Blue%20Hunter.apk"));
						startActivity(browserIntent);

						return true;
					}
				});
			}

			// Show Changelog Click
			Preference showChangelogPref = findPreference("pref_changelog");
			if (showChangelogPref != null) {
				showChangelogPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						if (bhApp != null && bhApp.authentification != null) bhApp.authentification.showChangelog();

						return true;
					}
				});
			}

			// Implement Issue Tracker linking
			Preference issueTrackerPref = findPreference("pref_issuetracker");
			issueTrackerPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent trackerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bluehunter.maks-dev.com/issues"));
					startActivity(trackerIntent);
					return true;
				}
			});

			// Test Adding 10000 devices

			Preference infoPref = findPreference("pref_version");
			infoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					int number = 0;

					ArrayList<FoundDevice> foundDevices = new ArrayList<FoundDevice>();

					for (short a = 0x00; a < 0xFF; a++) {

						if (number >= 30000) break;

						for (short b = 0x00; b < 0xFF; b++) {

							if (number >= 30000) break;

							for (short c = 0x00; c < 0xFF; c++) {

								if (number >= 30000) break;

								FoundDevice fd = new FoundDevice();
								fd.setMac(new MacAddress(a, b, c, (short) 0, (short) 0, (short) 0));
								fd.setName("" + number);
								fd.setTime(System.currentTimeMillis() - (number * 10000));
								fd.setRssi((short) 0);
								fd.setBoost(0f);

								foundDevices.add(fd);

								Log.d("Debug Devices Calc.", "" + number);

								number++;

							}
						}
					}

					new DatabaseManager(bhApp).newSyncedDatabase(foundDevices);

					return true;
				}
			});

		}

		private void initializeStaticPrefs() {

			Preference infoPref = findPreference("pref_version");
			Preference serialPref = findPreference("pref_serialNumber");
			Preference userIDPref = findPreference("pref_userID");

			try {
				infoPref.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
				userIDPref.setSummary("" + bhApp.loginManager.getUid());
				serialPref.setSummary(Authentification.getSerialNumber());
			}
			catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static class BehaviourFragment extends PreferenceFragment {

		BlueHunter bhApp;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			bhApp = (BlueHunter) getActivity().getApplication();

			addPreferencesFromResource(R.xml.behaviour_preference);

			registerListeners();
		}

		private void registerListeners() {

			Preference showNotificationPref = findPreference("pref_showNotification");
			if (showNotificationPref != null) {
				showNotificationPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {

						bhApp.mainActivity.alterNotification(newValue.equals(true));

						return true;
					}
				});
			}

			Preference deleteRemember = findPreference("pref_deleteRemember");
			if (deleteRemember != null) {
				deleteRemember.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						PreferenceManager.setPref(bhApp, "pref_enableBT_remember", 0);

						return true;
					}
				});

			}

			Preference enableBackground = findPreference("pref_enableBackground");
			if (enableBackground != null) {
				enableBackground.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						if (newValue.equals(true)) {

							try {
								bhApp.mainActivity.getWindow().setBackgroundDrawableResource(R.drawable.bg_main);
							}
							catch (Exception e) {
								PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
								bhApp.mainActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
							}
							catch (OutOfMemoryError e) {
								PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
								bhApp.mainActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
							}

						}
						else {
							bhApp.mainActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
						}
						return true;
					}
				});
			}

		}

	}

	/**
	 * @author Maksl5
	 * 
	 */
	public static class ProfileFragment extends PreferenceFragment {

		BlueHunter bhApp;

		public static Preference changeOnlinePass;
		public static Preference changeLoginPass;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			bhApp = (BlueHunter) getActivity().getApplication();

			addPreferencesFromResource(R.xml.profile_preference);

			changeOnlinePass = findPreference("pref_changePass");

			if (bhApp.loginManager != null) {

				changeOnlinePass.setEnabled(bhApp.loginManager.getLoginState());
			}

			changeLoginPass = findPreference("pref_localPass");

			if (bhApp.loginManager != null && bhApp.mainActivity != null && !bhApp.mainActivity.passSet
					&& bhApp.loginManager.getLoginState()) {
				changeOnlinePass.setTitle(R.string.str_Preferences_changePass_new_title);
				changeOnlinePass.setSummary(R.string.str_Preferences_changePass_new_sum);
				changeLoginPass.setEnabled(false);
			}

			if (bhApp.loginManager != null) {
				bhApp.loginManager.login();
			}

			registerListeners();

		}

		private void registerListeners() {

			changeOnlinePass.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
					Bundle parametersBundle = new Bundle();
					parametersBundle.putInt("mode", ChangePasswordActivity.MODE_CHANGE_ONLINE_PASS);
					parametersBundle.putBoolean("passSet", bhApp.mainActivity.passSet);
					intent.putExtras(parametersBundle);

					getActivity().startActivityForResult(intent, ChangePasswordActivity.MODE_CHANGE_ONLINE_PASS);

					return true;
				}
			});

			changeLoginPass.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
					Bundle parametersBundle = new Bundle();
					parametersBundle.putInt("mode", ChangePasswordActivity.MODE_CHANGE_LOGIN_PASS);
					intent.putExtras(parametersBundle);

					getActivity().startActivityForResult(intent, ChangePasswordActivity.MODE_CHANGE_LOGIN_PASS);

					return true;
				}
			});

			bhApp.authentification.setOnLoginChangeListener(new OnLoginChangeListener() {

				@Override
				public void loginStateChange(boolean loggedIn) {

					if (loggedIn) {
						changeOnlinePass.setEnabled(true);
					}
					else {
						changeOnlinePass.setEnabled(false);
					}

				}
			});

			android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity())
					.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

						@Override
						public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

							if (key.equals("pref_syncingActivated")) {
								if (sharedPreferences.getBoolean(key, false)) {
									bhApp.synchronizeFoundDevices.start();
								}
							}

						}
					});

			Preference syncInterval = findPreference("pref_syncInterval");
			syncInterval.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					Builder builder = new Builder(getActivity());
					builder.setTitle("Set sync interval.");
					builder.setPositiveButton("Save", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							NumberPicker numberPicker = (NumberPicker) ((AlertDialog) dialog).findViewById(R.id.numberPicker1);

							int value = numberPicker.getValue();

							PreferenceManager.setPref(getActivity(), "pref_syncInterval", value);
							dialog.dismiss();

						}
					});

					builder.setNegativeButton("Cancel", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();

						}
					});

					builder.setView(LayoutInflater.from(builder.getContext()).inflate(R.layout.dlg_sync_interval, null));

					AlertDialog alertDialog = builder.show();

					NumberPicker numberPicker = (NumberPicker) alertDialog.findViewById(R.id.numberPicker1);
					numberPicker.setMinValue(1);
					numberPicker.setMaxValue(100);
					numberPicker.setWrapSelectorWheel(false);
					numberPicker.setValue(PreferenceManager.getPref(getActivity(), "pref_syncInterval", 5));

					return true;
				}
			});

			Preference forceUpSync = findPreference("pref_forceUpSync");
			Preference forceDownSync = findPreference("pref_forceDownSync");

			forceUpSync.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					bhApp.synchronizeFoundDevices.startSyncing(SynchronizeFoundDevices.MODE_INIT, true);

					return true;
				}
			});

			forceDownSync.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					bhApp.synchronizeFoundDevices.startSyncing(SynchronizeFoundDevices.MODE_DOWN, true);

					return true;
				}
			});

		}
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public static class MiscFragment extends PreferenceFragment {

		BlueHunter bhApp;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			bhApp = (BlueHunter) getActivity().getApplication();

			addPreferencesFromResource(R.xml.misc_preference);

			registerListeners();
		}

		/**
		 * 
		 */
		private void registerListeners() {

			Preference rebuildDB = findPreference("pref_rebuildDB");
			rebuildDB.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					int result = new DatabaseManager(bhApp).rebuildDatabase();

					if (result == 0) {
						Toast.makeText(getActivity(), "Successfully rebuilt database. App will now restart.", Toast.LENGTH_LONG).show();
					}
					else if (result < 0) {
						Toast.makeText(getActivity(), (-result) + " devices could not be rebuild. App will now restart.", Toast.LENGTH_LONG)
								.show();
					}
					else if (result == 1001) {
						Toast.makeText(getActivity(), "Database could not be rebuilt.", Toast.LENGTH_LONG).show();
						return true;
					}

					Intent i = getActivity().getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

					return true;
				}
			});

		}

	}

	/**
	 * @author Maksl5
	 * 
	 */
	public class PrefNetManager {

		private SettingsActivity prefActivity;
		private List<PrefNetThread> curRunningThreads;

		public PrefNetManager(SettingsActivity prefActivity) {

			this.prefActivity = prefActivity;
			curRunningThreads = new ArrayList<PrefNetThread>();
		}

		public void addRunningThread(PrefNetThread netThread) {

			curRunningThreads.add(netThread);
		}

		public synchronized void threadFinished(PrefNetThread netThread) {

			curRunningThreads.remove(netThread);
			checkList();
		}

		public void cancelAllTasks() {
			for (PrefNetThread prefNetThread : curRunningThreads) {
				if (prefNetThread != null) prefNetThread.cancel(true);
			}

		}

		/**
		 * 
		 */
		private void checkList() {

			if (curRunningThreads.size() == 0) {
				if (!prefActivity.isDestroyed()) {
					MenuItem progressBar = prefActivity.menu.findItem(R.id.menu_progress);
					progressBar.setVisible(false);
				}
			}

		}
	}

	/**
	 * @author Maksl5
	 * 
	 */
	public class PrefNetThread extends AsyncTask<String, Integer, String> {

		private SettingsActivity preferenceActivity;
		private PrefNetManager networkMananger;
		private BlueHunter bhApp;

		public PrefNetThread(SettingsActivity preferenceActivity, BlueHunter app, PrefNetManager networkMananger) {

			super();
			this.preferenceActivity = preferenceActivity;
			this.bhApp = app;
			this.networkMananger = networkMananger;
			networkMananger.addRunningThread(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... params) {

			String remoteFile = params[0];
			int requestId = Integer.parseInt(params[1]);
			boolean https = false;

			if (remoteFile.startsWith("https")) https = true;

			try {

				HashMap<String, String> postValues = new HashMap<String, String>();

				for (int i = 2; i < params.length; i++) {
					Pattern pattern = Pattern.compile("(.+)=(.+)", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(params[i]);

					if (matcher.matches()) {

						postValues.put(matcher.group(1), matcher.group(2));
					}
				}

				URL httpUri = new URL(remoteFile);

				HttpURLConnection conn = (HttpURLConnection) httpUri.openConnection();
				conn.setReadTimeout(15000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(NetworkThread.getPostDataString(postValues));

				writer.flush();
				writer.close();
				os.close();

				int responseCode = conn.getResponseCode();

				String result = "";

				if (responseCode == HttpURLConnection.HTTP_OK) {

					String line = "";
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

					StringBuilder stringBuilder = new StringBuilder();

					while ((line = br.readLine()) != null) {
						stringBuilder.append(line + System.lineSeparator());
					}

					stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(System.lineSeparator()));

					result = stringBuilder.toString();

				}
				else {

					return "<requestID='" + requestId + "' />" + "Error=" + responseCode + "\n" + conn.getResponseMessage();

				}

				return "<requestID='" + requestId + "' />" + result;
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "<requestID='" + requestId + "' />" + "Error=5\n" + e.getMessage();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "<requestID='" + requestId + "' />" + "Error=1\n" + e.getMessage();
			}

			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {

			Pattern reqIdPattern = Pattern.compile("<requestID='(\\d+)' />");
			Matcher reqIdMatcher = reqIdPattern.matcher(result);
			reqIdMatcher.find();
			int reqId = Integer.parseInt(reqIdMatcher.group(1));

			result = reqIdMatcher.replaceFirst("");

			bhApp.authentification.fireOnNetworkResultAvailable(reqId, result);

			networkMananger.threadFinished(this);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			if (!preferenceActivity.isDestroyed) {
				MenuItem progressBar = preferenceActivity.menu.findItem(R.id.menu_progress);
				progressBar.setVisible(true);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {

		}
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}
}
