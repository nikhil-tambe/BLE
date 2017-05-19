package com.nikhil.bletrial.bluetooth.device;

import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.Objects;

/**
 * Created by nikhil on 05/04/17.
 */

public interface IActofitDeviceView {

    void deviceConnected();
    void deviceDisconnected();

    void onServicesDiscovered(List<BluetoothGattService> gattServices);

    void onDataReceived(String raw, String processed);

}
