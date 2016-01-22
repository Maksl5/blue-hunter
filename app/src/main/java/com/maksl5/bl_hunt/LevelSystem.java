/**
 *  LevelSystem.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.SimpleAdapter;

import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.util.FoundDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class LevelSystem {

	private static int cachedExp = -1;

	public static int getUserExp(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> allDevices = DatabaseManager.getCachedList();

		if (allDevices == null) {

			new DatabaseManager(bhApp).loadAllDevices();
			return (cachedExp != -1) ? cachedExp : 0;

		}

		int size = allDevices.size();

		for (int i = 0; i < size; i++) {
			FoundDevice foundDevice = allDevices.get(i);

			int manufacturerId = foundDevice.getManufacturer();
			float bonus = foundDevice.getBoost();

			if (bonus == -1f) bonus = 0f;

			exp += (int) (ManufacturerList.getExp(manufacturerId) * (1 + bonus));

		}

		cachedExp = exp;

        //return exp;

        return 1200000;

	}

	public static int getUserExp(List<FoundDevice> listToUse) {

		int exp = 0;

		List<FoundDevice> tempList = new ArrayList<>(listToUse);

		for (FoundDevice foundDevice : tempList) {

			int manufacturerId = foundDevice.getManufacturer();
			float bonus = foundDevice.getBoost();

			if (bonus == -1f) bonus = 0f;

			exp += Math.floor(ManufacturerList.getExp(manufacturerId) * (1 + bonus));

		}

		cachedExp = exp;

        //return exp;
        return 1200000;

	}

	public static int getCachedUserExp(BlueHunter bhApp) {
		if (cachedExp == -1) {
			getUserExp(bhApp);
		}
        //return cachedExp;
        return 1200000;

	}

	public static int getExpWoBonus(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> allDevices = DatabaseManager.getCachedList();

		if (allDevices == null) {

			new DatabaseManager(bhApp).loadAllDevices();
			return exp;

		}

		for (FoundDevice foundDevice : allDevices) {

			int manufacturer = foundDevice.getManufacturer();

			exp += ManufacturerList.getExp(manufacturer);

		}

		return exp;

	}

	public static int getLevel(int exp) {

		int level = 1;

		int compareExp = 50;

		while (compareExp < exp) {
			compareExp = compareExp + level * level * 50;
			level++;
		}

		return level;
	}

	public static int getLevelStartExp(int level) {

		if (level == 1) return 0;

		int exp = 50;

		for (int i = 1; i < level - 1; i++) {
			exp = exp + i * i * 50;
		}

		return exp;
	}

	public static int getLevelEndExp(int level) {

		int exp = 50;

		for (int i = 1; i < level; i++) {
			exp = exp + i * i * 50;
		}

		return exp;
	}

	public static AlertDialog getBoostCompositionDialog(BlueHunter bhApp) {

		int[] to = new int[] {
				R.id.descriptionTxt, R.id.boostTxt };
		String[] from = new String[] {
				"description", "boost" };

		List<HashMap<String, String>> adapterList = AchievementSystem.getBoostList(bhApp);

		SimpleAdapter boostAdapater = new SimpleAdapter(bhApp, adapterList, R.layout.dlg_boost_composite_row, from, to);

		AlertDialog.Builder builder = new Builder(bhApp.mainActivity);
		builder.setTitle(R.string.str_boostComposition_title);

		builder.setAdapter(boostAdapater, null);
		builder.setPositiveButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		return builder.create();

	}

}
