package com.nikhil.bletrial.bluetooth.utils.manufacturer_id;

import java.util.ArrayList;

public class AdParser {
	public static ArrayList<AdElement> parseAdData(byte data[]) {
		int pos=0;
		ArrayList<AdElement> out = new ArrayList<AdElement>();
		int dlen = data.length;
		while((pos+1) < dlen) {
			int bpos = pos;
			int blen = ((int)data[pos]) & 0xFF;
			if( blen == 0 )
				break;
			if( bpos+blen > dlen )
				break;
			++pos;
			int type = ((int)data[pos]) & 0xFF;
			++pos;
			int len = blen - 1;
			AdElement e = null;
			switch( type ) {

			case 0xFF:
				e = new TypeManufacturerData(data,pos,len);
				break;

			}
			out.add(e);
			pos = bpos + blen+1;
		}
		return out;
	}
	
}
