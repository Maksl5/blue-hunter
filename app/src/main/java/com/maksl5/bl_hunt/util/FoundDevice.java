/**
 *  FoundDevice.java in com.maksl5.bl_hunt.custom_ui
 *  © Markus 2014
 */
package com.maksl5.bl_hunt.util;

import com.maksl5.bl_hunt.storage.ManufacturerList;

public class FoundDevice {

	private MacAddress macAddress;
	private String name;
	private short rssi = 1;
	private long time = -1;
	private int manufacturer = -1;
	private float bonus = -1f;
	private boolean isOld = false;

	public FoundDevice() {

	}

	public void setMac(String dMac) {

		macAddress = new MacAddress(dMac);

	}

	public void setMac(MacAddress dMac) {

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

	public void setManu(int dManu) {

		manufacturer = dManu;

	}

	public void setBoost(float dBonus) {

		bonus = dBonus;

	}

	public void setBoost(String dBonus) {

		if (dBonus != null) {

			if (dBonus.equals(""))
				bonus = 0f;
			else
				bonus = Float.valueOf(dBonus);

		}
		else {
			bonus = 0f;
		}

	}

	public String getMacAddressString() {

		return macAddress.getMacString();
	}

	public MacAddress getMacAddress() {

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

	public int getManufacturer() {

		if (manufacturer == -1) {
			manufacturer = ManufacturerList.getManufacturer(macAddress.getA(), macAddress.getB(), macAddress.getC()).getId();
		}
		return manufacturer;
	}

	public float getBoost() {

		return bonus;
	}

	public void setOld(boolean old) {
		this.isOld = old;
	}

	public boolean isOld() {
		return isOld;
	}

	public int checkNull() {

		if (macAddress == null) return 0;
		if (name == null || name.equals("")) return -1;
		if (rssi == 0) return 0;
		if (time == 0) return 0;
		if (manufacturer == -1) return -2;

		return 1;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FoundDevice)) return false;

		if (((FoundDevice) o).macAddress.equals(macAddress))
			return true;
		else
			return false;
	}

}