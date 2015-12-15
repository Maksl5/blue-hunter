/**
 *  Authentification.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.net;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.ErrorHandler;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.PreferenceManager;

import org.acra.ACRA;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class Authentification {

	public static final int NETRESULT_ID_SERIAL_CHECK = 1;
	public static final int NETRESULT_ID_CHECK_UPDATE = 2;
	public static final int NETRESULT_ID_GET_USER_INFO = 3;
	public static final int NETRESULT_ID_UPDATED = 4;
	public static final int NETRESULT_ID_PRE_LOGIN = 5;
	public static final int NETRESULT_ID_LOGIN = 6;
	public static final int NETRESULT_ID_CHECK_LOGIN = 7;
	public static final int NETRESULT_ID_PASS_CHANGE = 8;
	public static final int NETRESULT_ID_SYNC_FD_CHECK = 9;
	public static final int NETRESULT_ID_SYNC_FD_APPLY = 10;
	public static final int NETRESULT_ID_APPLY_NAME = 11;
	public static boolean newUpdateAvailable = false;
	public boolean internetIsAvailable = false;
	protected BlueHunter bhApp;
	private AuthentificationSecure secure;
	private Context context;
	private volatile ArrayList<OnNetworkResultAvailableListener> listenerList = new ArrayList<Authentification.OnNetworkResultAvailableListener>();
	private ArrayList<OnLoginChangeListener> loginChangeListeners = new ArrayList<Authentification.OnLoginChangeListener>();

	public Authentification(BlueHunter app) {

		context = app;
		this.bhApp = app;

		checkInternetConnection();

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

		if (serial == null || serial.equals("") || serial.equals("NULL")) {

			if (serial == null || serial.equals("") || serial.equals("NULL")) {
				serial = Build.SERIAL;
			}

		}

		if (serial == null || serial.equals("")) {
			serial = "NULL";
		}

		return serial;
	}

	public static String getPassHash(String pass) {

		return AuthentificationSecure.getPassHash(pass);
	}

	public String getSerialNumberHash() {

		return secure.getSerialHash(getSerialNumber());
	}

	public String getLoginHash(String resultFromPreLogin) {

		return secure.getLoginHash(getSerialNumber(), resultFromPreLogin);
	}

	public String getPassChangeHash(String newPass) {

		return secure.getPassChangeHash(getSerialNumber(), newPass);
	}

	public String getStoredPass() {

		return secure.getStoredPass();
	}

	public String getStoredLoginToken() {

		return secure.getStoredLoginToken();
	}

	/**
	 * @param tokenString
	 */
	public void storeLoginToken(String tokenString) {

		secure.storeLoginToken(tokenString);

	}

	public void storePass(String pass) {

		secure.storePass(pass);

	}

	/**
	 * @return the context
	 */
	public Context getContext() {

		return context;
	}

	public void checkUpdate() {

		if (true) return;

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

							if (verCode > bhApp.getVersionCode()) {
								Toast.makeText(bhApp,
										bhApp.getString(R.string.str_auth_newUpdateAvailable, bhApp.getVersionCode(), verCode),
										Toast.LENGTH_LONG).show();
								newUpdateAvailable = true;
							}
						}
						catch (IllegalStateException e) {
							Pattern pattern = Pattern.compile("Error=(\\d+)");
							Matcher matcher = pattern.matcher(resultString);
							if (matcher.find()) {
								int errorCode = Integer.parseInt(matcher.group(1));
								Toast.makeText(bhApp, bhApp.getString(R.string.str_Error_checkUpdate, errorCode), Toast.LENGTH_LONG).show();
							}
						}

						return true;
					}

					return false;
				}
			});

			NetworkThread checkUpdateThread = new NetworkThread(bhApp);

			checkUpdateThread.execute(AuthentificationSecure.SERVER_CHECK_UPDATE, String.valueOf(NETRESULT_ID_CHECK_UPDATE));
		}

	}

	public void showChangelog() {

		showChangelog(0);
	}

	public void showChangelog(int limit) {

		showChangelog(0, 0, limit);
	}

	/**
	* 
	*/
	public void showChangelog(int oldVersion, int newVersion, int limit) {

		NetworkThread getChangelog = new NetworkThread(bhApp);

		setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId, String resultString) {

				if (requestId == Authentification.NETRESULT_ID_UPDATED) {

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));
						String updateMsg = ErrorHandler.getErrorString(context, requestId, error);
						updateMsg = String.format(updateMsg, bhApp.getVersionName());

						Toast.makeText(bhApp, updateMsg, Toast.LENGTH_LONG).show();
					}
					else {

						Builder builder = new Builder(bhApp.currentActivity);
						builder.setTitle(bhApp.getString(R.string.str_auth_changelog));
						builder.setNeutralButton(R.string.str_auth_changelog_ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								dialog.dismiss();

							}
						});

						AlertDialog changelogDialog = builder.create();
						ScrollView changeLogScrollView = new ScrollView(bhApp.currentActivity);

						changeLogScrollView.setSmoothScrollingEnabled(true);

						int padding = bhApp.getResources().getDimensionPixelSize(R.dimen.padding_small);

						TextView changelogTextView = new TextView(bhApp.currentActivity);

						changelogTextView.setPadding(padding, padding, padding, padding);
						changeLogScrollView.addView(changelogTextView,
								new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

						changelogTextView.setText(Html.fromHtml(resultString));
						changelogTextView.setMovementMethod(LinkMovementMethod.getInstance());
						changelogTextView.setClickable(true);

						changelogDialog.setView(changeLogScrollView, 0, 0, 0, 0);

						try {
							changelogDialog.show();
						}
						catch (BadTokenException e) {

							ACRA.getErrorReporter().handleSilentException(e);

						}

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
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED),
						"old=" + oldVersion, "new=" + newVersion);
			}
		}
		else {
			if (oldVersion == 0 | newVersion == 0) {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED),
						"l=" + limit);
			}
			else {
				getChangelog.execute(AuthentificationSecure.SERVER_UPDATED, String.valueOf(Authentification.NETRESULT_ID_UPDATED),
						"old=" + oldVersion, "new=" + newVersion, "l=" + limit);
			}

		}

	}

	public synchronized void setOnNetworkResultAvailableListener(OnNetworkResultAvailableListener listener) {

		synchronized (listenerList) {
			if (!listenerList.contains(listener)) listenerList.add(listener);
		}

	}

	/**
	 * @return the onNetworkResultAvailableListener
	 */
	public synchronized void fireOnNetworkResultAvailable(int requestId, String resultString) {

		List<OnNetworkResultAvailableListener> iterateList = new ArrayList<Authentification.OnNetworkResultAvailableListener>(listenerList);

		for (OnNetworkResultAvailableListener onNetworkResultAvailableListener : iterateList) {
			if (onNetworkResultAvailableListener.onResult(requestId, resultString)) {
				listenerList.remove(onNetworkResultAvailableListener);
			}
		}
	}

	public synchronized void unregisterListener(OnNetworkResultAvailableListener listener) {

		synchronized (listenerList) {
			listenerList.remove(listener);
		}

	}

	public synchronized void setOnLoginChangeListener(OnLoginChangeListener onLoginChangeListener) {

		if (!loginChangeListeners.contains(onLoginChangeListener))
			loginChangeListeners.add(onLoginChangeListener);
	}

	public synchronized void fireLoginChange(boolean loggedIn) {

		for (OnLoginChangeListener onLoginChangeListener : loginChangeListeners) {
			onLoginChangeListener.loginStateChange(loggedIn);
		}

	}

	public synchronized void removeOnLoginChangeListener(OnLoginChangeListener onLoginChangeListener) {

		loginChangeListeners.remove(onLoginChangeListener);
	}

	private void checkInternetConnection() {

		ConnectivityManager connectivityManager = (ConnectivityManager) bhApp.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null) {
			internetIsAvailable = false;
		} else internetIsAvailable = networkInfo.isConnected();
	}

	public boolean isInternetAvailable() {

		return internetIsAvailable;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getAchieveHash(int id) {

		return secure.getAchieveHash(id);
	}

	public interface OnNetworkResultAvailableListener {

		/**
		 * Called, when a network result is available.
		 *
		 * @param requestId    The requestId, that identifies the request.
		 * @param resultString The String, that is returned.
		 * @return True, if you want to remove the listener from listening to
		 * the results available. False, if not removing.
		 */
		boolean onResult(int requestId, String resultString);
	}

	public interface OnLoginChangeListener {

		void loginStateChange(boolean loggedIn);
	}

	/**
	 * @author Maksl5
	 *
	 */
	public class LoginManager extends BroadcastReceiver implements OnNetworkResultAvailableListener {

		private int timestamp;
		private String password;
		private String loginToken;
		private String serialNumber;

		private int uid;

		private boolean loggedIn = false;

		public LoginManager(String serialNumber) {

			this(serialNumber, null);

		}

		public LoginManager(String serialNumber, String loginToken) {

			this(serialNumber, null, loginToken);

		}

		public LoginManager(String serialNumber, String password, String loginToken) {

			this.password = password;
			this.serialNumber = serialNumber;
			this.loginToken = loginToken;
			this.uid = -1;

			registerInternetReceiver();

		}

		private void registerInternetReceiver() {

			IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

			bhApp.registerReceiver(this, intentFilter);
		}

		public void unregisterInternetReceiver() {

			try {
				bhApp.unregisterReceiver(this);
			}
			catch (IllegalArgumentException e) {

			}

		}

		public void login() {

			setOnNetworkResultAvailableListener(this);
			checkLogin();
		}

		private void internalLogin(String resultFromPreLogin) {

			if (resultFromPreLogin != null && !resultFromPreLogin.equalsIgnoreCase("")) {
				Toast.makeText(getContext(), resultFromPreLogin, Toast.LENGTH_LONG).show();

				NetworkThread login = new NetworkThread(bhApp);

				if (password == null) {
					login.execute(AuthentificationSecure.SERVER_LOGIN, String.valueOf(NETRESULT_ID_LOGIN),
							"h=" + Authentification.this.getLoginHash(resultFromPreLogin), "s=" + Authentification.getSerialNumber(),
							"v=" + bhApp.getVersionCode(), "t=" + resultFromPreLogin);
				}
				else {
					login.execute(AuthentificationSecure.SERVER_LOGIN, String.valueOf(NETRESULT_ID_LOGIN),
							"h=" + Authentification.this.getLoginHash(resultFromPreLogin), "s=" + Authentification.getSerialNumber(),
							"v=" + bhApp.getVersionCode(), "t=" + resultFromPreLogin, "p=" + password);
				}
			}
		}

		private void preLogin() {

			NetworkThread preLogin = new NetworkThread(bhApp);
			preLogin.execute(AuthentificationSecure.SERVER_PRE_LOGIN, String.valueOf(Authentification.NETRESULT_ID_PRE_LOGIN),
					"s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(),
					"h=" + Authentification.this.getSerialNumberHash());

		}

		private void checkLogin() {

			password = getStoredPass();

			if (loginToken != null) {

				NetworkThread checkLogin = new NetworkThread(bhApp);

				if (password == null) {
					checkLogin.execute(AuthentificationSecure.SERVER_CHECK_LOGIN, String.valueOf(Authentification.NETRESULT_ID_CHECK_LOGIN),
							"s=" + getSerialNumber(), "lt=" + loginToken);
				}
				else {
					checkLogin.execute(AuthentificationSecure.SERVER_CHECK_LOGIN, String.valueOf(Authentification.NETRESULT_ID_CHECK_LOGIN),
							"s=" + getSerialNumber(), "lt=" + loginToken, "p=" + password);
				}
			}
			else {
				preLogin();
			}

		}

		public boolean getLoginState() {

			return loggedIn;
		}

		private void setLoginState(boolean loggedIn) {

			if (this.loggedIn != loggedIn) {
				fireLoginChange(loggedIn);
			}
			this.loggedIn = loggedIn;

		}

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener#
		 * onResult(int, java.lang.String)
		 */
		@Override
		public boolean onResult(int requestId, String resultString) {

			switch (requestId) {
			case NETRESULT_ID_PRE_LOGIN:

				Pattern pattern = Pattern.compile("Error=(\\d+)");
				Matcher matcher = pattern.matcher(resultString);

				if (matcher.find()) {

					setLoginState(false);

					int error = Integer.parseInt(matcher.group(1));

					String errorMsg = ErrorHandler.getErrorString(context, requestId, error);

					Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
					return true;
				}

				internalLogin(resultString);
				return false;
			case NETRESULT_ID_LOGIN:

				Pattern pattern1 = Pattern.compile("Error=(\\d+)");
				Matcher matcher1 = pattern1.matcher(resultString);

				if (matcher1.find()) {

					setLoginState(false);

					int error = Integer.parseInt(matcher1.group(1));

					String errorMsg = ErrorHandler.getErrorString(context, requestId, error);

					Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
					return true;
				}

				Toast.makeText(getContext(), resultString, Toast.LENGTH_LONG).show();

				Pattern loginTokenPattern = Pattern.compile("<token>(s_[0-9a-f]{14}\\.[0-9]{8})</token><passExists>([0-1])</passExists>");
				Matcher loginTokenMatcher = loginTokenPattern.matcher(resultString);
				if (loginTokenMatcher.find()) {
					String tokenString = loginTokenMatcher.group(1);
					boolean passExists = "1".equals(loginTokenMatcher.group(2));

					Authentification.this.storeLoginToken(tokenString);
					this.loginToken = tokenString;

					bhApp.mainActivity.passSet = passExists;

					setLoginState(true);

					if (!passExists) {
						Toast.makeText(bhApp, String.format("%s%n%s", bhApp.getString(R.string.str_auth_loginSuccess),
								bhApp.getString(R.string.str_auth_securityMsg)), Toast.LENGTH_LONG).show();
						Toast.makeText(bhApp, String.format("%s%n%s", bhApp.getString(R.string.str_auth_loginSuccess),
								bhApp.getString(R.string.str_auth_securityMsg)), Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(bhApp, bhApp.getString(R.string.str_auth_loginSuccess), Toast.LENGTH_LONG).show();
					}

				}
				else {
					Toast.makeText(bhApp, bhApp.getString(R.string.str_auth_storeTokenFailed), Toast.LENGTH_LONG).show();
				}

				return true;
			case NETRESULT_ID_CHECK_LOGIN:

				Pattern pattern2 = Pattern.compile("Error=(\\d+)");
				Matcher matcher2 = pattern2.matcher(resultString);

				if (matcher2.find()) {

					setLoginState(false);

					int error = Integer.parseInt(matcher2.group(1));

					String errorMsg = ErrorHandler.getErrorString(context, requestId, error);

					switch (error) {

					case 1004:
						bhApp.mainActivity.passSet = true;
						break;
					}

					Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
					return true;
				}

				Pattern checkLoginPattern = Pattern.compile("<loggedIn>([0-1])</loggedIn><passExists>([0-1])</passExists>");
				Matcher checkLoginMatcher = checkLoginPattern.matcher(resultString);
				if (checkLoginMatcher.find()) {

					setLoginState("1".equals(checkLoginMatcher.group(1)));

					boolean passExists = "1".equals(checkLoginMatcher.group(2));
					bhApp.mainActivity.passSet = passExists;

					if (!loggedIn) {
						preLogin();
						return false;
					}
					else {
						return true;
					}

				}
				else {
					preLogin();
					return false;
				}
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				boolean beforeCheck = isInternetAvailable();
				checkInternetConnection();
				boolean afterCheck = isInternetAvailable();

				if (beforeCheck == false && afterCheck == true) {
					NetworkThread serialSubmit = new NetworkThread(bhApp);
					serialSubmit.execute(AuthentificationSecure.SERVER_CHECK_SERIAL,
							String.valueOf(Authentification.NETRESULT_ID_SERIAL_CHECK), "s=" + Authentification.getSerialNumber(),
							"v=" + bhApp.getVersionCode(), "h=" + bhApp.authentification.getSerialNumberHash());
				}
				else if (beforeCheck == true && afterCheck == false) {
					setLoginState(false);
				}
			}
		}

	}
}
