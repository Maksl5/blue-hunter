/**
 *  DatabaseHelper.java in com.maksl5.bl_hunt
 *  Â© Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.custom_ui.fragment.AchievementsLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;
import com.maksl5.bl_hunt.custom_ui.fragment.FoundDevicesLayout;
import com.maksl5.bl_hunt.net.SynchronizeFoundDevices;
import com.maksl5.bl_hunt.util.FoundDevice;
import com.maksl5.bl_hunt.util.MacAddress;

import android.R.bool;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

public class DatabaseManager {

	private int version;
	private BlueHunter bhApp;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	private static ArrayList<FoundDevice> temporaryAsyncList;
	private static ArrayList<FoundDevice> cachedList;

	private static LoadAllDevicesThread loadAllDevicesTask;

	public static final int INDEX_MAC_ADDRESS = 1;
	public static final int INDEX_NAME = 2;
	public static final int INDEX_RSSI = 3;
	public static final int INDEX_TIME = 4;
	public static final int INDEX_MANUFACTURER = 5;
	public static final int INDEX_BONUS = 6;

	/**
	 * @throws InterruptedException
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

		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_A, device.getMacAddress().getA());
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_B, device.getMacAddress().getB());
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_C, device.getMacAddress().getC());
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_D, device.getMacAddress().getD());
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_E, device.getMacAddress().getE());
		values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_F, device.getMacAddress().getF());

		values.put(DatabaseHelper.COLUMN_MANUFACTURER, device.getManufacturer());
		values.put(DatabaseHelper.COLUMN_NAME, device.getName());
		values.put(DatabaseHelper.COLUMN_RSSI, device.getRssi());
		values.put(DatabaseHelper.COLUMN_TIME, device.getTime());
		values.put(DatabaseHelper.COLUMN_BONUS, device.getBoost());

		if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) != -1) {

			if (cachedList != null) {
				cachedList.add(0, device);
				close();
			}
			else {
				loadAllDevices(true);
			}

			bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_ADD, device, false);

			updateModifiedTime(System.currentTimeMillis());
			return true;
		}
		else {
			close();
			updateModifiedTime(System.currentTimeMillis());
			return false;
		}

	}

	private static boolean addNewDeviceForIterate(SQLiteStatement sqLiteStatement, FoundDevice device) {

		sqLiteStatement.bindLong(1, device.getMacAddress().getA());
		sqLiteStatement.bindLong(2, device.getMacAddress().getB());
		sqLiteStatement.bindLong(3, device.getMacAddress().getC());
		sqLiteStatement.bindLong(4, device.getMacAddress().getD());
		sqLiteStatement.bindLong(5, device.getMacAddress().getE());
		sqLiteStatement.bindLong(6, device.getMacAddress().getF());

		sqLiteStatement.bindLong(7, device.getManufacturer());

		if (device.getName() != null) sqLiteStatement.bindString(8, device.getName());

		sqLiteStatement.bindLong(9, device.getRssi());
		sqLiteStatement.bindLong(10, device.getTime());
		sqLiteStatement.bindDouble(11, device.getBoost());

		long entry = sqLiteStatement.executeInsert();
		sqLiteStatement.clearBindings();

		if (entry != -1) {
			return true;
		}
		else {
			return false;
		}

	}

	public synchronized void loadAllDevices(boolean closeAfter) {

		if (cachedList == null || cachedList.size() == 0) {

			Log.d("loadAllDevices", "running = " + ((loadAllDevicesTask != null) ? loadAllDevicesTask.running : "false"));

			if (loadAllDevicesTask == null || !loadAllDevicesTask.running) {

				loadAllDevicesTask = new LoadAllDevicesThread();
				loadAllDevicesTask.execute(closeAfter);
			}

		}

		// check ob schon läuft nicht vergessen.

		// closeAfter nicht vergessen!

	}

	public static synchronized ArrayList<FoundDevice> getCachedList() {

		if (cachedList != null) {
			return cachedList;
		}
		else {
			return null;
		}

	}

	public static synchronized ArrayList<FoundDevice> getProgressList() {

		if (temporaryAsyncList != null) {
			return temporaryAsyncList;
		}
		else {
			return null;
		}

	}

	public synchronized int getDeviceNum() {

		return getDeviceNum(null);
	}

	public synchronized int getDeviceNum(String where) {

		if (where == null && cachedList != null) {
			close();
			return cachedList.size();
		}

		int num = (int) DatabaseUtils.queryNumEntries(db, DatabaseHelper.FOUND_DEVICES_TABLE, where);
		close();

		return num;
	}

	public synchronized void addNameToDevice(MacAddress macAddress, String name) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NAME, name);

		// @formatter:off
		
		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, 
																	DatabaseHelper.COLUMN_MAC_ADDRESS_A + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_B + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_C + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_D + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_E + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_F + " = ?"
															, 
																	new String[] { 	"" + macAddress.getA(),
																					"" + macAddress.getB(),
																					"" + macAddress.getC(),
																					"" + macAddress.getD(),
																					"" + macAddress.getE(),
																					"" + macAddress.getF()
															});

		// @formatter:on

		FoundDevice change = new FoundDevice();
		change.setMac(macAddress);
		change.setName(name);

		// Caching

		if (cachedList != null) {
			int index = cachedList.indexOf(change);
			FoundDevice foundDevice = cachedList.get(index);
			foundDevice.setName(name);

			cachedList.set(index, foundDevice);
			close();
		}
		else {
			loadAllDevices(true);
		}

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_CHANGE, change, false);

		FoundDevicesLayout.refreshFoundDevicesList(bhApp, false);

		updateModifiedTime(System.currentTimeMillis());

	}

	public synchronized void addManufacturerToDevice(MacAddress macAddress, int manufacturer) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_MANUFACTURER, manufacturer);

		// @formatter:off
		
		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, 
																	DatabaseHelper.COLUMN_MAC_ADDRESS_A + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_B + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_C + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_D + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_E + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_F + " = ?"
															, 
																	new String[] { 	"" + macAddress.getA(),
																					"" + macAddress.getB(),
																					"" + macAddress.getC(),
																					"" + macAddress.getD(),
																					"" + macAddress.getE(),
																					"" + macAddress.getF()
															});

		// @formatter:on

		FoundDevice change = new FoundDevice();
		change.setMac(macAddress);

		// Caching

		if (cachedList != null) {
			int index = cachedList.indexOf(change);
			FoundDevice foundDevice = cachedList.get(index);
			foundDevice.setManu(manufacturer);;

			cachedList.set(index, foundDevice);
			close();
		}
		else {
			loadAllDevices(true);
		}

		updateModifiedTime(System.currentTimeMillis());
	}

	public synchronized void addBoostToDevices(MacAddress macAddress, float bonus) {

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_BONUS, bonus);

		// @formatter:off
		
		db.update(DatabaseHelper.FOUND_DEVICES_TABLE, values, 
																	DatabaseHelper.COLUMN_MAC_ADDRESS_A + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_B + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_C + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_D + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_E + " = ? AND " +
																	DatabaseHelper.COLUMN_MAC_ADDRESS_F + " = ?"
															, 
																	new String[] { 	"" + macAddress.getA(),
																					"" + macAddress.getB(),
																					"" + macAddress.getC(),
																					"" + macAddress.getD(),
																					"" + macAddress.getE(),
																					"" + macAddress.getF()
															});

		// @formatter:on

		FoundDevice change = new FoundDevice();
		change.setMac(macAddress);
		change.setBoost(bonus);

		// Caching

		if (cachedList != null) {
			int index = cachedList.indexOf(change);
			FoundDevice foundDevice = cachedList.get(index);
			foundDevice.setBoost(bonus);

			cachedList.set(index, foundDevice);
			close();
		}
		else {
			loadAllDevices(true);
		}

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_CHANGE, change, false);

		updateModifiedTime(System.currentTimeMillis());
	}

	public boolean deleteDevice(MacAddress macAddress) {

		// @formatter:off
		
		int result = db.delete(DatabaseHelper.FOUND_DEVICES_TABLE	, 
																			DatabaseHelper.COLUMN_MAC_ADDRESS_A + " = ? AND " +
																			DatabaseHelper.COLUMN_MAC_ADDRESS_B + " = ? AND " +
																			DatabaseHelper.COLUMN_MAC_ADDRESS_C + " = ? AND " +
																			DatabaseHelper.COLUMN_MAC_ADDRESS_D + " = ? AND " +
																			DatabaseHelper.COLUMN_MAC_ADDRESS_E + " = ? AND " +
																			DatabaseHelper.COLUMN_MAC_ADDRESS_F + " = ?"
																	, 
																			new String[] { 	"" + macAddress.getA(),
																							"" + macAddress.getB(),
																							"" + macAddress.getC(),
																							"" + macAddress.getD(),
																							"" + macAddress.getE(),
																							"" + macAddress.getF()
																	});
		
		// @formatter:on

		FoundDevice removeDevice = new FoundDevice();
		removeDevice.setMac(macAddress);

		if (cachedList != null) {
			int index = cachedList.indexOf(removeDevice);

			cachedList.remove(index);
			close();
		}
		else {
			loadAllDevices(true);
		}

		updateModifiedTime(System.currentTimeMillis());

		if (result == 0) return false;

		bhApp.synchronizeFoundDevices.addNewChange(SynchronizeFoundDevices.MODE_REMOVE, removeDevice, true);

		return true;
	}

	/**
	 * @param devices
	 */
	public void newSyncedDatabase(List<FoundDevice> devices) {

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.FOUND_DEVICES_TABLE);
		db.execSQL(DatabaseHelper.FOUND_DEVICES_CREATE);

		int number = 1;

		long syncA = System.currentTimeMillis();

		String sql = "insert into " + DatabaseHelper.FOUND_DEVICES_TABLE + " (" + DatabaseHelper.COLUMN_MAC_ADDRESS_A + ", "
				+ DatabaseHelper.COLUMN_MAC_ADDRESS_B + ", " + DatabaseHelper.COLUMN_MAC_ADDRESS_C + ", "
				+ DatabaseHelper.COLUMN_MAC_ADDRESS_D + ", " + DatabaseHelper.COLUMN_MAC_ADDRESS_E + ", "
				+ DatabaseHelper.COLUMN_MAC_ADDRESS_F + ", "

				+ DatabaseHelper.COLUMN_MANUFACTURER + ", " + DatabaseHelper.COLUMN_NAME + ", " + DatabaseHelper.COLUMN_RSSI + ", "
				+ DatabaseHelper.COLUMN_TIME + ", " + DatabaseHelper.COLUMN_BONUS + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		db.beginTransaction();
		SQLiteStatement sqLiteStatement = db.compileStatement(sql);

		for (FoundDevice device : devices) {
			addNewDeviceForIterate(sqLiteStatement, device);

			Log.d("newSyncedDatabase number", "" + number);
			number++;

		}

		db.setTransactionSuccessful();
		db.endTransaction();

		long syncB = System.currentTimeMillis();

		Log.d("newSyncedDatabase TIME", "" + (syncB - syncA) + "ms");

		cachedList = null;
		loadAllDevices(true);

		updateModifiedTime(System.currentTimeMillis());
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

		Cursor cursor = db.query(DatabaseHelper.CHANGES_SYNC_TABLE, new String[] {
				DatabaseHelper.COLUMN_CHANGE }, null, null, null, null, null);

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

			if (cachedList == null) {
				throw new Exception("cachedList is null.");
			}

			if (bhApp.deleteDatabase(DatabaseHelper.DATABASE_NAME)) {

				dbHelper = new DatabaseHelper(bhApp, version);
				db = dbHelper.getWritableDatabase();

				List<Integer> failureRows = new ArrayList<Integer>();

				for (FoundDevice device : cachedList) {

					ContentValues values = new ContentValues();

					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_A, device.getMacAddress().getA());
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_B, device.getMacAddress().getB());
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_C, device.getMacAddress().getC());
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_D, device.getMacAddress().getD());
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_E, device.getMacAddress().getE());
					values.put(DatabaseHelper.COLUMN_MAC_ADDRESS_F, device.getMacAddress().getF());

					values.put(DatabaseHelper.COLUMN_MANUFACTURER, device.getManufacturer());
					values.put(DatabaseHelper.COLUMN_NAME, device.getName());
					values.put(DatabaseHelper.COLUMN_RSSI, device.getRssi());
					values.put(DatabaseHelper.COLUMN_TIME, device.getTime());
					values.put(DatabaseHelper.COLUMN_BONUS, device.getBoost());

					if (db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values) == -1) {
						failureRows.add(cachedList.indexOf(device));
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

		Cursor cursor = db.query(DatabaseHelper.LEADERBOARD_CHANGES_TABLE, null, null, null, null, null, null);
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

	private void copy(File src, File dst) throws IOException {

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

		public final static String COLUMN_MAC_ADDRESS_A = "macA";
		public final static String COLUMN_MAC_ADDRESS_B = "macB";
		public final static String COLUMN_MAC_ADDRESS_C = "macC";
		public final static String COLUMN_MAC_ADDRESS_D = "macD";
		public final static String COLUMN_MAC_ADDRESS_E = "macE";
		public final static String COLUMN_MAC_ADDRESS_F = "macF";

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

		// @formatter:off
		// CREATE DECLARATION
		public final static String FOUND_DEVICES_CREATE = 
				"Create Table " + FOUND_DEVICES_TABLE +
				" (_id Integer Primary Key Autoincrement, " + 
				COLUMN_MAC_ADDRESS_A + " Integer Not Null, " + 
				COLUMN_MAC_ADDRESS_B + " Integer Not Null, " + 
				COLUMN_MAC_ADDRESS_C + " Integer Not Null, " + 
				COLUMN_MAC_ADDRESS_D + " Integer Not Null, " + 
				COLUMN_MAC_ADDRESS_E + " Integer Not Null, " + 
				COLUMN_MAC_ADDRESS_F + " Integer Not Null, " + 
				COLUMN_NAME + " Text, " + 
				COLUMN_RSSI + " Integer Not Null, "	+ 
				COLUMN_TIME + " Integer, " + 
				COLUMN_MANUFACTURER + " Integer, " +
				COLUMN_BONUS + " Real);";

		public final static String CHANGES_SYNC_CREATE = "Create Table " + CHANGES_SYNC_TABLE + " (_id Integer Primary Key Autoincrement, "
				+ COLUMN_CHANGE + " Text Not Null);";

		public final static String LEADERBOARD_CHANGES_CREATE = "Create Table " + LEADERBOARD_CHANGES_TABLE + " (uid Integer Not Null, "
				+ COLUMN_RANK + " Integer Not Null);";
		
		// @formatter:on

		public DatabaseHelper(BlueHunter app, int version) {

			super(app.mainActivity, DATABASE_NAME, null, version);

			this.bhApplication = app;
			this.version = version;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
		 * .sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(FOUND_DEVICES_CREATE);
			db.execSQL(CHANGES_SYNC_CREATE);
			if (bhApplication != null) if (bhApplication.authentification != null) bhApplication.authentification.showChangelog(10);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			bhApp.mainActivity.oldVersion = oldVersion;
			bhApp.mainActivity.newVersion = newVersion;

			PreferenceManager.setPref(bhApp, "requireManuCheck", true);

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

			if (oldVersion < 1419) {
				List<FoundDevice> devices = new ArrayList<FoundDevice>();

				Cursor cursor = db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null,
						DatabaseHelper.COLUMN_TIME + " DESC");
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					FoundDevice device = new FoundDevice();

					device.setMac(cursor.getString(1));
					device.setName(cursor.getString(2));
					device.setRssi(cursor.getShort(3));
					device.setTime(cursor.getLong(4));
					device.setManu(-1);
					device.setBoost(cursor.getFloat(6));

					devices.add(device);
					cursor.moveToNext();

				}

				cursor.close();

				db.execSQL("Drop Table If Exists " + FOUND_DEVICES_TABLE + ";");
				db.execSQL(FOUND_DEVICES_CREATE);

				for (FoundDevice foundDevice : devices) {
					ContentValues values = new ContentValues();

					if (foundDevice.checkNull() != 0) {

						values.put(DatabaseHelper.COLUMN_MAC_ADDRESS, foundDevice.getMacAddressString());
						values.put(DatabaseHelper.COLUMN_MANUFACTURER, foundDevice.getManufacturer());
						values.put(DatabaseHelper.COLUMN_NAME, foundDevice.getName());
						values.put(DatabaseHelper.COLUMN_RSSI, foundDevice.getRssi());
						values.put(DatabaseHelper.COLUMN_TIME, foundDevice.getTime());
						values.put(DatabaseHelper.COLUMN_BONUS, foundDevice.getBoost());

						db.insert(DatabaseHelper.FOUND_DEVICES_TABLE, null, values);

						updateModifiedTime(System.currentTimeMillis());

					}
				}

			}

			if (oldVersion < 1562) {

				PreferenceManager.setPref(bhApp, "pref_enableBackground", true);

				try {
					bhApp.mainActivity.getWindow().setBackgroundDrawableResource(R.drawable.bg_main);
				}
				catch (Exception e) {
					PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
					bhApp.mainActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
				}
				catch (OutOfMemoryError e) {
					PreferenceManager.setPref(bhApp, "pref_enableBackground", false);
					bhApp.mainActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
				}

			}

			if (oldVersion < 1687) {

				Toast.makeText(bhApplication,
						"Upgrading database structure. This may take several minutes for found device values over 10000. Don't force close! Otherwise data is lost!",
						Toast.LENGTH_LONG).show();

				List<FoundDevice> devices = new ArrayList<FoundDevice>();

				Cursor cursor = db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null,
						DatabaseHelper.COLUMN_TIME + " DESC");
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					FoundDevice device = new FoundDevice();

					device.setMac(cursor.getString(1));
					device.setName(cursor.getString(2));
					device.setRssi(cursor.getShort(3));
					device.setTime(cursor.getLong(4));
					device.setManu(cursor.getInt(5));
					device.setBoost(cursor.getFloat(6));

					devices.add(device);
					cursor.moveToNext();

				}

				cursor.close();

				db.execSQL("Drop Table If Exists " + FOUND_DEVICES_TABLE + ";");
				db.execSQL(FOUND_DEVICES_CREATE);

				String sql = "insert into " + DatabaseHelper.FOUND_DEVICES_TABLE + " (" + DatabaseHelper.COLUMN_MAC_ADDRESS_A + ", "
						+ DatabaseHelper.COLUMN_MAC_ADDRESS_B + ", " + DatabaseHelper.COLUMN_MAC_ADDRESS_C + ", "
						+ DatabaseHelper.COLUMN_MAC_ADDRESS_D + ", " + DatabaseHelper.COLUMN_MAC_ADDRESS_E + ", "
						+ DatabaseHelper.COLUMN_MAC_ADDRESS_F + ", "

						+ DatabaseHelper.COLUMN_MANUFACTURER + ", " + DatabaseHelper.COLUMN_NAME + ", " + DatabaseHelper.COLUMN_RSSI + ", "
						+ DatabaseHelper.COLUMN_TIME + ", " + DatabaseHelper.COLUMN_BONUS + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

				db.beginTransaction();
				SQLiteStatement sqLiteStatement = db.compileStatement(sql);

				for (FoundDevice device : devices) {
					addNewDeviceForIterate(sqLiteStatement, device);

				}

				db.setTransactionSuccessful();
				db.endTransaction();

			}

			bhApp.authentification.showChangelog(oldVersion, newVersion, 0);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.
		 * sqlite.SQLiteDatabase)
		 */
		@Override
		public void onOpen(SQLiteDatabase db) {

			final String FOUND_DEVICES_CREATE_IF_NOT_EXISTS = "Create Table If Not Exists " + FOUND_DEVICES_TABLE
					+ " (_id Integer Primary Key Autoincrement, " + COLUMN_MAC_ADDRESS_A + " Integer Not Null, " + COLUMN_MAC_ADDRESS_B
					+ " Integer Not Null, " + COLUMN_MAC_ADDRESS_C + " Integer Not Null, " + COLUMN_MAC_ADDRESS_D + " Integer Not Null, "
					+ COLUMN_MAC_ADDRESS_E + " Integer Not Null, " + COLUMN_MAC_ADDRESS_F + " Integer Not Null, " + COLUMN_NAME + " Text, "
					+ COLUMN_RSSI + " Integer Not Null, " + COLUMN_TIME + " Integer, " + COLUMN_MANUFACTURER + " Integer, " + COLUMN_BONUS
					+ " Real);";

			final String CHANGES_SYNC_CREATE_IF_NOT_EXISTS = "Create Table If Not Exists " + CHANGES_SYNC_TABLE
					+ " (_id Integer Primary Key Autoincrement, " + COLUMN_CHANGE + " Text Not Null);";

			final String LEADERBOARD_CHANGES_CREATE_IF_NOT_EXISTS = "Create Table If Not Exists " + LEADERBOARD_CHANGES_TABLE
					+ " (uid Integer Not Null, " + COLUMN_RANK + " Integer Not Null);";

			db.execSQL(FOUND_DEVICES_CREATE_IF_NOT_EXISTS);
			db.execSQL(CHANGES_SYNC_CREATE_IF_NOT_EXISTS);
			db.execSQL(LEADERBOARD_CHANGES_CREATE_IF_NOT_EXISTS);

		}

	}

	public class LoadAllDevicesThread extends AsyncTask<Boolean, ArrayList<FoundDevice>, ArrayList<FoundDevice>> {

		boolean closeDBAfterFinish = true;
		boolean running = false;

		@Override
		protected ArrayList<FoundDevice> doInBackground(Boolean... params) {

			closeDBAfterFinish = params[0];

			int count = 0;

			// Doing some init

			ManufacturerList.getManufacturers();

			temporaryAsyncList = new ArrayList<FoundDevice>();

			long startTime = System.currentTimeMillis();

			db.beginTransaction();

			Cursor cursor = db.query(DatabaseHelper.FOUND_DEVICES_TABLE, null, null, null, null, null,
					DatabaseHelper.COLUMN_TIME + " DESC");
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				FoundDevice device = new FoundDevice();

				short a = cursor.getShort(1);
				short b = cursor.getShort(2);
				short c = cursor.getShort(3);
				short d = cursor.getShort(4);
				short e = cursor.getShort(5);
				short f = cursor.getShort(6);

				MacAddress macAddress = new MacAddress(a, b, c, d, e, f);

				device.setMac(macAddress);
				device.setName(cursor.getString(7));
				device.setRssi(cursor.getShort(8));
				device.setTime(cursor.getLong(9));
				device.setManu(cursor.getInt(10));
				device.setBoost(cursor.getFloat(11));

				temporaryAsyncList.add(device);
				cursor.moveToNext();

				count++;

				if (count >= 2500) {

					publishProgress(temporaryAsyncList);
					count = 0;

				}

			}

			cursor.close();

			db.setTransactionSuccessful();
			db.endTransaction();

			if (closeDBAfterFinish) {
				close();
			}

			long endTime = System.currentTimeMillis();
			Log.d("LoadAllDevicesThread", "doBackground() took " + (endTime - startTime) + "ms @ " + temporaryAsyncList.size() + "devices");

			return temporaryAsyncList;
		}

		@Override
		protected void onPreExecute() {

			Toast.makeText(bhApp, "Loading found devices from database...", Toast.LENGTH_SHORT).show();

			bhApp.actionBarHandler.setDiscoverySwitchEnabled(false);

			running = true;

		}

		@Override
		protected void onProgressUpdate(ArrayList<FoundDevice>... values) {

			// Allways use temporaryAsyncList here!!!

			Log.d("LoadAllDevicesThread", "onProgressUpdate() is called @ " + temporaryAsyncList.size() + " devices.");

			DeviceDiscoveryLayout.updateDuringDBLoading(bhApp.mainActivity, true);

		}

		@Override
		protected void onPostExecute(ArrayList<FoundDevice> result) {

			Log.d("LoadAllDevicesThread", "onPostExecute() is called.");

			running = false;

			cachedList = new ArrayList<FoundDevice>(result);
			temporaryAsyncList = null;

			DeviceDiscoveryLayout.updateIndicatorViews(bhApp.mainActivity);
			FoundDevicesLayout.refreshFoundDevicesList(bhApp, false);
			AchievementsLayout.initializeAchievements(bhApp);

			if (bhApp.synchronizeFoundDevices.needForceOverrideUp) {
				bhApp.synchronizeFoundDevices.startSyncing(3, true);
			}

			bhApp.actionBarHandler.setDiscoverySwitchEnabled(true);

			bhApp.mainActivity.updateNotification();
		}

	}

}
