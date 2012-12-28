/**
 *  LevelSystem.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.MacAdressAllocations;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class LevelSystem {

	public static int getUserExp(MainActivity mainActivity) {

		int exp = 0;

		List<HashMap<String, String>> foundDevices =
				new DatabaseManager(mainActivity, mainActivity.versionCode).getAllDevices();
		HashMap<String, Integer> expHashMap = MacAdressAllocations.getExpHashMap();
		HashMap<String, String[ ]> macAllocations = MacAdressAllocations.getHashMap();

		Set<String> keys = macAllocations.keySet();

		for (HashMap<String, String> foundDevice : foundDevices) {
			boolean foundManufacturer = false;

			for (String key : keys) {
				String[ ] manufactuerMacs = macAllocations.get(key);

				for (String mac : manufactuerMacs) {

					if (foundDevice.get(DatabaseHelper.COLUMN_MAC_ADDRESS).startsWith(mac)) {
						foundManufacturer = true;
						int tempExp = expHashMap.get(key.replace(" ", "_")
														+ "_exp");
						exp += tempExp;

					}

				}

			}

			if (!foundManufacturer) {
				exp += expHashMap.get("Unknown_exp");
			}

		}

		return exp;

	}

	public static int getLevel(int exp) {

		int level = 1;

		int compareExp = 50;

		while (compareExp < exp) {
			compareExp = compareExp
							* 2
							+ compareExp;
			level += 1;
		}

		return level;
	}

	public static int getLevelStartExp(int level) {

		if (level == 1) return 0;

		int exp = 50;

		for (int i = 1; i < level - 1; i++) {
			exp = exp
					* 2
					+ exp;
		}

		return exp;
	}

	public static int getLevelEndExp(int level) {

		int exp = 50;

		for (int i = 1; i < level; i++) {
			exp = exp
					* 2
					+ exp;
		}

		return exp;
	}

}
