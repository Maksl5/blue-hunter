/**
 *  NetworkThread.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.maksl5.bl_hunt.R.id;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class NetworkThread extends AsyncTask<String, Integer, String> {

	MainActivity mainActivity;

	public NetworkThread(MainActivity mainActivity) {

		super();
		this.mainActivity = mainActivity;
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
		
		
		try {

			List<NameValuePair> postValues = new ArrayList<NameValuePair>();

			for (int i = 2; i < params.length; i++) {
				Pattern pattern = Pattern.compile("(.+)=(.+)", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(params[i]);

				matcher.matches();
				
				postValues.add(new BasicNameValuePair(matcher.group(1), matcher.group(2)));
			}

			URI httpUri = URI.create("http://maks.mph-p.de/blueHunter/" + remoteFile);

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(httpUri);

			postRequest.setEntity(new UrlEncodedFormEntity(postValues));
			
			HttpResponse httpResponse = httpClient.execute(postRequest);
			
			String result = EntityUtils.toString(httpResponse.getEntity());
			
			return "<requestID='" + requestId + "' />" + result;
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<requestID='" + requestId + "' />" + "NA(-1)(1)";
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<requestID='" + requestId + "' />" + "NA(-1)(2)";
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<requestID='" + requestId + "' />" + "NA(-1)(3)";
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
			
		if (!mainActivity.isDestroyed()) {
			ProgressBar progressBar = (ProgressBar) mainActivity.actionBarHandler.getActionView(R.id.menu_progress);
			progressBar.setVisibility(ProgressBar.GONE);
		}
		
		mainActivity.authentification.fireOnNetworkResultAvailable(reqId, result);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {

		if (!mainActivity.isDestroyed()) {
			ProgressBar progressBar = (ProgressBar) mainActivity.actionBarHandler.getActionView(R.id.menu_progress);
			progressBar.setVisibility(ProgressBar.VISIBLE);
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
