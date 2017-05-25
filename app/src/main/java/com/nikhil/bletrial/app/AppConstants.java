package com.nikhil.bletrial.app;

/**
 * Created by nikhil on 28/03/17.
 */

public interface AppConstants {

    String TAG = "NNN";

    interface IntentKeys {
        String KEY_DEVICE_NAME = "device_name";
        String KEY_DEVICE_MAC = "device_mac";
        String KEY_DEVICE_MANUFACTURER_ID = "device_manufacturer_id";
    }

    interface SharedPreferences {
        String SPF_DEVICE = "device";
    }

    interface Storage {
        String ACTOFIT_FOLDER = "/Actofit";
    }

    interface PermissionRequests {
        int PERMISSION_REQUEST_COARSE_LOCATION = 101;
        int PERMISSION_REQUEST_WRITE_STORAGE = 102;
        int PERMISSION_REQUEST_CALL_ACCESS = 103;
        int PERMISSION_REQUEST_SMS_ACCESS = 104;
        int PERMISSION_REQUEST_CALENDER_ACCESS = 105;
        int PERMISSION_REQUEST_CONTACTS_ACCESS = 106;
    }

}
