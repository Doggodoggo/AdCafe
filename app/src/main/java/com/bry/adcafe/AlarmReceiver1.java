package com.bry.adcafe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.AgeGroup;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.ui.LoginActivity;
import com.bry.adcafe.ui.Splash;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by bryon on 21/11/2017.
 */

public class AlarmReceiver1 extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver ";
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1880;
    Notification notification;
    private Context mContext;
    private String mKey;
    private int numberOfSubsFromFirebase = 0;
    private int iterations = 0;
    private int numberOfAdsInTotal = 0;
    private LinkedHashMap<String,Integer> Subscriptions  = new LinkedHashMap<>();
    private int constantAmountPerView = 3;

    private String userName;
    private boolean doesUserWantNotf;

    private DataSnapshot mTargetUsersDataList;
    Handler h = new Handler();
    Runnable r;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("AlarmReceiver","Broadcast has been received by alarm.");
        mContext = context;
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERRED_NOTIF, MODE_PRIVATE);
        doesUserWantNotf = prefs.getBoolean(Constants.PREFERRED_NOTIF, true);

        if(isOnline(mContext)) {
            if (isUserLoggedIn()) loadTimeFirst();
        }else{
            startRecursiveCheckerForWhenOnline();
        }
    }

    private void startRecursiveCheckerForWhenOnline() {
        r = new Runnable() {
            @Override
            public void run() {
                Log(TAG, "Waiting for connection to be established");
                if (isOnline(mContext)) {
                    if (isUserLoggedIn()) loadTimeFirst();
                    h.removeCallbacks(r);
                }else{
                    h.postDelayed(r, 30000);
                }

            }
        };
        h.postDelayed(r, 30000);
    }

    private void loadTimeFirst(){
        TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetUpTime,
                new IntentFilter(Constants.LOAD_TIME));
    }

    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG,"Finished setting up time.");
            checkIfUserWasLastOnlineToday();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void checkIfUserWasLastOnlineToday(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log(TAG,"Starting to check if user was last online today.");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = dataSnapshot.getValue(String.class);
                if(!date.equals(getDate())){
                    Log(TAG,"User was not last online today,checking if there are any ads today.");
                    loadSubscriptionsThenCheckForAds();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadSubscriptionsThenCheckForAds(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log(TAG,"Starting to load users data to check if there are ads");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot cpvSnap = dataSnapshot.child(Constants.CONSTANT_AMMOUNT_PER_VIEW);
                constantAmountPerView = cpvSnap.getValue(int.class);

                DataSnapshot usernameSnap = dataSnapshot.child(Constants.USER_NICKNAME);
                userName = usernameSnap.getValue(String.class);

                DataSnapshot subSnap = dataSnapshot.child(Constants.SUBSCRIPTION_lIST);
                for(DataSnapshot snap: subSnap.getChildren()){
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log(TAG,"Key category gotten from firebase is : "+category+" Value : "+cluster);
                    Subscriptions.put(category,cluster);
                }
                DataSnapshot isNeedToResetSubsSnap = dataSnapshot.child(Constants.RESET_ALL_SUBS_BOOLEAN);
                if(!isNeedToResetSubsSnap.getValue(Boolean.class)) {
                    numberOfSubsFromFirebase = Subscriptions.size();
//                    checkNumberForEach();
                    getTargetedUsersDataList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Something went wrong : "+databaseError.getMessage());
            }
        });
    }

    private void getTargetedUsersDataList(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Constants.TARGET_USER_DATA);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTargetUsersDataList = dataSnapshot;
                checkNumberForAll();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void checkNumberForAll(){
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate())
                .child(Integer.toString(constantAmountPerView));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for(String subscription:Subscriptions.keySet()){
                        int cluster = Subscriptions.get(subscription);
                        DataSnapshot adsInSubscription = dataSnapshot.child(subscription).child(Integer.toString(cluster));
                        if(adsInSubscription.exists()) {
                            for (DataSnapshot snap : adsInSubscription.getChildren()) {
                                boolean isFlagged = snap.child("flagged").getValue(boolean.class);
                                if (!isFlagged && doesUserMeetCriteria(snap.child("targetdata"), snap.child("pushRefInAdminConsole"))) {
                                    numberOfAdsInTotal += 1;
                                    if (numberOfAdsInTotal == 1) setStartingPoint(subscription);
                                }
                            }
                        }
                    }
                }
                Log(TAG,"All the categories have been handled, total is : "+numberOfAdsInTotal);
                if(numberOfAdsInTotal>0) beforeHandlingEverything(numberOfAdsInTotal);
                if(numberOfAdsInTotal==0) setStartingPoint(getSubscriptionValue(numberOfSubsFromFirebase-1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkNumberForEach(){
        int currentClusterToBeChecked = getClusterValue(iterations);
        String Subscription = getSubscriptionValue(iterations);
        checkInForEachCategory(Subscription,currentClusterToBeChecked);
    }

    private void checkInForEachCategory(final String category, int cluster){
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getDate())
                .child(Integer.toString(constantAmountPerView))
                .child(category).child(Integer.toString(cluster));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        boolean isFlagged = snap.child("flagged").getValue(boolean.class);
                        if(!isFlagged && doesUserMeetCriteria(snap.child("targetdata"), snap.child("pushRefInAdminConsole"))){
                            numberOfAdsInTotal+=1;
                            if(numberOfAdsInTotal ==1)setStartingPoint(category);
                        }
                    }
                }
                iterations++;
                if(iterations<numberOfSubsFromFirebase){
                    checkNumberForEach();
                }else{
                    Log(TAG,"All the categories have been handled, total is : "+numberOfAdsInTotal);
                    if(numberOfAdsInTotal>0) beforeHandlingEverything(numberOfAdsInTotal);
                    if(numberOfAdsInTotal==0) setStartingPoint(category);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Something went wrong : "+databaseError.getMessage());
            }
        });
    }

    private void beforeHandlingEverything(final int number){
        if(!Variables.isLoginOnline){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            User.setUid(uid);
            Log(TAG,"Starting to check if user was last online today.");
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(uid).child(Constants.DATE_IN_FIREBASE);
            adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String date = dataSnapshot.getValue(String.class);
                    if(!date.equals(getDate())){
                        Log(TAG,"User was not last online today, continuing to notify user.");
                        if(doesUserWantNotf) handleEverything(number);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }




    private void handleEverything(int number) {
        if(constantAmountPerView>3 && number>Constants.MAX_NUMBER_FOR7){
            number = Constants.MAX_NUMBER_FOR7;
        }
        String message;
        if (number > 1)message = "Hey "+userName+", "+ "we've got " + number + " ads for you today."+Html.fromHtml("&#128076;") ;
        else message = "Hey "+userName+", "+ "we've got " + number + " ad for you today."+Html.fromHtml("&#128516;") ;
        Context context = mContext;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(context, Splash.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("Test","test");
        mIntent.putExtras(bundle);
        pendingIntent = PendingIntent.getActivity(context,0,mIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_notification2)
                .setTicker("ticker value")
                .setColor(mContext.getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                .setPriority(8)
//                .setSound(soundUri)
                .setContentTitle("AdCafé.")
                .setContentText(message).build();
        notification.flags|=Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
        notification.defaults|=Notification.DEFAULT_VIBRATE;
//        notification.defaults|=Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
        notification.ledARGB = 0xFFFFA500;
        notification.ledOnMS = 800;
        notification.ledOffMS = 1000;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID,notification);

        Log.i("notif","Notification sent");

    }

    private String getDate(){
        return TimeManager.getDate();
    }

    private String getNextDay(){
        return TimeManager.getNextDay();

    }




    private void cancelAlarm(){
        if(notificationManager!=null)
        notificationManager.cancelAll();
    }

    private int getClusterValue(int index) {
        LinkedHashMap map = Subscriptions;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log(TAG, "Cluster gotten from current subscription is : " + cluster);
        return cluster;
    }

    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log(TAG, "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }

    private boolean isUserLoggedIn(){
        SharedPreferences prefs4 = mContext.getSharedPreferences("IsSignedIn", MODE_PRIVATE);
        return prefs4.getBoolean("isSignedIn", false);
    }

    private void setStartingPoint(String subscription){
        Log(TAG,"*****Setting starting point when app starts up: "+subscription);
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.CUSTOM_STARTING_POINT_ENABLED, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.CUSTOM_STARTING_POINT_ENABLED, true);
        editor.apply();

        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.CUSTOM_STARTING_POINT_VALUE, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.putString(Constants.CUSTOM_STARTING_POINT_VALUE, subscription);
        editor2.apply();
    }


    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals(Constants.ADMIN_ACC)) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean doesUserMeetCriteria(DataSnapshot targetDataSnap,DataSnapshot pushId){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pushIdInAdminConsole = pushId.getValue(String.class);

        SharedPreferences prefConsent = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        boolean canUseData = prefConsent.getBoolean(Constants.CONSENT_TO_TARGET,false);
        if(!canUseData) return false;

        if(targetDataSnap.child("gender").exists()){
            String gender = targetDataSnap.child("gender").getValue(String.class);
            SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
            String myGender = prefs2.getString(Constants.GENDER, "NULL");
            if(!gender.equals(myGender))return false;
        }
        if(targetDataSnap.child("agegroup").exists()){
            AgeGroup ageGroup = targetDataSnap.child("agegroup").getValue(AgeGroup.class);
            SharedPreferences pref = mContext.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
            if(pref.getInt("year",0)!=0) {
                int day = pref.getInt("day", 0);
                int month = pref.getInt("month", 0);
                int year = pref.getInt("year", 0);
                Integer userAge = getAge(year,month,day);
                if(userAge < ageGroup.getStartingAge()|| userAge > ageGroup.getFinishAge()) return false;
            }else return false;
        }if(targetDataSnap.child("locations").exists()){
            List<LatLng> checkLocalList = new ArrayList<>();
            for(DataSnapshot locSnap:targetDataSnap.child("locations").getChildren()){
                double lat = locSnap.child("lat").getValue(double.class);
                double lng = locSnap.child("lng").getValue(double.class);
                checkLocalList.add(new LatLng(lat,lng));
            }
            if(locationContained(checkLocalList) == 0) return false;
        }if(targetDataSnap.child("devicerange").exists()){
            String deviceRangeCategory = targetDataSnap.child("devicerange").getValue(String.class);
            if(!deviceRangeCategory.equals(getUserDeviceCagegory()))return false;
        }if(targetDataSnap.child("categorylist").exists()){
            List<String> requiredCategories = new ArrayList<>();
            for(DataSnapshot catSnap:targetDataSnap.child("categorylist").getChildren()){
                String cat = catSnap.getValue(String.class);
                requiredCategories.add(cat);
            }
            for(String requiredCategory:requiredCategories){
                if(!Variables.Subscriptions.keySet().contains(requiredCategory)) return false;
            }
        }

        if(mTargetUsersDataList.child(getDate()).child(pushIdInAdminConsole).exists()){
            List<String> targetUids;
            String targetUserListString = mTargetUsersDataList.child(getDate()).child(pushIdInAdminConsole).getValue(String.class);

            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<ArrayList<String>>(){}.getType();
            targetUids = gson.fromJson(targetUserListString,type);
            if(targetUids != null)Log(TAG,"target uids are "+targetUids.size());
            if (targetUids != null && !targetUids.contains(uid)) return false;
        }
        return true;
    }

    private Integer getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = TimeManager.getCal();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return new Integer(age);
    }

    private int locationContained(List<LatLng> checkLocalLst){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.USER_MARKERS,MODE_PRIVATE);
        Variables.usersLatLongs.clear();
        int size = sharedPreferences.getInt(Constants.USER_MARKERS_SIZE, 0);
        for(int i = 0; i < size; i++){
            double lat = (double) sharedPreferences.getFloat("lat"+i,0);
            double longit = (double) sharedPreferences.getFloat("long"+i,0);
            LatLng latLng = new LatLng(lat,longit);
            Variables.usersLatLongs.add(latLng);
        }

        int locations = 0;
        for(LatLng latlngUser : Variables.usersLatLongs){
            for(LatLng latlngAdv: checkLocalLst){
                Location locAdv = new Location("");
                locAdv.setLatitude(latlngAdv.latitude);
                locAdv.setLongitude(latlngAdv.longitude);

                Location locUser = new Location("");
                locUser.setLatitude(latlngUser.latitude);
                locUser.setLongitude(latlngUser.longitude);
                float distance = Variables.distanceInMetersBetween2Points(locAdv,locUser);
                if(distance<=Constants.MAX_DISTANCE_IN_METERS) locations++;
            }
        }
        return locations;
    }

    public String getUserDeviceCagegory(){
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.DEVICE_CATEGORY, MODE_PRIVATE);
        return prefs2.getString(Constants.DEVICE_CATEGORY, Constants.MID_RANGE_DEVICE);
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }


}
