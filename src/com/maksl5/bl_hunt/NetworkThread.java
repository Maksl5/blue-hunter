/**
 *  NetworkThread.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.view.MenuItem;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class NetworkThread extends AsyncTask<String, Integer, String> {

	private MainActivity mainActivity;
	private NetworkMananger networkMananger;

	public NetworkThread(MainActivity mainActivity,
			NetworkMananger networkMananger) {

		super();
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
				httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

				ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

				httpClient = new DefaultHttpClient(cm, httpParams);
			}
			else {
				httpClient = new DefaultHttpClient();
			}
			HttpPost postRequest = new HttpPost(httpUri);

			postRequest.setEntity(new UrlEncodedFormEntity(postValues));

			HttpResponse httpResponse = httpClient.execute(postRequest);

			String result = EntityUtils.toString(httpResponse.getEntity());

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

		if (!mainActivity.isDestroyed()) {
			MenuItem progressBar = mainActivity.actionBarHandler.getMenuItem(R.id.menu_progress);
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
