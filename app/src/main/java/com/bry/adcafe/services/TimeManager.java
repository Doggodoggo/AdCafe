package com.bry.adcafe.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.google.firebase.auth.FirebaseAuth;

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

    private static boolean isBackgroundUpdaterOnline = false;


    public static boolean isTimerOnline(){
        return cal != null ;
    }


    public static void setUpTimeManager(String callbackString , Context context){
        getCurrentNetworkTime(callbackString,context);
    }

    private static void getCurrentNetworkTime(final String callbackString, final Context context) {
        OkHttpClient client = new OkHttpClient();
        String url1 = "http://api.timezonedb.com/v2/get-time-zone?key=KGGAQAWJNQZS&format=json&by=zone&zone=Africa/Nairobi";
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
                    Log("Response","Time and date gotten is : "+date);
                    Log("Response","Timestamp time gotten is : "+timestamp);

                    setCalendar2(context,date,((timestamp-(3*60*60))*1000));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(callbackString));
                }
            }
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }




    private static void setCalendar2(Context context,String timeNDay,long timeInMills){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date dt = sdf.parse(timeNDay);
            cal = Calendar.getInstance();
            cal.setTime(dt);
//            updateTimer();
            updateTimer2(context);
            isTimeManagerInitialized = true;
        } catch (ParseException e) {
            e.printStackTrace();
            setCalendar(context,timeInMills);
        }

    }

    private static void setCalendar(Context context,long timeInMills){
        cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMills);
//        updateTimer();
        updateTimer2(context);
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

    private static void updateTimer2(Context context){
        final Intent i = new Intent(context, MyTestService.class);
        context.startService(i);
        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                h.removeCallbacks(r);
                Log("Time-Service","Received broadcast to stop timer service");
                context.stopService(i);
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            }
        },new IntentFilter("TimerService"));
    }




    public static String getDate() {
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        return (dd + ":" + mm + ":" + yy);
    }

    public static boolean isAlmostMidNight() {
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);

        Log(TAG, "Current time is " + hours + ":" + minutes + ":" + seconds);
        if (hours == 23 && (minutes == 59) && (seconds >= 0)) {
            Log(TAG, "---Day is approaching midnight,returning true to reset the activity and values. Time is:" + hours + " : " + minutes + " : " + seconds);
            return true;
        } else {
            Log(TAG, "---Day is not approaching midnight,so activity will continue normally.");
            return false;
        }
    }

    public static String getNextDay() {
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH) + 1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        String tomorrowsDate = (dd + ":" + mm + ":" + yy);

        Log(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;

    }

    public static Long getDateInDays(){
        long currentTimeMillis = cal.getTimeInMillis();
        long currentDay = (currentTimeMillis)/(Constants.HRS_24_IN_MILLS);
        Log(TAG,"The current day is : "+currentDay);
        return currentDay;
    }




    public static String getPreviousDay(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)-1);

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log(TAG,"Tomorrows date is : "+tomorrowsDate);
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

        Log(TAG, "Tomorrows date is : " + tomorrowsDate);
        return tomorrowsDate;
    }


    public static String getTime(){
        Calendar c = cal;
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        return hours+":"+minutes+":"+seconds;
    }

    public static String getTimeStamp(){
//        Log(TAG,("Timestamp is :"+cal.getTimeInMillis()/1000));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss",Locale.US);
        String currentDateandTime = sdf.format(cal.getTime());

        Log(TAG,"Timestamp is:"+currentDateandTime);

        return currentDateandTime;
    }




    public static class MyTestService extends IntentService {
        private Context mContext;
        boolean canUpdate = true;


        public MyTestService() {
            // Used to name the worker thread, important only for debugging.
            super("test-service");

        }

        @Override
        public void onCreate() {
            super.onCreate(); // if you override onCreate(), make sure to call super().
            // If a Context object is needed, call getApplicationContext() here.
            mContext = getApplicationContext();
            canUpdate = true;
            isBackgroundUpdaterOnline = true;

        }

        @Override
        protected void onHandleIntent(Intent intent) {
            // This describes what will happen when service is triggered
            r = new Runnable() {
                @Override
                public void run() {
                    try{
                        cal.add(Calendar.SECOND,4);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(canUpdate) {
                        h.postDelayed(r, 4000);
                        Log("Time-Service", "Updating timer."+getTime());
                    }else{
                        h.removeCallbacks(r);
                    }
                }
            };
            h.postDelayed(r, 4000);
        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            isBackgroundUpdaterOnline = false;
        }
    }

    private static void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals("bryonyoni@gmail.com")) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static String getMonthName_Abbr(int month) {
        Calendar calx = Calendar.getInstance();
        calx.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(calx.getTime());
        return month_name;
    }




    public static String getMonth(){
        Calendar c = cal;
        return getMonthName_Abbr(c.get(Calendar.MONTH) + 1);
    }

    public static int getMonthVal(){
        Calendar c = cal;
        return c.get(Calendar.MONTH);
    }

    public static String getYear(){
        Calendar c = cal;
        return Integer.toString(c.get(Calendar.YEAR));
    }

    public static String getDay(){
        Calendar c = cal;
        return Integer.toString(c.get(Calendar.DAY_OF_MONTH));
    }



    public static String getPreviousDayYear(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)-1);

        return yy;
    }

    public static String getPreviousDayMonth(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        int mm = (c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)-1);

        return getMonthName_Abbr(mm);
    }

    public static String getPreviousDayDay(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)-1);

        return dd;
    }


    public static String getNextDayYear(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        return yy;
    }

    public static String getNextDayMonth(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        int mm = (c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        return getMonthName_Abbr(mm);
    }

    public static String getNextDayDay(){
        Calendar c = cal;
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH)+1);

        return dd;
    }


    public static Calendar getCal() {
        return cal;
    }


}
