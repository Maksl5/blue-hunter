package com.maksl5.bl_hunt.custom_ui.fragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.AchievementSystem;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.util.Achievement;

import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

/**
 * @author Maksl5[Markus Bensing]
 */
public class AchievementsLayout {

	public static void initializeAchievements(BlueHunter bhApp) {

		ListView lv = (ListView) bhApp.mainActivity.findViewById(R.id.listView_ach);

		int[] to = new int[] {
				R.id.txtName, R.id.txtDescription, R.id.txtBoost, R.id.chkBox, R.id.txtProgress };
		String[] from = new String[] {
				"name", "description", "boost", "accomplished", "progress" };

		ViewBinder viewBinder = new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {

				if (view instanceof CheckBox) {

					if (((String) data).equals("true")) {
						((CheckBox) view).setChecked(true);
					}
					else if (((String) data).equals("false")) {
						((CheckBox) view).setChecked(false);
					}

					return true;

				}

				if (view instanceof TextView && view.getId() == R.id.txtProgress) {
					TextView txtProgress = (TextView) view;
					String progressString = (String) data;

					if (progressString.equals("none")) {
						txtProgress.setVisibility(TextView.GONE);
						return true;
					}
					else {
						txtProgress.setText(progressString);
						return true;
					}

				}

				return false;
			}
		};

		List<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();

		for (Achievement achievement : AchievementSystem.achievements) {

			HashMap<String, String> dataHashMap = new HashMap<String, String>();

			dataHashMap.put("name", achievement.getName(bhApp));
			dataHashMap.put("description", achievement.getDescription(bhApp));
			dataHashMap.put("boost", String.format("Boost: + %s", NumberFormat.getPercentInstance().format(achievement.getBoost())));

			boolean accomplished = false;

			if (AchievementSystem.achievementStates.containsKey(achievement.getId())) {
				accomplished = AchievementSystem.achievementStates.get(achievement.getId());
			}

			dataHashMap.put("accomplished", String.valueOf(accomplished));

			if (achievement.hasProgress() && !accomplished) {
				dataHashMap.put("progress", achievement.getProgressString());
			}
			else {
				dataHashMap.put("progress", "none");
			}

			if (!achievement.isHidden()) {
				rows.add(dataHashMap);
			}
			else {
				if (accomplished) {
					rows.add(dataHashMap);
				}
			}
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(bhApp.mainActivity, rows, R.layout.act_page_achievements_row, from, to);
		simpleAdapter.setViewBinder(viewBinder);
		lv.setAdapter(simpleAdapter);

	}

	public static void updateBoostIndicator(BlueHunter bhApp) {

		float boost = AchievementSystem.getBoost(bhApp);

		NumberFormat pFormat = DecimalFormat.getPercentInstance();

		MenuItem boostIndicator = bhApp.actionBarHandler.getMenuItem(R.id.menu_boostIndicator);
		boostIndicator.setTitleCondensed(bhApp.getString(R.string.str_achievement_totalBoost, pFormat.format(boost)));

	}


}