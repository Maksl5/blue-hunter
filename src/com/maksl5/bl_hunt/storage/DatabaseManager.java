/**
 *  DatabaseHelper.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.storage;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.custom_ui.FragmentLayoutManager;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;



public class DatabaseManager {

	private int version;
	private BlueHunter bhApp;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	public static final int INDEX_MAC_ADDRESS = 1;
	public static final int INDEX_NAME = 2;
	public static final int INDEX_RSSI = 3;
	public static final int INDEX_TIME = 4;
	public static final int INDEX_MANUFACTURER = 5;

	/**
	 * 
	 */
	public DatabaseManager(BlueHunter app,
			int version) {

		this.version = version;
		this.bhApp = app;

		this.dbHelper = new DatabaseHelper(bhApp, version);
		this.db = dbHelper.getWritableDatabase();

	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress) {

		long time = System.currentTimeMillis();

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
		values.put(DatabaseHelper.COLUMN_TIME, time);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();

			SparseArray<String> changes = new SparseArray<String>();
			changes.put(INDEX_MAC_ADDRESS, macAddress);
			changes.put(INDEX_TIME, String.valueOf(time));

			bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_ADD, changes);

			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress,
								short RSSI) {

		long time = System.currentTimeMillis();

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
		values.put(DatabaseHelper.COLUMN_RSSI, RSSI);
		values.put(DatabaseHelper.COLUMN_TIME, time);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();

			SparseArray<String> changes = new SparseArray<String>();
			changes.put(INDEX_MAC_ADDRESS, macAddress);
			changes.put(INDEX_TIME, String.valueOf(time));
			changes.put(INDEX_RSSI, String.valueOf(RSSI));

			bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_ADD, changes);

			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	/**
	 * 
	 */
	public boolean addNewDevice(String macAddress,
								String name,
								short RSSI) {

		long time = System.currentTimeMillis();

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
		values.put(DatabaseHelper.COLUMN_NAME, name);
		values.put(DatabaseHelper.COLUMN_RSSI, RSSI);
		values.put(DatabaseHelper.COLUMN_TIME, time);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();

			SparseArray<String> changes = new SparseArray<String>();
			changes.put(INDEX_MAC_ADDRESS, macAddress);
			changes.put(INDEX_NAME, name);
			changes.put(INDEX_TIME, String.valueOf(time));
			changes.put(INDEX_RSSI, String.valueOf(RSSI));

			bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_ADD, changes);

			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	private boolean addNewDeviceForIterate(	String macAddress,
											String name,
											short RSSI,
											long time,
											boolean close) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, macAddress);
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(macAddress));
		values.put(DatabaseHelper.COLUMN_NAME, name);
		values.put(DatabaseHelper.COLUMN_RSSI, RSSI);
		values.put(DatabaseHelper.COLUMN_TIME, time);

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			if (close) close();
			return true;
		}
		else {
			if (close) close();
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

	public synchronized List<SparseArray<String>> getAllDevices() {

		List<SparseArray<String>> devices = new ArrayList<SparseArray<String>>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null, DatabaseHelper.COLUMN_TIME + " DESC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SparseArray<String> device = new SparseArray<String>();

			device.put(INDEX_MAC_ADDRESS, cursor.getString(1));
			device.put(INDEX_NAME, cursor.getString(2));
			device.put(INDEX_RSSI, cursor.getString(3));
			device.put(INDEX_TIME, cursor.getString(4));
			device.put(INDEX_MANUFACTURER, cursor.getString(5));

			devices.add(device);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return devices;
	}

	public synchronized int getDeviceNum() {

		int num = (int) DatabaseUtils.queryNumEntries(db, DatabaseHelper.FOUND_DEVICES_TABLE);
		close();

		return num;
	}

	public void addNameToDevice(String macAddress,
								String name) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NAME, name);

		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });

		SparseArray<String> change = new SparseArray<String>();
		change.put(INDEX_NAME, name);

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_CHANGE, change);

		FragmentLayoutManager.FoundDevicesLayout.refreshFoundDevicesList(bhApp);
		close();
		updateModifiedTime(System.currentTimeMillis());

	}

	public synchronized void addManufacturerToDevice(	String macAddress,
														String manufacturer) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, manufacturer);

		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });
		close();
		updateModifiedTime(System.currentTimeMillis());
	}

	public boolean deleteDevice(String macAddress) {

		int result =
				db.delete(DatabaseHelper.FOUND_DEVICES_TABLE, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });

		close();
		updateModifiedTime(System.currentTimeMillis());

		if (result == 0) return false;

		SparseArray<String> change = new SparseArray<String>();
		change.put(INDEX_MAC_ADDRESS, macAddress);

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_REMOVE, change);

		return true;
	}

	/**
	 * @param devices
	 */
	public void newSyncedDatabase(List<SparseArray<String>> devices) {

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.FOUND_DEVICES_TABLE);
		db.execSQL(DatabaseHelper.FOUND_DEVICES_CREATE);

		for (SparseArray<String> sparseArray : devices) {
			addNewDeviceForIterate(sparseArray.get(INDEX_MAC_ADDRESS), sparseArray.get(INDEX_NAME), Short.parseShort(sparseArray.get(INDEX_RSSI)), Long.parseLong(sparseArray.get(INDEX_TIME)), false);
		}
		updateModifiedTime(System.currentTimeMillis());
		close();
	}

	public boolean addChange(String changeToSync) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_CHANGE, changeToSync);

		if (db.insert(DatabaseHelper.CHANGES_SYNC_TABLE, null, values) != -1) {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	public void addChanges(List<String> changesToSync) {

		for (String change : changesToSync) {

			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.COLUMN_CHANGE, change);

			db.insert(DatabaseHelper.CHANGES_SYNC_TABLE, null, values);
			updateModifiedTime(System.currentTimeMillis());
		}

		close();
	}

	public List<String> getAllChanges() {

		List<String> changes = new ArrayList<String>();

		Cursor cursor =
				db.query(DatabaseHelper.CHANGES_SYNC_TABLE, new String[] { DatabaseHelper.COLUMN_CHANGE }, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			changes.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CHANGE)));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return changes;
	}

	public void resetChanges() {

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.CHANGES_SYNC_TABLE);
		db.execSQL(DatabaseHelper.CHANGES_SYNC_CREATE);

		close();

	}

	public int rebuildDatabase() {

		List<SparseArray<String>> allDevices = getAllDevices();
		List<String> allChanges = getAllChanges();

		File dbFile = new File(db.getPath());
		File backup = new File(dbFile.getPath() + ".bak");
		try {
			copy(dbFile, backup);
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			if (bhApp.deleteDatabase(DatabaseHelper.DATABASE_NAME)) {

				dbHelper = new DatabaseHelper(bhApp, version);
				db = dbHelper.getWritableDatabase();

				List<Integer> failureRows = new ArrayList<Integer>();

				for (SparseArray<String> device : allDevices) {

					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, device.get(INDEX_MAC_ADDRESS));
					values.put(DatabaseHelper.COLUMN_MANUFACTURER, MacAddressAllocations.getManufacturer(device.get(INDEX_MAC_ADDRESS)));
					values.put(DatabaseHelper.COLUMN_NAME, device.get(INDEX_NAME));
					values.put(DatabaseHelper.COLUMN_RSSI, device.get(INDEX_RSSI));
					values.put(DatabaseHelper.COLUMN_TIME, device.get(INDEX_TIME));

					if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) == -1) {
						failureRows.add(allDevices.indexOf(device));
					}
				}
				
				for(String change : allChanges) {
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.COLUMN_CHANGE, change);
					
					db.insert(DatabaseHelper.CHANGES_SYNC_TABLE, null, values);
				}
				

				close();
				updateModifiedTime(System.currentTimeMillis());

				if (failureRows.size() == 0) {
					return 0;
				}
				else {
					return -failureRows.size();
				}

			}
			else {
				return 1001;
			}

		}
		catch (Exception e) {

			bhApp.deleteDatabase(DatabaseHelper.DATABASE_NAME);
			dbFile.delete();
			backup.renameTo(dbFile);
			updateModifiedTime(System.currentTimeMillis());

			return 1002;
		}

	}

	public long getLastModifiedTime() {

		long time = PreferenceManager.getPref(bhApp, "dbLastModified", (long) 0);
		Log.d("SMBD", "Modified time: " + time);
		close();
		return time;
	}

	private void updateModifiedTime(long time) {

		PreferenceManager.setPref(bhApp, "dbLastModified", time);
	}

	public void close() {

		db.close();
		dbHelper.close();
	}

	private void copy(	File src,
						File dst) throws IOException {

		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * @author Maksl5[Markus Bensing]
	 * 
	 */
	public class DatabaseHelper extends SQLiteOpenHelper {

		public final static String DATABASE_NAME = "foundDevicesDataStorage.db";
		public final static String FOUND_DEVICES_TABLE = "foundDevices";
		public final static String CHANGES_SYNC_TABLE = "changesSync";

		public final static String COLUMN_MAC_ADDRESS = "macAddress";
		public final static String COLUMN_NAME = "name";
		public final static String COLUMN_RSSI = "RSSI";
		public final static String COLUMN_TIME = "time";
		public final static String COLUMN_MANUFACTURER = "manufacturer";

		public final static String COLUMN_CHANGE = "change";

		private BlueHunter bhApplication;
		private int version;

		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE =
				"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Text);";

		public final static String CHANGES_SYNC_CREATE =
				"Create Table " + CHANGES_SYNC_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_CHANGE + " Text Not Null);";

		public DatabaseHelper(BlueHunter app,
				int version) {

			super(app.mainActivity, DATABASE_NAME, null, version);

			this.bhApplication = app;
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
			db.execSQL(CHANGES_SYNC_CREATE);
			if (bhApplication != null)
				if (bhApplication.authentification != null)
					bhApplication.authentification.showChangelog(10);

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

			if (oldVersion < 916) {
				db.execSQL(CHANGES_SYNC_CREATE);
			}

			bhApp.authentification.showChangelog(oldVersion, newVersion, 0);

		}

		/* (non-Javadoc)
		 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
		 */
		@Override
		public void onOpen(SQLiteDatabase db) {

			final String FOUND_DEVICES_CREATE_IF_NOT_EXISTS =
					"Create Table If Not Exists " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Text);";

			final String CHANGES_SYNC_CREATE_IF_NOT_EXISTS =
					"Create Table If Not Exists " + CHANGES_SYNC_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_CHANGE + " Text Not Null);";

			db.execSQL(FOUND_DEVICES_CREATE_IF_NOT_EXISTS);
			db.execSQL(CHANGES_SYNC_CREATE_IF_NOT_EXISTS);
			
		}

	}
}
