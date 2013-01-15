/**
 *  DatabaseHelper.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.storage;



import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.maksl5.bl_hunt.FragmentLayoutManager;
import com.maksl5.bl_hunt.activity.MainActivity;



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
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
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
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
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
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
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

	public synchronized List<HashMap<String, String>> getAllDevices() {

		List<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();

			tempHashMap.put(DatabaseHelper.COLUMN_MAC_ADDRESS, cursor.getString(1));
			tempHashMap.put(DatabaseHelper.COLUMN_NAME, cursor.getString(2));
			tempHashMap.put(DatabaseHelper.COLUMN_RSSI, cursor.getString(3));
			tempHashMap.put(DatabaseHelper.COLUMN_TIME, cursor.getString(4));
			tempHashMap.put(DatabaseHelper.COLUMN_MANUFACTURER, cursor.getString(5));

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

	public synchronized void addManufacturerToDevice(String macAddress,
										String manufacturer) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, manufacturer);
		
		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });
		close();
	}
	
	public int rebuildDatabase() {
		List<HashMap<String, String>> allDevices = getAllDevices();
		
		if(mainActivity.deleteDatabase(DatabaseHelper.DATABASE_NAME)) {
			
			dbHelper = new DatabaseHelper(mainActivity, version);
			db = dbHelper.getWritableDatabase();
			
			List<Integer> failureRows = new ArrayList<Integer>();
			
			for (HashMap<String, String> device : allDevices) {
				
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, device.get(DatabaseHelper.COLUMN_MAC_ADDRESS));
				values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(device.get(DatabaseHelper.COLUMN_MAC_ADDRESS)));
				values.put(DatabaseHelper.COLUMN_NAME, device.get(DatabaseHelper.COLUMN_NAME));
				values.put(DatabaseHelper.COLUMN_RSSI, device.get(DatabaseHelper.COLUMN_RSSI));
				values.put(DatabaseHelper.COLUMN_TIME, device.get(DatabaseHelper.COLUMN_TIME));

				if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) == -1) {
					failureRows.add(allDevices.indexOf(device));
				}
			}
			
			close();
			
			if(failureRows.size() == 0) {
				return 0;
			}else {
				return -failureRows.size();
			}
			
		} else {
			return 1001;
		}
		
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
		public final static String COLUMN_MANUFACTURER = "manufacturer";

		private MainActivity mainActivity;
		private int version;

		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE =
				"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Text);";

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

			if (oldVersion < 499) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_TIME + " Integer;");
			}

			if (oldVersion < 566) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_MANUFACTURER + " Text;");
			}

			mainActivity.authentification.showChangelog(mainActivity, oldVersion, newVersion, 0);

		}

	}
}
