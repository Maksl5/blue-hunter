/**
 *  PreferenceActivity.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.activity;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.EasySSLSocketFactory;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.PreferenceManager;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class SettingsActivity extends android.preference.PreferenceActivity implements OnNetworkResultAvailableListener {

	Menu menu;
	ProgressBar progressBar;
	MenuItem progressBarItem;

	private boolean destroyed = false;
	private String newPass = null;

	BlueHunter bhApp;
	PrefNetManager netManager;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onBuildHeaders(java.util.List)
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {

		loadHeadersFromResource(R.xml.preference_headers, target);
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

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.menu = menu;

		getMenuInflater().inflate(R.menu.act_settings, this.menu);

		progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
		this.menu.findItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);
		progressBar.setPadding(5, 0, 5, 0);

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
	protected void onActivityResult(int requestCode,
									int resultCode,
									Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 0) { return; }

		switch (requestCode) {
		case ChangePasswordActivity.MODE_CHANGE_ONLINE_PASS:
			if (resultCode == 1) {

				String oldPass = data.getStringExtra("oldPass");
				newPass = data.getStringExtra("newPass");

				bhApp.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, bhApp, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE), "h=" + bhApp.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "op=" + oldPass, "np=" + newPass, "lt=" + bhApp.authentification.getStoredLoginToken());
			}
			else if (resultCode == 2) {

				newPass = data.getStringExtra("newPass");

				bhApp.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, bhApp, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE), "h=" + bhApp.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "np=" + newPass, "lt=" + bhApp.authentification.getStoredLoginToken());

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
	 * @see com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
	 */
	@Override
	public boolean onResult(int requestId,
							String resultString) {

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

		destroyed = true;

		super.onDestroy();
	}

	/**
	 * @return
	 */
	public boolean isDestroyed() {

		return destroyed;
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
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			bhApp = (BlueHunter) getActivity().getApplication();

			addPreferencesFromResource(R.xml.info_preference);

			if (!Authentification.newUpdateAvailable) {
				Preference newUpdatePref = findPreference("pref_newUpdateAvailable");
				PreferenceScreen infoScreen = getPreferenceScreen();
				infoScreen.removePreference(newUpdatePref);
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

						Intent browserIntent =
								new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Maksl5/blue-hunter/raw/master/bin/Blue%20Hunter.apk"));
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

						bhApp.authentification.showChangelog();

						return true;
					}
				});
			}

		}

		private void initializeStaticPrefs() {

			Preference infoPref = findPreference("pref_version");

			try {
				infoPref.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
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
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
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
					public boolean onPreferenceChange(	Preference preference,
														Object newValue) {

						bhApp.mainActivity.alterNotification(newValue.equals(true));

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
			changeOnlinePass.setEnabled(bhApp.loginManager.getLoginState());

			changeLoginPass = findPreference("pref_localPass");
			if (!bhApp.mainActivity.passSet && bhApp.loginManager.getLoginState()) {
				changeOnlinePass.setTitle(R.string.str_Preferences_changePass_new_title);
				changeOnlinePass.setSummary(R.string.str_Preferences_changePass_new_sum);
				changeLoginPass.setEnabled(false);
			}

			bhApp.loginManager.login();
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

			android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
				
				@Override
				public void onSharedPreferenceChanged(	SharedPreferences sharedPreferences,
														String key) {
				
					if(key.equals("pref_syncingActivated")) {
						if(sharedPreferences.getBoolean(key, false)) {
							bhApp.authentification.synchronizeFoundDevices.start();
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
						public void onClick(DialogInterface dialog,
											int which) {

							NumberPicker numberPicker =
									(NumberPicker) ((AlertDialog) dialog).findViewById(R.id.numberPicker1);

							int value = numberPicker.getValue();

							PreferenceManager.setPref(getActivity(), "pref_syncInterval", value);
							dialog.dismiss();

						}
					});
					
					builder.setNegativeButton("Cancel", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog,
											int which) {
						
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
				
					bhApp.authentification.synchronizeFoundDevices.startSyncing(1, true);

					return true;
				}
			});
			
			forceDownSync.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
	
					bhApp.authentification.synchronizeFoundDevices.startSyncing(2, true);
					
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
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
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

					int result =
							new DatabaseManager(bhApp, bhApp.getVersionCode()).rebuildDatabase();

					if (result == 0) {
						Toast.makeText(getActivity(), "Successfully rebuilt database. App will now restart.", Toast.LENGTH_LONG).show();
					}
					else if (result < 0) {
						Toast.makeText(getActivity(), (-result) + " devices could not be rebuild. App will now restart.", Toast.LENGTH_LONG).show();
					}
					else if (result == 1001) {
						Toast.makeText(getActivity(), "Database could not be rebuilt.", Toast.LENGTH_LONG).show();
						return true;
					}

					Intent i =
							getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
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

		public PrefNetThread(SettingsActivity preferenceActivity,
				BlueHunter app,
				PrefNetManager networkMananger) {

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

				List<NameValuePair> postValues = new ArrayList<NameValuePair>();

				for (int i = 2; i < params.length; i++) {
					Pattern pattern = Pattern.compile("(.+)=(.+)", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(params[i]);

					matcher.matches();

					postValues.add(new BasicNameValuePair(matcher.group(1), matcher.group(2)));
				}

				URI httpUri = URI.create(remoteFile);

				// SSL Implementation

				HttpClient httpClient;

				if (https) {

					SchemeRegistry schemeRegistry = new SchemeRegistry();
					// http scheme
					schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
					// https scheme
					schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

					HttpParams httpParams = new BasicHttpParams();
					httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
					httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
					httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
					HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

					ClientConnectionManager cm =
							new ThreadSafeClientConnManager(httpParams, schemeRegistry);

					httpClient = new DefaultHttpClient(cm, httpParams);
				}
				else {
					httpClient = new DefaultHttpClient();
				}
				HttpPost postRequest = new HttpPost(httpUri);

				postRequest.setEntity(new UrlEncodedFormEntity(postValues));

				HttpResponse httpResponse = httpClient.execute(postRequest);

				String result = EntityUtils.toString(httpResponse.getEntity());

				if (!String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("2")) { return "<requestID='" + requestId + "' />" + "Error=" + httpResponse.getStatusLine().getStatusCode(); }

				return "<requestID='" + requestId + "' />" + result;
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "<requestID='" + requestId + "' />" + "Error=5\n" + e.getMessage();
			}
			catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "<requestID='" + requestId + "' />" + "Error=4\n" + e.getMessage();
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

			if (!preferenceActivity.isDestroyed()) {
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
}
