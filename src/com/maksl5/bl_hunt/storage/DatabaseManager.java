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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.custom_ui.FoundDevice;
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
	public static final int INDEX_BONUS = 6;

	/**
	 * 
	 */
	public DatabaseManager(BlueHunter app) {

		this.version = app.getVersionCode();
		this.bhApp = app;

		this.dbHelper = new DatabaseHelper(bhApp, version);
		this.db = dbHelper.getWritableDatabase();

	}

	public boolean addNewDevice(FoundDevice device) {

		long time = System.currentTimeMillis();

		ContentValues values = new ContentValues();

		if (device.checkNull() == 0) return false;

		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, device.getMacAddress());
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, device.getManufacturer());
		values.put(DatabaseHelper.COLUMN_NAME, device.getName());
		values.put(DatabaseHelper.COLUMN_RSSI, device.getRssi());
		values.put(DatabaseHelper.COLUMN_TIME, device.getTime());
		values.put(DatabaseHelper.COLUMN_BONUS, device.getBonus());

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {
			close();

			bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_ADD, device);

			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	private boolean addNewDeviceForIterate(	FoundDevice device,
											boolean close) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, device.getMacAddress());
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, device.getManufacturer());
		values.put(DatabaseHelper.COLUMN_NAME, device.getName());
		values.put(DatabaseHelper.COLUMN_RSSI, device.getRssi());
		values.put(DatabaseHelper.COLUMN_TIME, device.getTime());
		values.put(DatabaseHelper.COLUMN_BONUS, device.getBonus());

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

	public synchronized List<FoundDevice> getAllDevices() {

		List<FoundDevice> devices = new ArrayList<FoundDevice>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null, DatabaseHelper.COLUMN_TIME + " DESC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FoundDevice device = new FoundDevice();

			device.setMac(cursor.getString(1));
			device.setName(cursor.getString(2));
			device.setRssi(cursor.getString(3));
			device.setTime(cursor.getString(4));
			device.setManu(cursor.getString(5));
			device.setBonus(cursor.getString(6));

			devices.add(device);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return devices;
	}

	public synchronized List<FoundDevice> getDevices(	String where,
																String orderBy) {

		List<FoundDevice> devices = new ArrayList<FoundDevice>();

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, where, null, null, null, orderBy);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FoundDevice device = new FoundDevice();

			device.setMac(cursor.getString(1));
			device.setName(cursor.getString(2));
			device.setRssi(cursor.getString(3));
			device.setTime(cursor.getString(4));
			device.setManu(cursor.getString(5));
			device.setBonus(cursor.getString(6));

			devices.add(device);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return devices;
	}

	public synchronized int getDeviceNum() {

		return getDeviceNum(null);
	}

	public synchronized int getDeviceNum(String where) {

		int num =
				(int) DatabaseUtils.queryNumEntries(db, DatabaseHelper.FOUND_DEVICES_TABLE, where);
		close();

		return num;
	}

	public synchronized FoundDevice getDevice(	String where,
														String orderBy) {

		Cursor cursor =
				db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, where, null, null, null, orderBy);
		cursor.moveToFirst();

		FoundDevice device = new FoundDevice();

		while (!cursor.isAfterLast()) {
			
			device.setMac(cursor.getString(1));
			device.setName(cursor.getString(2));
			device.setRssi(cursor.getString(3));
			device.setTime(cursor.getString(4));
			device.setManu(cursor.getString(5));
			device.setBonus(cursor.getString(6));

			cursor.moveToNext();
		}

		cursor.close();
		close();
		return device;
	}

	public synchronized void addNameToDevice(	String macAddress,
												String name) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NAME, name);

		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });

		FoundDevice change = new FoundDevice();
		change.setMac(macAddress);
		change.setName(name);

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

	public synchronized void addBonusToDevices(	String macAddress,
												float bonus) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_BONUS, bonus);

		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });
		
		FoundDevice change = new FoundDevice();
		change.setMac(macAddress);
		change.setBonus(bonus);

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_CHANGE, change);

		close();
		updateModifiedTime(System.currentTimeMillis());
	}

	public boolean deleteDevice(String macAddress) {

		int result =
				db.delete(DatabaseHelper.FOUND_DEVICES_TABLE, DatabaseHelper.COLUMN_MAC_ADDRESS + " = ?", new String[] { macAddress });

		close();
		updateModifiedTime(System.currentTimeMillis());

		if (result == 0) return false;
		
		FoundDevice removeDevice = new FoundDevice();
		removeDevice.setMac(macAddress);

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_REMOVE, removeDevice);

		return true;
	}

	/**
	 * @param devices
	 */
	public void newSyncedDatabase(List<FoundDevice> devices) {

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.FOUND_DEVICES_TABLE);
		db.execSQL(DatabaseHelper.FOUND_DEVICES_CREATE);

		for (FoundDevice device : devices) {
			addNewDeviceForIterate(device, false);
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

		List<FoundDevice> allDevices = getAllDevices();

		dbHelper = new DatabaseHelper(bhApp, version);
		db = dbHelper.getWritableDatabase();

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

				for (FoundDevice device : allDevices) {

					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, device.getMacAddress());
					values.put(DatabaseHelper.COLUMN_MANUFACTURER, device.getManufacturer());
					values.put(DatabaseHelper.COLUMN_NAME, device.getName());
					values.put(DatabaseHelper.COLUMN_RSSI, device.getRssi());
					values.put(DatabaseHelper.COLUMN_TIME, device.getTime());
					values.put(DatabaseHelper.COLUMN_BONUS, device.getBonus());

					if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) == -1) {
						failureRows.add(allDevices.indexOf(device));
					}
				}

				for (String change : allChanges) {
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

	// LEADERBOARD CHANGE

	public void setLeaderboardChanges(HashMap<Integer, Integer> changes) {

		Set<Integer> keySet = changes.keySet();
		for (Integer uid : keySet) {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.COLUMN_UID, uid);
			values.put(DatabaseHelper.COLUMN_RANK, changes.get(uid));

			db.insert(DatabaseHelper.LEADERBOARD_CHANGES_TABLE, null, values);
		}

		close();

	}

	public void resetLeaderboardChanges() {

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.LEADERBOARD_CHANGES_TABLE);
		db.execSQL(DatabaseHelper.LEADERBOARD_CHANGES_CREATE);

		close();
	}

	public HashMap<Integer, Integer> getLeaderboardChanges() {

		HashMap<Integer, Integer> changes = new HashMap<Integer, Integer>();

		Cursor cursor =
				db.query(DatabaseHelper.LEADERBOARD_CHANGES_TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			changes.put(cursor.getInt(0), cursor.getInt(1));
			cursor.moveToNext();
		}

		cursor.close();
		close();

		return changes;
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
		public final static String LEADERBOARD_CHANGES_TABLE = "ldChanges";

		public final static String COLUMN_MAC_ADDRESS = "macAddress";
		public final static String COLUMN_NAME = "name";
		public final static String COLUMN_RSSI = "RSSI";
		public final static String COLUMN_TIME = "time";
		public final static String COLUMN_MANUFACTURER = "manufacturer";
		public final static String COLUMN_BONUS = "bonus";

		public final static String COLUMN_CHANGE = "change";

		public final static String COLUMN_UID = "uid";
		public final static String COLUMN_RANK = "rank";

		private BlueHunter bhApplication;
		private int version;

		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE =
				"Create Table " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Text, " + COLUMN_BONUS + " Real);";

		public final static String CHANGES_SYNC_CREATE =
				"Create Table " + CHANGES_SYNC_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_CHANGE + " Text Not Null);";

		public final static String LEADERBOARD_CHANGES_CREATE =
				"Create Table " + LEADERBOARD_CHANGES_TABLE + " (uid Integer Not Null, " + COLUMN_RANK + " Integer Not Null);";

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

			bhApp.mainActivity.oldVersion = oldVersion;
			bhApp.mainActivity.newVersion = newVersion;

			if (oldVersion < 499) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_TIME + " Integer;");
			}

			if (oldVersion < 566) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_MANUFACTURER + " Text;");
			}

			if (oldVersion < 916) {
				db.execSQL(CHANGES_SYNC_CREATE);
			}

			if (oldVersion < 1065) {
				db.execSQL("Alter Table " + FOUND_DEVICES_TABLE + " Add Column " + COLUMN_BONUS + " Real;");

			}

			bhApp.authentification.showChangelog(oldVersion, newVersion, 0);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
		 */
		@Override
		public void onOpen(SQLiteDatabase db) {

			final String FOUND_DEVICES_CREATE_IF_NOT_EXISTS =
					"Create Table If Not Exists " + FOUND_DEVICES_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS + " Text Not Null, " + COLUMN_NAME + " Text, " + COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Text);";

			final String CHANGES_SYNC_CREATE_IF_NOT_EXISTS =
					"Create Table If Not Exists " + CHANGES_SYNC_TABLE + " (_id Integer Primary Key Autoincrement, " + COLUMN_CHANGE + " Text Not Null);";

			final String LEADERBOARD_CHANGES_CREATE_IF_NOT_EXISTS =
					"Create Table If Not Exists " + LEADERBOARD_CHANGES_TABLE + " (uid Integer Not Null, " + COLUMN_RANK + " Integer Not Null);";

			db.execSQL(FOUND_DEVICES_CREATE_IF_NOT_EXISTS);
			db.execSQL(CHANGES_SYNC_CREATE_IF_NOT_EXISTS);
			db.execSQL(LEADERBOARD_CHANGES_CREATE_IF_NOT_EXISTS);

		}

	}
}
