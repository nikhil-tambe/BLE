package com.nikhil.bletrial.bluetooth.utils;

import android.util.Log;

import static com.nikhil.bletrial.app.AppConstants.TAG;
import static com.nikhil.bletrial.bluetooth.utils.BLEInteractor.FIRMWARE_VERSION;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SESSION_COUNT;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SLEEP_READ;

/**
 * Created by nikhil on 26/04/17.
 */

class ParseBytes {

    private String COMMAND = "";

    ParseBytes(String channel) {
        this.COMMAND = channel;
    }

    String getParsedBytes(String frame) {
        String parsedFrame = "";

        switch (COMMAND) {

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

            case COMMAND_SESSION_COUNT:
                parsedFrame = sessionCountToDecimal(frame);
        }
        Log.d(TAG, "rawData: " + COMMAND + ": " + frame);
        Log.d(TAG, "parsedFrame: " + parsedFrame);
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
     * method to differentiate the frame on the basis of firmware.
     */
    private String offlineMuscleFrame(String line) {
        float firmwareNumber = Float.parseFloat(FIRMWARE_VERSION.replace("3.0.", ""));
        if (firmwareNumber > 12.3f) {
            return threeMuscleFrame(line);
        } else {
            return multipleMuscleFrame(line);
        }
    }

    /**
     * frame length = 20
     * frame components = FF m11 m12 m21 m22 m31 m32 yy mm dd HH min dHour dMin dSec FF
     * m1 = m11 + m12 first muscle
     * m2 = m21 + m22 second muscle
     * m3 = m31 + m32 third muscle
     * yy = year
     * mm = month
     * dd = date of month
     * HH = Start hour
     * min = Start minute
     * dHour = duration Hours
     * dMin = duration Minutes
     * dSec = duration Seconds
     * */ // 20 bytes
    private String threeMuscleFrame(String line) {
        String[] frameArray = line.split(" ");

        int m1 = Integer.parseInt(frameArray[1] + frameArray[2], 16);
        int m2 = Integer.parseInt(frameArray[3] + frameArray[4], 16);
        int m3 = Integer.parseInt(frameArray[5] + frameArray[6], 16);

        String muscleIdList = "";
        if (m1 != 0) {
            muscleIdList += m1;
        }
        if (m2 != 0) {
            muscleIdList += "," + m2;
        }
        if (m3 != 0) {
            muscleIdList += "," + m3;
        }

        String dd = checkDigit(Integer.parseInt(frameArray[9], 16));
        String mm = checkDigit(Integer.parseInt(frameArray[8], 16));
        String yy = checkDigit(Integer.parseInt(frameArray[7], 16));
        String HH = checkDigit(Integer.parseInt(frameArray[10], 16));
        String min = checkDigit(Integer.parseInt(frameArray[11], 16));

        String durationHH = checkDigit(Integer.parseInt(frameArray[12], 16));
        String durationMin = checkDigit(Integer.parseInt(frameArray[13], 16));
        String durationSec = checkDigit(Integer.parseInt(frameArray[14], 16));

        String start_date = dd + "/" + mm + "/20" + yy;
        String start_time = HH + ":" + min + ":00";
        String duration = durationHH + ":" + durationMin + ":" + durationSec;

        Log.d(TAG, "threeMuscleFrame: muscleIdList: " + muscleIdList);
        Log.d(TAG, "threeMuscleFrame: start_date: " + start_date + "\t" + start_time + "\t" + duration);

        return start_date + "," + start_time + "," + duration + "," + muscleIdList;
    }

    /**
     * frame length = 20
     * frame components = FF m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 yy mm dd HH min dHour dMin dSec FF
     * m1 to m10 are all muscle selections
     * yy = year
     * mm = month
     * dd = date of month
     * HH = Start hour
     * min = Start minute
     * dHour = duration Hours
     * dMin = duration Minutes
     * dSec = duration Seconds
     * */ // 20 bytes
    private String multipleMuscleFrame(String line) {
        String[] frameArray = line.split(" ");
        String muscleIdList = "";
        for (int i = 1; i < 10; i++) {
            int muscle = Integer.parseInt(frameArray[i], 16);
            if (muscle != 0) {
                muscleIdList += muscle + "00";
                if (i != 9) {
                    muscleIdList += ",";
                }
            }
        }

        String dd = checkDigit(Integer.parseInt(frameArray[13], 16));
        String mm = checkDigit(Integer.parseInt(frameArray[12], 16));
        String yy = checkDigit(Integer.parseInt(frameArray[11], 16));
        String HH = checkDigit(Integer.parseInt(frameArray[14], 16));
        String min = checkDigit(Integer.parseInt(frameArray[15], 16));

        String durationHH = checkDigit(Integer.parseInt(frameArray[16], 16));
        String durationMin = checkDigit(Integer.parseInt(frameArray[17], 16));
        String durationSec = checkDigit(Integer.parseInt(frameArray[18], 16));

        String start_date = dd + "/" + mm + "/20" + yy;
        String start_time = HH + ":" + min + ":00";
        String duration = durationHH + ":" + durationMin + ":" + durationSec;

        Log.d(TAG, "multipleMuscleFrame: allMuscles: " + muscleIdList);
        Log.d(TAG, "multipleMuscleFrame: " + start_date + "\t" + start_time + "\t" + duration);

        return start_date + "," + start_time + "," + duration + "," + muscleIdList;
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
     */ // 9 bytes
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
     */ // 9 bytes
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

    /**
     * method to differentiate the frame on the basis of firmware.
     * if firmware is 3.0.14.0 or above numberOfSessionsAndPercentage() is called.
     * else numberOfSessions() is called.
     */
    private String sessionCountToDecimal(String frame) {
        float firmwareNumber = Float.parseFloat(FIRMWARE_VERSION.replace("3.0.", ""));
        if (firmwareNumber >= 14.0f) {
            return numberOfSessionsAndPercentage(frame);
        } else {
            return numberOfSessions(frame);
        }
    }

    /**
     * frame length = 4
     * frame components = FF number1 number2 FF
     * totalSessions = number1 + number2
     * where totalSessions is the total number of sessions available on the band in offline storage.
     */ // 4 bytes
    private String numberOfSessions(String frame) {
        String[] frameArray = frame.split(" ");
        int totalSessions = Integer.parseInt(frameArray[1] + frameArray[2], 16);
        return String.valueOf(totalSessions);
    }

    /**
     * frame length = 4
     * frame components = FF totalSessions tf1 tf2 tf3 FF
     * totalSessions = total number of sessions available on the band in offline storage.
     * totalFrames = tf1 + tf2 + tf3
     * where totalFrames is the total number of frames available on the band in offline storage.
     * framesPercent = totalFrames/100
     * */ // 6 bytes
    private String numberOfSessionsAndPercentage(String frame) {
        String[] frameArray = frame.split(" ");
        int numberOfSession = Integer.parseInt(frameArray[1], 16);
        int totalFrames = Integer.parseInt(frameArray[2] + frameArray[3] + frameArray[4], 16);
        int framesPercent = totalFrames / 100;
        return numberOfSession + "," + totalFrames + "," + framesPercent;
    }


    private String checkDigit(int digit) {
        if (digit < 10) {
            return "0" + digit;
        }
        return String.valueOf(digit);
    }

}
