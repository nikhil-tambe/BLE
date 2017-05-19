package com.nikhil.bletrial.bluetooth.utils;

/**
 * Created by nikhil on 28/03/17.
 */

public interface BTConstants {

    int ENABLE_BT_REQUEST = 1;
    int SCAN_FAILED = 2;
    int CONNECTION_ATTEMPT_FAILED = 3;

    interface COMMANDS {

        int COMMAND_CLOSE_GATT = 0;

        String COMMAND_LIFE_LOG_ONLINE_START = "LifeLog Online";
        int COMMAND_LIFE_LOG_ONLINE_STOP = 2;

        String COMMAND_LIFE_LOG_OFFLINE_READ = "LifeLog Offline";
        int COMMAND_LIFE_LOG_OFFLINE_ERASE = 4;

        String COMMAND_GYM_ONLINE_START = "Gym Online";
        int COMMAND_GYM_ONLINE_STOP = 6;

        String COMMAND_GYM_OFFLINE_READ = "Gym Offline";

        String COMMAND_SLEEP_READ = "Sleep";
        int COMMAND_SLEEP_ERASE = 9;

        int COMMAND_DATE_FRAME = 10;


    }
}
