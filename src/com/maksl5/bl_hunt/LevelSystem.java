/**
 *  LevelSystem.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.SimpleAdapter;

import com.maksl5.bl_hunt.custom_ui.FoundDevice;
import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class LevelSystem {

	public static int getUserExp(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> foundDevices = new DatabaseManager(bhApp)
				.getAllDevices();

		for (FoundDevice foundDevice : foundDevices) {

			int manufacturerId = foundDevice.getManufacturer();
			float bonus = foundDevice.getBoost();

			if (bonus == -1f)
				bonus = 0f;

			exp += Math.floor(ManufacturerList.getExp(manufacturerId)
					* (1 + bonus));

		}

		return exp;

	}

	public static int getExpWoBonus(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> foundDevices = new DatabaseManager(bhApp)
				.getAllDevices();

		for (FoundDevice foundDevice : foundDevices) {

			int manufacturer = foundDevice.getManufacturer();

			exp += ManufacturerList.getExp(manufacturer);

		}

		return exp;

	}

	public static int getLevel(int exp) {

		int level = 1;

		int compareExp = 50;

		while (compareExp < exp) {
			compareExp = compareExp * 2 + compareExp;
			level += 1;
		}

		return level;
	}

	public static int getLevelStartExp(int level) {

		if (level == 1)
			return 0;

		int exp = 50;

		for (int i = 1; i < level - 1; i++) {
			exp = exp * 2 + exp;
		}

		return exp;
	}

	public static int getLevelEndExp(int level) {

		int exp = 50;

		for (int i = 1; i < level; i++) {
			exp = exp * 2 + exp;
		}

		return exp;
	}

	public static AlertDialog getBoostCompositionDialog(BlueHunter bhApp) {

		int[] to = new int[] { R.id.descriptionTxt, R.id.boostTxt };
		String[] from = new String[] { "description", "boost" };

		List<HashMap<String, String>> adapterList = AchievementSystem
				.getBoostList(bhApp);

		SimpleAdapter boostAdapater = new SimpleAdapter(bhApp, adapterList,
				R.layout.dlg_boost_composite_row, from, to);

		AlertDialog.Builder builder = new Builder(bhApp.mainActivity);
		builder.setTitle(R.string.str_boostComposition_title);

		builder.setAdapter(boostAdapater, null);
		builder.setNeutralButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		return builder.create();

	}

}
