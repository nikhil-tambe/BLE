package com.nikhil.bletrial.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.nikhil.bletrial.bluetooth.utils.BLEInteractor;
import com.nikhil.bletrial.bluetooth.utils.OnBLEInteractorFinishedListener;

import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by nikhil on 27/03/17.
 */

public class DeviceScanPresenter implements IDeviceScanPresenter, OnBLEInteractorFinishedListener {

    private BLEInteractor interactor;
    private IDeviceScanView view;
    private Context context;

    public DeviceScanPresenter(IDeviceScanView view, Context context) {
        this.view = view;
        interactor = new BLEInteractor(this);
        this.context = context;
    }

    @Override
    public void initiateScanProcess(BluetoothAdapter bluetoothAdapter) {
        interactor.initiateScanProcess(bluetoothAdapter);
    }

    @Override
    public void stopScanProcess() {
        interactor.scanLeDevice(false);
    }

    @Override
    public void connectDevice(BluetoothDevice device) {
        interactor.connectDevice(device);
    }

    @Override
    public void sendCommand(BluetoothDevice device, int command) {
        //interactor.sendCommand(device, command);
    }

    @Override
    public void onScanSuccess(HashMap<String, ScanResult> scanResultHashMap) {
        view.onListLoadedSuccessfully(scanResultHashMap);
    }

    @Override
    public void onScanFailure(int FAILURE_CODE) {
        view.onListLoadedFailed(FAILURE_CODE);
    }

    @Override
    public void connectionState(int state) {
        Log.d(TAG, "connectionState: " + state);
    }

    @Override
    public void servicesDiscovered(List<BluetoothGattService> bluetoothGattServiceList) {
    }

    @Override
    public void onDataReceived(String s, String formatted) {
        Log.d(TAG, "DeviceScanPresenter: onDataReceived: " + s);
    }



}
