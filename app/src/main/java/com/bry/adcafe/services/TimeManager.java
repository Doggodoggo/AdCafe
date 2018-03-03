package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bryon on 25/02/2018.
 */

public class TimeManager {
    private static final String TAG = TimeManager.class.getSimpleName();
    private static Calendar cal;
    private static Handler h = new Handler();
    private static Runnable r;
    public static boolean isTimeManagerInitialized = false;


    public static boolean isTimerOnline(){
        return cal != null;
    }


    public static void setUpTimeManager(String callbackString , Context context){
        getCurrentNetworkTime(callbackString,context);
    }

    private static void getCurrentNetworkTime(final String callbackString, final Context context) {
        OkHttpClient client = new OkHttpClient();
        String url1 = "http://api.timezonedb.com/v2/get-time-zone?key=KGGAQAWJNQZS&format=json&by=zone&zone=Africa/Nairobi";
        String url2 = "http://api.timezonedb.com/v2/list-time-zone?key=KGGAQAWJNQZS&format=json&country=KE";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url1).newBuilder();
        String url = urlBuilder
                .build()
                .toString();
        Request request = new Request.Builder().url(url).build();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processResponse(response,callbackString,context);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);

    }

    private static void processResponse(Response response,String callbackString, Context context) {
        try{
            String jsonData = response.body().string();
            if (response.isSuccessful()) {
                JSONObject timeJSON = new JSONObject(jsonData);
                if(timeJSON.getString("status").equals("OK")){
                    Long timestamp = timeJSON.getLong("timestamp");
                    String date = timeJSON.getString("formatted");
                    Log.d("Response","Time and date gotten is : "+date);
                    Log.d("Response","Timestamp time gotten is : "+timestamp);

                    setCalendar2(date,((timestamp-(3*60*60))*1000));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(callbackString));
                }
            }
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }




    private static void setCalendar2(String timeNDay,long timeInMills){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date dt = sdf.parse(timeNDay);
            cal = Calendar.getInstance();
            cal.setTime(dt);
            updateTimer();
            isTimeManagerInitialized = true;
        } catch (ParseException e) {
            e.printStackTrace();
            setCalendar(timeInMills);
        }

    }

    private static void setCalendar(long timeInMills){
        cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMills);
        updateTimer();
        isTimeManagerInitialized = true;
    }

    private static void updateTimer(){
        r = new Runnable() {
            @Override
            public void run() {
                cal.add(Calendar.SECOND,4);
                h.postDelayed(r, 4000);
            }
        };
        h.postDelayed(r, 4000);
    }




    public static String getDate() {
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        return (dd + ":" + mm + ":" + yy);
    }

    public static boolean isAlmostMidNight() {
        Calendar c = cal;
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
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        String tomorrowsDate = (dd + ":" + mm + ":" + yy);

        Log.d(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;

    }

    public static Long getDateInDays(){
        long currentTimeMillis = cal.getTimeInMillis();
        long currentDay = (currentTimeMillis)/(Constants.HRS_24_IN_MILLS);
        Log.d(TAG,"The current day is : "+currentDay);
        return currentDay;
    }




    public static String getPreviousDay(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)-1);

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
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

    public static String getNextDayPlus(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm;
        if((c.get(Calendar.MONTH) + 1)>10) mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        else mm = "0"+Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        String tomorrowsDate = (dd + ":" + mm + ":" + yy);

        Log.d(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;
    }


    public static String getTime(){
        Calendar c = cal;
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        return hours+":"+minutes+":"+seconds;
    }


}
