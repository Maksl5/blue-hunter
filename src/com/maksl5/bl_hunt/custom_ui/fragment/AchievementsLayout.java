package com.maksl5.bl_hunt.custom_ui.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.storage.Achievement;
import com.maksl5.bl_hunt.storage.AchievementSystem;

/**
 * @author Maksl5[Markus Bensing]
 */
public class AchievementsLayout {

	public static void initializeAchievements(BlueHunter bhApp) {

		AchievementSystem.checkAchievements(bhApp, true);

		ListView lv = (ListView) bhApp.mainActivity.findViewById(R.id.listView_ach);

		int[] to = new int[] { R.id.txtName, R.id.txtDescription, R.id.txtBoost, R.id.chkBox };
		String[] from = new String[] { "name", "description", "boost", "accomplished" };

		ViewBinder viewBinder = new ViewBinder() {

			@Override
			public boolean setViewValue(View view,
										Object data,
										String textRepresentation) {

				if (view instanceof CheckBox) {

					if (((String) data).equals("true")) {
						((CheckBox) view).setChecked(true);
					}
					else if (((String) data).equals("false")) {
						((CheckBox) view).setChecked(false);
					}

					return true;

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

			rows.add(dataHashMap);

		}

		SimpleAdapter simpleAdapter =
				new SimpleAdapter(bhApp.mainActivity, rows, R.layout.act_page_achievements_row, from, to);
		simpleAdapter.setViewBinder(viewBinder);
		lv.setAdapter(simpleAdapter);

	}

}