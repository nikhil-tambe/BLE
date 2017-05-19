package com.nikhil.bletrial.bluetooth.utils;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;

import java.util.HashMap;
import java.util.List;

/**
 * Created by nikhil on 28/03/17.
 */

public interface OnBLEInteractorFinishedListener {

    void onScanSuccess(HashMap<String, ScanResult> scanResultHashMap);
    void onScanFailure(int FAILURE_CODE);

    void connectionState(int state);

    void servicesDiscovered(List<BluetoothGattService> bluetoothGattServiceList);

    void onDataReceived(String raw, String formatted);

}
