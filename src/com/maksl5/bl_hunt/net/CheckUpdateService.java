package com.maksl5.bl_hunt.net;



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
		if (!cm.getActiveNetworkInfo().isConnected()) {
			stopSelf();
			return;
		}

		// do the actual work, in a separate thread

		PollTask checkUpdateThread = new PollTask();

		checkUpdateThread.execute(AuthentificationSecure.SERVER_CHECK_UPDATE);

	}

	private class PollTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String remoteFile = params[0];
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

				if (!String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("2")) { return String.valueOf(httpResponse.getStatusLine().getStatusCode()); }

				return result;
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error=5\n" + e.getMessage();
			}
			catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error=4\n" + e.getMessage();
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

	@Override
	public void onStart(Intent intent,
						int startId) {

		handleIntent(intent);
	}

	@Override
	public int onStartCommand(	Intent intent,
								int flags,
								int startId) {

		handleIntent(intent);
		return START_NOT_STICKY;
	}

	public void onDestroy() {

		super.onDestroy();
		mWakeLock.release();
	}

}
