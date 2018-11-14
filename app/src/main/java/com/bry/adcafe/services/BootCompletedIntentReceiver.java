package com.bry.adcafe.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver1.class); // AlarmReceiver1 = broadcast receiver

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        try{
            alarmManager.cancel(pendingIntent);
        }catch(Exception e){
            e.printStackTrace();
        }

        SharedPreferences pref9 = context.getSharedPreferences(Constants.PREFERRED_NOTF_HOUR,MODE_PRIVATE);
        Variables.preferredHourOfNotf = pref9.getInt(Constants.PREFERRED_NOTF_HOUR,7);

        SharedPreferences pref10 = context.getSharedPreferences(Constants.PREFERRED_NOTF_MIN,MODE_PRIVATE);
        Variables.preferredMinuteOfNotf = pref10.getInt(Constants.PREFERRED_NOTF_MIN,30);

        Calendar alarmStartTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, Variables.preferredHourOfNotf);
        alarmStartTime.set(Calendar.MINUTE, Variables.preferredMinuteOfNotf);
        alarmStartTime.set(Calendar.SECOND, 0);
        if (now.after(alarmStartTime)) {
            Log.d("RebootReceiver", "Setting alarm to tomorrow morning.");
            alarmStartTime.add(Calendar.DATE, 1);
        }
        try {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            String message = String.format("Alarms set for everyday %s:%s hrs.",Variables.preferredHourOfNotf,Variables.preferredMinuteOfNotf);
            Log.w("Alarm", message);
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            String auth = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent i = new Intent(context, MessagesService.class);
            context.startService(i);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
