/**
 *  LevelSystem.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



import java.util.List;

import android.util.SparseArray;

import com.maksl5.bl_hunt.custom_ui.FoundDevice;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.MacAddressAllocations;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class LevelSystem {

	public static int getUserExp(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> foundDevices =
				new DatabaseManager(bhApp).getAllDevices();

		for (FoundDevice foundDevice : foundDevices) {

			String manufacturer = foundDevice.getManufacturer();
			float bonus = foundDevice.getBonus();
			
			if(bonus == -1f) bonus = 0f;

			if (manufacturer == null) {
				exp += Math.floor(MacAddressAllocations.Unknown_exp * (1 + bonus));
			}
			else {
				exp +=
						Math.floor(MacAddressAllocations.getExp(manufacturer) * (1 + bonus));
			}

		}

		return exp;

	}

	public static int getExpWoBonus(BlueHunter bhApp) {

		int exp = 0;

		List<FoundDevice> foundDevices =
				new DatabaseManager(bhApp).getAllDevices();

		for (FoundDevice foundDevice : foundDevices) {

			String manufacturer = foundDevice.getManufacturer();

			if (manufacturer == null) {
				exp += MacAddressAllocations.Unknown_exp;
			}
			else {
				exp += MacAddressAllocations.getExp(manufacturer);
			}

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

		if (level == 1) return 0;

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

}
