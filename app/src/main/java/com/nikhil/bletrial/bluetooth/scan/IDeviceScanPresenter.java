package com.nikhil.bletrial.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Created by nikhil on 28/03/17.
 */

public interface IDeviceScanPresenter {

    void initiateScanProcess(BluetoothAdapter bluetoothAdapter);
    void stopScanProcess();

    void connectDevice(BluetoothDevice device);

    void sendCommand(BluetoothDevice device, int command);

}
