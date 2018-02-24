package com.bry.adcafe.services;

import android.util.Log;

import com.bry.adcafe.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 25/02/2018.
 */

public class TimeManager {
    private static final String TAG = TimeManager.class.getSimpleName();

    private static long mDateInDays;
    private static int mHour;
    private static int mMinute;

    private static boolean isTimeManagerInitialized = false;


    public static void initializeDate(){

    }

    private static void sendMessageThatDateIsSet(){

    }

    public static String getDate() {
        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        return (dd + ":" + mm + ":" + yy);
    }




    public static boolean isAlmostMidNight() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        Log.d(TAG, "Current time is " + hours + ":" + minutes + ":" + seconds);
        if (hours == 23 && (minutes == 59) && (seconds >= 0)) {
            Log.d(TAG, "---Day is approaching midnight,returning true to reset the activity and values. Time is:" + hours + " : " + minutes + " : " + seconds);
            return true;
        } else {
            Log.d(TAG, "---Day is not approaching midnight,so activity will continue normally.");
            return false;
        }
    }

    public static String getNextDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd + ":" + mm + ":" + yy);

        Log.d(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;

    }

    public static Long getDateInDays(){
        long currentTimeMillis = System.currentTimeMillis();
        long currentDay = (currentTimeMillis+ Constants.HRS_3_IN_MILLS)/(Constants.HRS_24_IN_MILLS);
        Log.d(TAG,"The current day is : "+currentDay);
        return currentDay;
    }




    public static String getPreviousDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d("ADMIN_STAT_ITEM","Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    public static String getDateFromDays(long days){
        long currentTimeInMills = days*(Constants.HRS_24_IN_MILLS);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        return (dayOfMonth + ":" + monthOfYear + ":" + year);
    }


}
