package com.maksl5.bl_hunt.util;

public class Manufacturer {

	private int id;
	private ManufacturerMac[] macAddresses;
	private String name;
	private int exp;

	public Manufacturer(int id, String name, int exp, ManufacturerMac[] macAddresses) {
		this.id = id;
		this.macAddresses = macAddresses;
		this.name = name;
		this.exp = exp;

	}

	@Override
	public boolean equals(Object o) {

		if (o == null)
			return false;

		if (o instanceof Integer) {
			if (((Integer) o) == id)
				return true;
		} else if (o instanceof String) {

			if (o.equals(name)) {
				return true;
			}
		}

		return false;
	}

	public int getId() {
		return id;
	}

	public ManufacturerMac[] getMacAddresses() {
		return macAddresses;
	}

	public String getName() {
		return name;
	}

	public int getExp() {
		return exp;
	}

}
