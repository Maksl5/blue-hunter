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
import com.maksl5.bl_hunt.custom_ui.FoundDevice;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class AchievementSystem {

	private static float bonus = 0.0f;

	public static HashMap<Integer, Boolean> achievementStates;

	public static List<Achievement> achievements = Arrays
			.asList(new Achievement[] {
					new Achievement(5, R.string.str_achieve_5_title,
							R.string.str_achieve_5_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							if (bhApp.getPackageName().equals(
									"com.maksl5.bl_hunt_sup"))
								return true;
							else
								return false;

						}

					}.setBoost(1.0f),

					new Achievement(1, R.string.str_achieve_1_title,
							R.string.str_achieve_1_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							if (exp >= 100)
								return true;
							else
								return false;

						}

					}.setNumExp(100).setBoost(0.01f),

					new Achievement(2, R.string.str_achieve_2_title,
							R.string.str_achieve_2_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							if (deviceNum >= 200)
								return true;
							else
								return false;

						}
					}.setNumDevices(200).setBoost(0.02f),

					new Achievement(15, R.string.str_achieve_15_title,
							R.string.str_achieve_15_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							if (deviceNum >= 300)
								if (exp >= 4000)
									return true;
								else
									return false;
							else
								return false;

						}
					}.setNumDevices(300).setNumExp(4000).setBoost(0.04f),

					new Achievement(3, R.string.str_achieve_3_title,
							R.string.str_achieve_3_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_MANUFACTURER
											+ " = 'Apple'", null);
							if (devices.size() >= 25)
								return true;
							else
								return false;

						}

					}.setNumDevices(25)
							.setManufacturerList(
									Arrays.asList(new String[] { "Apple" }))
							.setBoost(0.02f),

					new Achievement(6, R.string.str_achieve_6_title,
							R.string.str_achieve_6_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getAllDevices();

							for (int i = 4; i < devices.size(); i++) {

								if (devices.get(i - 4).getManufacturer()
										.equals("Apple")) {
									if (devices.get(i - 3).getManufacturer()
											.equals("Apple")) {
										if (devices.get(i - 2)
												.getManufacturer()
												.equals("Apple")) {
											if (devices.get(i - 1)
													.getManufacturer()
													.equals("Apple")) {
												if (devices.get(i)
														.getManufacturer()
														.equals("Apple")) {
													return true;
												} else {
													i = i + 4;
												}
											} else {
												i = i + 3;
											}
										} else {
											i = i + 2;
										}
									} else {
										i = i + 1;
									}
								}

							}

							return false;

						}

					}.setNumDevices(5)
							.setManufacturerList(
									Arrays.asList(new String[] { "Apple" }))
							.setBoost(0.03f),

					new Achievement(4, R.string.str_achieve_4_title,
							R.string.str_achieve_4_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 14; i < devices.size(); i++) {

								long firstTime = devices.get(i - 14).getTime();
								long secondTime = devices.get(i).getTime();

								if ((firstTime - secondTime) <= 120000) {

									return true;
								}
							}

							return false;
						}

					}.setNumDevices(15).setTimeInterval(120).setBoost(0.05f),

					new Achievement(10, R.string.str_achieve_10_title,
							R.string.str_achieve_10_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 99; i < devices.size(); i++) {

								long firstTime = devices.get(i - 99).getTime();
								long secondTime = devices.get(i).getTime();

								if ((firstTime - secondTime) <= 900000) {

									return true;
								}
							}

							return false;
						}

					}.setNumDevices(100).setTimeInterval(900).setBoost(0.06f),

					new Achievement(16, R.string.str_achieve_16_title,
							R.string.str_achieve_16_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 19; i < devices.size(); i++) {

								long firstTime = devices.get(i - 19).getTime();
								long secondTime = devices.get(i).getTime();

								if ((firstTime - secondTime) <= 15000) {

									return true;
								}
							}

							return false;
						}

					}.setNumDevices(20).setTimeInterval(15).setBoost(0.12f),

					new Achievement(7, R.string.str_achieve_7_title,
							R.string.str_achieve_7_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_MANUFACTURER
											+ " = 'Siemens'", null);
							if (devices.size() >= 5)
								return true;
							else
								return false;
						}

					}.setNumDevices(5)
							.setManufacturerList(
									Arrays.asList(new String[] { "Siemens" }))
							.setBoost(0.04f),

					new Achievement(17, R.string.str_achieve_17_title,
							R.string.str_achieve_17_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_MANUFACTURER
											+ " = 'Sony Ericsson'", null);
							if (devices.size() >= 50)
								return true;
							else
								return false;
						}

					}.setNumDevices(50)
							.setManufacturerList(
									Arrays.asList(new String[] { "Sony Ericsson" }))
							.setBoost(0.08f),

					new Achievement(8, R.string.str_achieve_8_title,
							R.string.str_achieve_8_description) {

						@SuppressWarnings({ "deprecation" })
						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 14; i < devices.size(); i++) {

								long firstTime = devices.get(i - 14).getTime();
								long secondTime = devices.get(i).getTime();

								if ((firstTime - secondTime) <= 1800000) {

									Date recentDate = new Date(firstTime);
									Date firstDate = new Date(secondTime);

									if (recentDate.getHours() == 22
											&& firstDate.getHours() == 22) {
										if (recentDate.getMinutes() >= 0
												&& recentDate.getMinutes() < 30) {
											if (firstDate.getMinutes() >= 0
													&& firstDate.getMinutes() < 30) {
												return true;
											}
										}
									}

								}
							}

							return false;
						}

					}.setNumDevices(15).setTimeInterval(1800).setBoost(0.02f),

					new Achievement(9, R.string.str_achieve_9_title,
							R.string.str_achieve_9_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 14; i < devices.size(); i++) {

								long firstTime = devices.get(i - 14).getTime();
								long secondTime = devices.get(i).getTime();

								if ((firstTime - secondTime) <= 7200000) {

									Date recentDate = new Date(firstTime);
									Date firstDate = new Date(secondTime);

									if (firstDate.getHours() >= 5
											&& firstDate.getHours() < 7) {
										if (recentDate.getHours() >= 5
												&& recentDate.getHours() < 7) {

											return true;

										}
									}

								}
							}

							return false;
						}

					}.setNumDevices(65).setTimeInterval(7200).setBoost(0.04f),

					new Achievement(11, R.string.str_achieve_11_title,
							R.string.str_achieve_11_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 0; i < devices.size(); i++) {

								long time = devices.get(i).getTime();

								Date date = new Date(time);

								if (date.getMinutes() == 0
										&& date.getSeconds() == 0) {

									return true;

								}

							}

							return false;
						}

					}.setNumDevices(1).setBoost(0.08f),

					new Achievement(14, R.string.str_achieve_14_title,
							R.string.str_achieve_14_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 0; i < devices.size(); i++) {

								long time = devices.get(i).getTime();

								Date date = new Date(time);

								if (date.getDate() == 11
										&& date.getMonth() == 11
										&& date.getHours() == 11
										&& date.getMinutes() == 11
										&& date.getSeconds() == 11) {

									return true;

								}

							}

							return false;
						}

					}.setNumDevices(1).setBoost(0.25f),

					new Achievement(12, R.string.str_achieve_12_title,
							R.string.str_achieve_12_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getAllDevices();

							for (int i = 2; i < devices.size(); i++) {

								if (devices.get(i - 2).getManufacturer()
										.equals("Samsung")) {
									if (devices.get(i - 1).getManufacturer()
											.equals("BlackBerry")) {
										if (devices.get(i).getManufacturer()
												.equals("Apple")) {
											return true;
										}

									}

								}

							}

							return false;

						}

					}.setNumDevices(3)
							.setManufacturerList(
									Arrays.asList(new String[] { "Apple",
											"BlackBerry", "Samsung" }))
							.setBoost(0.04f),

					new Achievement(18, R.string.str_achieve_18_title,
							R.string.str_achieve_18_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getAllDevices();

							for (int i = 3; i < devices.size(); i++) {

								if (devices.get(i - 3).getManufacturer()
										.equals("D-Link")) {
									if (devices.get(i - 2).getManufacturer()
											.equals("Cisco")) {
										if (devices.get(i - 1)
												.getManufacturer()
												.equals("Bury")) {
											if (devices.get(i)
													.getManufacturer()
													.equals("Acer")) {
												return true;
											}
										}

									}

								}

							}

							return false;

						}

					}.setNumDevices(4)
							.setManufacturerList(
									Arrays.asList(new String[] { "Acer",
											"Bury", "Cisco", "D-Link" }))
							.setBoost(0.25f),

					new Achievement(13, R.string.str_achieve_13_title,
							R.string.str_achieve_13_description) {

						@Override
						public boolean check(BlueHunter bhApp, int deviceNum,
								int exp) {

							List<FoundDevice> devices = new DatabaseManager(
									bhApp).getDevices(
									DatabaseHelper.COLUMN_TIME + " != 0",
									DatabaseHelper.COLUMN_TIME + " DESC");

							for (int i = 0; i < devices.size(); i++) {

								long recentTime = devices.get(i).getTime();

								int exp24 = 0;

								for (int j = i + 1; j < devices.size(); j++) {

									long earlyTime = devices.get(j).getTime();

									if ((recentTime - earlyTime) < 86400000) {

										float bonus = devices.get(j).getBonus();

										if (bonus == -1f)
											bonus = 0.0f;

										exp24 += MacAddressAllocations
												.getExp(devices.get(j)
														.getManufacturer())
												* (1 + bonus);
									} else {
										break;
									}

								}

								if (exp24 >= 200)
									return true;
							}

							return false;
						}

					}.setNumExp(200).setTimeInterval(86400).setBoost(0.03f)

			});

	public static void checkAchievements(BlueHunter bhApp, boolean completeCheck) {

		achievementStates = new HashMap<Integer, Boolean>();

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {

			boolean alreadyAccomplished = PreferenceManager.getPref(bhApp,
					"pref_achievement_" + achievement.getId(), "").equals(
					bhApp.authentification.getAchieveHash(achievement.getId()));

			if (!completeCheck) {
				if (alreadyAccomplished) {
					achievementStates.put(achievement.getId(), true);
				} else {

					if (achievement.check(bhApp, deviceNum, exp)) {
						achievement.accomplish(bhApp, alreadyAccomplished);
						achievementStates.put(achievement.getId(), true);
					} else {
						achievement.invalidate(bhApp);
						achievementStates.put(achievement.getId(), false);
					}
				}
			} else {
				if (achievement.check(bhApp, deviceNum, exp)) {
					achievement.accomplish(bhApp, alreadyAccomplished);
					achievementStates.put(achievement.getId(), true);
				} else {
					achievement.invalidate(bhApp);
					achievementStates.put(achievement.getId(), false);
				}
			}

		}

		float boost = getBonus();

		NumberFormat pFormat = DecimalFormat.getPercentInstance();

		MenuItem boostIndicator = bhApp.actionBarHandler
				.getMenuItem(R.id.menu_boostIndicator);
		boostIndicator.setTitleCondensed(bhApp.getString(
				R.string.str_achievement_totalBoost, pFormat.format(boost)));

	}

	public static void checkSpecificAchievements(BlueHunter bhApp,
			boolean completeCheck, Integer... id) {

		List<Integer> ids = Arrays.asList(id);

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
		int exp = LevelSystem.getUserExp(bhApp);

		for (Achievement achievement : achievements) {
			if (!ids.contains(achievement.getId())) {

				boolean alreadyAccomplished = PreferenceManager.getPref(bhApp,
						"pref_achievement_" + achievement.getId(), "").equals(
						bhApp.authentification.getAchieveHash(achievement
								.getId()));

				if (!completeCheck) {
					if (alreadyAccomplished) {
						achievementStates.put(achievement.getId(), true);
					} else {

						if (achievement.check(bhApp, deviceNum, exp)) {
							achievement.accomplish(bhApp, alreadyAccomplished);
							achievementStates.put(achievement.getId(), true);
						} else {
							achievement.invalidate(bhApp);
							achievementStates.put(achievement.getId(), false);
						}
					}
				} else {
					if (achievement.check(bhApp, deviceNum, exp)) {
						achievement.accomplish(bhApp, alreadyAccomplished);
						achievementStates.put(achievement.getId(), true);
					} else {
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
