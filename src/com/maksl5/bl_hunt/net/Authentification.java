/**
 *  Authentification.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.net;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.R.dimen;
import com.maksl5.bl_hunt.R.string;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Toast;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class Authentification {

	private AuthentificationSecure secure;
	private Context context;
	private MainActivity mainActivity;
	private ArrayList<OnNetworkResultAvailableListener> listenerList =
			new ArrayList<Authentification.OnNetworkResultAvailableListener>();

	public static final int NETRESULT_ID_SERIAL_CHECK = 1;
	public static final int NETRESULT_ID_CHECK_UPDATE = 2;
	public static final int NETRESULT_ID_GET_USER_INFO = 3;
	public static final int NETRESULT_ID_UPDATED = 4;
	public static final int NETRESULT_ID_PRE_LOGIN = 5;
	public static final int NETRESULT_ID_LOGIN = 6;

	public static boolean newUpdateAvailable = false;

	public Authentification(Context con, MainActivity mainActivity) {

		context = con;
		this.mainActivity = mainActivity;

		secure = new AuthentificationSecure(this);

	}

	public static String getSerialNumber() {

		String serial = "NULL";

		try {
			Class<?> sysPropClass = Class.forName("android.os.SystemProperties");
			Method get = sysPropClass.getMethod("get", String.class);
			serial = (String) get.invoke(sysPropClass, "ro.serialno");
		}
		catch (Exception ignored) {
			serial = "NULL";
		}

		if (serial == null | serial.equals("")) serial = "NULL";

		return serial;
	}

	public String getSerialNumberHash() {

		return secure.getSerialHash(getSerialNumber());
	}

	public String getLoginHash(String resultFromPreLogin) {

		return secure.getLoginHash(getSerialNumber(), resultFromPreLogin);
	}

	/**
	 * @return the context
	 */
	public Context getContext() {

		return context;
	}

	public static int getVersionCode(Context con) {

		try {
			return con.getPackageManager().getPackageInfo(con.getPackageName(), PackageManager.GET_META_DATA).versionCode;
		}
		catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public static String getVersionName(Context con) {

		try {
			return con.getPackageManager().getPackageInfo(con.getPackageName(), PackageManager.GET_META_DATA).versionName;
		}
		catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public void checkUpdate() {

		if (PreferenceManager.getPref(context, "pref_checkUpdate", true)) {

			// Set result listener
			setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

				@Override
				public boolean onResult(int requestId, String resultString) {

					if (requestId == NETRESULT_ID_CHECK_UPDATE) {
						try {
							Pattern pattern = Pattern.compile("android:versionCode=\"(\\d+)\"");
							Matcher matcher = pattern.matcher(resultString);
							matcher.find();

							int verCode = Integer.parseInt(matcher.group(1));

							if (verCode > getVersionCode(context)) {
								Toast.makeText(context, "NEW NIGHTLY VERSION AVAILABLE\nCurrently installed build: " + getVersionCode(context) + "\nAvailable build: " + verCode, Toast.LENGTH_LONG).show();
								newUpdateAvailable = true;
							}
						}
						catch (IllegalStateException e) {
							Pattern pattern = Pattern.compile("Error=(\\d+)");
							Matcher matcher = pattern.matcher(resultString);
							if (matcher.find()) {
								int errorCode = Integer.parseInt(matcher.group(1));
								Toast.makeText(context, "Error " + errorCode + "while checking for update.", Toast.LENGTH_LONG).show();
							}
						}

						return true;
					}

					return false;
				}
			});

			NetworkThread checkUpdateThread = new NetworkThread(mainActivity, mainActivity.netMananger);

			checkUpdateThread.execute(AuthentificationSecure.SERVER_CHECK_UPDATE, String.valueOf(NETRESULT_ID_CHECK_UPDATE));
		}

	}

	public void showChangelog(final Activity activity) {

		showChangelog(activity, 0);
	}

	public void showChangelog(final Activity activity, int limit) {

		showChangelog(activity, 0, 0, limit);
	}

	/**
 * 
 */
	public void showChangelog(final Activity activity, int oldVersion, int newVersion, int limit) {

		NetworkThread getChangelog = new NetworkThread(mainActivity, mainActivity.netMananger);

		mainActivity.authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId, String resultString) {

				if (requestId == Authentification.NETRESULT_ID_UPDATED) {

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));

						String updateMsg = "Updated to new version.";

						switch (error) {
							case 1:
							case 4:
							case 5:
								updateMsg += " (Could not retrieve changelog. Possibly not internet connection.)";
								break;
							case 90:
								updateMsg += " (No Changelog available.)";
								break;
							case 404:
								updateMsg +=
										" (Could not retrieve changelog. Possibly server down or changelog script not available.)";
								break;
							case 500:
								updateMsg += " (Could not retrieve changelog. Server error. Please contact developer.)";
								break;
						}

						Toast.makeText(mainActivity, updateMsg, Toast.LENGTH_LONG).show();

					}
					else {

						Dialog changelogDialog = new Dialog(activity);
						ScrollView changeLogScrollView = new ScrollView(activity);
						WebView changelogWebView = new WebView(activity);
						changelogDialog.setTitle("Changelog");
						int padding = mainActivity.getResources().getDimensionPixelSize(R.dimen.padding_small);
						changelogWebView.setPadding(padding, padding, padding, padding);
						changelogDialog.addContentView(changeLogScrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						changeLogScrollView.addView(changelogWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						changelogWebView.loadDataWithBaseURL(null, resultString, "text/html", "UTF-8", null);
						changelogDialog.show();

					}

					return true;
				}

				return false;
			}
		});
		if (limit == 0) {
			if (oldVersion == 0 | newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED));
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old=" + oldVersion, "new=" + newVersion);
			}
		}
		else {
			if (oldVersion == 0 | newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "l=" + limit);
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old=" + oldVersion, "new=" + newVersion, "l=" + limit);
			}

		}

	}

	public interface OnNetworkResultAvailableListener {

		public abstract boolean onResult(int requestId, String resultString);
	}

	public void setOnNetworkResultAvailableListener(OnNetworkResultAvailableListener listener) {

		listenerList.add(listener);
	}

	/**
	 * @return the onNetworkResultAvailableListener
	 */
	public synchronized void fireOnNetworkResultAvailable(int requestId, String resultString) {

		ArrayList<OnNetworkResultAvailableListener> removeListeners = new ArrayList<OnNetworkResultAvailableListener>();

		for (OnNetworkResultAvailableListener listener : listenerList) {
			if (listener.onResult(requestId, resultString)) {
				removeListeners.add(listener);
			}
		}

		for (OnNetworkResultAvailableListener listener : removeListeners) {
			listenerList.remove(listener);
		}

		removeListeners.clear();
	}

	public synchronized void unregisterListener(OnNetworkResultAvailableListener listener) {

		listenerList.remove(listener);
	}

	/**
	 * @author Maksl5
	 * 
	 */
	public class LoginManager implements OnNetworkResultAvailableListener {

		private int timestamp;
		private String password;
		private String serialNumber;

		public LoginManager(String serialNumber) {

			this(serialNumber, null);

		}

		public LoginManager(String serialNumber, String password) {

			this.password = password;
			this.serialNumber = serialNumber;

		}

		public void login() {
			
			
			preLogin();
		}

		private void login(String resultFromPreLogin) {

			if (resultFromPreLogin != null && !resultFromPreLogin.equalsIgnoreCase("")) {
				Toast.makeText(getContext(), resultFromPreLogin, Toast.LENGTH_LONG).show();

				NetworkThread login = new NetworkThread(mainActivity, mainActivity.netMananger);
				login.execute(AuthentificationSecure.SERVER_LOGIN, String.valueOf(NETRESULT_ID_LOGIN), "h=" + Authentification.this.getLoginHash(resultFromPreLogin), "s=" + Authentification.getSerialNumber(), "v=" + mainActivity.versionCode, "t=" + resultFromPreLogin, "p=test");
			}
		}

		private void preLogin() {

			setOnNetworkResultAvailableListener(this);

			NetworkThread preLogin = new NetworkThread(mainActivity, mainActivity.netMananger);
			preLogin.execute(AuthentificationSecure.SERVER_PRE_LOGIN, String.valueOf(Authentification.NETRESULT_ID_PRE_LOGIN), "s=" + Authentification.getSerialNumber(), "v=" + mainActivity.versionCode, "h=" + Authentification.this.getSerialNumberHash());

		}
		
		private void checkLogin() {
			
			
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
		 */
		@Override
		public boolean onResult(int requestId, String resultString) {

			switch (requestId) {
				case NETRESULT_ID_PRE_LOGIN:

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));

						String errorMsg = "Error " + error + " while prelogin.";

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
								errorMsg += " (Could not retrieve data. Server error. Please contact the developer.)";
								break;
							case 1001:
							case 1002:
							case 1006:
								errorMsg += " (Uncomplete parameters)";
								break;
							case 1003:
								errorMsg += " (Hashes don't match)";
								break;
							case 1004:
							case 1005:
								errorMsg += " (Unexpected number of rows in DB query result)";
								break;
						}

						Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_LONG).show();
						break;
					}

					login(resultString);
					return false;
				case NETRESULT_ID_LOGIN:

					Pattern pattern1 = Pattern.compile("Error=(\\d+)");
					Matcher matcher1 = pattern1.matcher(resultString);

					if (matcher1.find()) {
						int error = Integer.parseInt(matcher1.group(1));

						String errorMsg = "Error " + error + " while login.";

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
								errorMsg += " (Could not retrieve data. Server error. Please contact the developer.)";
								break;
							case 1001:
							case 1002:
							case 1003:
							case 1006:
							case 1011:
								errorMsg += " (Uncomplete parameters)";
								break;
							case 1004:
								errorMsg += " (Hashes don't match)";
								break;
							case 1005:
							case 1007:
								errorMsg += " (Failed inserting session into DB)";
								break;
							case 1008:
								errorMsg += " (Password is wrong)";
								break;
							case 1009:
								errorMsg += " (Unexpected number of rows in DB query result)";
								break;
							case 1010:
								errorMsg += " (Request timeout)";
								break;

						}

						Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_LONG).show();
						break;
					}

					Toast.makeText(getContext(), resultString, Toast.LENGTH_LONG).show();
					return true;
			}

			return false;
		}

	}
}
