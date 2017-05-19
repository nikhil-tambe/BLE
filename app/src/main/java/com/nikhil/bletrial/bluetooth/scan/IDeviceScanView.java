package com.nikhil.bletrial.bluetooth.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import java.util.HashMap;

/**
 * Created by nikhil on 27/03/17.
 */

public interface IDeviceScanView {

    void onListLoadedSuccessfully(HashMap<String, ScanResult> hashMap);
    void onListLoadedFailed(int FAILURE_CODE);

}
