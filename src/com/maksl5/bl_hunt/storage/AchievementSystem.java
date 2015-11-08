/**
 *  AchievementSystem.java in com.maksl5.bl_hunt.storage
 *  Â© Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.storage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.custom_ui.fragment.AchievementsLayout;
import com.maksl5.bl_hunt.storage.DatabaseManager.DatabaseHelper;
import com.maksl5.bl_hunt.util.Achievement;
import com.maksl5.bl_hunt.util.FoundDevice;

import android.bluetooth.BluetoothHealthAppConfiguration;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;

/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class AchievementSystem {

	private static float boost = 0.0f;

	public static HashMap<Integer, Boolean> achievementStates;
	public static HashMap<Integer, Boolean> temporaryStates;

	private static AchievementsCheckerThread checkerThread;
	
	private static List<FoundDevice> allDevices;

	static int checksInRow = 0;

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

					int max = 0;

					// 5 Apple in row

					for (int i = 4; i < allDevices.size(); i++) {

						if (allDevices.get(i - 4).getManufacturer() == 1) {
							max = (max < 1) ? 1 : max;
							if (allDevices.get(i - 3).getManufacturer() == 1) {
								max = (max < 2) ? 2 : max;
								if (allDevices.get(i - 2).getManufacturer() == 1) {
									max = (max < 3) ? 3 : max;
									if (allDevices.get(i - 1).getManufacturer() == 1) {
										max = (max < 4) ? 4 : max;
										if (allDevices.get(i).getManufacturer() == 1) {
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

					// 15D in 2min

					for (int i = 14; i < allDevices.size(); i++) {

						long firstTime = allDevices.get(i - 14).getTime();
						long secondTime = allDevices.get(i).getTime();

						if (firstTime != 0 && secondTime != 0 && (firstTime - secondTime) <= 120000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.05f),

			new Achievement(10, R.string.str_achieve_10_title, R.string.str_achieve_10_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					// 100D in 15min

					for (int i = 99; i < allDevices.size(); i++) {

						long firstTime = allDevices.get(i - 99).getTime();
						long secondTime = allDevices.get(i).getTime();

						if (firstTime != 0 && secondTime != 0 && (firstTime - secondTime) <= 900000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.06f),

			new Achievement(16, R.string.str_achieve_16_title, R.string.str_achieve_16_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					// 20D in 15sec

					for (int i = 19; i < allDevices.size(); i++) {

						long firstTime = allDevices.get(i - 19).getTime();
						long secondTime = allDevices.get(i).getTime();

						if (firstTime != 0 && secondTime != 0 && (firstTime - secondTime) <= 15000) {

							return true;
						}
					}

					return false;
				}

			}.setBoost(0.12f),

			new Achievement(7, R.string.str_achieve_7_title, R.string.str_achieve_7_description, true) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					int devices = new DatabaseManager(bhApp).getDeviceNum(DatabaseHelper.COLUMN_MANUFACTURER + " = '21'"); // Siemens

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

					int devices = new DatabaseManager(bhApp).getDeviceNum(DatabaseHelper.COLUMN_MANUFACTURER + " = '9'"); // Sony
																															// Ericsson

					setNewProgressString("" + devices + " / 50");

					if (devices >= 50)
						return true;
					else
						return false;
				}

			}.setBoost(0.08f),

			new Achievement(8, R.string.str_achieve_8_title, R.string.str_achieve_8_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					// 15D between 10:00 - 10:30pm

					for (int i = 14; i < allDevices.size(); i++) {

						long firstTime = allDevices.get(i - 14).getTime();
						long secondTime = allDevices.get(i).getTime();

						if (firstTime != 0 && secondTime != 0 && (firstTime - secondTime) <= 1800000) {

							DateTime recentDate = new DateTime(firstTime);
							DateTime firstDate = new DateTime(secondTime);

							int recHour = recentDate.getHourOfDay();
							int firstHour = firstDate.getHourOfDay();
							int recMinute = recentDate.getMinuteOfHour();
							int firstMinute = firstDate.getMinuteOfHour();

							if (recHour == 22 && firstHour == 22) {
								if (recMinute >= 0 && recMinute < 30) {
									if (firstMinute >= 0 && firstMinute < 30) {
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

					// 65D between 5:00 - 7:00am

					for (int i = 64; i < allDevices.size(); i++) {

						long firstTime = allDevices.get(i - 64).getTime();
						long secondTime = allDevices.get(i).getTime();

						if (firstTime != 0 && secondTime != 0 && (firstTime - secondTime) <= 7200000) {

							DateTime recentDate = new DateTime(firstTime);
							DateTime firstDate = new DateTime(secondTime);

							int firstHour = firstDate.getHourOfDay();
							int recHour = recentDate.getHourOfDay();

							if (firstHour >= 5 && firstHour < 7) {
								if (recHour >= 5 && recHour < 7) {

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

					// 1D to full hour

					for (int i = 0; i < allDevices.size(); i++) {

						long time = allDevices.get(i).getTime();

						DateTime date = new DateTime(time);

						if (time != 0 && date.getMinuteOfHour() == 0) {

							return true;

						}

					}

					return false;
				}

			}.setBoost(0.08f),

			new Achievement(14, R.string.str_achieve_14_title, R.string.str_achieve_14_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					// 1D 11th Nov 11:11am

					for (int i = 0; i < allDevices.size(); i++) {

						long time = allDevices.get(i).getTime();

						DateTime date = new DateTime(time);

						if (time != 0 && date.getDayOfMonth() == 11 && date.getMonthOfYear() == 11 && date.getHourOfDay() == 11
								&& date.getMinuteOfHour() == 11) {

							return true;

						}

					}

					return false;
				}

			}.setBoost(0.40f),

			new Achievement(12, R.string.str_achieve_12_title, R.string.str_achieve_12_description) {

				@Override
				public boolean check(BlueHunter bhApp, int deviceNum, int exp) {

					// In row: 1 Apple -> 1 BlackBerry -> 1 Samsung

					for (int i = 2; i < allDevices.size(); i++) {

						if (allDevices.get(i - 2).getManufacturer() == 3) {
							if (allDevices.get(i - 1).getManufacturer() == 10) {
								if (allDevices.get(i).getManufacturer() == 1) {
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

					// In row: Acer -> Bury -> Cisco -> D-Link

					for (int i = 3; i < allDevices.size(); i++) {

						if (allDevices.get(i - 3).getManufacturer() == 51) {
							if (allDevices.get(i - 2).getManufacturer() == 30) {
								if (allDevices.get(i - 1).getManufacturer() == 50) {
									if (allDevices.get(i).getManufacturer() == 11) {
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

					long first = System.nanoTime();

					long end;
					int max = 0;

					int deviceListSize = allDevices.size();

					long recentTime;
					int exp24;
					long earlyTime;
					float bonus;

					for (int i = 0; i < deviceListSize; i++) {

						recentTime = allDevices.get(i).getTime();

						exp24 = 0;

						for (int j = i + 1; j < deviceListSize; j++) {

							earlyTime = allDevices.get(j).getTime();

							if (recentTime != 0 && earlyTime != 0 && (recentTime - earlyTime) < 86400000) {

								bonus = allDevices.get(j).getBoost();

								if (bonus == -1f) bonus = 0.0f;

								exp24 += ManufacturerList.getExp(allDevices.get(j).getManufacturer()) * (1 + bonus);
							}
							else {
								break;
							}

						}

						max = (exp24 > max) ? exp24 : max;

						if (exp24 >= 200) {

							end = System.nanoTime();
							float msPerDev = (end - first) / (float) i;
							Log.d("Exp Achievement Time:", "" + msPerDev / (float) 1000 + "µs/device");
							Log.d("Exp Achievement Time:", "Total time: " + ((end - first) / (float) 1000000) + "ms | Total dev: " + i);

							return true;
						}
					}

					setNewProgressString("" + max + " / 200");

					end = System.nanoTime();
					float msPerDev = (end - first) / (float) allDevices.size();
					Log.d("Exp Achievement Time:", "" + msPerDev / (float) 1000 + "µs/device");
					Log.d("Exp Achievement Time:",
							"Total time: " + ((end - first) / (float) 1000000) + "ms | Total dev: " + allDevices.size());

					return false;
				}

			}.setBoost(0.03f)

	});

	public static void checkAchievements(BlueHunter bhApp, boolean completeCheck) {

		if(checkerThread == null || !checkerThread.running) {
			checkerThread = new AchievementSystem().new AchievementsCheckerThread(bhApp);
			checkerThread.execute(completeCheck);
		}

	}

	public static void checkSpecificAchievements(BlueHunter bhApp, boolean completeCheck, Integer... id) {

		List<Integer> ids = Arrays.asList(id);

		int deviceNum = new DatabaseManager(bhApp).getDeviceNum();
		int exp = LevelSystem.getCachedUserExp(bhApp);

		allDevices = DatabaseManager.getCachedList();

		if (allDevices == null) {

			new DatabaseManager(bhApp).loadAllDevices(true);
			return;

		}

		for (Achievement achievement : achievements) {
			if (!ids.contains(achievement.getId())) {

				boolean alreadyAccomplished = PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "")
						.equals(bhApp.authentification.getAchieveHash(achievement.getId()));

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

		int level = LevelSystem.getLevel(LevelSystem.getCachedUserExp(bhApp));
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
		int curLevel = LevelSystem.getLevel(LevelSystem.getCachedUserExp(bhApp));

		for (int level = 0; level <= curLevel; level++) {
			levelBoost += (float) level / (float) 100;
		}

		return levelBoost;

	}

	public class AchievementsCheckerThread extends AsyncTask<Boolean, Integer, Integer> {

		BlueHunter bhApp;
		boolean running = false;

		boolean completeCheck = false;

		public AchievementsCheckerThread(BlueHunter bhApp) {
			this.bhApp = bhApp;
		}

		@Override
		protected void onPreExecute() {
			
			Log.d("AchievementsCheckerThread", "onPreExecute() called.");
			
			running = true;

		}

		@Override
		protected void onPostExecute(Integer result) {
			
			running = false;
			checksInRow++;
			
			Log.d("AchievementsCheckerThread", "onPostExecute() called | result = " + result + " | checksInRow = " + checksInRow);

			if (result == 0) {
				// allDevices was null
				if (checksInRow < 4) {
					// check if this thread was run under 4 times in a row

					checkAchievements(bhApp, completeCheck);

				}
				else {
					checksInRow = 0;
				}

			}
			else if (result == 1) {
				checksInRow = 0;

				achievementStates = new HashMap<Integer, Boolean>(temporaryStates);
				temporaryStates = null;
				
				// Update indicator views
				AchievementsLayout.initializeAchievements(bhApp);
				AchievementsLayout.updateBoostIndicator(bhApp);

			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}

		@Override
		protected Integer doInBackground(Boolean... params) {

			
			
			
			long checkATime = System.currentTimeMillis();

			completeCheck = params[0];
			Log.d("AchievementsCheckerThread", "doInBackground() called. | completeCheck = " + completeCheck);
			
			
			temporaryStates = new HashMap<Integer, Boolean>();

			allDevices = DatabaseManager.getCachedList();

			if (allDevices == null) {

				new DatabaseManager(bhApp).loadAllDevices(true);
				return 0;
			}

			int deviceNum = allDevices.size();
			int exp = LevelSystem.getCachedUserExp(bhApp);
			
			List<Achievement> temporaryAchievements =  new ArrayList<Achievement>(achievements);

			for (Achievement achievement : temporaryAchievements) {

				boolean alreadyAccomplished = PreferenceManager.getPref(bhApp, "pref_achievement_" + achievement.getId(), "")
						.equals(bhApp.authentification.getAchieveHash(achievement.getId()));

				if (!completeCheck) {
					if (alreadyAccomplished) {
						temporaryStates.put(achievement.getId(), true);
					}
					else {

						if (achievement.check(bhApp, deviceNum, exp)) {
							achievement.accomplish(bhApp, alreadyAccomplished);
							temporaryStates.put(achievement.getId(), true);
						}
						else {
							achievement.invalidate(bhApp);
							temporaryStates.put(achievement.getId(), false);
						}
					}
				}
				else {
					if (achievement.check(bhApp, deviceNum, exp)) {
						achievement.accomplish(bhApp, alreadyAccomplished);
						temporaryStates.put(achievement.getId(), true);
					}
					else {
						achievement.invalidate(bhApp);
						temporaryStates.put(achievement.getId(), false);
					}
				}

			}
			
			temporaryAchievements = null;

			long checkBTime = System.currentTimeMillis();

			Log.d("checkAchievements()", "" + (checkBTime - checkATime) + "ms");

			return 1;
		}

	}

}
