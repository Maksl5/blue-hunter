package com.maksl5.bl_hunt.util;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.json.JSONException;
import org.json.JSONObject;

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

import static org.acra.ACRA.LOG_TAG;

/**
 * An JSON ReportSender for ACRA 4 (http://code.google.com/p/acra/)
 *
 * @author Felix Schulze
 */
public class JsonSender implements ReportSender {
    private static final String CONTENT_TYPE;

    static {
        CONTENT_TYPE = "application/json";
    }

    private Uri mFormUri = null;
    private Map<ReportField, String> mMapping = null;

    /**
     * <p>
     * Create a new HttpPostSender instance.
     * </p>
     *
     * @param formUri The URL of your server-side crash report collection script.
     * @param mapping If null, POST parameters will be named with
     *                {@link org.acra.ReportField} values converted to String with
     *                .toString(). If not null, POST parameters will be named with
     *                the result of mapping.get(ReportField.SOME_FIELD);
     */
    public JsonSender(String formUri, Map<ReportField, String> mapping) {
        mFormUri = Uri.parse(formUri);
        mMapping = mapping;
    }

    private static boolean isNull(String aString) {
        return aString == null || aString == ACRAConstants.NULL_VALUE;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
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

    public void send(Context context, CrashReportData report) throws ReportSenderException {

        try {
            URL reportUrl;
            reportUrl = new URL(mFormUri.toString());
            Log.d(LOG_TAG, "Connect to " + reportUrl.toString());

            JSONObject json = createJSON(report);

            sendHttpPost(json.toString(), reportUrl, ACRA.getConfig().formUriBasicAuthLogin(), ACRA.getConfig().formUriBasicAuthPassword());

        } catch (Exception e) {
            throw new ReportSenderException("Error while sending report to Http Post Form.", e);
        }

    }

    private JSONObject createJSON(Map<ReportField, String> report) {
        JSONObject json = new JSONObject();

        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRAConstants.DEFAULT_REPORT_FIELDS;
        }
        for (ReportField field : fields) {
            try {
                if (mMapping == null || mMapping.get(field) == null) {
                    json.put(field.toString(), report.get(field));
                } else {
                    json.put(mMapping.get(field), report.get(field));
                }
            } catch (JSONException e) {
                Log.e("JSONException", "There was an error creating JSON", e);
            }
        }

        return json;
    }

    //TODO: login + password
    //(isNull(login) ? null : login, isNull(password) ? null : password);
    private void sendHttpPost(String data, URL url, String login, String password) {


        HttpURLConnection conn = null;

        try {


            HashMap<String, String> nameValuePairs = new HashMap<>();
            nameValuePairs.put("json", data);

            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(30000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(nameValuePairs));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            String result = "";

            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line).append(System.lineSeparator());

                }

                stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(System.lineSeparator()));

                result = stringBuilder.toString();

            }


            Log.d(LOG_TAG, "Server Status: " + responseCode);
            Log.d(LOG_TAG, "Server Response: " + result);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
    }
}
