package com.bry.adcafe.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.User;
import com.bry.adcafe.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 15/11/2017.
 */

public class AlarmReceiver extends BroadcastReceiver{
    private static final String TAG = "AlarmReceiver - ";
    private DatabaseReference dbRef;
    private String mKey = "";
    private Context mContext;
    private String message = "";
    private int numberOfhildrenFromFirebase = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM_RECEIVER","Alarm has been received by AlarmReceiver..");
        mContext = context;
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate()).child(Integer.toString(User.getClusterID(mKey)));
        dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()&& isApproppriateTime()){
                    message = "We've got some ads for you today.";
                    Log.d("AlarmReceiver","Set message to: "+message);
                    buildStuff(mContext);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                message = "We might have some ads for you today.";
                Log.d("AlarmReceiver","Set message to: "+message);
                buildStuff(mContext);
            }
        });

    }


    private boolean isApproppriateTime() {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if(hours==3){
            return true;
        }else{
            return true;
        }
    }

    public void buildStuff(Context context){
        //Intent to invoke app when click on notification.
        //In this sample, we want to start/launch this sample app when user clicks on notification
        Intent intentToRepeat = new Intent(context, LoginActivity.class);
        //set flag to restart/relaunch the app
        intentToRepeat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Pending intent to handle launch of Activity in intent above
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC,
                intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT);
        //Build notification
        Notification repeatedNotification = buildLocalNotification(context, pendingIntent).build();
        //Send local notification
        NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
    }

    public NotificationCompat.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher2)
                        .setColor(context.getResources().getColor(R.color.darkslategrey))
                        .setContentTitle("AdCafe")
                        .setContentText(message)
                        .setAutoCancel(true);

    }

    private String getDate(){
        return TimeManager.getDate();
    }


}
