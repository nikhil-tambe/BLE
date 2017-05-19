package com.nikhil.bletrial.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nikhil.bletrial.app.AppConstants;

/**
 * Created by actofitteam on 5/18/17.
 */

public class NotificationsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(AppConstants.TAG, "onReceive: " + intent.getAction());
    }
}
