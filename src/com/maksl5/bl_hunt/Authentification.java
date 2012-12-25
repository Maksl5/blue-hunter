/**
 *  Authentification.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		if (serial == null
			| serial.equals("")) serial = "NULL";

		return serial;
	}

	public String getSerialNumberHash() {

		return secure.getSerialHash(getSerialNumber());
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
								Toast.makeText(context, "NEW NIGHTLY VERSION AVAILABLE\nCurrently installed build: "
														+ getVersionCode(context)
														+ "\nAvailable build: "
														+ verCode, Toast.LENGTH_LONG).show();
								newUpdateAvailable = true;
							}
						}
						catch (IllegalStateException e) {
							Pattern pattern = Pattern.compile("Error=(\\d+)");
							Matcher matcher = pattern.matcher(resultString);
							if (matcher.find()) {
								Toast.makeText(context, "There was an error while connecting to the Server.", Toast.LENGTH_LONG).show();
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

						switch (error) {
							case 1:
							case 4:
							case 5:
								Toast.makeText(mainActivity, "Error retrieving changelog for new version", Toast.LENGTH_LONG).show();
								break;
							case 90:
								Toast.makeText(mainActivity, "Updated to new version. No Changelog available.", Toast.LENGTH_LONG).show();
								break;
							case 404:
								Toast.makeText(mainActivity, "Updated to new version. Changelog could not be found on the server.", Toast.LENGTH_LONG).show();
								break;
						}
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
			if (oldVersion == 0
				| newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED));
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old="
																																	+ oldVersion, "new="
																																					+ newVersion);
			}
		}
		else {
			if (oldVersion == 0
				| newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "l="
																																	+ limit);
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED), "old="
																																	+ oldVersion, "new="
																																					+ newVersion, "l="
																																									+ limit);
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
}
