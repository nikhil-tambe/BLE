package com.nikhil.bletrial.bluetooth.utils.manufacturer_id;

public abstract class AdElement {
	public abstract String toString();

	private static char hexDigit(int v,int shift) {
		int v1 = ( v >> shift ) & 0xF;
		return hexDigits.charAt(v1);
	}

	public static String hex8(int v) {
		return ""+
				hexDigit(v,4)+
				hexDigit(v,0);
	}

	public static String hex16(int v) {
		return ""+hexDigit(v,12)+
				hexDigit(v,8)+
				hexDigit(v,4)+
				hexDigit(v,0);
	}

	public static String hex32(int v) {
		return ""+hexDigit(v,28)+
				hexDigit(v,24)+
				hexDigit(v,20)+
				hexDigit(v,16)+
				hexDigit(v,12)+
				hexDigit(v,8)+
				hexDigit(v,4)+
				hexDigit(v,0);
	}

	
	private static String hexDigits = "0123456789ABCDEF";
}
