package com.nikhil.bletrial.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by nikhil on 20/04/17.
 */

public class ApplicationClass extends Application {

    private static ApplicationClass instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getAppContext(){
        return instance.getApplicationContext();
    }

    public static synchronized ApplicationClass getInstance() {
        return instance;
    }

}
