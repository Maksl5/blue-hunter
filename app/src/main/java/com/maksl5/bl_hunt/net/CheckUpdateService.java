package com.maksl5.bl_hunt.net;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.SettingsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CheckUpdateService extends Service {

	private WakeLock mWakeLock;

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	private void handleIntent(Intent intent) {

		// obtain the wake lock
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CheckUpdateService");
		mWakeLock.acquire();

		// check the global background data setting
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
			stopSelf();
			return;
		}

		// do the actual work, in a separate thread

		PollTask checkUpdateThread = new PollTask();

		checkUpdateThread.execute(AuthentificationSecure.SERVER_CHECK_UPDATE);

	}

	@Override
	public void onStart(Intent intent,
						int startId) {

		handleIntent(intent);
	}

	@Override
	public int onStartCommand(Intent intent,
							  int flags,
							  int startId) {

		handleIntent(intent);
		return START_NOT_STICKY;
	}

	public void onDestroy() {

		super.onDestroy();
		mWakeLock.release();
	}

	private class PollTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String remoteFile = params[0];
			boolean https = false;

			if (remoteFile.startsWith("https")) https = true;

			try {


				URL httpUri = new URL(remoteFile);

				HttpURLConnection conn = (HttpURLConnection) httpUri.openConnection();
				conn.setReadTimeout(15000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("GET");

				conn.connect();

				int responseCode = conn.getResponseCode();

				String result = "";

				if (responseCode == HttpURLConnection.HTTP_OK) {

					String line = "";
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

					StringBuilder stringBuilder = new StringBuilder();

					while ((line = br.readLine()) != null) {
						stringBuilder.append(line).append(System.lineSeparator());
					}

					stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(System.lineSeparator()));

					result = stringBuilder.toString();

				}
				else {

					return "Error=" + responseCode + "\n" + conn.getResponseMessage();

				}

				return result;
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error=5\n" + e.getMessage();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error=1\n" + e.getMessage();
			}

		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {

			Pattern pattern = Pattern.compile("android:versionCode=\"(\\d+)\"");
			Matcher matcher = pattern.matcher(result);

			Log.d("CheckUpdateService", result);

			int verCode;
			int oldVerCode;
			boolean newUpdateAvailable = false;

			if (matcher.find()) {

				verCode = Integer.parseInt(matcher.group(1));

				oldVerCode = ((BlueHunter) getApplication()).getVersionCode();

				if (verCode > oldVerCode) {
					newUpdateAvailable = true;
					Authentification.newUpdateAvailable = true;
				}
			}
			else {
				Log.e("CheckUpdateService", result);
				return;
			}

			if (newUpdateAvailable) {

				NotificationManager notificationManager =
						(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder stateNotificationBuilder =
						new Notification.Builder(CheckUpdateService.this);

				stateNotificationBuilder.setOnlyAlertOnce(true);
				stateNotificationBuilder.setOngoing(false);
				stateNotificationBuilder.setSmallIcon(R.drawable.ic_launcher);
				stateNotificationBuilder.setAutoCancel(true);
				stateNotificationBuilder.setContentTitle("New nightly update is available!");
				stateNotificationBuilder.setContentText("Current: " + oldVerCode + " New: " + verCode);
				stateNotificationBuilder.setContentIntent(PendingIntent.getActivity(CheckUpdateService.this, 0, new Intent(CheckUpdateService.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("goToInfoPref", true), 0));
				stateNotificationBuilder.setTicker("New nightly update is available!");

				if (VERSION.SDK_INT >= 16) {
					notificationManager.notify(2, stateNotificationBuilder.build());
				}
				else {
					notificationManager.notify(2, stateNotificationBuilder.getNotification());
				}

			}

			stopSelf();
		}
	}

}
