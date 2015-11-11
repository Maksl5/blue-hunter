package com.maksl5.bl_hunt.custom_ui.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.maksl5.bl_hunt.R;
import com.maksl5.bl_hunt.activity.MainActivity;
import com.maksl5.bl_hunt.storage.DatabaseManager;
import com.maksl5.bl_hunt.storage.ManufacturerList;
import com.maksl5.bl_hunt.util.FoundDevice;
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

public class StatisticsFragment {

	public static void initializeStatisticsView(final MainActivity mainActivity) {

		ListView listView = (ListView) mainActivity.findViewById(R.id.SlistView);

		ArrayList<String> categories = new ArrayList<String>();

		categories.add(mainActivity.getString(R.string.str_statistics_manufacturerCount_title));

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, categories);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
				case 0:

					manufacturerCount(mainActivity);
					break;

				default:
					break;
				}

			}
		});

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

			Iterator iterator = sortedMap.keySet().iterator();

			List<HashMap<String, String>> dialogMappings = new ArrayList<HashMap<String, String>>();

			while (iterator.hasNext()) {

				Integer manufacturerID = (Integer) iterator.next();

				String name = ManufacturerList.getName(manufacturerID);

				HashMap<String, String> fromToHashMap = new HashMap<String, String>();

				fromToHashMap.put("name", name);
				fromToHashMap.put("count", "" + manuCountMappings.get(manufacturerID));

				dialogMappings.add(fromToHashMap);

			}

			int[] to = new int[] {
					R.id.descriptionTxt, R.id.boostTxt };
			String[] from = new String[] {
					"name", "count" };

			SimpleAdapter boostAdapater = new SimpleAdapter(mainActivity, dialogMappings, R.layout.dlg_boost_composite_row, from, to);

			AlertDialog.Builder builder = new Builder(mainActivity);
			builder.setTitle(R.string.str_statistics_manufacturerCount_title);

			builder.setAdapter(boostAdapater, null);
			builder.setNeutralButton("Ok", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});

			builder.create().show();

		}
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
