/**
 *  FoundDevice.java in com.maksl5.bl_hunt.custom_ui
 *  © Markus 2014
 */
package com.maksl5.bl_hunt.custom_ui;

import com.maksl5.bl_hunt.storage.MacAddressAllocations;



public class FoundDevice {

	private String macAddress;
	private String name;
	private short rssi = 1;
	private long time = -1;
	private String manufacturer;
	private float bonus = -1f;

	public FoundDevice() {

	}

	public void setMac(String dMac) {

		macAddress = dMac;

	}

	public void setName(String dName) {

		name = dName;

	}

	public void setRssi(short dRssi) {

		rssi = dRssi;

	}

	public void setRssi(String dRssi) {

		if (dRssi != null) rssi = Short.valueOf(dRssi);

	}

	public void setTime(long dTime) {

		time = dTime;

	}

	public void setTime(String dTime) {

		if (dTime != null) time = Long.valueOf(dTime);

	}
	
	public void setManu(String dManu) {

		manufacturer = dManu;
		
	}

	public void setBonus(float dBonus) {

		bonus = dBonus;

	}

	public void setBonus(String dBonus) {

		if (dBonus != null) bonus = Float.valueOf(dBonus);

	}

	public String getMacAddress() {

		return macAddress;
	}

	public String getName() {

		return name;
	}

	public short getRssi() {

		return rssi;
	}

	public long getTime() {

		return time;
	}

	public String getManufacturer() {

		if(manufacturer == null) {
			manufacturer = MacAddressAllocations.getManufacturer(macAddress);
		}
		return manufacturer;
	}

	public float getBonus() {

		return bonus;
	}

	public int checkNull() {

		if (macAddress == null || macAddress.equals("")) return 0;
		if (name == null || name.equals("")) return -1;
		if (rssi == 0) return 0;
		if (time == 0) return 0;
		if (manufacturer == null || manufacturer.equals("")) return 0;

		return 1;
	}

}