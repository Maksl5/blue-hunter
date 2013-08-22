/**
 *  AchievementSystem.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.storage;



import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.util.SparseArray;
import android.view.MenuItem;

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
												new Achievement(5, R.string.str_achieve_5_title, R.string.str_achieve_5_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														if (bhApp.getPackageName().equals("com.maksl5.bl_hunt_sup"))
															return true;
														else
															return false;

													}

												}.setBoost(0.5f),

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
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_MANUFACTURER + " = 'Apple'", null);
														if (devices.size() >= 25)
															return true;
														else
															return false;

													}

												}.setNumDevices(25).setManufacturerList(Arrays.asList(new String[] { "Apple" })).setBoost(0.02f),

												new Achievement(6, R.string.str_achieve_6_title, R.string.str_achieve_6_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getAllDevices();

														for (int i = 4; i < devices.size(); i++) {

															if (devices.get(i - 4).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																if (devices.get(i - 3).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																	if (devices.get(i - 2).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																		if (devices.get(i - 1).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																			if (devices.get(i).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																				return true;
																			}
																			else {
																				i = i + 4;
																			}
																		}
																		else {
																			i = i + 3;
																		}
																	}
																	else {
																		i = i + 2;
																	}
																}
																else {
																	i = i + 1;
																}
															}

														}

														return false;

													}

												}.setNumDevices(5).setManufacturerList(Arrays.asList(new String[] { "Apple" })).setBoost(0.03f),

												new Achievement(4, R.string.str_achieve_4_title, R.string.str_achieve_4_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 14; i < devices.size(); i++) {

															long firstTime =
																	Long.parseLong(devices.get(i - 14).get(DatabaseManager.INDEX_TIME));
															long secondTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															if ((firstTime - secondTime) <= 120000) {

															return true; }
														}

														return false;
													}

												}.setNumDevices(15).setTimeInterval(120).setBoost(0.05f),

												new Achievement(10, R.string.str_achieve_10_title, R.string.str_achieve_10_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 99; i < devices.size(); i++) {

															long firstTime =
																	Long.parseLong(devices.get(i - 99).get(DatabaseManager.INDEX_TIME));
															long secondTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															if ((firstTime - secondTime) <= 900000) {

															return true; }
														}

														return false;
													}

												}.setNumDevices(100).setTimeInterval(900).setBoost(0.06f),

												new Achievement(7, R.string.str_achieve_7_title, R.string.str_achieve_7_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_MANUFACTURER + " = 'Siemens'", null);
														if (devices.size() >= 5)
															return true;
														else
															return false;
													}

												}.setNumDevices(5).setManufacturerList(Arrays.asList(new String[] { "Siemens" })).setBoost(0.02f),

												new Achievement(8, R.string.str_achieve_8_title, R.string.str_achieve_8_description) {

													@SuppressWarnings({
														"deprecation" })
													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 14; i < devices.size(); i++) {

															long firstTime =
																	Long.parseLong(devices.get(i - 14).get(DatabaseManager.INDEX_TIME));
															long secondTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															if ((firstTime - secondTime) <= 1800000) {

																Date recentDate =
																		new Date(firstTime);
																Date firstDate =
																		new Date(secondTime);
																
																if (recentDate.getHours() == 22 && firstDate.getHours() == 22) {
																	if (recentDate.getMinutes() >= 0 && recentDate.getMinutes() < 30) {
																		if (firstDate.getMinutes() >= 0 && firstDate.getMinutes() < 30) { return true; }
																	}
																}

															}
														}

														return false;
													}

												}.setNumDevices(15).setTimeInterval(1800).setBoost(0.02f),

												new Achievement(9, R.string.str_achieve_9_title, R.string.str_achieve_9_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 14; i < devices.size(); i++) {

															long firstTime =
																	Long.parseLong(devices.get(i - 14).get(DatabaseManager.INDEX_TIME));
															long secondTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															if ((firstTime - secondTime) <= 7200000) {

																Date recentDate =
																		new Date(firstTime);
																Date firstDate =
																		new Date(secondTime);

																if (firstDate.getHours() >= 5 && firstDate.getHours() < 7) {
																	if (recentDate.getHours() >= 5 && recentDate.getHours() < 7) {

																	return true;

																	}
																}

															}
														}

														return false;
													}

												}.setNumDevices(65).setTimeInterval(7200).setBoost(0.04f),

												new Achievement(11, R.string.str_achieve_11_title, R.string.str_achieve_11_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 0; i < devices.size(); i++) {

															long time =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															Date date = new Date(time);

															if (date.getMinutes() == 0 && date.getSeconds() == 0) {

															return true;

															}

														}

														return false;
													}

												}.setNumDevices(1).setBoost(0.08f),

												new Achievement(12, R.string.str_achieve_12_title, R.string.str_achieve_12_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getAllDevices();

														for (int i = 2; i < devices.size(); i++) {

															if (devices.get(i - 2).get(DatabaseManager.INDEX_MANUFACTURER).equals("Apple")) {
																if (devices.get(i - 1).get(DatabaseManager.INDEX_MANUFACTURER).equals("BlackBerry")) {
																	if (devices.get(i).get(DatabaseManager.INDEX_MANUFACTURER).equals("Samsung")) { return true; }

																}

															}

														}

														return false;

													}

												}.setNumDevices(3).setManufacturerList(Arrays.asList(new String[] {
																													"Apple",
																													"BlackBerry",
																													"Samsung" })).setBoost(0.02f),

												new Achievement(13, R.string.str_achieve_13_title, R.string.str_achieve_13_description) {

													@Override
													public boolean check(	BlueHunter bhApp,
																			int deviceNum,
																			int exp) {

														List<SparseArray<String>> devices =
																new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0", DatabaseHelper.COLUMN_TIME + " DESC");

														for (int i = 0; i < devices.size(); i++) {

															long recentTime =
																	Long.parseLong(devices.get(i).get(DatabaseManager.INDEX_TIME));

															int exp24 = 0;

															for (int j = i + 1; j < devices.size(); j++) {

																long earlyTime =
																		Long.parseLong(devices.get(j).get(DatabaseManager.INDEX_TIME));

																if ((recentTime - earlyTime) < 86400000) {

																	String bonusString =
																			devices.get(j).get(DatabaseManager.INDEX_BONUS);
																	float bonus = 0.0f;

																	if (bonusString != null)
																		bonus =
																				Float.parseFloat(bonusString);

																	exp24 +=
																			MacAddressAllocations.getExp(devices.get(j).get(DatabaseManager.INDEX_MANUFACTURER)) * (1 + bonus);
																}
																else {
																	break;
																}

															}

															if (exp >= 200) return true;
														}

														return false;
													}

												}.setNumExp(200).setTimeInterval(86400).setBoost(0.03f) });

	public static void checkAchievements(	BlueHunter bhApp,
											boolean completeCheck) {

		achievementStates = new HashMap<Integer, Boolean>();

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
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

		float boost = getBonus();

		NumberFormat pFormat = DecimalFormat.getPercentInstance();

		MenuItem boostIndicator = bhApp.actionBarHandler.getMenuItem(R.id.menu_boostIndicator);
		boostIndicator.setTitleCondensed(bhApp.getString(R.string.str_achievement_totalBoost, pFormat.format(boost)));

	}

	public static void checkSpecificAchievements(	BlueHunter bhApp,
													boolean completeCheck,
													Integer... id) {

		List<Integer> ids = Arrays.asList(id);

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
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
