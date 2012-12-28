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

import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.R.xml;
import com.maksl5.bl_hunt.net.Authentification;
import com.maksl5.bl_hunt.net.Authentification.OnLoginChangeListener;
import com.maksl5.bl_hunt.net.Authentification.OnNetworkResultAvailableListener;
import com.maksl5.bl_hunt.net.AuthentificationSecure;
import com.maksl5.bl_hunt.net.EasySSLSocketFactory;
import com.maksl5.bl_hunt.net.NetworkManager;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class PreferenceActivity extends android.preference.PreferenceActivity implements OnNetworkResultAvailableListener {

	Menu menu;
	ProgressBar progressBar;
	MenuItem progressBarItem;

	private boolean destroyed = false;
	private String newPass = null;

	MainActivity mainActivity;
	PrefNetManager netManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mainActivity = MainActivity.thisActivity;

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

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.menu = menu;

		getMenuInflater().inflate(R.menu.act_settings, this.menu);

		progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
		this.menu.findItem(R.id.menu_progress).setVisible(false).setActionView(progressBar);

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

				mainActivity.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, mainActivity, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE), "h=" + mainActivity.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(), "v=" + mainActivity.versionCode, "op=" + oldPass, "np=" + newPass, "lt=" + mainActivity.authentification.getStoredLoginToken());
			}
			else if (resultCode == 2) {

				newPass = data.getStringExtra("newPass");

				mainActivity.authentification.setOnNetworkResultAvailableListener(this);

				PrefNetThread changePass = new PrefNetThread(this, mainActivity, netManager);
				changePass.execute(AuthentificationSecure.SERVER_PASS_CHANGE, String.valueOf(Authentification.NETRESULT_ID_PASS_CHANGE), "h=" + mainActivity.authentification.getPassChangeHash(newPass), "s=" + Authentification.getSerialNumber(), "v=" + mainActivity.versionCode, "np=" + newPass, "lt=" + mainActivity.authentification.getStoredLoginToken());

				
				
			}
			break;
		case ChangePasswordActivity.MODE_CHANGE_LOGIN_PASS:
			if(resultCode == 1) {
				
				String newLoginPass = data.getStringExtra("newLoginPass");
				mainActivity.authentification.storePass(newLoginPass);
				
				mainActivity.loginManager.login();
				
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

				String errorMsg = "Error " + error + " while changing password.";

				switch (error) {
				case 1:
				case 4:
				case 5:
					errorMsg += " (Could not retrieve data. Possibly no internet connection.)";
					break;
				case 404:
					errorMsg +=
							" (Could not retrieve data. Possibly server is down or script not available.)";
					break;
				case 500:
					errorMsg +=
							" (Could not retrieve data. Server error. Please contact the developer.)";
					break;
				case 1001:
				case 1002:
				case 1003:
				case 1004:
				case 10016:
					errorMsg += " (Uncomplete parameters)";
					break;
				case 1005:
					errorMsg += " (Hashes don't match)";
					break;
				case 1006:
				case 1008:
					errorMsg += " (Could not set new password in DB)";
					break;
				case 1007:
					errorMsg += " (Old password must be committed)";
					break;
				case 1009:
					errorMsg += " (Password does not match entered one)";
					break;
				case 1010:
				case 1013:
					errorMsg += " (Unexpected number of rows in DB query result)";
					break;
				case 1011:
					errorMsg += " (Unknown login token. [Error deleting it from DB.])";
					break;
				case 1012:
					errorMsg += " (Unknown login token. Try again.)";
					mainActivity.loginManager.login();
					break;
				case 1014:
					errorMsg += " (Found more than 1 login token. [Error deleting them from DB.])";
					break;
				case 1015:
					errorMsg += " (Found more than 1 login token. Try again.)";
					mainActivity.loginManager.login();
					break;
				}

				Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_LONG).show();
			}

			if(resultString.equals("<SUCCESS>")) {
				Toast.makeText(mainActivity, "Successfully changed password.", Toast.LENGTH_LONG).show();
				
				if(ProfileFragment.changeLoginPass != null) {
					ProfileFragment.changeLoginPass.setEnabled(true);
				}
				
				if(newPass != null) {
					mainActivity.authentification.storePass(newPass);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

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

						MainActivity main = MainActivity.thisActivity;
						main.authentification.showChangelog(InfoFragment.this.getActivity());

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

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

						MainActivity.thisActivity.alterNotification(newValue.equals(true));

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


		public static Preference changeOnlinePass;
		public static Preference changeLoginPass;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {

			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.profile_preference);
			
			changeOnlinePass = findPreference("pref_changePass");
			changeOnlinePass.setEnabled(MainActivity.thisActivity.loginManager.getLoginState());
			
			changeLoginPass = findPreference("pref_localPass");
			if(!MainActivity.thisActivity.passSet && MainActivity.thisActivity.loginManager.getLoginState()) {
				changeOnlinePass.setTitle(R.string.str_Preferences_changePass_new_title);
				changeOnlinePass.setSummary(R.string.str_Preferences_changePass_new_sum);
				changeLoginPass.setEnabled(false);
			}

			MainActivity.thisActivity.loginManager.login();
			registerListeners();

		}

		private void registerListeners() {

			changeOnlinePass.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					Intent intent =
							new Intent(getActivity(), ChangePasswordActivity.class);
					Bundle parametersBundle = new Bundle();
					parametersBundle.putInt("mode", ChangePasswordActivity.MODE_CHANGE_ONLINE_PASS);
					parametersBundle.putBoolean("passSet", MainActivity.thisActivity.passSet);
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

			MainActivity.thisActivity.authentification.setOnLoginChangeListener(new OnLoginChangeListener() {

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

		}
	}

	/**
	 * @author Maksl5
	 * 
	 */
	public class PrefNetManager {

		private PreferenceActivity prefActivity;
		private List<PrefNetThread> curRunningThreads;

		public PrefNetManager(PreferenceActivity prefActivity) {

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

		private MainActivity mainActivity;
		private PreferenceActivity preferenceActivity;
		private PrefNetManager networkMananger;

		public PrefNetThread(PreferenceActivity preferenceActivity,
				MainActivity mainActivity,
				PrefNetManager networkMananger) {

			super();
			this.preferenceActivity = preferenceActivity;
			this.mainActivity = mainActivity;
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

			mainActivity.authentification.fireOnNetworkResultAvailable(reqId, result);

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
