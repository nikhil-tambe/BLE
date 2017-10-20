package com.nikhil.bletrial.bluetooth.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.nikhil.bletrial.app.ApplicationClass;

/**
 * Created by nikhil on 21/04/17.
 */

public class BluetoothAdapterSingleton {

    private static BluetoothAdapter adapter;

    public static BluetoothAdapter getAdapter() {
        if (adapter == null){
            BluetoothManager manager = (BluetoothManager) ApplicationClass.getInstance()
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            return manager.getAdapter();
        }
        return adapter;
    }

}
