package com.maksl5.bl_hunt.custom_ui.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.maksl5.bl_hunt.LevelSystem;
import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.util.FoundDevice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsFragment {

	private final static String DIALOG_ENTRY_NAME = "name";
	private final static String DIALOG_ENTRY_COUNT = "count";

	public static void initializeStatisticsView(final MainActivity mainActivity) {

		ListView listView = (ListView) mainActivity.findViewById(R.id.SlistView);

		ArrayList<String> categories = new ArrayList<String>();

		categories.add(mainActivity.getString(R.string.str_statistics_general_title));
		categories.add(mainActivity.getString(R.string.str_statistics_manufacturerCount_title));

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, categories);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
				case 0:

					general(mainActivity);
					break;
				case 1:

					manufacturerCount(mainActivity);
					break;

				default:
					break;
				}

			}
		});

	}

	private static void general(MainActivity mainActivity) {

		int userExp = LevelSystem.getCachedUserExp(mainActivity.getBlueHunter());
		int deviceNum = DatabaseManager.getCachedList().size();

		List<HashMap<String, String>> dialogMappings = new ArrayList<HashMap<String, String>>();

		float averageExpPerDevice = userExp / (float) deviceNum;

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put(DIALOG_ENTRY_NAME, "Average Exp / Device");
		hashMap.put(DIALOG_ENTRY_COUNT, String.format("%.2f", averageExpPerDevice));
		
		dialogMappings.add(hashMap);
		
		showDialog(mainActivity, dialogMappings, R.string.str_statistics_general_title);

	}

	private static void manufacturerCount(MainActivity mainActivity) {

		if (DatabaseManager.getCachedList() == null) {
			new DatabaseManager(mainActivity.getBlueHunter()).loadAllDevices(true);
		}
		else {

			ArrayList<FoundDevice> allDevices = DatabaseManager.getCachedList();
			HashMap<Integer, Integer> manuCountMappings = new HashMap<Integer, Integer>();

			for (FoundDevice foundDevice : allDevices) {

				int manufacturerID = foundDevice.getManufacturer();

				if (!manuCountMappings.containsKey(manufacturerID)) {
					manuCountMappings.put(manufacturerID, 1);
				}
				else {

					int count = manuCountMappings.get(manufacturerID);
					count++;
					manuCountMappings.put(manufacturerID, count);

				}

			}

			ValueComparator valueComparator = new StatisticsFragment().new ValueComparator(manuCountMappings);
			TreeMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>(valueComparator);
			sortedMap.putAll(manuCountMappings);

			Iterator<Integer> iterator = sortedMap.keySet().iterator();

			List<HashMap<String, String>> dialogMappings = new ArrayList<HashMap<String, String>>();

			while (iterator.hasNext()) {

				Integer manufacturerID = iterator.next();

				String name = ManufacturerList.getName(manufacturerID);

				HashMap<String, String> fromToHashMap = new HashMap<String, String>();

				fromToHashMap.put(DIALOG_ENTRY_NAME, name);
				fromToHashMap.put(DIALOG_ENTRY_COUNT, "" + manuCountMappings.get(manufacturerID));

				dialogMappings.add(fromToHashMap);

			}

			showDialog(mainActivity, dialogMappings, R.string.str_statistics_manufacturerCount_title);

		}
	}

	private static void showDialog(MainActivity mainActivity, List<HashMap<String, String>> dialogMappings, int titleResource) {

		int[] to = new int[] {
				R.id.descriptionTxt, R.id.boostTxt };
		String[] from = new String[] {
				DIALOG_ENTRY_NAME, DIALOG_ENTRY_COUNT };

		SimpleAdapter boostAdapater = new SimpleAdapter(mainActivity, dialogMappings, R.layout.dlg_boost_composite_row, from, to);

		AlertDialog.Builder builder = new Builder(mainActivity);
		builder.setTitle(titleResource);

		builder.setAdapter(boostAdapater, null);
		builder.setNeutralButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		builder.create().show();

	}

	class ValueComparator implements Comparator<Integer> {

		Map<Integer, Integer> map;

		public ValueComparator(Map<Integer, Integer> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer lhs, Integer rhs) {
			if (map.get(lhs) >= map.get(rhs)) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}

}
