package com.maksl5.bl_hunt;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.maksl5.bl_hunt.DatabaseManager.DatabaseHelper;
import com.maksl5.bl_hunt.MainActivity.CustomSectionFragment;



/**
 * 
 * @author Maksl5[Markus Bensing]
 * 
 */

public class FragmentLayoutManager {

	private LayoutInflater parentInflater;
	private ViewGroup rootContainer;
	private Bundle params;
	private Context parentContext;
	private MainActivity mainActivity;

	public FragmentLayoutManager(LayoutInflater inflater,
			ViewGroup container,
			Bundle args,
			MainActivity mainActivity) {

		parentInflater = inflater;
		rootContainer = container;
		params = args;
		parentContext = mainActivity;
		this.mainActivity = mainActivity;

	}

	public View getSpecificView() {

		int sectionNumber = params.getInt(CustomSectionFragment.ARG_SECTION_NUMBER);

		switch (sectionNumber) {
		case 1:
			return parentInflater.inflate(R.layout.act_page_discovery, rootContainer, false);
		case 2:
			return parentInflater.inflate(R.layout.act_page_leaderboard, rootContainer, false);
		case 3:
			return parentInflater.inflate(R.layout.act_page_founddevices, rootContainer, false);
		case 4:

		}

		return new View(parentContext);
	}

	public static void refreshFoundDevicesList(MainActivity mainActivity) {

		ListView lv = (ListView) mainActivity.mViewPager.getChildAt(3).findViewById(R.id.listView2);


		List<HashMap<String, String>> devices = new DatabaseManager(mainActivity, mainActivity.versionCode).getAllDevices();
		List<HashMap<String, String>> listViewHashMaps = new ArrayList<HashMap<String, String>>();

		HashMap<String, String[]> macHashMap = MacAdressAllocations.getHashMap();
		HashMap<String, Integer> expHashMap = MacAdressAllocations.getExpHashMap();

		Set<String> keySet = macHashMap.keySet();
		for (HashMap<String, String> hashMap : devices) {
			HashMap<String, String> tempDataHashMap = new HashMap<String, String>();

			tempDataHashMap.put("macAddress", hashMap.get(DatabaseHelper.COLUMN_MAC_ADDRESS));
			tempDataHashMap.put("name", hashMap.get(DatabaseHelper.COLUMN_NAME));
			tempDataHashMap.put("RSSI", "RSSI: " + hashMap.get(DatabaseHelper.COLUMN_RSSI));

			boolean foundManufacturer = false;

			for (String key : keySet) {
				String[] specificMacs = macHashMap.get(key);

				for (String mac : specificMacs) {
					if (hashMap.get(DatabaseHelper.COLUMN_MAC_ADDRESS).startsWith(mac)) {
						foundManufacturer = true;
						tempDataHashMap.put("manufacturer", key);
						tempDataHashMap.put("exp", "+" + expHashMap.get(key.replace(" ", "_") + "_exp") + " " + mainActivity.getString(R.string.str_foundDevices_exp_abbreviation));
					}
				}

			}

			if (!foundManufacturer) {
				tempDataHashMap.put("manufacturer", "Unknown");
				tempDataHashMap.put("exp", String.valueOf(expHashMap.get("Unknown_exp")));
			}
			
			
			listViewHashMaps.add(tempDataHashMap);
			

		}

		String[] from = {
							"macAddress", "manufacturer", "exp", "RSSI", "name" };
		int[] to = {
					R.id.macTxtView, R.id.manufacturerTxtView, R.id.expTxtView, R.id.rssiTxtView, R.id.nameTxtView };

		SimpleAdapter sAdapter =
				new SimpleAdapter(mainActivity, listViewHashMaps, R.layout.act_page_founddevices_row, from, to);
		
		
		lv.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			
			@Override
			public void onChildViewRemoved(	View parent,
											View child) {
			
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onChildViewAdded(	View parent,
											View child) {
				
				final TextView nameTextView = (TextView) child.findViewById(R.id.nameTxtView);
				if(nameTextView.getText().equals("")) {
					nameTextView.addTextChangedListener(new TextWatcher() {
						
						@Override
						public void onTextChanged(	CharSequence s,
													int start,
													int before,
													int count) {
						if(!s.equals("")){
							TableRow nameTableRow = (TableRow) nameTextView.getParent();
							nameTableRow.setVisibility(TableRow.VISIBLE);
							nameTextView.removeTextChangedListener(this);
							
						}
							
						}
						
						@Override
						public void beforeTextChanged(	CharSequence s,
														int start,
														int count,
														int after) {
						
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void afterTextChanged(Editable s) {
						

							
						}
					});
					TableRow nameTableRow = (TableRow) nameTextView.getParent();
					nameTableRow.setVisibility(TableRow.GONE);
					
				}
			
				// TODO Auto-generated method stub
				
			}
		});
		
		int scroll = lv.getFirstVisiblePosition();
		lv.setAdapter(sAdapter);
		lv.setSelection(scroll);
	}

}
