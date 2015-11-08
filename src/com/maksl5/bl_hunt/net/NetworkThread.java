/**
 *  NetworkThread.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;

import android.os.AsyncTask;
import android.view.MenuItem;

/**
 * 
 * 
 * Call execute(String remoteFile, String requestID, String params...);
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */

public class NetworkThread extends AsyncTask<String, Integer, String> {

	private BlueHunter bhApp;

	public NetworkThread(BlueHunter app) {

		super();
		this.bhApp = app;
		bhApp.netMananger.addRunningThread(this);
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
			conn.setReadTimeout(120000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getPostDataString(postValues));

			writer.flush();
			writer.close();
			os.close();

			int responseCode = conn.getResponseCode();

			String result = "";

			if (responseCode == HttpURLConnection.HTTP_OK) {

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				StringBuilder stringBuilder = new StringBuilder();

				String line = "";
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

		bhApp.netMananger.threadFinished(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {

		MenuItem progressBar = bhApp.actionBarHandler.getMenuItem(R.id.menu_progress);
		if (!progressBar.isVisible()) progressBar.setVisible(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {

	}

	public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}

}
