package com.maksl5.bl_hunt.util;

public class MacAddress {

	private short a, b, c, d, e, f;
	
	private String macAdress;

	public MacAddress(short a, short b, short c, short d, short e, short f) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}

	public MacAddress(String macAddress) {
		a = Short.parseShort(macAddress.substring(0, 2), 16);
		b = Short.parseShort(macAddress.substring(3, 5), 16);
		c = Short.parseShort(macAddress.substring(6, 8), 16);
		d = Short.parseShort(macAddress.substring(9, 11), 16);
		e = Short.parseShort(macAddress.substring(12, 14), 16);
		f = Short.parseShort(macAddress.substring(15, 17), 16);
	}

	public short getA() {
		return a;
	}

	public short getB() {
		return b;
	}

	public short getC() {
		return c;
	}

	public short getD() {
		return d;
	}

	public short getE() {
		return e;
	}

	public short getF() {
		return f;
	}

	public String getMacString() {
		if(macAdress == null || macAdress.length() == 0) {
			macAdress = String.format("%02X:%02X:%02X:%02X:%02X:%02X", a, b, c, d, e, f);
		}
		return macAdress;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		result = prime * result + c;
		result = prime * result + d;
		result = prime * result + e;
		result = prime * result + f;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MacAddress other = (MacAddress) obj;
		if (a != other.a) return false;
		if (b != other.b) return false;
		if (c != other.c) return false;
		if (d != other.d) return false;
		if (e != other.e) return false;
		if (f != other.f) return false;
		return true;
	}

}
