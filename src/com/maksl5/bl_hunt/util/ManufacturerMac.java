package com.maksl5.bl_hunt.util;

public class ManufacturerMac {

	short one, two, three;

	public ManufacturerMac(short one, short two, short three) {
		this.one = one;
		this.two = two;
		this.three = three;
	}
	
	public ManufacturerMac(String macAddress) {
		one = Short.parseShort(macAddress.substring(0, 2), 16);
		two = Short.parseShort(macAddress.substring(3, 5), 16);
		three = Short.parseShort(macAddress.substring(6, 8), 16);
	}

	@Override
	public boolean equals(Object o) {

		if (o == null)
			return false;

		if (o instanceof ManufacturerMac) {
			
			ManufacturerMac manuMac = ((ManufacturerMac) o);
			
			if(manuMac.one == one && manuMac.two == two && manuMac.three == three)
				return true;
			else 
				return false;

		} else {
			return false;
		}

	}
	
	public boolean equals(ManufacturerMac manuMac) {
		if(manuMac != null && manuMac.one == one && manuMac.two == two && manuMac.three == three)
			return true;
		else 
			return false;
	}
}