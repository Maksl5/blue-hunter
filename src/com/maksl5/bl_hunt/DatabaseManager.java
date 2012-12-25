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

import android.R.string;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
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
		values.put(DatabaseHelper.COLUMN_TIME, System.currentTimeMillis());

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
		values.put(DatabaseHelper.COLUMN_TIME, System.currentTimeMillis());

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
		values.put(DatabaseHelper.COLUMN_TIME, System.currentTimeMillis());
	
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

			tempHashMap.put(DatabaseHelper.COLUMN_MAC_ADDRESS, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAC_ADDRESS)));
			tempHashMap.put(DatabaseHelper.COLUMN_NAME, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
			tempHashMap.put(DatabaseHelper.COLUMN_RSSI, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RSSI)));
			tempHashMap.put(DatabaseHelper.COLUMN_TIME, cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)));
			

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
		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(mainActivity);
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
		public final static String COLUMN_TIME = "time";


		private MainActivity mainActivity;
		private int version;

		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE =
				"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer);";

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
			mainActivity.authentification.showChangelog(mainActivity, 10);

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
			
			
			if(oldVersion < 499) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_TIME + " Integer;");
			}
			
			
			mainActivity.authentification.showChangelog(mainActivity, oldVersion, newVersion, 0);

		}

	}
}
