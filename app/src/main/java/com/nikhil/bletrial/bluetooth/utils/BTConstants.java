package com.nikhil.bletrial.bluetooth.utils;

import java.util.HashMap;

/**
 * Created by nikhil on 28/03/17.
 */

public interface BTConstants {

    int ENABLE_BT_REQUEST = 1;
    int SCAN_FAILED = 2;
    int CONNECTION_ATTEMPT_FAILED = 3;

    interface COMMANDS {
        String COMMAND_LIFE_LOG_ONLINE_START    = "LifeLog Online";
        String COMMAND_LIFE_LOG_OFFLINE_READ    = "LifeLog Offline";
        String COMMAND_GYM_ONLINE_START         = "Gym Online";
        String COMMAND_GYM_OFFLINE_READ         = "Gym Offline";
        String COMMAND_SLEEP_READ               = "Sleep";
        String COMMAND_SESSION_COUNT            = "Session Count";
    }

}
