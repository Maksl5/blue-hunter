/**
 *  PreferenceManager.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import android.content.Context;
import android.content.SharedPreferences;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class PreferenceManager {

	public static boolean getPref(Context con, String key, boolean defValue) {

		SharedPreferences shPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getBoolean(key, defValue);

	}

	public static int getPref(Context con, String key, int defValue) {

		SharedPreferences shPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getInt(key, defValue);

	}

	public static String getPref(Context con, String key, String defValue) {

		SharedPreferences shPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(con);
		return shPreferences.getString(key, defValue);

	}
}
