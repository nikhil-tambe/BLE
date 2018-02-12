package com.nikhil.bletrial.bluetooth.device;

import android.bluetooth.BluetoothDevice;

/**
 * Created by nikhil on 05/04/17.
 */

public interface IDevicePresenter {

    boolean isDeviceConnected();
    void connnectDevice(BluetoothDevice device);
    void sendCommand(BluetoothDevice device, int command);

    boolean checkBytes(Object channel, Object bytes);

}
