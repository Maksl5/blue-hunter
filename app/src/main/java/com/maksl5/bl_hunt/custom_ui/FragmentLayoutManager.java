package com.maksl5.bl_hunt.custom_ui;

import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity.CustomSectionFragment;
import com.maksl5.bl_hunt.custom_ui.fragment.DeviceDiscoveryLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Maksl5[Markus Bensing]
 */

public class FragmentLayoutManager {

	public static final int PAGE_DEVICE_DISCOVERY = 0;
	public static final int PAGE_LEADERBOARD = 1;
	public static final int PAGE_FOUND_DEVICES = 2;
	public static final int PAGE_ACHIEVEMENTS = 3;
	public static final int PAGE_PROFILE = 4;
	public static final int PAGE_STATISTICS = 5;

	public static View getSpecificView(Bundle params, LayoutInflater parentInflater, ViewGroup rootContainer, Context context) {

		int sectionNumber = params.getInt(CustomSectionFragment.ARG_SECTION_NUMBER);

		switch (sectionNumber) {
		case PAGE_DEVICE_DISCOVERY:

			DeviceDiscoveryLayout.lv = null;
			DeviceDiscoveryLayout.dAdapter = null;

			return parentInflater.inflate(R.layout.act_page_discovery, rootContainer, false);
		case PAGE_LEADERBOARD:
			return parentInflater.inflate(R.layout.act_page_leaderboard, rootContainer, false);
		case PAGE_FOUND_DEVICES:
			return parentInflater.inflate(R.layout.act_page_founddevices, rootContainer, false);
		case PAGE_ACHIEVEMENTS:
			return parentInflater.inflate(R.layout.act_page_achievements, rootContainer, false);
		case PAGE_PROFILE:
			return parentInflater.inflate(R.layout.act_page_profile, rootContainer, false);
		case PAGE_STATISTICS:
			return parentInflater.inflate(R.layout.act_page_statistics, rootContainer, false);

		}

		return new View(context);
	}

}
