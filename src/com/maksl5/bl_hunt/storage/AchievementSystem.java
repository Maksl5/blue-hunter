/**
 *  AchievementSystem.java in com.maksl5.bl_hunt.storage
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.storage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.content.Context;
import android.graphics.Matrix;
import android.util.SparseArray;
import android.view.MenuItem;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.FoundDevice;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class AchievementSystem {

	private static float boost = 0.0f;

	public static HashMap<Integer, Boolean> achievementStates;

	public static List<Achievement> achievements = Arrays.asList(new Achievement[] {
			new Achievement(5, R.string.str_achieve_5_title, R.string.str_achieve_5_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					if (BlueHunter.isSupport)
						return true;
					else
						return false;

				}

			}.setBoost(1.0f),

			new Achievement(1, R.string.str_achieve_1_title, R.string.str_achieve_1_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					setNewProgressString("" + exp + " / 100");

					if (exp >= 100)
						return true;
					else
						return false;

				}

			}.setBoost(0.01f),

			new Achievement(2, R.string.str_achieve_2_title, R.string.str_achieve_2_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					setNewProgressString("" + deviceNum + " / 200");

					if (deviceNum >= 200)
						return true;
					else
						return false;

				}

			}.setBoost(0.02f),

			new Achievement(15, R.string.str_achieve_15_title, R.string.str_achieve_15_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					setNewProgressString(bhApp.getString(R.string.str_discovery_devices) + " " + deviceNum + " / 300\n"
							+ bhApp.getString(R.string.str_foundDevices_exp_abbreviation) + ": " + exp + " / 8000");

					if (deviceNum >= 300)
						if (exp >= 8000)
							return true;
						else
							return false;
					else
						return false;

				}

			}.setBoost(0.04f),

			new Achievement(3, R.string.str_achieve_3_title, R.string.str_achieve_3_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					int devices = new DatabaseManager(bhApp).getDeviceNum(DatabaseHelper.COLUMN_MANUFACTURER + " = '1'"); // Apple

					setNewProgressString("" + devices + " / 25");

					if (devices >= 25)
						return true;
					else
						return false;

				}

			}.setBoost(0.02f),

			new Achievement(6, R.string.str_achieve_6_title, R.string.str_achieve_6_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getAllDevices();

					int max = 0;

					//5 Apple in row
					
					for (int i = 4; i < devices.size(); i++) {

						if (devices.get(i - 4).getManufacturer() == 1) {
							max = (max < 1) ? 1 : max;
							if (devices.get(i - 3).getManufacturer() == 1) {
								max = (max < 2) ? 2 : max;
								if (devices.get(i - 2).getManufacturer() == 1) {
									max = (max < 3) ? 3 : max;
									if (devices.get(i - 1).getManufacturer() == 1) {
										max = (max < 4) ? 4 : max;
										if (devices.get(i).getManufacturer() == 1) {
											max = (max < 5) ? 5 : max;
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
					
					setNewProgressString("" + max + " / 5");
					
					return false;

				}

			}.setBoost(0.03f),

			new Achievement(4, R.string.str_achieve_4_title, R.string.str_achieve_4_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//15D in 2min
					
					for (int i = 14; i < devices.size(); i++) {

						long firstTime = devices.get(i - 14).getTime();
						long secondTime = devices.get(i).getTime();

						if ((firstTime - secondTime) <= 120000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.05f),

			new Achievement(10, R.string.str_achieve_10_title, R.string.str_achieve_10_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//100D in 15min
					
					for (int i = 99; i < devices.size(); i++) {

						long firstTime = devices.get(i - 99).getTime();
						long secondTime = devices.get(i).getTime();

						if ((firstTime - secondTime) <= 900000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.06f),

			new Achievement(16, R.string.str_achieve_16_title, R.string.str_achieve_16_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//20D in 15sec
					
					for (int i = 19; i < devices.size(); i++) {

						long firstTime = devices.get(i - 19).getTime();
						long secondTime = devices.get(i).getTime();

						if ((firstTime - secondTime) <= 15000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.12f),

			new Achievement(7, R.string.str_achieve_7_title, R.string.str_achieve_7_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					int devices = new DatabaseManager(bhApp).getDeviceNum(DatabaseHelper.COLUMN_MANUFACTURER + " = '21'"); //Siemens

					setNewProgressString("" + devices + " / 5");

					if (devices >= 5)
						return true;
					else
						return false;
				}

			}.setBoost(0.04f),

			new Achievement(17, R.string.str_achieve_17_title, R.string.str_achieve_17_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					int devices = new DatabaseManager(bhApp).getDeviceNum(DatabaseHelper.COLUMN_MANUFACTURER + " = '9'"); //Sony Ericsson

					setNewProgressString("" + devices + " / 50");

					if (devices >= 50)
						return true;
					else
						return false;
				}

			}.setBoost(0.08f),

			new Achievement(8, R.string.str_achieve_8_title, R.string.str_achieve_8_description) {

				@SuppressWarnings({ "deprecation" })
				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//15D between 10:00 - 10:30pm
					
					for (int i = 14; i < devices.size(); i++) {

						long firstTime = devices.get(i - 14).getTime();
						long secondTime = devices.get(i).getTime();

						if ((firstTime - secondTime) <= 1800000) {

							Date recentDate = new Date(firstTime);
							Date firstDate = new Date(secondTime);

							if (recentDate.getHours() == 22 && firstDate.getHours() == 22) {
								if (recentDate.getMinutes() >= 0 && recentDate.getMinutes() < 30) {
									if (firstDate.getMinutes() >= 0 && firstDate.getMinutes() < 30) {
										return true;
									}
								}
							}

						}
					}

					return false;
				}

			}.setBoost(0.02f),

			new Achievement(9, R.string.str_achieve_9_title, R.string.str_achieve_9_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//65D between 5:00 - 7:00am
					
					for (int i = 64; i < devices.size(); i++) {

						long firstTime = devices.get(i - 64).getTime();
						long secondTime = devices.get(i).getTime();

						if ((firstTime - secondTime) <= 7200000) {

							Date recentDate = new Date(firstTime);
							Date firstDate = new Date(secondTime);

							if (firstDate.getHours() >= 5 && firstDate.getHours() < 7) {
								if (recentDate.getHours() >= 5 && recentDate.getHours() < 7) {

									return true;

								}
							}

						}
					}

					return false;
				}

			}.setBoost(0.08f),

			new Achievement(11, R.string.str_achieve_11_title, R.string.str_achieve_11_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//1D to full hour
					
					for (int i = 0; i < devices.size(); i++) {

						long time = devices.get(i).getTime();

						Date date = new Date(time);

						if (date.getMinutes() == 0) {

							return true;

						}

					}

					return false;
				}

			}.setBoost(0.08f),

			new Achievement(14, R.string.str_achieve_14_title, R.string.str_achieve_14_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					//1D 11th Nov 11:11am
					
					for (int i = 0; i < devices.size(); i++) {

						long time = devices.get(i).getTime();

						Date date = new Date(time);

						if (date.getDate() == 11 && date.getMonth() == 11 && date.getHours() == 11 && date.getMinutes() == 11) {

							return true;

						}

					}

					return false;
				}

			}.setBoost(0.40f),

			new Achievement(12, R.string.str_achieve_12_title, R.string.str_achieve_12_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getAllDevices();

					//In row: 1 Apple -> 1 BlackBerry -> 1 Samsung
					
					for (int i = 2; i < devices.size(); i++) {

						if (devices.get(i - 2).getManufacturer() == 3) {
							if (devices.get(i - 1).getManufacturer() == 10) {
								if (devices.get(i).getManufacturer() == 1) {
									return true;
								}

							}

						}

					}

					return false;

				}

			}.setBoost(0.15f),

			new Achievement(18, R.string.str_achieve_18_title, R.string.str_achieve_18_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getAllDevices();

					//In row: Acer -> Bury -> Cisco -> D-Link
					
					for (int i = 3; i < devices.size(); i++) {

						if (devices.get(i - 3).getManufacturer() == 51) {
							if (devices.get(i - 2).getManufacturer() == 30) {
								if (devices.get(i - 1).getManufacturer() == 50) {
									if (devices.get(i).getManufacturer() == 11) {
										return true;
									}
								}

							}

						}

					}

					return false;

				}

			}.setBoost(0.25f),

			new Achievement(13, R.string.str_achieve_13_title, R.string.str_achieve_13_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					List<FoundDevice> devices = new DatabaseManager(bhApp).getDevices(DatabaseHelper.COLUMN_TIME + " != 0",
							DatabaseHelper.COLUMN_TIME + " DESC");

					int max = 0;

					for (int i = 0; i < devices.size(); i++) {

						long recentTime = devices.get(i).getTime();

						int exp24 = 0;

						for (int j = i + 1; j < devices.size(); j++) {

							long earlyTime = devices.get(j).getTime();

							if ((recentTime - earlyTime) < 86400000) {

								float bonus = devices.get(j).getBoost();

								if (bonus == -1f) bonus = 0.0f;

								exp24 += ManufacturerList.getExp(devices.get(j).getManufacturer()) * (1 + bonus);
							}
							else {
								break;
							}

						}

						max = (exp24 > max) ? exp24 : max;

						if (exp24 >= 200) return true;
					}

					setNewProgressString("" + max + " / 200");

					return false;
				}

			}.setBoost(0.03f)

	});

	public static void checkAchievements(BlueHunter bhApp, boolean completeCheck) {

		achievementStates = new HashMap<Integer, Boolean>();

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {

			boolean alreadyAccomplished = PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "").equals(
					bhApp.authentification.getAchieveHash(achievement.getId()));

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

		float boost = getBoost(bhApp);

		NumberFormat pFormat = DecimalFormat.getPercentInstance();

		MenuItem boostIndicator = bhApp.actionBarHandler.getMenuItem(R.id.menu_boostIndicator);
		boostIndicator.setTitleCondensed(bhApp.getString(R.string.str_achievement_totalBoost, pFormat.format(boost)));

	}

	public static void checkSpecificAchievements(BlueHunter bhApp, boolean completeCheck, Integer... id) {

		List<Integer> ids = Arrays.asList(id);

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {
			if (!ids.contains(achievement.getId())) {

				boolean alreadyAccomplished = PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "").equals(
						bhApp.authentification.getAchieveHash(achievement.getId()));

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

	public static float getBoost(BlueHunter bhApp) {

		float bonus = 0.0f;

		bonus += getLevelBoost(bhApp);

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

	public static List<HashMap<String, String>> getBoostList(BlueHunter bhApp) {
		List<HashMap<String, String>> boostList = new ArrayList<HashMap<String, String>>();

		int level = LevelSystem.getLevel(LevelSystem.getUserExp(bhApp));
		float levelBoost = getLevelBoost(bhApp);

		NumberFormat percentage = NumberFormat.getPercentInstance();

		HashMap<String, String> levelHashMap = new HashMap<String, String>();
		levelHashMap.put("description", bhApp.getString(R.string.str_boostComposition_levelBoost, level));
		levelHashMap.put("boost", "+" + percentage.format(levelBoost));

		boostList.add(levelHashMap);

		HashMap<String, String> emptyHashMap = new HashMap<String, String>();

		emptyHashMap.put("description", "");
		emptyHashMap.put("boost", "");

		boostList.add(emptyHashMap);

		Set<Integer> ids = achievementStates.keySet();

		for (Achievement achievement : achievements) {
			if (ids.contains(achievement.getId())) {
				if (achievementStates.get(achievement.getId())) {

					HashMap<String, String> itemHashMap = new HashMap<String, String>();
					itemHashMap.put("description",
							bhApp.getString(R.string.str_boostComposition_achievementBoost, achievement.getName(bhApp)));
					itemHashMap.put("boost", "+" + percentage.format(achievement.getBoost()));

					boostList.add(itemHashMap);

				}
			}
		}

		HashMap<String, String> sumHashMap = new HashMap<String, String>();

		sumHashMap.put("description", "");
		sumHashMap.put("boost", "-------");

		boostList.add(sumHashMap);

		HashMap<String, String> totalHashMap = new HashMap<String, String>();

		totalHashMap.put("description", "Total Boost:");
		totalHashMap.put("boost", percentage.format(getBoost(bhApp)));

		boostList.add(totalHashMap);

		return boostList;

	}

	private static float getLevelBoost(BlueHunter bhApp) {

		float levelBoost = 0f;
		int curLevel = LevelSystem.getLevel(LevelSystem.getUserExp(bhApp));

		for (int level = 0; level <= curLevel; level++) {
			levelBoost += (float) level / (float) 100;
		}

		return levelBoost;

	}
}