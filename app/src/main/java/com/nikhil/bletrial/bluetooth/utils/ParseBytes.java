package com.nikhil.bletrial.bluetooth.utils;

import android.util.Log;

import static com.nikhil.bletrial.app.AppConstants.TAG;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SLEEP_READ;

/**
 * Created by nikhil on 26/04/17.
 */

public class ParseBytes {

    private String COMMAND = "";

    public ParseBytes(){

    }

    /*public ParseBytes(String channel) {
        this.COMMAND = channel;
    }*/

    String getParsedBytes(String channel, String frame) {
        Log.d(TAG, "rawData: " + channel + ": " + frame);
        String parsedFrame = "";

        switch (channel) {

            case COMMAND_GYM_ONLINE_START:
                parsedFrame = gymOnlineHexToDecimal(frame);
                break;

            case COMMAND_GYM_OFFLINE_READ:
                parsedFrame = gymOfflineHexToDecimal(frame);
                break;

            case COMMAND_LIFE_LOG_OFFLINE_READ:
                parsedFrame = lifeLogOfflineHexToDecimal(frame);
                break;

            case COMMAND_LIFE_LOG_ONLINE_START:
                parsedFrame = lifeLogOnlineHexToDecimal(frame);
                break;

            case COMMAND_SLEEP_READ:
                parsedFrame = sleepHexToDecimal(frame);
                break;
        }
        //Log.d(TAG, "parsedFrame: " + channel + " : " + parsedFrame);
        return parsedFrame;
    }

    /**
     * frame size 16 bytes.
     * frame components => FF ax1 ax2 ay1 ay2 az1 az2 gx1 gx2 gy1 gy2 gz1 gz2 hr wt FF
     * ax component = String(ax1) + String(ax2); and so on for ay and az.
     * similarly, gx = String(gx1) + String(gx2); and so on for gy and gz.
     * hr = Heart Rate (bpm).
     * wt = Weight Lifted.
     */ // 16 bytes
    private String gymOnlineHexToDecimal(String frame) {
        String[] frameArray = frame.split(" ");
        if (frameArray.length == 16) {
            int ax = Integer.parseInt(frameArray[1] + frameArray[2], 16);
            int ay = Integer.parseInt(frameArray[3] + frameArray[4], 16);
            int az = Integer.parseInt(frameArray[5] + frameArray[6], 16);
            int gx = Integer.parseInt(frameArray[7] + frameArray[8], 16);
            int gy = Integer.parseInt(frameArray[9] + frameArray[10], 16);
            int gz = Integer.parseInt(frameArray[11] + frameArray[12], 16);
            int hr = Integer.parseInt(frameArray[13], 16);
            int wt = Integer.parseInt(frameArray[14], 16);

            return ax + "," + ay + "," + az + "," + gx + "," + gy + "," + gz
                    + "," + hr + "," + wt;
        } else {
            Log.e(TAG, "gymOnlineHexToDecimal: " + frame);
            return "";
        }
    }

    /**
     * frame size 20 bytes.
     * frame components => FF mode hh mm ss ax1 ax2 ay1 ay2 az1 az2 gx1 gx2 gy1 gy2 gz1 gz2 hr wt FF
     * mode => 0 = Gym, 1 = Run and 2 = Sports.
     * hh = hours
     * mm = minutes
     * ss = seconds
     * ax component = String(ax1) + String(ax2); and so on for ay and az.
     * similarly, gx = String(gx1) + String(gx2); and so on for gy and gz.
     * hr = Heart Rate (bpm).
     * wt = Weight Lifted.
     */ // 20 bytes
    private String gymOfflineHexToDecimal(String frame) {
        String[] frameArray = frame.split(" ");

        if (frameArray.length == 20) {
            int mode = Integer.parseInt(frameArray[1], 16);
            int hh = Integer.parseInt(frameArray[2], 16);
            int mm = Integer.parseInt(frameArray[3], 16);
            int ss = Integer.parseInt(frameArray[4], 16);
            int ax = Integer.parseInt(frameArray[5] + frameArray[6], 16);
            int ay = Integer.parseInt(frameArray[7] + frameArray[8], 16);
            int az = Integer.parseInt(frameArray[9] + frameArray[10], 16);
            int gx = Integer.parseInt(frameArray[11] + frameArray[12], 16);
            int gy = Integer.parseInt(frameArray[13] + frameArray[14], 16);
            int gz = Integer.parseInt(frameArray[15] + frameArray[16], 16);
            int hr = Integer.parseInt(frameArray[17], 16);
            int wt = Integer.parseInt(frameArray[18], 16);

            String hour = checkDigit(hh);
            String min = checkDigit(mm);
            String sec = checkDigit(ss);

            return ax + "," + ay + "," + az + "," + gx + "," + gy + "," + gz
                    + "," + hr + "," + wt + "," + hour + min + sec + "," + mode;
        } else {
            Log.e(TAG, "gymOfflineHexToDecimal: " + frame);
            return "";
        }
    }

    /**
     *
     * */ // 20 bytes
    private String gymOfflineLastFrame(String frame) {
        return "";
    }

    /**
     * frame length = 14
     * frame components =>  FF yy MM dd hh mm step1 step2 dist1 dist2 cal1 cal2 hr FF
     * yy = year
     * MM = month
     * dd = day of month
     * hh = hours
     * mm = minutes
     * steps = String(step1) + String(step2)
     * distance = String(dist1) + String(dist2)
     * calories = String(cal1) + String(cal2)
     * hr = Heart Rate (bpm).
     */ // 14 bytes
    private String lifeLogOfflineHexToDecimal(String frame) {
        String[] frameArray = frame.split(" ");

        if (frameArray.length == 14) {
            int yy = Integer.parseInt(frameArray[1], 16);
            int mm = Integer.parseInt(frameArray[2], 16);
            int dd = Integer.parseInt(frameArray[3], 16);
            int time = Integer.parseInt(frameArray[4] + frameArray[5], 16);
            int step = Integer.parseInt(frameArray[6] + frameArray[7], 16);
            int dist = Integer.parseInt(frameArray[8] + frameArray[9], 16);
            int cal = Integer.parseInt(frameArray[10] + frameArray[11], 16);
            int hr = Integer.parseInt(frameArray[12], 16);

            return yy + "," + mm + "," + dd + "," + time + "," + step + "," + dist + "," +
                    cal + "," + hr;
        } else {
            Log.e(TAG, "lifeLogOfflineHexToDecimal: " + frame);
            return "";
        }
    }

    /**
     * frame length = 9
     * frame components => FF steps1 steps2 dist1 dist2 cal1 cal2 hr FF
     * steps = String(steps1) + String(steps2)
     * dist = String(dist1) + String(dist2)
     * cal = String(cal1) + String(cal2)
     * hr = HeartRate (bpm)
     * */ // 9 bytes
    private String lifeLogOnlineHexToDecimal(String frame) {
        String[] frameArray = frame.split(" ");

        if (frameArray.length == 9) {
            int step = Integer.parseInt(frameArray[1] + frameArray[2], 16);
            int dist = Integer.parseInt(frameArray[3] + frameArray[4], 16);
            int cal = Integer.parseInt(frameArray[5] + frameArray[6], 16);
            int hr = Integer.parseInt(frameArray[7], 16);

            return step + "," + dist + "," + cal + "," + hr;
        } else {
            Log.e(TAG, "lifeLogOfflineHexToDecimal: " + frame);
            return "";
        }
    }

    /**
     * frame length = 9
     * frame components => FF yy MM dd kk mm act hr FF
     * yy = year
     * MM = month
     * dd = day of month
     * hh = hours
     * mm = minutes
     * act = Activity
     * hr = HeartRate (bpm)
     * */ // 9 bytes
    private String sleepHexToDecimal(String frame) {
        String[] frameArray = frame.split(" ");

        if (frameArray.length == 9) {
            int yy = Integer.parseInt(frameArray[1], 16);
            int MM = Integer.parseInt(frameArray[2], 16);
            int dd = Integer.parseInt(frameArray[3], 16);
            int kk = Integer.parseInt(frameArray[4], 16);
            int mm = Integer.parseInt(frameArray[5], 16);
            int act = Integer.parseInt(frameArray[6], 16);
            int hr = Integer.parseInt(frameArray[7], 16);

            return yy + "," + MM + "," + dd + "," + kk + "," + mm + "," + act + "," + hr;
        } else {
            Log.e(TAG, "lifeLogOfflineHexToDecimal: " + frame);
            return "";
        }
    }

    private String checkDigit(int digit) {
        if (digit < 10) {
            return "0" + digit;
        }
        return String.valueOf(digit);
    }

}
