/**
 *  PreferenceManager.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt.storage;



import android.content.Context;
import android.content.SharedPreferences;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class PreferenceManager {

	public static boolean getPref(	Context con,
									String key,
									boolean defValue) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getBoolean(key, defValue);

	}

	public static int getPref(	Context con,
								String key,
								int defValue) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getInt(key, defValue);

	}

	public static String getPref(	Context con,
									String key,
									String defValue) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getString(key, defValue);

	}

	public static long getPref(	Context con,
								String key,
								long defValue) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getLong(key, defValue);

	}

	public static void setPref(	Context con,
								String key,
								String value) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		shPreferences.edit().putString(key, value).apply();

	}

	public static void setPref(	Context con,
								String key,
								Boolean value) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		shPreferences.edit().putBoolean(key, value).apply();

	}

	public static void setPref(	Context con,
								String key,
								int value) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		shPreferences.edit().putInt(key, value).apply();

	}

	public static void setPref(	Context con,
								String key,
								long value) {

		SharedPreferences shPreferences =
				android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		shPreferences.edit().putLong(key, value).apply();

	}
	
	public static void deletePreference(Context con, String key) {
		SharedPreferences shPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		shPreferences.edit().remove(key).apply();
	}

}
