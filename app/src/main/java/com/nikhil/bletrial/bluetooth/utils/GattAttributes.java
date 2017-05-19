package com.nikhil.bletrial.bluetooth.utils;

import java.util.UUID;

/**
 * Created by nikhil on 30/03/17.
 */

public interface GattAttributes {

    String HEART_RATE_MEASUREMENT           = "00002a10-0000-1000-8000-00805f9b34fb";
    String CLIENT_CHARACTERISTIC_CONFIG     = "00002902-0000-1000-8000-00805f9b34fb";


    // 180A Device Information
    String SERVICE_DEVICE_INFORMATION       = "0000180a-0000-1000-8000-00805f9b34fb";
    String CHAR_MANUFACTURER_NAME_STRING    = "00002a29-0000-1000-8000-00805f9b34fb";
    String CHAR_MODEL_NUMBER_STRING         = "00002a24-0000-1000-8000-00805f9b34fb";
    String CHAR_SERIAL_NUMBEAR_STRING       = "00002a25-0000-1000-8000-00805f9b34fb";
    String CHAR_HARDWARE_REVISION           = "00002a27-0000-1000-8000-00805f9b34fb";
    String CHAR_FIRMWARE_REVISION           = "00002a26-0000-1000-8000-00805f9b34fb";
    String CHAR_SOFTWARE_REVISION           = "00002a28-0000-1000-8000-00805f9b34fb";
    String CHAR_SYSTEM_ID                   = "00002a23-0000-1000-8000-00805f9b34fb";


    // 1802 Immediate Alert
    String SERVICE_IMMEDIATE_ALERT          = "00001802-0000-1000-8000-00805f9b34fb";
    String CHAR_ALERT_LEVEL                 = "00002a06-0000-1000-8000-00805f9b34fb";


    // 180F Battery Service
    String SERVICE_BATTERY_SERVICE          = "0000180F-0000-1000-8000-00805f9b34fb";
    String CHAR_BATTERY_LEVEL               = "00002a19-0000-1000-8000-00805f9b34fb";


    // 6e400001 Device data service
    String SERVICE_Rx                       = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    String CHAR_WRITE                       = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    //String CHAR_WRITE_BLANK                 = "6e40000a-b5a3-f393-e0a9-e50e24dcca9e";
    String CHAR_NOTIFY_GYM                  = "6e400004-b5a3-f393-e0a9-e50e24dcca9e";
    //String CHAR_NOTIFY_RUN                  = "6e400005-b5a3-f393-e0a9-e50e24dcca9e";
    //String CHAR_NOTIFY_WALK                 = "6e400006-b5a3-f393-e0a9-e50e24dcca9e";
    //String CHAR_NOTIFY_SPORTS               = "6e400008-b5a3-f393-e0a9-e50e24dcca9e";
    String CHAR_NOTIFY_OFFLINE_LIFE_LOG     = "6e400009-b5a3-f393-e0a9-e50e24dcca9e";
    //String CHAR_NOTIFY_SUMMARY              = "6e400010-b5a3-f393-e0a9-e50e24dcca9e";
    String CHAR_NOTIFY_ONLINE_LIFE_LOG      = "6e400011-b5a3-f393-e0a9-e50e24dcca9e";


    // UUIDs
    UUID UUID_CLIENT_CONFIG                 = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG);
    UUID UUID_SERVICE_WORKOUT               = UUID.fromString(SERVICE_Rx);

    UUID UUID_CHAR_WRITE                    = UUID.fromString(CHAR_WRITE);
    //UUID UUID_CHAR_ONLINE_LIFE_LOG          = UUID.fromString(CHAR_NOTIFY_ONLINE_LIFE_LOG);
    //UUID UUID_CHAR_WRITE_BLANK              = UUID.fromString(CHAR_WRITE_BLANK);
    //UUID UUID_CHAR_NOTIFY_GYM               = UUID.fromString(CHAR_NOTIFY_GYM);
    //UUID UUID_CHAR_SUMMARY                  = UUID.fromString(CHAR_NOTIFY_SUMMARY);

}
