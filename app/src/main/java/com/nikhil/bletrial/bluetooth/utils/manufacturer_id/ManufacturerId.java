package com.nikhil.bletrial.bluetooth.utils.manufacturer_id;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

/**
 * Created by nikhil on 06/04/17.
 */

public class ManufacturerId {

    public String getManufacturerId(ScanResult scanResult) {
        String manufacturerID;

        try {
            byte[] scanRecord = scanResult.getScanRecord().getBytes();
            String additionalData = null;
            StringBuffer b = new StringBuffer();
            int byteCtr = 0;
            for (int i = 0; i < scanRecord.length; ++i) {
                if (byteCtr > 0) {
                    b.append(" ");
                }
                b.append(Integer.toHexString(((int) scanRecord[i]) & 0xFF));
                ++byteCtr;
                if (byteCtr == 8) {
                    byteCtr = 0;
                    b = new StringBuffer();
                }
            }
            ArrayList<AdElement> ads = AdParser.parseAdData(scanRecord);
            if (ads != null) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < ads.size(); ++i) {
                    AdElement e = ads.get(i);
                    if (e != null) {
                        if (i > 0) {
                            sb.append(" ; ");
                        }
                        sb.append(e.toString());
                    }
                }
                additionalData = new String(sb);
            }

            manufacturerID = additionalData.replace(",", "");
        } catch (Exception e) {
            return "";
        }

        return manufacturerID.replace(";", "");
    }

}
