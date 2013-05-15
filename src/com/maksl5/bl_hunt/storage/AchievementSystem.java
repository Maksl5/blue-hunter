/**
 *  AchievementSystem.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.storage;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class AchievementSystem {

	private static float bonus = 0.0f;

	public static HashMap<Integer, Boolean> achievementStates;

	public static List<Achievement> achievements =
			Arrays.asList(new Achievement[] {
												new Achievement(1, R.string.str_achieve_1_title, R.string.str_achieve_1_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														if (exp >= 100)
															return true;
														else
															return false;

													}

												}.setNumExp(100).setBoost(0.01f),

												new Achievement(2, R.string.str_achieve_2_title, R.string.str_achieve_2_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														if (deviceNum >= 200)
															return true;
														else
															return false;

													}
												}.setNumDevices(200).setBoost(0.02f),

												new Achievement(3, R.string.str_achieve_3_title, R.string.str_achieve_3_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp, bhApp.getVersionCode()).getDevices(DatabaseHelper.COLUMN_MANUFACTURER + " = 'Apple'", null);
														if (devices.size() >= 25)
															return true;
														else
															return false;

													}

												}.setNumDevices(25).setManufacturerList(Arrays.asList(new String[] { "Apple" })).setBoost(0.02f),

												new Achievement(4, R.string.str_achieve_4_title, R.string.str_achieve_4_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp, bhApp.getVersionCode()).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 9; i < devices.size(); i++) {

															long firstTime =
																	Long.parseLong(devices.get(i - 9).get(DatabaseManager.INDEX_TIME));
															long secondTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															if ((firstTime - secondTime) <= 120000) {

																return true;
															}
														}

														return false;
													}

												}.setNumDevices(10).setTimeInterval(120).setBoost(0.05f) });

	public static void checkAchievements(	BlueHunter bhApp,
											boolean completeCheck) {

		achievementStates = new HashMap<Integer, Boolean>();

		int deviceNum = new DatabaseManager(bhApp, bhApp.getVersionCode()).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {

			boolean alreadyAccomplished =
					PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "").equals(bhApp.authentification.getAchieveHash(achievement.getId()));

			if (!completeCheck) {
				if (alreadyAccomplished) {
					achievementStates.put(achievement.getId(), true);
				}
				else {

					if (achievement.check(bhApp, deviceNum, exp)) {
						achievement.accomplish(bhApp, alreadyAccomplished);
						achievementStates.put(achievement.getId(), true);
					}
					else {
						achievement.invalidate(bhApp);
						achievementStates.put(achievement.getId(), false);
					}
				}
			}
			else {
				if (achievement.check(bhApp, deviceNum, exp)) {
					achievement.accomplish(bhApp, alreadyAccomplished);
					achievementStates.put(achievement.getId(), true);
				}
				else {
					achievement.invalidate(bhApp);
					achievementStates.put(achievement.getId(), false);
				}
			}

		}

	}

	public static void checkSpecificAchievements(	BlueHunter bhApp,
													boolean completeCheck,
													Integer... id) {

		List<Integer> ids = Arrays.asList(id);

		int deviceNum = new DatabaseManager(bhApp, bhApp.getVersionCode()).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {
			if (!ids.contains(achievement.getId())) {

				boolean alreadyAccomplished =
						PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "").equals(bhApp.authentification.getAchieveHash(achievement.getId()));

				if (!completeCheck) {
					if (alreadyAccomplished) {
						achievementStates.put(achievement.getId(), true);
					}
					else {

						if (achievement.check(bhApp, deviceNum, exp)) {
							achievement.accomplish(bhApp, alreadyAccomplished);
							achievementStates.put(achievement.getId(), true);
						}
						else {
							achievement.invalidate(bhApp);
							achievementStates.put(achievement.getId(), false);
						}
					}
				}
				else {
					if (achievement.check(bhApp, deviceNum, exp)) {
						achievement.accomplish(bhApp, alreadyAccomplished);
						achievementStates.put(achievement.getId(), true);
					}
					else {
						achievement.invalidate(bhApp);
						achievementStates.put(achievement.getId(), false);
					}
				}

			}
		}

	}

	public static float getBonus() {

		float bonus = 0.0f;

		Set<Integer> ids = achievementStates.keySet();

		for (Achievement achievement : achievements) {
			if (ids.contains(achievement.getId())) {
				if (achievementStates.get(achievement.getId())) {
					bonus += achievement.getBoost();
				}
			}
		}

		return bonus;

	}
}
