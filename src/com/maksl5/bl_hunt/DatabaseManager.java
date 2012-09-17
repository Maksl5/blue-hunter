/**
 *  DatabaseHelper.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.maksl5.bl_hunt.Authentification.OnNetworkResultAvailableListener;



public class DatabaseManager {

	private int version;
	private MainActivity mainActivity;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	/**
	 * 
	 */
	public DatabaseManager(MainActivity mainActivity,
			int version) {

		this.version = version;
		this.mainActivity = mainActivity;

		this.dbHelper = new DatabaseHelper(mainActivity, version);
		this.db = dbHelper.getWritableDatabase();

	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();
			return true;
		}
		else {
			close();
			return false;
		}
		


	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress,

								short RSSI) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_RSSI, RSSI);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();
			return true;
		}
		else {
			close();
			return false;
		}

	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress,
								String name,
								short RSSI) {
	
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_NAME, name);
		values.put(DatabaseHelper.COLUMN_RSSI, RSSI);
	
		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();
			return true;
		}
		else {
			close();
			return false;
		}
	
	}

	public List<String> getMacAddresses() {

		List<String> macStrings = new ArrayList<String>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, new String[] { DatabaseHelper.COLUMN_MAC_ADDRESS }, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			macStrings.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAC_ADDRESS)));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return macStrings;
	}

	public List<HashMap<String, String>> getAllDevices() {

		List<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();

			tempHashMap.put(DatabaseHelper.COLUMN_MAC_ADDRESS, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAC_ADDRESS)));
			tempHashMap.put(DatabaseHelper.COLUMN_NAME, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
			tempHashMap.put(DatabaseHelper.COLUMN_RSSI, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RSSI)));

			devices.add(tempHashMap);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return devices;
	}

	public void addNameToDevice(String macAddress,
								String name) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NAME, name);

		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });
		FragmentLayoutManager.refreshFoundDevicesList(mainActivity);
		close();

	}

	public void close() {

		db.close();
		dbHelper.close();
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public class DatabaseHelper extends SQLiteOpenHelper {

		public final static String DATABASE_NAME = "foundDevicesDataStorage.db";
		public final static String FOUND_DEVICES_TABLE = "foundDevices";

		public final static String COLUMN_MAC_ADDRESS = "macAddress";
		public final static String COLUMN_NAME = "name";
		public final static String COLUMN_RSSI = "RSSI";


		private MainActivity mainActivity;
		private int version;

		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE =
				"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null);";

		public DatabaseHelper(MainActivity mainActivity,
				int version) {

			super(mainActivity, DATABASE_NAME, null, version);

			this.mainActivity = mainActivity;
			this.version = version;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(FOUND_DEVICES_CREATE);
			showChangelog(10);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(	SQLiteDatabase db,
								int oldVersion,
								int newVersion) {
			
			
			showChangelog(oldVersion, newVersion, 0);

		}

		private void showChangelog() {

			showChangelog(0);
		}

		private void showChangelog(int limit) {

			showChangelog(0, 0, limit);
		}

		/**
	 * 
	 */
		private void showChangelog(	int oldVersion,
									int newVersion,
									int limit) {

			NetworkThread getChangelog = new NetworkThread(mainActivity, mainActivity.netMananger);

			mainActivity.authentification.setOnNetworkResultAvailableListener(new OnNetworkResultAvailableListener() {

				@Override
				public boolean onResult(int requestId,
										String resultString) {

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

							Dialog changelogDialog = new Dialog(mainActivity);
							ScrollView changeLogScrollView = new ScrollView(mainActivity);
							TextView changelogTextView = new TextView(mainActivity);
							changelogDialog.setTitle("Changelog");
							int padding =
									mainActivity.getResources().getDimensionPixelSize(R.dimen.padding_small);
							changelogTextView.setPadding(padding, padding, padding, padding);
							changelogDialog.addContentView(changeLogScrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
							changeLogScrollView.addView(changelogTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
							changelogTextView.setText(Html.fromHtml(resultString));
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
	}
}
