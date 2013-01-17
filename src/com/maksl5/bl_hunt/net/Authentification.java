/**
 *  Authentification.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.net;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acra.ACRA;

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
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.PreferenceManager;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class Authentification {

	private AuthentificationSecure secure;
	private Context context;
	protected BlueHunter bhApp;
	private ArrayList<OnNetworkResultAvailableListener> listenerList =
			new ArrayList<Authentification.OnNetworkResultAvailableListener>();
	private ArrayList<OnLoginChangeListener> loginChangeListeners =
			new ArrayList<Authentification.OnLoginChangeListener>();

	public static final int NETRESULT_ID_SERIAL_CHECK = 1;
	public static final int NETRESULT_ID_CHECK_UPDATE = 2;
	public static final int NETRESULT_ID_GET_USER_INFO = 3;
	public static final int NETRESULT_ID_UPDATED = 4;
	public static final int NETRESULT_ID_PRE_LOGIN = 5;
	public static final int NETRESULT_ID_LOGIN = 6;
	public static final int NETRESULT_ID_CHECK_LOGIN = 7;
	public static final int NETRESULT_ID_PASS_CHANGE = 8;

	public static boolean newUpdateAvailable = false;
	public boolean internetIsAvailable = false;

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

		if (serial == null || serial.equals("")) serial = "NULL";

		return serial;
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

	public static String getPassHash(String pass) {

		return AuthentificationSecure.getPassHash(pass);
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

		if (PreferenceManager.getPref(context, "pref_checkUpdate", true)) {

			// Set result listener
			setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

				@Override
				public boolean onResult(int requestId,
										String resultString) {

					if (requestId == NETRESULT_ID_CHECK_UPDATE) {
						try {
							Pattern pattern = Pattern.compile("android:versionCode=\"(\\d+)\"");
							Matcher matcher = pattern.matcher(resultString);
							matcher.find();

							int verCode = Integer.parseInt(matcher.group(1));

							if (verCode > bhApp.getVersionCode()) {
								Toast.makeText(bhApp, bhApp.getString(R.string.str_auth_newUpdateAvailable, bhApp.getVersionCode(), verCode), Toast.LENGTH_LONG).show();
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
	public void showChangelog(	int oldVersion,
								int newVersion,
								int limit) {

		NetworkThread getChangelog = new NetworkThread(bhApp);

		setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

			@Override
			public boolean onResult(int requestId,
									String resultString) {

				if (requestId == Authentification.NETRESULT_ID_UPDATED) {

					Pattern pattern = Pattern.compile("Error=(\\d+)");
					Matcher matcher = pattern.matcher(resultString);

					String updateMsg =
							bhApp.getString(R.string.str_auth_updated, bhApp.getVersionName());

					if (matcher.find()) {
						int error = Integer.parseInt(matcher.group(1));

						switch (error) {
						case 1:
						case 4:
						case 5:
							updateMsg +=
									String.format("(%s)", bhApp.getString(R.string.str_auth_updated_1_4_5));
							break;
						case 90:
							updateMsg +=
									String.format("(%s)", bhApp.getString(R.string.str_auth_updated_90));
							break;
						case 404:
							updateMsg +=
									String.format("(%s)", bhApp.getString(R.string.str_auth_updated_404));
							break;
						case 500:
							updateMsg +=
									String.format("(%s)", bhApp.getString(R.string.str_auth_updated_500));
							break;
						}

						Toast.makeText(bhApp, updateMsg, Toast.LENGTH_LONG).show();
					}
					else {

						Builder builder = new Builder(bhApp.currentActivity);
						builder.setTitle(bhApp.getString(R.string.str_auth_changelog));
						builder.setNeutralButton(R.string.str_auth_changelog_ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {

								dialog.dismiss();

							}
						});

						AlertDialog changelogDialog = builder.create();
						ScrollView changeLogScrollView = new ScrollView(bhApp.currentActivity);

						changeLogScrollView.setSmoothScrollingEnabled(true);

						WebView changelogWebView = new WebView(bhApp.currentActivity);
						int padding =
								bhApp.getResources().getDimensionPixelSize(R.dimen.padding_small);
						changelogWebView.setPadding(padding, padding, padding, padding);
						changeLogScrollView.addView(changelogWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						changelogWebView.loadDataWithBaseURL(null, resultString, "text/html", "UTF-8", null);

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

		public abstract boolean onResult(	int requestId,
											String resultString);
	}

	public void setOnNetworkResultAvailableListener(OnNetworkResultAvailableListener listener) {

		listenerList.add(listener);
	}

	/**
	 * @return the onNetworkResultAvailableListener
	 */
	public synchronized void fireOnNetworkResultAvailable(	int requestId,
															String resultString) {

		ArrayList<OnNetworkResultAvailableListener> removeListeners =
				new ArrayList<OnNetworkResultAvailableListener>();

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
	public class LoginManager extends BroadcastReceiver implements OnNetworkResultAvailableListener {

		private int timestamp;
		private String password;
		private String loginToken;
		private String serialNumber;

		private boolean loggedIn = false;

		public LoginManager(String serialNumber) {

			this(serialNumber, null);

		}

		public LoginManager(String serialNumber,
				String loginToken) {

			this(serialNumber, null, loginToken);

		}

		public LoginManager(String serialNumber,
				String password,
				String loginToken) {

			this.password = password;
			this.serialNumber = serialNumber;
			this.loginToken = loginToken;

			registerInternetReceiver();

		}

		private void registerInternetReceiver() {

			IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

			bhApp.registerReceiver(this, intentFilter);
		}

		public void unregisterInternetReceiver() {

			bhApp.unregisterReceiver(this);
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
					login.execute(AuthentificationSecure.SERVER_LOGIN, String.valueOf(NETRESULT_ID_LOGIN), "h=" + Authentification.this.getLoginHash(resultFromPreLogin), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "t=" + resultFromPreLogin);
				}
				else {
					login.execute(AuthentificationSecure.SERVER_LOGIN, String.valueOf(NETRESULT_ID_LOGIN), "h=" + Authentification.this.getLoginHash(resultFromPreLogin), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "t=" + resultFromPreLogin, "p=" + password);
				}
			}
		}

		private void preLogin() {

			NetworkThread preLogin = new NetworkThread(bhApp);
			preLogin.execute(AuthentificationSecure.SERVER_PRE_LOGIN, String.valueOf(Authentification.NETRESULT_ID_PRE_LOGIN), "s=" + Authentification.getSerialNumber(), "v=" + bhApp.getVersionCode(), "h=" + Authentification.this.getSerialNumberHash());

		}

		private void checkLogin() {

			password = getStoredPass();

			if (loginToken != null) {

				NetworkThread checkLogin = new NetworkThread(bhApp);

				if (password == null) {
					checkLogin.execute(AuthentificationSecure.SERVER_CHECK_LOGIN, String.valueOf(Authentification.NETRESULT_ID_CHECK_LOGIN), "s=" + getSerialNumber(), "lt=" + loginToken);
				}
				else {
					checkLogin.execute(AuthentificationSecure.SERVER_CHECK_LOGIN, String.valueOf(Authentification.NETRESULT_ID_CHECK_LOGIN), "s=" + getSerialNumber(), "lt=" + loginToken, "p=" + password);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener#onResult(int, java.lang.String)
		 */
		@Override
		public boolean onResult(int requestId,
								String resultString) {

			switch (requestId) {
			case NETRESULT_ID_PRE_LOGIN:

				Pattern pattern = Pattern.compile("Error=(\\d+)");
				Matcher matcher = pattern.matcher(resultString);

				if (matcher.find()) {

					setLoginState(false);

					int error = Integer.parseInt(matcher.group(1));

					String errorMsg = bhApp.getString(R.string.str_Error_preLogin, error);

					switch (error) {
					case 1:
					case 4:
					case 5:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_1_4_5));
						break;
					case 404:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_404));
						break;
					case 500:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_500));
						break;
					case 1001:
					case 1002:
					case 1006:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_preLogin_100_1_2_6));
						break;
					case 1003:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_preLogin_100_3));
						break;
					case 1004:
					case 1005:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_preLogin_100_4_5));
						break;
					}

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

					String errorMsg = bhApp.getString(R.string.str_Error_Login, error);

					switch (error) {
					case 1:
					case 4:
					case 5:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_1_4_5));
						break;
					case 404:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_404));
						break;
					case 500:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_500));
						break;
					case 1001:
					case 1002:
					case 1003:
					case 1011:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_1_2_3_11));
						break;
					case 1004:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_4));
						break;
					case 1005:
					case 1007:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_5_7));
						break;
					case 1006:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_6));
						break;
					case 1008:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_8));
						break;
					case 1009:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_9));
						break;
					case 1010:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_Login_100_10));
						break;

					}

					Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
					return true;
				}

				Toast.makeText(getContext(), resultString, Toast.LENGTH_LONG).show();

				Pattern loginTokenPattern =
						Pattern.compile("<token>(s_[0-9a-f]{14}\\.[0-9]{8})</token><passExists>([0-1])</passExists>");
				Matcher loginTokenMatcher = loginTokenPattern.matcher(resultString);
				if (loginTokenMatcher.find()) {
					String tokenString = loginTokenMatcher.group(1);
					boolean passExists = "1".equals(loginTokenMatcher.group(2));

					Authentification.this.storeLoginToken(tokenString);
					this.loginToken = tokenString;

					bhApp.mainActivity.passSet = passExists;

					setLoginState(true);

					if (!passExists) {
						Toast.makeText(bhApp, String.format("%s%n%s", bhApp.getString(R.string.str_auth_loginSuccess), bhApp.getString(R.string.str_auth_securityMsg)), Toast.LENGTH_LONG).show();
						Toast.makeText(bhApp, String.format("%s%n%s", bhApp.getString(R.string.str_auth_loginSuccess), bhApp.getString(R.string.str_auth_securityMsg)), Toast.LENGTH_LONG).show();
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

					String errorMsg = bhApp.getString(R.string.str_Error_checkLogin, error);

					switch (error) {
					case 1:
					case 4:
					case 5:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_1_4_5));
						break;
					case 404:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_404));
						break;
					case 500:
						errorMsg += String.format(" (%s)", bhApp.getString(R.string.str_Error_500));
						break;
					case 1001:
					case 1002:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_checkLogin_100_1_2));
						break;
					case 1003:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_checkLogin_100_3));
						break;
					case 1004:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_checkLogin_100_4));
						bhApp.mainActivity.passSet = true;
						break;
					case 1005:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_checkLogin_100_5));
						break;
					case 1006:
					case 1007:
						errorMsg +=
								String.format(" (%s)", bhApp.getString(R.string.str_Error_checkLogin_100_6_7));
						break;
					}
					Toast.makeText(bhApp, errorMsg, Toast.LENGTH_LONG).show();
					return true;
				}

				Pattern checkLoginPattern =
						Pattern.compile("<loggedIn>([0-1])</loggedIn><passExists>([0-1])</passExists>");
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
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(	Context context,
								Intent intent) {

			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				boolean beforeCheck = isInternetAvailable();
				checkInternetConnection();
				boolean afterCheck = isInternetAvailable();

				if (beforeCheck == false && afterCheck == true) {
					login();
				}
				else if (beforeCheck == true && afterCheck == false) {
					setLoginState(false);
				}
			}
		}

	}

	public interface OnLoginChangeListener {

		public abstract void loginStateChange(boolean loggedIn);
	}

	public synchronized void setOnLoginChangeListener(OnLoginChangeListener onLoginChangeListener) {

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

		ConnectivityManager connectivityManager =
				(ConnectivityManager) bhApp.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null) {
			internetIsAvailable = false;
		}
		else {
			if (networkInfo.isConnected()) {
				internetIsAvailable = true;
			}
			else {
				internetIsAvailable = false;
			}
		}
	}

	public boolean isInternetAvailable() {

		return internetIsAvailable;
	}
}
