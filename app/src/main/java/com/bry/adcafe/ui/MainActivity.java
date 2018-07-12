package com.bry.adcafe.ui;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bry.adcafe.AlarmReceiver1;
import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.AdCounterBar;
import com.bry.adcafe.fragments.ContactAdvertiserBottomsheet;
import com.bry.adcafe.fragments.FeedbackFragment;
import com.bry.adcafe.fragments.FragmentUserPayoutBottomSheet;
import com.bry.adcafe.fragments.ReportDialogFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.AdvertiserLocation;
import com.bry.adcafe.models.AgeGroup;
import com.bry.adcafe.models.User;
import com.bry.adcafe.models.myLatLng;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.NetworkStateReceiver;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipeDirectionalView;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String TAG = "MainActivity";
    public String NOTIFICATION_ID = "notification_id";
    public String NOTIFICATION = "notification";
    private static int NOTIFICATION_ID2 = 1880;
    private LinearLayout mFailedToLoadLayout;
    private Button mRetryButton;
    private ImageButton mLogoutButton;
    private SwipeDirectionalView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private String mKey = "";

    private List<Advert> mAdList = new ArrayList<>();
    private Runnable mViewRunnable;
    private LinearLayout mBottomNavButtons;
    private AVLoadingIndicatorView mAvi;
    private ProgressBar mLoadingProgressBar;
    private AVLoadingIndicatorView mAviLoadingMoreAds;
    private ProgressBar spinner;
    private TextView mLoadingText;
    private boolean mIsBeingReset = false;

    private DatabaseReference dbRef;
    private int mChildToStartFrom = 0;
    Handler h = new Handler();
    Runnable r;

    private NetworkStateReceiver networkStateReceiver;
    boolean doubleBackToExitPressedOnce = false;
    private boolean isFirebaseResetNecessary = false;
    private boolean isOffline = false;
    private boolean isLastAd = false;

    private String igsNein = "none";
    private boolean isLoadingMoreAds = false;
    private boolean mDoublePressedToPin = false;
    private Advert lastAdSeen = null;
    private boolean isSeingNormalAds = true;

    private boolean hasLoadedAnnouncements = false;
    private String stage;
    private LinearLayout cannotLoadLayout;
    private Button retryLoadingFromCannotLoad;

    private int iterations = 0;
    private boolean isHiddenBecauseNetworkDropped = false;
    private boolean areViewsHidden = false;
    private boolean isTimerPausedBecauseOfOfflineActivity = false;

    private int numberOfResponsesForLoadingBlurredImages = 0;
    private boolean hasSentMessageThatBlurrsHaveFinished = false;
    private boolean hasShowedToastForNoMoreAds = false;
    private int numberOfInitiallyLoadedAds = 0;
    private boolean isNewDay = false;
    private boolean isLoaderShowing = false;
    private boolean doesWindowHaveFocus = true;

    private boolean isLoadingDataFromFirebase = false;

    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private DataSnapshot mAdsSnapshot;
    private DataSnapshot mTargetUsersDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        Variables.isMainActivityOnline = true;
        stage = "LOADING_ADS";
        registerReceivers();

        if (!Fabric.isInitialized()) Fabric.with(this, new Crashlytics());
        setUpAllTheViews();
        Variables.isLocked = false;

        if(!isOnline()){
            mLoadingProgressBar.setVisibility(View.GONE);
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            mBottomNavButtons.setVisibility(View.GONE);
            cannotLoadLayout.setVisibility(View.VISIBLE);
            retryLoadingFromCannotLoad.setOnClickListener(this);
        } else setUpTimeIfNeedBe();
        logUser();

        mAviLoadingMoreAds.hide();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        try{
            notificationManager.cancel(NOTIFICATION_ID2);
        }catch (Exception e){
            e.printStackTrace();
        }
        setIsAppOnline(true);
        setLastUserOfAppInSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid());
        setUserDeviceCategoryInFirebaseAndSharedPrefs();
    }

    private void setUpTimeIfNeedBe(){
        if(!TimeManager.isTimeManagerInitialized) {
            hideViews();
            TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,
                    new IntentFilter(Constants.LOAD_TIME));
        } else loadAdsFromThread();
    }





    @Override protected void onStart() {
        super.onStart();
        setIsUserLoggedOnInSharedPrefs(true);
        lastAdSeen = Variables.lastAdSeen;

    }

    @Override protected void onResume() {
        super.onResume();
        isWindowPaused = false;
        if (!TimeManager.isTimerOnline()) handleOnResumeMethodsIfTimeIsOffline();
        else handleOnResumeMethodsAndLogic();
    }

    private void handleOnResumeMethodsAndLogic(){
        Variables.isMainActivityOnline = true;
        if(isTimerPausedBecauseOfOfflineActivity) {
            Log(TAG,"resuming timer by starting it");
            resumeTimerByStartingIt();
        }
        if (!getCurrentDateInSharedPreferences().equals("0") && !getCurrentDateInSharedPreferences().equals(getDate())) {
            Log(TAG, "---Date in shared preferences does not match current date,therefore resetting everything.");
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            resetEverything();
            new DatabaseManager().clearAdsSeenSoFarInFirebase();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
        }else if (isAlmostMidNight() && Variables.isMainActivityOnline) {
            mIsBeingReset = true;
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            resetEverything();
            new DatabaseManager().clearAdsSeenSoFarInFirebase();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
        }else if(Variables.hasChangesBeenMadeToCategories && !mIsBeingReset){
            sendBroadcastToUnregisterAllReceivers();
            removeAllViews();
            loadAdsFromThread();
            lastAdSeen = null;
            Variables.lastAdSeen = null;
            Variables.hasChangesBeenMadeToCategories = false;
        }
        r = new Runnable() {
            @Override
            public void run() {
                Log(TAG, "---started the time checker for when it is almost midnight.");
                if (isAlmostMidNight() && Variables.isMainActivityOnline) {
                    mIsBeingReset = true;
                    resetEverything();
                    sendBroadcastToUnregisterAllReceivers();
                    removeAllViews();
                    new DatabaseManager().clearAdsSeenSoFarInFirebase();
                    lastAdSeen = null;
                    Variables.lastAdSeen = null;
                }
                h.postDelayed(r, 60000);
            }
        };
        h.postDelayed(r, 60000);
        addListenerForChangeInSessionKey();
    }

    private void handleOnResumeMethodsIfTimeIsOffline(){
        TimeManager.setUpTimeManager("RESET_UP_TIMER",mContext);
//        mAvi.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        mBottomNavButtons.setVisibility(View.GONE);
        mSwipeView.setVisibility(View.INVISIBLE);
        mAdCounterView.setVisibility(View.INVISIBLE);
        findViewById(R.id.easterText).setVisibility(View.GONE);
        mAviLoadingMoreAds.setVisibility(View.GONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleOnResumeMethodsAndLogic();
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                Log(TAG,"Unhiding views");
                mAdCounterView.setVisibility(View.VISIBLE);
//                mAvi.setVisibility(View.GONE);
                mLoadingProgressBar.setVisibility(View.GONE);
                mLoadingText.setVisibility(View.GONE);
                mBottomNavButtons.setVisibility(View.VISIBLE);
                findViewById(R.id.easterText).setVisibility(View.VISIBLE);
                mSwipeView.setVisibility(View.VISIBLE);
            }
        },new IntentFilter("RESET_UP_TIMER"));
    }

    @Override protected void onPause() {
        isWindowPaused = true;
        super.onPause();
        h.removeCallbacks(r);
        if(Variables.hasTimerStarted){
            pauseTimerByStoppingItEntirely();
            isTimerPausedBecauseOfOfflineActivity = true;
        }
        setCurrentDateToSharedPrefs();
        setUserDataInSharedPrefs();
        removeListenerForChangeInSessionKey();
    }




    @Override protected void onStop() {
        super.onStop();
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
//        setUserDataInSharedPrefs();
        setAlarmForNotifications();
        Variables.lastAdSeen = lastAdSeen;
        Log(TAG, "---removing callback for checking time of day.");
    }

    @Override protected void onDestroy() {
        setLastUsedDateInFirebaseDate(User.getUid());
        unregisterAllReceivers();
        removeAllViews();
        Variables.clearAllAdsFromAdList();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("TimerService"));
        if (!Variables.isDashboardActivityOnline) Variables.clearAdTotal();
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
        }

        Variables.isMainActivityOnline = false;
        super.onDestroy();
        setIsAppOnline(false);
    }

    private void setUserDataInSharedPrefs() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("TodaysTotals", Variables.getAdTotal(mKey));
        Log("MAIN_ACTIVITY--", "Setting todays ad totals in shared preferences - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.apply();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt("MonthsTotals", Variables.getMonthAdTotals(mKey));
        Log("MAIN_ACTIVITY--", "Setting the month totals in shared preferences - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.apply();

        SharedPreferences pref7 = getApplicationContext().getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putInt(Constants.REIMBURSEMENT_TOTALS, Variables.getTotalReimbursementAmount());
        Log("MAIN_ACTIVITY--", "Setting the Reimbursement totals in shared preferences - " + Integer.toString(Variables.getTotalReimbursementAmount()));
        editor7.apply();

        SharedPreferences pref8 = getApplicationContext().getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        SharedPreferences.Editor editor8 = pref8.edit();
        editor8.clear();
        editor8.putInt(Constants.CONSTANT_AMMOUNT_PER_VIEW,Variables.constantAmountPerView);
        Log(TAG,"Setting the constant amount per view in shared preferences - "+Integer.toString(Variables.constantAmountPerView));
        editor8.apply();

        SharedPreferences pref4 = mContext.getSharedPreferences("UID", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.putString("Uid", User.getUid());
        Log("MAIN_ACTIVITY---", "Setting the user uid in shared preferences - " + User.getUid());
        editor4.apply();

        SharedPreferences pref5 = mContext.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.putInt("CurrentSubIndex", Variables.getCurrentSubscriptionIndex());
        Log("MAIN_ACTIVITY---", "Setting the users current subscription index in shared preferences - " + Variables.getCurrentSubscriptionIndex());
        editor5.apply();

        SharedPreferences pref6 = mContext.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.putInt("CurrentAdInSubscription", Variables.getCurrentAdInSubscription());
        Log("MAIN_ACTIVITY---", "Setting the current ad in subscription in shared preferences - " + Variables.getCurrentAdInSubscription());
        editor6.apply();

        setSubsInSharedPrefs();
        setMarkersInSharedPrefs();
        setSeenAdsInSharedPrefs();
    }

    private void clearUserDataFromSharedPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.apply();

        SharedPreferences pref7 = getApplicationContext().getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.apply();

        SharedPreferences pref8 = getApplicationContext().getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        SharedPreferences.Editor editor8 = pref8.edit();
        editor8.clear();
        editor8.apply();

        SharedPreferences pref4 = mContext.getSharedPreferences("UID", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.apply();

        SharedPreferences pref5 = mContext.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.apply();

        SharedPreferences pref6 = mContext.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.apply();
    }




    private void setSubsInSharedPrefs() {
        Gson gson = new Gson();
        String hashMapString = gson.toJson(Variables.Subscriptions);

        SharedPreferences prefs = getSharedPreferences("Subscriptions", MODE_PRIVATE);
        prefs.edit().putString("hashString", hashMapString).apply();
    }

    private void setMarkersInSharedPrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_MARKERS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt(Constants.USER_MARKERS_SIZE, Variables.usersLatLongs.size());

        for(int i = 0; i <Variables.usersLatLongs.size(); i++){
            editor.putFloat("lat"+i, (float) Variables.usersLatLongs.get(i).latitude);
            editor.putFloat("long"+i, (float) Variables.usersLatLongs.get(i).longitude);
        }
        editor.apply();
    }

    private void setSeenAdsInSharedPrefs(){
        Gson gson = new Gson();
        String hashMapString = gson.toJson(Variables.adsSeenSoFar);

        SharedPreferences prefs = getSharedPreferences("AdsSeenSoFar", MODE_PRIVATE);
        prefs.edit().putString("AdsSeenSoFarHashString", hashMapString).apply();
    }




    private void loadSeenAdsInSharedPrefs(){
        if(!Variables.adsSeenSoFar.isEmpty())Variables.adsSeenSoFar.clear();
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("AdsSeenSoFar", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("AdsSeenSoFarHashString", "nil");

        java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
        Variables.adsSeenSoFar = gson.fromJson(storedHashMapString, type);
    }

    private void loadMarkersInSharedPrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_MARKERS,MODE_PRIVATE);
        Variables.usersLatLongs.clear();
        int size = sharedPreferences.getInt(Constants.USER_MARKERS_SIZE, 0);
        for(int i = 0; i < size; i++){
            double lat = (double) sharedPreferences.getFloat("lat"+i,0);
            double longit = (double) sharedPreferences.getFloat("long"+i,0);
            LatLng latLng = new LatLng(lat,longit);
            Variables.usersLatLongs.add(latLng);
        }
    }

    private void loadAdsFromThread() {
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        if (Variables.isStartFromLogin) {
            try {
                Log(TAG, "---Starting the getAds method...");
                hideViews();
                startGetAds();
                Variables.isStartFromLogin = false;
            } catch (Exception e) {
                Log.e("BACKGROUND_PROC---", e.getMessage());
                e.printStackTrace();
            }
        } else {
            hideViews();
            loadUserDataFromSharedPrefs();
        }
    }




    private void loadUserDataFromFirebaseLikeInLogin(){
        if(!TimeManager.isTimerOnline()) {
            TimeManager.setUpTimeManager(Constants.LOAD_TIME,mContext);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
                    nowRealyStartLoadingUserDataFromFirebaseLikeInLogin();
                }
            },new IntentFilter(Constants.LOAD_TIME));
        }else{
            nowRealyStartLoadingUserDataFromFirebaseLikeInLogin();
        }
    }

    private void nowRealyStartLoadingUserDataFromFirebaseLikeInLogin(){
        isLoadingDataFromFirebase = true;
        Variables.Subscriptions.clear();
        DatabaseManager dbMan = new DatabaseManager();
        dbMan.setContext(mContext);
        dbMan.loadUserData(mContext);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                User.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                startGetAds();
                isLoadingDataFromFirebase = false;
            }
        },new IntentFilter(Constants.LOADED_USER_DATA_SUCCESSFULLY));

        while (isLoadingDataFromFirebase) nothn();
    }

    private void loadUserDataFromSharedPrefs() {
        loadSubsFromSharedPrefs();
        loadMarkersInSharedPrefs();
        Log(TAG, "Loading user data from shared preferences first...");
        SharedPreferences prefs = getSharedPreferences("TodayTotals", MODE_PRIVATE);
        int number = prefs.getInt("TodaysTotals", 0);
        Log(TAG, "AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setAdTotal(0, mKey);
            isNewDay = true;
            Log(TAG, "Setting ad totals in firebase to 0 since is being reset...");
        } else {
            Variables.setAdTotal(number, mKey);
        }

        SharedPreferences prefs2 = getSharedPreferences("MonthTotals", MODE_PRIVATE);
        int number2 = prefs2.getInt("MonthsTotals", 0);
        Log(TAG, "MONTH AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number2);
        Variables.setMonthAdTotals(mKey, number2);

        SharedPreferences prefs7 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        int number7 = prefs7.getInt(Constants.REIMBURSEMENT_TOTALS, 0);
        Log(TAG, "REIMBURSEMENT TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number7);
        Variables.setTotalReimbursementAmount(number7);

        SharedPreferences prefs8 = getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        int number8 = prefs8.getInt(Constants.CONSTANT_AMMOUNT_PER_VIEW,3);
        Log(TAG,"CONSTANT AMOUNT GOTTEN PER AD FROM SHARED PREFERENCES IS -   "+number8);
        Variables.constantAmountPerView = number8;

        SharedPreferences pref9 = getSharedPreferences(Constants.PREFERRED_NOTF_HOUR,MODE_PRIVATE);
        Variables.preferredHourOfNotf = pref9.getInt(Constants.PREFERRED_NOTF_HOUR,7);

        SharedPreferences pref10 = getSharedPreferences(Constants.PREFERRED_NOTF_MIN,MODE_PRIVATE);
        Variables.preferredMinuteOfNotf = pref10.getInt(Constants.PREFERRED_NOTF_MIN,30);

        SharedPreferences prefs4 = getSharedPreferences("UID", MODE_PRIVATE);
        String uid = prefs4.getString("Uid", "");
        Log(TAG, "UID NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + uid);
        User.setUid(uid);

        SharedPreferences prefs5 = getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        int currentSubIndex = prefs5.getInt("CurrentSubIndex",0);
        Log(TAG, "CURRENT SUBSCRIPTION INDEX NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + currentSubIndex);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setCurrentSubscriptionIndex(0);
            Log(TAG,"Setting current sub index to 0 since date in shared prefs doesnt match current date or is being reset");
        }else{
            Variables.setCurrentSubscriptionIndex(currentSubIndex);
        }

        SharedPreferences prefs6 = getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        int currentAdInSubscription = prefs6.getInt("CurrentAdInSubscription",0);
        Log(TAG,"CURRENT AD IN SUBSCRIPTION GOTTEN FROM SHARED PREFERENCES IS : "+currentAdInSubscription);
        if (mIsBeingReset || !getCurrentDateInSharedPreferences().equals(getDate())) {
            Variables.setCurrentAdInSubscription(0);
        }else{
            Variables.setCurrentAdInSubscription(currentAdInSubscription);
        }

        Variables.isStartFromLogin = false;
        if(isNewDay){
            hideViews();
            DatabaseManager dbmanager = new DatabaseManager();
            dbmanager.setContext(mContext);
            dbmanager.setAnnouncementBoolean(mContext);
            dbmanager.loadAnyAnnouncementsFromMainActivity();
            dbmanager.clearAdsSeenSoFarInFirebase();
            clearSeenAds();
            dbmanager.syncUserDataInFirebase();
            dbmanager.checkIfNeedToResetUsersSubscriptions(mContext);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForDoneCheckingIfNeedToReCreateClusters,
                    new IntentFilter(Constants.LOADED_USER_DATA_SUCCESSFULLY));
            isNewDay = false;
        }else{
            loadSeenAdsInSharedPrefs();
            try {
                Log(TAG, "---Starting the getAds method...");
                startGetAds();
            } catch (Exception e) {
                Log.e("BACKGROUND_PROC---", e.getMessage());
            }
        }

    }

    private void loadSubsFromSharedPrefs() {
        if(!Variables.Subscriptions.isEmpty())Variables.Subscriptions.clear();
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("Subscriptions", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("hashString", "nil");

        java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String,Integer>>(){}.getType();
        Variables.Subscriptions = gson.fromJson(storedHashMapString, type);
    }

    private void clearSeenAds(){
        if(!Variables.adsSeenSoFar.isEmpty())Variables.adsSeenSoFar.clear();
        SharedPreferences prefs = getSharedPreferences("AdsSeenSoFar", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }




    private void startGetAds() {
        setUpAllTheViews();
        hideViews();
        if(canStartFromCustomClass()){
            Variables.setCurrentSubscriptionIndex(getTheCustomCategoryToStartFrom());
            Variables.setCurrentAdInSubscription(0);
            removeTheCustomCategoryEnabling();
        }
//        getGetAdsFromFirebase();
        startGetAdsThroughOneSnapShot();
    } ///////////////////////////




    private void getAds() {
        try {
            Log(TAG, "---The getAdsFromFirebase method has been called...");
            getGetAdsFromFirebase();
            Thread.sleep(300);
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }//method for loading ads from thread.Contains sleep length...



    //this loads the entire top node,with all the categories, then equates it to a local variable, then uses that
    // as a snapshot. Faster and more efficient than previous method.
    private void startGetAdsThroughOneSnapShot(){
        if(Variables.Subscriptions.isEmpty()){
            loadSubsFromSharedPrefs();
        }
        getAdsSnapShot();
    }

    private void getAdsSnapShot(){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdsSnapshot = dataSnapshot;
                getTargetedUsersDataList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mAvi.setVisibility(View.GONE);
                mLoadingText.setVisibility(View.GONE);
                showFailedView();
            }
        });
    }

    private void getTargetedUsersDataList(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Constants.TARGET_USER_DATA);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTargetUsersDataList = dataSnapshot;
                getAdsFromSnapshot();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAdsFromSnapshot(){
        String date;
        date = mIsBeingReset ? getNextDay() : getDate();

        if(!mAdList.isEmpty()) mAdList.clear();
        Variables.clearAllAdsFromAdList();

        Variables.nextSubscriptionIndex = Variables.getCurrentSubscriptionIndex();
        DataSnapshot mySnap = mAdsSnapshot.child(date).child(Integer.toString(Variables.constantAmountPerView))
                .child(getSubscriptionValue(Variables.getCurrentSubscriptionIndex()))
                .child(Integer.toString(getClusterValue(Variables.getCurrentSubscriptionIndex())));

        Log(TAG, "---Query set up is : " + date + " : "+Variables.constantAmountPerView+ " : "
                + getSubscriptionValue(Variables.getCurrentSubscriptionIndex())+ " : "
                + getClusterValue(Variables.getCurrentSubscriptionIndex()));

        List<DataSnapshot> dbSnaps = new ArrayList<>();
        if(Variables.getCurrentAdInSubscription()==0){
            int start = 1;
            int stop = start+Constants.NO_OF_ADS_TO_LOAD;
            for(int i = start; i<stop; i++){
                String key = Integer.toString(i);
                Log(TAG,"Attempting to load ad "+key+" in category "+getSubscriptionValue(Variables.getCurrentSubscriptionIndex()));
                DataSnapshot adSnap = mySnap.child(key);
                if(adSnap.exists()){
                    dbSnaps.add(adSnap);
                }
            }
        }else{
            int start = Variables.getCurrentAdInSubscription();
            int stop = start+Constants.NO_OF_ADS_TO_LOAD;
            for(int i = start; i<stop; i++){
                String key = Integer.toString(i);
                Log(TAG,"Attempting to load ad "+key+" in category "+getSubscriptionValue(Variables.getCurrentSubscriptionIndex()));
                DataSnapshot adSnap = mySnap.child(key);
                if(adSnap.exists()){
                    dbSnaps.add(adSnap);
                }
            }
        }
        handleDbSnapShotList(dbSnaps);
    }

    private void handleDbSnapShotList(List<DataSnapshot> dbSnaps){
        Log(TAG,"Number of children from firebase is : "+dbSnaps.size());
        boolean canLoad = true;
        if(Variables.constantAmountPerView>3
                && Variables.getAdTotal(mKey)+Constants.NO_OF_ADS_TO_LOAD2>Constants.MAX_NUMBER_FOR7){
            canLoad = false;
        }
        if (!dbSnaps.isEmpty()) {
            if(dbSnaps.size()==1){
                //if only one ad has loaded.
                Log(TAG,"Only one ad has loaded.");
                for (DataSnapshot snap : dbSnaps) {
                    Advert ad = snap.getValue(Advert.class);
                    if(snap.child("contactdata").exists()){
                        ad.setAdvertiserPhoneNo(snap.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                                .getValue(String.class));
                        if(snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).exists()){
                            List<AdvertiserLocation> advertisersLoc = new ArrayList<>();
                            for(DataSnapshot locSnap:snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).getChildren()){
                                String name = "";
                                if(locSnap.child("name").exists())name = locSnap.child("name").getValue(String.class);
                                double lat = locSnap.child("lat").getValue(double.class);
                                double lng = locSnap.child("lng").getValue(double.class);
                                advertisersLoc.add(new AdvertiserLocation(new myLatLng(lat,lng),name));
                            }
                            ad.setAdvertiserLocations(advertisersLoc);
                        }
                    }
                    DataSnapshot snpsht = snap.child("pushId");
                    String pushID = snpsht.getValue(String.class);
                    ad.setPushId(pushID);
                    ad.setPushIdNumber(Integer.parseInt(pushID));
                    if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)) mAdList.add(ad);
                }
                if(mAdList.size()!=0){
                    Log(TAG,"The one ad was not flagged so its in the adlist");
                    if(Variables.getCurrentAdInSubscription()!=Integer.parseInt(mAdList.get(0).getPushId())){
                        //user hasn't seen the one ad that has been loaded
                        Log(TAG,"The user hasn't seen the one ad in the adlist.");
                        Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                        mChildToStartFrom = Variables.getCurrentAdInSubscription()+ mAdList.size();
                        Log(TAG,"The child set to start from is : "+mChildToStartFrom);
                        Variables.setCurrentAdNumberForAllAdsList(0);
                        loadAdsIntoAdvertCard();
                    }else{
                        //user has seen the one ad that has been loaded.
                        Log(TAG,"User has seen the one ad that has been loaded so going to the next subscription");
                        Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                        lastAdSeen = mAdList.get(0);
                        mChildToStartFrom = Integer.parseInt(lastAdSeen.getPushId());
                        if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                            Log(TAG,"Trying the next subscription.");
                            Variables.setNextSubscriptionIndex();
                            Variables.setCurrentAdInSubscription(0);
                            getAdsFromSnapshot();
                        } else {
                            Log(TAG, "---There are no ads in any of the subscriptions");
                            loadAdsIntoAdvertCard();
                        }
                    }
                }else{
                    //the one ad may have been flagged so moving on to the next subscription
                    Log(TAG,"the one ad may have been flagged so moving on to the next subscription");
                    if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                        Log(TAG,"Trying the next subscription.");
                        Variables.setNextSubscriptionIndex();
                        Variables.setCurrentAdInSubscription(0);
                        getAdsFromSnapshot();
                    } else {
                        Log(TAG, "---There are no ads in any of the subscriptions");
                        loadAdsIntoAdvertCard();
                    }
                }

            }else{
                //if multiple ads have loaded.
                Log(TAG,"More than one ad has been loaded from firebase");
                for (DataSnapshot snap : dbSnaps) {
                    Advert ad = snap.getValue(Advert.class);
                    DataSnapshot snpsht = snap.child("pushId");
                    String pushID = snpsht.getValue(String.class);
                    if(snap.child("contactdata").exists()){
                        ad.setAdvertiserPhoneNo(snap.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                                .getValue(String.class));
                        if(snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).exists()){
                            List<AdvertiserLocation> advertisersLoc = new ArrayList<>();
                            for(DataSnapshot locSnap:snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).getChildren()){
                                String name = "";
                                if(locSnap.child("name").exists())name = locSnap.child("name").getValue(String.class);
                                double lat = locSnap.child("lat").getValue(double.class);
                                double lng = locSnap.child("lng").getValue(double.class);
                                advertisersLoc.add(new AdvertiserLocation(new myLatLng(lat,lng),name));
                            }
                            ad.setAdvertiserLocations(advertisersLoc);
                        }
                    }
                    ad.setPushId(pushID);
                    ad.setPushIdNumber(Integer.parseInt(pushID));
                    if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)){
                        if(Variables.constantAmountPerView>3 && Variables.getAdTotal(mKey)+1>Constants.MAX_NUMBER_FOR7) {
                            Log(TAG,"User cannot see more than "+Constants.MAX_NUMBER_FOR7+" ads.");
                        }else mAdList.add(ad);
                    }
                }
                if(mAdList.size()==0){
                    //all the ads loaded may have been flagged so loading the next ads after those ones.
                    Log(TAG,"All the ads loaded have been flagged so loading the next batch");
                    Variables.setCurrentAdInSubscription(Variables.getCurrentAdInSubscription()+ dbSnaps.size());
                    getAdsFromSnapshot();
                }else if(mAdList.size()==1 && Variables.getCurrentAdInSubscription()==Integer.parseInt(mAdList.get(0).getPushId())){
                    Log(TAG,"There is only one ad that has been loaded.Perhaps the others were skipped because they were flagged");
                    Log(TAG,"User has seen the one ad that has been loaded so going to the next subscription");
                    Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                            "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                    lastAdSeen = mAdList.get(0);
                    mChildToStartFrom = Integer.parseInt(lastAdSeen.getPushId());
                    if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                        Log(TAG,"Trying the next subscription.");
                        Variables.setNextSubscriptionIndex();
                        Variables.setCurrentAdInSubscription(0);
                        getAdsFromSnapshot();
                    } else {
                        Log(TAG, "---There are no ads in any of the subscriptions");
                        loadAdsIntoAdvertCard();
                    }
                } else{
                    Variables.setCurrentAdNumberForAllAdsList(0);
                    //removing the first ad if the user has seen it.
                    Log(TAG,"removing the first ad if the user has seen it.");
                    if(Variables.getCurrentAdInSubscription()==Integer.parseInt(mAdList.get(0).getPushId())) {
                        mAdList.remove(0);
                        Log(TAG,"First ad has been removed because it has been seen");
                    }
                    mChildToStartFrom = Variables.getCurrentAdInSubscription()+mAdList.size();
                    Log(TAG, "Child set to start from is -- " + mChildToStartFrom);
                    Log(TAG, "---All the ads have been handled.Total is " + mAdList.size());
                    loadAdsIntoAdvertCard();
                }
            }

        }else{
            if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                Log(TAG, "---There are no ads in subscription : "
                        + getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                Variables.setNextSubscriptionIndex();
                Variables.setCurrentAdInSubscription(0);
                getAdsFromSnapshot();
            } else {
                Log(TAG, "---There are no ads in any of the subscriptions");
                loadAdsIntoAdvertCard();
            }
        }
    }



    //this is the original method, individually querying one node at a time.
    private void getGetAdsFromFirebase() {
        String date;
        date = mIsBeingReset ? getNextDay() : getDate();

        if(!mAdList.isEmpty()) mAdList.clear();
        Variables.clearAllAdsFromAdList();

        Variables.nextSubscriptionIndex = Variables.getCurrentSubscriptionIndex();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                .child(Integer.toString(Variables.constantAmountPerView))
                .child(getSubscriptionValue(Variables.getCurrentSubscriptionIndex()))
                .child(Integer.toString(getClusterValue(Variables.getCurrentSubscriptionIndex())));
        Log(TAG, "---Query set up is : " + Constants.ADVERTS + " : " + date + " : "+Variables.constantAmountPerView+ " : "
                + getSubscriptionValue(Variables.getCurrentSubscriptionIndex())+ " : " + getClusterValue(Variables.getCurrentSubscriptionIndex()));
        dbRef = query.getRef();

        if (Variables.getCurrentAdInSubscription() == 0) {
            Log(TAG, "User current ad in subscription is 0, so is starting at 1");
            dbRef.orderByKey().startAt(Integer.toString(1)).limitToFirst(Constants.NO_OF_ADS_TO_LOAD).addValueEventListener(val);
        } else {
            Log(TAG, "User current ad in subscription is not 0, so starting at its value : " + Variables.getCurrentAdInSubscription());
            dbRef.orderByKey().startAt(Integer.toString(Variables.getCurrentAdInSubscription()))
                    .limitToFirst(Constants.NO_OF_ADS_TO_LOAD).addListenerForSingleValueEvent(val);
        }
    }

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log(TAG,"Number of children from firebase is : "+dataSnapshot.getChildrenCount());
            boolean canLoad = true;
            if(Variables.constantAmountPerView>3
                    && Variables.getAdTotal(mKey)+Constants.NO_OF_ADS_TO_LOAD2>Constants.MAX_NUMBER_FOR7){
                canLoad = false;
            }
            if (dataSnapshot.hasChildren()) {
                if(dataSnapshot.getChildrenCount()==1){
                    //if only one ad has loaded.
                    Log(TAG,"Only one ad has loaded.");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Advert ad = snap.getValue(Advert.class);
                        DataSnapshot snpsht = snap.child("pushId");
                        String pushID = snpsht.getValue(String.class);
                        ad.setPushId(pushID);
                        ad.setPushIdNumber(Integer.parseInt(pushID));
                        if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)) mAdList.add(ad);
                    }
                    if(mAdList.size()!=0){
                        Log(TAG,"The one ad was not flagged so its in the adlist");
                        if(Variables.getCurrentAdInSubscription()!=Integer.parseInt(mAdList.get(0).getPushId())){
                            //user hasn't seen the one ad that has been loaded
                            Log(TAG,"The user hasn't seen the one ad in the adlist.");
                            Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                    "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                            mChildToStartFrom = Variables.getCurrentAdInSubscription()+ mAdList.size();
                            Log(TAG,"The child set to start from is : "+mChildToStartFrom);
                            Variables.setCurrentAdNumberForAllAdsList(0);
                            loadAdsIntoAdvertCard();
                        }else{
                            //user has seen the one ad that has been loaded.
                            Log(TAG,"User has seen the one ad that has been loaded so going to the next subscription");
                            Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                    "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                            lastAdSeen = mAdList.get(0);
                            mChildToStartFrom = Integer.parseInt(lastAdSeen.getPushId());
                            if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                                Log(TAG,"Trying the next subscription.");
                                Variables.setNextSubscriptionIndex();
                                Variables.setCurrentAdInSubscription(0);
                                getGetAdsFromFirebase();
                            } else {
                                Log(TAG, "---There are no ads in any of the subscriptions");
                                loadAdsIntoAdvertCard();
                            }
                        }
                    }else{
                        //the one ad may have been flagged so moving on to the next subscription
                        Log(TAG,"the one ad may have been flagged so moving on to the next subscription");
                        if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                            Log(TAG,"Trying the next subscription.");
                            Variables.setNextSubscriptionIndex();
                            Variables.setCurrentAdInSubscription(0);
                            getGetAdsFromFirebase();
                        } else {
                            Log(TAG, "---There are no ads in any of the subscriptions");
                            loadAdsIntoAdvertCard();
                        }
                    }

                }else{
                    //if multiple ads have loaded.
                    Log(TAG,"More than one ad has been loaded from firebase");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Advert ad = snap.getValue(Advert.class);
                            DataSnapshot snpsht = snap.child("pushId");
                            String pushID = snpsht.getValue(String.class);
                            ad.setPushId(pushID);
                            ad.setPushIdNumber(Integer.parseInt(pushID));
                            if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)){
                                if(Variables.constantAmountPerView>3 && Variables.getAdTotal(mKey)+1>Constants.MAX_NUMBER_FOR7) {
                                    Log(TAG,"User cannot see more than "+Constants.MAX_NUMBER_FOR7+" ads.");
                                }else mAdList.add(ad);
                            }
                    }
                    if(mAdList.size()==0){
                        //all the ads loaded may have been flagged so loading the next ads after those ones.
                        Log(TAG,"All the ads loaded have been flagged so loading the next batch");
                        Variables.setCurrentAdInSubscription(Variables.getCurrentAdInSubscription()+
                                (int)dataSnapshot.getChildrenCount());
                        getGetAdsFromFirebase();
                    }else if(mAdList.size()==1 && Variables.getCurrentAdInSubscription()==Integer.parseInt(mAdList.get(0).getPushId())){
                        Log(TAG,"There is only one ad that has been loaded.Perhaps the others were skipped because they were flagged");
                        Log(TAG,"User has seen the one ad that has been loaded so going to the next subscription");
                        Log(TAG,"Since the currentAdInSubscription is : "+Variables.getCurrentAdInSubscription()+" and " +
                                "the ad's pushID is : "+(mAdList.get(0).getPushId()));
                        lastAdSeen = mAdList.get(0);
                        mChildToStartFrom = Integer.parseInt(lastAdSeen.getPushId());
                        if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                            Log(TAG,"Trying the next subscription.");
                            Variables.setNextSubscriptionIndex();
                            Variables.setCurrentAdInSubscription(0);
                            getGetAdsFromFirebase();
                        } else {
                            Log(TAG, "---There are no ads in any of the subscriptions");
                            loadAdsIntoAdvertCard();
                        }
                    } else{
                        Variables.setCurrentAdNumberForAllAdsList(0);
                        //removing the first ad if the user has seen it.
                        Log(TAG,"removing the first ad if the user has seen it.");
                        if(Variables.getCurrentAdInSubscription()==Integer.parseInt(mAdList.get(0).getPushId())) {
                            mAdList.remove(0);
                            Log(TAG,"First ad has been removed because it has been seen");
                        }
                        mChildToStartFrom = Variables.getCurrentAdInSubscription()+mAdList.size();
                        Log(TAG, "Child set to start from is -- " + mChildToStartFrom);
                        Log(TAG, "---All the ads have been handled.Total is " + mAdList.size());
                        loadAdsIntoAdvertCard();
                    }
                }

            }else{
                if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                    Log(TAG, "---There are no ads in subscription : " + getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                    Variables.setNextSubscriptionIndex();
                    Variables.setCurrentAdInSubscription(0);
                    getGetAdsFromFirebase();
                } else {
                    Log(TAG, "---There are no ads in any of the subscriptions");
                    loadAdsIntoAdvertCard();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            showFailedView();
        }
    };

    ValueEventListener val2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.hasChildren()) {
                if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                    Log(TAG, "---There are no ads in subscription : " + getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                    Variables.setNextSubscriptionIndex();
                    Variables.setCurrentAdInSubscription(0);
                    getGetAdsFromFirebase();
                } else {
                    Log(TAG, "---There are no ads in any of the subscriptions");
                    mAvi.setVisibility(View.GONE);
                    mLoadingText.setVisibility(View.GONE);
                    mBottomNavButtons.setVisibility(View.VISIBLE);
                    loadAdsIntoAdvertCard();
                }
            } else {
                Log(TAG, "---Children in dataSnapshot from firebase exist");
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Advert ad = snap.getValue(Advert.class);
                    DataSnapshot snpsht = snap.child("pushId");
                    String pushID = snpsht.getValue(String.class);
                    ad.setPushId(pushID);
                    if(!ad.isFlagged()) mAdList.add(ad);
                }
                if(mAdList.size()!=0){
                    if (Variables.getCurrentAdInSubscription() != 0) {
                        //this means user has seen some ads in current subscription index.
                        //removing first ad from ad list if there are more than one ads.
                        //this is because user has seen the first ad in the ad list.
                        if (mAdList.size() > 1) mAdList.remove(0);
                        //setting the child to start from to the number of children gotten to current ad in sub + children count minus one.
                        //this is because snapshot contains child that has been seen by user.
                        mChildToStartFrom = Variables.getCurrentAdInSubscription() + (int) dataSnapshot.getChildrenCount() - 1;
                        Log(TAG,"user has seen some of the ads from current subscription index.");

                    } else {
                        //this means that user has not seen any ad in current subscription index.
                        mChildToStartFrom = (int) dataSnapshot.getChildrenCount();
                        Log(TAG,"User has seen none of the ads gotten from current subscription index.");
                        //setting the child to start from to number of children gotten.
                    }
                    Variables.setCurrentAdNumberForAllAdsList(0);
                    Log(TAG, "Child set to start from is -- " + mChildToStartFrom);
                    Log(TAG, "---All the ads have been handled.Total is " + mAdList.size());
                    mAvi.setVisibility(View.GONE);
                    mLoadingText.setVisibility(View.GONE);
                    mBottomNavButtons.setVisibility(View.VISIBLE);
                    loadAdsIntoAdvertCard();
                }else{
                    if (Variables.getCurrentSubscriptionIndex()+1 < Variables.Subscriptions.size()) {
                        Log(TAG, "---There was 1 flagged ad subscription : " +
                                getSubscriptionValue(Variables.getCurrentSubscriptionIndex()) + ". Retrying in the next category");
                        Variables.setNextSubscriptionIndex();
                        Variables.setCurrentAdInSubscription(0);
                        getGetAdsFromFirebase();
                    } else {
                        Log(TAG, "---There are no ads in any other subscriptions");
                        mAvi.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                        mBottomNavButtons.setVisibility(View.VISIBLE);
                        loadAdsIntoAdvertCard();
                    }
                }

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mAvi.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            showFailedView();
        }
    };




    private void showFailedView() {
        mFailedToLoadLayout.setVisibility(View.VISIBLE);
        mRetryButton.setOnClickListener(this);
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
//            loadAdsIntoAdvertCard();
        }
    };

    private void unregisterAllReceivers() {
        Log("MAIN_ACTIVITY--", "Unregistering all receivers");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddingToSharedPreferences);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOffline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOnline);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLastAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLoadMoreAds);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasStarted);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForOnSwiped);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnhideVeiws);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForContinueShareImage);

        try{
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForDoneCheckingIfNeedToReCreateClusters);
        }catch (Exception e){
            e.printStackTrace();
        }

        sendBroadcastToUnregisterAllReceivers();
    }

    private void sendBroadcastToUnregisterAllReceivers() {
        Intent intent = new Intent(Constants.UNREGISTER_ALL_RECEIVERS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }




    private void setUpAllTheViews() {
        mSwipeView = findViewById(R.id.swipeView);
        mAdCounterView = findViewById(R.id.adCounterView);
        mBottomNavButtons = findViewById(R.id.bottomNavButtons);

        mAvi = findViewById(R.id.mainActivityAvi);
        mLoadingProgressBar = findViewById(R.id.progressBarMain);
        mAviLoadingMoreAds = findViewById(R.id.aviLoadingNextAds);
        spinner = findViewById(R.id.progressBar1);

        mLoadingText = findViewById(R.id.loadingAdsMessage);
        mFailedToLoadLayout = findViewById(R.id.failedLoadAdsLayout);
        mRetryButton = findViewById(R.id.retryLoadingAds);
        mLogoutButton = findViewById(R.id.logoutBtn);

        cannotLoadLayout = findViewById(R.id.noInternetLayout);
        retryLoadingFromCannotLoad = findViewById(R.id.btn_retry);

        int bottomMargin = Utils.dpToPx(90);
        Point windowSize = Utils.getDisplaySize(getWindowManager());
        Variables.windowSize = windowSize;
        float relativeScale = density();
        Variables.relativeScale = relativeScale;

        mSwipeView.getBuilder()
                .setDisplayViewCount(4)
                .setIsUndoEnabled(false)
                .setHeightSwipeDistFactor(10)
                .setWidthSwipeDistFactor(5)
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setSwipeRotationAngle(0)
                        .setSwipeAnimTime(150)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(15)
                        .setRelativeScale(relativeScale));
    }

    private void removeAllViews() {
        if (mSwipeView != null) {
            mSwipeView.removeAllViews();
        }
        if (mAdCounterView != null) {
            mAdCounterView.removeAllViews();
        }
    }

    private void loadAdCounter() {
        mAdCounterView = findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(), mAdCounterView));

    }




    private void loadAdsIntoAdvertCard(){
        String date;
        date = mIsBeingReset ? getNextDay() : getDate();

       if(!mAdList.isEmpty()){
           //This will load all the ads images.
           for(final Advert ad: mAdList){
               String pushRefInAdminConsole = ad.getPushRefInAdminConsole();
               DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                       .child(pushRefInAdminConsole).child("imageUrl");
               adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       String imageUrl = dataSnapshot.getValue(String.class);
                       mAdList.get(mAdList.indexOf(ad)).setImageUrl(imageUrl);
                       iterations++;
                       if(iterations == mAdList.size()){
                           iterations = 0;
                           loadAdsIntoAdvertCard2();
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {
                        Log(TAG,"An error occured, "+databaseError.getDetails());
                   }
               });
           }

       }else if(lastAdSeen!=null){
           //this will load the image of the last ad only.
           String pushRefInAdminConsole = lastAdSeen.getPushRefInAdminConsole();
           DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                   .child(pushRefInAdminConsole).child("imageUrl");
           adRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   String imageUrl = dataSnapshot.getValue(String.class);
                   lastAdSeen.setImageUrl(imageUrl);
                   loadAdsIntoAdvertCard2();
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {
                    Log(TAG,"An error occurred while loading data, "+databaseError.getDetails());
               }
           });

       } else{
         loadAdsIntoAdvertCard2();
       }
    }

    private void loadAdsIntoAdvertCard2() {
        boolean loadMoreAds = false;
        Variables.isLocked = false;
        stage = "VIEWING_ADS";
        if (mAdCounterView == null) {
            Log(TAG, "---Setting up AdCounter...");
            loadAdCounter();
        }
        if (mSwipeView == null) {
            Log(TAG, "---Setting up Swipe views..");
            setUpAllTheViews();
        }
        if (mSwipeView.getChildCount() != 0) {
            Log(TAG, "Removing existing children from swipeView...");
            mSwipeView.removeAllViews();
        }
        if (mAdCounterView.getChildCount() == 0) {
            Log(TAG, "Loading the top timer now...");
            loadAdCounter();
        }
        if (mAdList != null && mAdList.size() > 0) {
            if (mAdList.size() == 1 && mChildToStartFrom == Variables.getCurrentAdInSubscription()) {
                Log(TAG, "---User has seen all the ads, thus will load only last ad...");
                Log(TAG,"The child to start from is : "+mChildToStartFrom+" and currentAdInSubscriptionIs : "+
                        Variables.getCurrentAdInSubscription());
                lockViews();
                mAdList.get(0).setNatureOfBanner(Constants.IS_AD);
                Variables.adToVariablesAdList(mAdList.get(0));
                Variables.firstAd = mAdList.get(0);
                mSwipeView.addView(new AdvertCard(mContext, mAdList.get(0), mSwipeView, Constants.LAST));
                Variables.setIsLastOrNotLast(Constants.LAST);
                Variables.setCurrentAdvert(mAdList.get(0));
                Variables.setCurrentSubscriptionIndex(getPositionOf(mAdList.get(0).getCategory()));
                try{
                    Variables.setCurrentAdInSubscription(Integer.parseInt(mAdList.get(0).getPushId()));
                }catch (Exception e){
                    e.printStackTrace();
                    Variables.setCurrentAdInSubscription(mAdList.get(0).getPushIdNumber());
                }
                if(mAdList.get(0).isFlagged())mAdList.get(0).setWebsiteLink(igsNein);
                if(mAdList.get(0).getWebsiteLink().equals(igsNein)){
                    findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }

//                if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
//                if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();
//                Toast.makeText(mContext, "We've got no more stuff for you today.", Toast.LENGTH_SHORT).show();
                isLastAd = true;
                Variables.isLockedBecauseOfNoMoreAds = true;
                loadAnyAnnouncements();
            } else {
                if (mAdList.size() == 1 && Variables.getCurrentSubscriptionIndex() + 1 < Variables.Subscriptions.size()) {
                    Variables.nextSubscriptionIndex = Variables.getCurrentSubscriptionIndex() + 1;
                    mChildToStartFrom = 0;
//                    loadMoreAds = true;
//                    loadMoreAds();
                }
                Variables.firstAd = mAdList.get(0);
                for (Advert ad : mAdList) {
                    ad.setNatureOfBanner(Constants.IS_AD);
                    Variables.adToVariablesAdList(ad);
                    mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.NOT_LAST));
                    Log(TAG, "Loading ad " + ad.getPushRefInAdminConsole());
                    Variables.setIsLastOrNotLast(Constants.NOT_LAST);
                }
            }
            numberOfInitiallyLoadedAds = mAdList.size();
            mAdList.clear();
            Log(TAG,"cleared the adlist");
        } else {
//            if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
//            if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();
            numberOfInitiallyLoadedAds = 1;
            if(lastAdSeen!=null){
                Log(TAG, "---Loading only last ad from lastAdSeen that was initialised...");
                lockViews();
                lastAdSeen.setNatureOfBanner(Constants.IS_AD);
                Variables.adToVariablesAdList(lastAdSeen);
                Variables.firstAd = lastAdSeen;
                mSwipeView.addView(new AdvertCard(mContext, lastAdSeen, mSwipeView, Constants.LAST));
                Variables.setIsLastOrNotLast(Constants.LAST);
                Variables.setCurrentAdvert(lastAdSeen);
                Variables.setCurrentSubscriptionIndex(getPositionOf(lastAdSeen.getCategory()));
                try{
                    Variables.setCurrentAdInSubscription(Integer.parseInt(lastAdSeen.getPushId()));
                }catch (Exception e){
                    e.printStackTrace();
                    Variables.setCurrentAdInSubscription(lastAdSeen.getPushIdNumber());
                }
                if(lastAdSeen.isFlagged())lastAdSeen.setWebsiteLink(igsNein);
                if(!lastAdSeen.didAdvertiserSetContactInfo()){
                    findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }
//                Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                isLastAd = true;
            }else{
                stretchCard();

                Advert noAds = new Advert();
                noAds.setWebsiteLink(igsNein);
                noAds.setPushRefInAdminConsole("NONE");
                noAds.setCategory("NoAds");
                noAds.setNatureOfBanner("NoAds");
                Variables.firstAd = noAds;
                Variables.adToVariablesAdList(noAds);
                mSwipeView.addView(new AdvertCard(mContext, noAds, mSwipeView, Constants.NO_ADS));
                lockViews();
                Variables.setIsLastOrNotLast(Constants.NO_ADS);
                findViewById(R.id.WebsiteIcon).setAlpha(0.4f);
                findViewById(R.id.websiteText).setAlpha(0.4f);
                findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);

                if(Variables.getAdTotal(mKey)==0)setCategoryIndexInFirebase();
            }
            Variables.isLockedBecauseOfNoMoreAds = true;
            loadAnyAnnouncements();
        }

        Log(TAG, "---Setting up On click listeners...");
        onclicks();
        Log(TAG,"Todays ad total is : "+Variables.getAdTotal(mKey));
        Log(TAG,"The month Ad Total is : "+Variables.getMonthAdTotals(mKey));
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
//        if(loadMoreAds) loadMoreAds();
    }

    private void setCategoryIndexInFirebase() {
        String uid  = User.getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        Log(TAG,"Setting current subscription index in firebase to :"+Variables.getCurrentSubscriptionIndex());
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());
    }

    private void stretchCard(){
        int bottomMargin = Utils.dpToPx(80);
        Point windowSize = Variables.windowSize;
        float relativeScale = Variables.relativeScale;

        mSwipeView.getBuilder().setSwipeDecor(new SwipeDecor()
                .setViewWidth(windowSize.x)
                .setViewHeight(windowSize.y - bottomMargin)
                .setSwipeRotationAngle(0)
                .setSwipeAnimTime(150)
                .setViewGravity(Gravity.TOP)
                .setPaddingTop(15)
                .setRelativeScale(relativeScale));
    }




    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences,
                new IntentFilter(Constants.ADD_TO_SHARED_PREFERENCES));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline,
                new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline,
                new IntentFilter(Constants.CONNECTION_ONLINE));


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLastAd,
                new IntentFilter(Constants.LAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLoadMoreAds,
                new IntentFilter(Constants.LOAD_MORE_ADS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForTimerHasStarted,
                new IntentFilter(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER));


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForOnSwiped,
                new IntentFilter("SWIPED"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForUnhideVeiws,
                new IntentFilter("BLUREDIMAGESDONE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForContinueShareImage,
                new IntentFilter("TRY_SHARE_IMAGE_AGAIN"));

//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForDoneCheckingIfNeedToReCreateClusters,
//                new IntentFilter(Constants.LOADED_USER_DATA_SUCCESSFULLY));

    }

    private void onclicks() {
        findViewById(R.id.logoutBtn).setOnClickListener(this);
        findViewById(R.id.WebsiteIcon).setOnClickListener(this);
        if (findViewById(R.id.bookmark2Btn) != null) {
            findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast.equals(Constants.NOT_LAST) ||
                            Variables.mIsLastOrNotLast.equals(Constants.LAST) && Variables.isNormalAdsBeingSeen) {
                        if (!Variables.hasBeenPinned) {
                            final Snackbar snackBar = Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.pinning,
                                    Snackbar.LENGTH_SHORT);
//                            snackBar.setAction("", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    snackBar.dismiss();
//                                }
//                            });
                            snackBar.show();
                            pinAd();
                        } else {
                            final Snackbar snackBar = Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.hasBeenPinned,
                                    Snackbar.LENGTH_SHORT);
//                            snackBar.setAction("", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    snackBar.dismiss();
//                                }
//                            });
                            snackBar.show();
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't pin that..",
                                Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
        }
        findViewById(R.id.bookmarkBtn).setOnClickListener(this);

        if (findViewById(R.id.profileImageView) != null) {
            findViewById(R.id.profileImageView).setOnClickListener(this);

            findViewById(R.id.profileImageView).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    findViewById(R.id.reportBtn).callOnClick();
                    return false;
                }
            });
        }


        findViewById(R.id.dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });


        findViewById(R.id.shareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator s = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                s.vibrate(50);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.shareText2));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.shareText)));
            }
        });

        if (findViewById(R.id.reportBtn) != null) {
            findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast.equals(Constants.NO_ADS) || !isSeingNormalAds) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't report that..",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        FragmentManager fm = getFragmentManager();
                        ReportDialogFragment reportDialogFragment = new ReportDialogFragment();
                        reportDialogFragment.setMenuVisibility(false);
                        reportDialogFragment.show(fm, "Report dialog fragment.");
                        reportDialogFragment.setfragcontext(mContext);
                        setBooleanForPausingTimer();
                    }

                }
            });
        }

        if (findViewById(R.id.shareImageIcon) != null) {
            findViewById(R.id.shareImageIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Variables.mIsLastOrNotLast.equals(Constants.NO_ADS) || !isSeingNormalAds) {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), "You can't share that..",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Variables.adToBeShared = Variables.getCurrentAdvert();
                        try{
                            isStoragePermissionGranted();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }


    }

    @Override public void onClick(View v) {
        if(v==findViewById(R.id.profileImageView)){
            if(mDoublePressedToPin) {
//                findViewById(R.id.bookmark2Btn).callOnClick();
            }else{
                if(!Variables.isLocked) mSwipeView.doSwipe(true);
            }
            mDoublePressedToPin = true;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mDoublePressedToPin=false;
                }
            }, 1000);
        }

        if(v == retryLoadingFromCannotLoad){
            if(isOnline()){
                cannotLoadLayout.setVisibility(View.GONE);
                loadAdsFromThread();
            }else{
                Toast.makeText(mContext,"Connect to the internet first.",Toast.LENGTH_SHORT).show();
            }
        }

        if (v == mLogoutButton) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("AdCafé");
//            builder.setMessage("Are you sure you want to log out?")
//                    .setCancelable(true)
//                    .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            logoutUser();
//                        }
//                    })
//                    .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    }).show();
            FragmentManager fm = getFragmentManager();
            Log(TAG,"Setting up fragment");
            FeedbackFragment reportDialogFragment = new FeedbackFragment();
            reportDialogFragment.setMenuVisibility(false);
            reportDialogFragment.show(fm, "Feedback.");
            reportDialogFragment.setfragContext(mContext);
            setBooleanForPausingTimer();
        }
        if (v == mRetryButton) {
            Log(TAG, "Retrying to load ads...");
//            mAvi.setVisibility(View.VISIBLE);
            mLoadingProgressBar.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mFailedToLoadLayout.setVisibility(View.GONE);
            Toast.makeText(mContext, "Retrying...", Toast.LENGTH_SHORT).show();
            loadAdsFromThread();
        }
        if (v == findViewById(R.id.WebsiteIcon) && Variables.getCurrentAdvert() != null) {
//            if (!Variables.getCurrentAdvert().getWebsiteLink().equals(igsNein)) {
//                Vibrator b = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                b.vibrate(30);
//                try {
//                    String url = Variables.getCurrentAdvert().getWebsiteLink();
//                    if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
//                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(webIntent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(mContext, "There's something wrong with the link", Toast.LENGTH_SHORT).show();
//                }
//            }
            if(Variables.getCurrentAdvert().didAdvertiserSetContactInfo()) {
                ContactAdvertiserBottomsheet fragmentModalBottomSheet = new ContactAdvertiserBottomsheet();
                fragmentModalBottomSheet.setActivity(MainActivity.this);
                fragmentModalBottomSheet.setAdvert(Variables.getCurrentAdvert());
                fragmentModalBottomSheet.show(getSupportFragmentManager(), "BottomSheet Fragment");
            }
        }
        if(v==findViewById(R.id.bookmarkBtn)){
            Intent intent = new Intent(MainActivity.this, Bookmarks.class);
            startActivity(intent);
        }

    }

    private void setIsUserLoggedOnInSharedPrefs(boolean bol){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("IsSignedIn", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isSignedIn",bol);
        editor.apply();
    }

    private void logoutUser() {
        setLastUsedDateInFirebaseDate(User.getUid());
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        User.setID(0, mKey);
        unregisterAllReceivers();
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        setIsUserLoggedOnInSharedPrefs(false);
        clearUserDataFromSharedPreferences();
        Variables.resetAllValues();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }





    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG,"Finished setting up time.");
            loadAdsFromThread();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForUnhideVeiws = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("COUNTER_BAR_TO_MAIN- ", "Broadcast has been received that blurred images have loaded, unhiding views.");
            int visibleChildren =  numberOfInitiallyLoadedAds <= 4 ? numberOfInitiallyLoadedAds : 4;
            numberOfResponsesForLoadingBlurredImages ++;
            Log(TAG,"Number of visible cards : "+visibleChildren+" Number of responses for loading blurred images :"+ numberOfResponsesForLoadingBlurredImages);
            if(numberOfResponsesForLoadingBlurredImages == visibleChildren && !hasSentMessageThatBlurrsHaveFinished){
                hasSentMessageThatBlurrsHaveFinished = true;
                unhideViews();
                numberOfResponsesForLoadingBlurredImages = 0;
                Intent intent2 = new Intent("START_TIMER_NOW");
                Variables.hasFinishedLoadingBlurredImages = true;
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
            }
        }
    };


    private void hideViews(){
        Log(TAG,"Hiding views...");
        areViewsHidden = true;
//        mAvi.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        mBottomNavButtons.setVisibility(View.GONE);
        mSwipeView.setVisibility(View.INVISIBLE);
        mAdCounterView.setVisibility(View.INVISIBLE);
        findViewById(R.id.easterText).setVisibility(View.GONE);
        mAviLoadingMoreAds.setVisibility(View.GONE);
        hasSentMessageThatBlurrsHaveFinished = false;
    }

    private void unhideViews(){
        if(areViewsHidden){
            areViewsHidden = false;
            Log(TAG,"Unhiding views");
            mAdCounterView.setVisibility(View.VISIBLE);
//            mAvi.setVisibility(View.GONE);
            mLoadingProgressBar.setVisibility(View.GONE);
            mLoadingText.setVisibility(View.GONE);
            mBottomNavButtons.setVisibility(View.VISIBLE);
            findViewById(R.id.easterText).setVisibility(View.VISIBLE);
            mSwipeView.setVisibility(View.VISIBLE);
            if(isLastAd){
                Toast.makeText(mContext, "There's nothing else for you today.", Toast.LENGTH_SHORT).show();
            }

            if(Variables.mIsLastOrNotLast.equals(Constants.NO_ADS)||isLastAd) {
                if (Variables.didAdCafeRemoveCategory) informUserOfSubscriptionChanges();
                if (Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();
            }
        }

        showAnyTextAnnouncements();
    }





    private BroadcastReceiver mMessageReceiverForDoneCheckingIfNeedToReCreateClusters = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received that database manager is done checking if need to readd categories");
            try {
                Log(TAG, "---Starting the getAds method...");
                startGetAds();
            } catch (Exception e) {
                Log.e("BACKGROUND_PROC---", e.getMessage());
                e.printStackTrace();
            }
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("COUNTER_BAR_TO_MAIN- ", "Broadcast has been received to add to shared preferences.");
            updateData();
        }
    };

    private void updateData() {
        Variables.adAdToTotal(mKey);
        Variables.adToMonthTotals(mKey);
        Variables.addOneToTotalReimbursementAmount(mKey);

        Variables.adOneToCurrentAdNumberForAllAdsList();
        addToSharedPreferences();
        adDayAndMonthTotalsToFirebase();
        onclicks();

        new DatabaseManager().updateValueForUnneededPayoutAmount(Variables.getCurrentAdvert());

        getNumberOfTimesAndSetNewNumberOfTimes();
        getAndSetAllAdsThatHaveBeenSeenEver();
        getAndSetTheOtherTotals();

        try{
            Variables.setCurrentAdInSubscription(Integer.parseInt(Variables.getCurrentAdvert().getPushId()));
        }catch (Exception e){
            e.printStackTrace();
            Log(TAG,"Something went wrong setting the current ad in subscription");
            Log(TAG,"Setting using the getPushIdNumber instead. Number is : "+Variables.getCurrentAdvert().getPushIdNumber());
            Variables.setCurrentAdInSubscription(Variables.getCurrentAdvert().getPushIdNumber());
        }
        Variables.setCurrentSubscriptionIndex(getPositionOf(Variables.getCurrentAdvert().getCategory()));
        Log(TAG, "Setting current subscription to : " + getPositionOf(Variables.getCurrentAdvert().getCategory()));
        Log(TAG, "Setting Current ad in subscription to : " + Variables.getCurrentAdvert().getPushId());
        setCurrentAdInSubscriptionAndCurrentSubscriptionIndexInFireBase();

        if(Variables.didAdCafeRemoveCategory)informUserOfSubscriptionChanges();
        if(Variables.didAdCafeAddNewCategory) tellUserOfNewSubscription();

        if(mSwipeView.getChildCount() == 1 && !isLoadingMoreAds)
            Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();

        setPreviousAdsImage();
    }

    private void setPreviousAdsImage() {
        int currentAdsPos = Variables.getPositionOfAdInVariablesAdsList(Variables.getCurrentAdvert());
        try{
            if(currentAdsPos>0){
                int pos = currentAdsPos-1;
                Log(TAG,"Removing image in ad: "+Variables.getAdFromVariablesAdList(pos).getPushRefInAdminConsole());
                Variables.getAdFromVariablesAdList(pos).setImageUrl("cleared");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }






    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("CONNECTION_C-MAIN_A", "Connection has been dropped");
            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.connectionDropped2,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("CONNECTION_C-MAIN_A", "Connection has come online");
        }
    };

    private BroadcastReceiver mMessageReceiverForLastAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mIsBeingReset && !isLoadingMoreAds) {
                loadAnyAnnouncements();
            }
        }
    };





    private BroadcastReceiver mMessageReceiverForTimerHasStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Variables.getCurrentAdvert().didAdvertiserSetContactInfo()) {
                        mSwipeView.findViewById(R.id.WebsiteIcon).setAlpha(1.0f);
                        mSwipeView.findViewById(R.id.websiteText).setAlpha(1.0f);
                        mSwipeView.findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.WebsiteIcon).setAlpha(0.4f);
                        findViewById(R.id.websiteText).setAlpha(0.4f);
                        findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                        Variables.hasBeenPinned = false;
                    }
                    onclicks();
                }
            }, 300);

        }
    };

    private BroadcastReceiver mMessageReceiverForOnSwiped = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isSeingNormalAds = false;
                    onclicks();
                }
            }, 300);

        }
    };

    private BroadcastReceiver mMessageReceiverForLoadMoreAds = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mIsBeingReset && !isLoadingMoreAds && Variables.nextSubscriptionIndex + 1 < Variables.Subscriptions.size()) {
                startLoadingMoreAdsFromSnapshot();
            }
        }
    };

    private BroadcastReceiver mMessageReceiverForContinueShareImage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startShareImage2();
        }
    };




    private void loadMoreAds() {
        isLoadingMoreAds = true;
        if(!areViewsHidden){
            if(!isLoaderShowing){
                isLoaderShowing = true;
                mAviLoadingMoreAds.smoothToShow();
            }
        }
//        spinner.setVisibility(View.VISIBLE);
        Log("MAIN-ACTIVITY---", "Loading more ads since user has seen almost all....");
        String date;
        date = isAlmostMidNight() ? getNextDay() : getDate();

        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                .child(Integer.toString(Variables.constantAmountPerView))
                .child(getSubscriptionValue(Variables.nextSubscriptionIndex))
                .child(Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));


        Log(TAG, "---Query set up is : " + Constants.ADVERTS + " : " + date + " : "+Variables.constantAmountPerView+ " : "
                + getSubscriptionValue(Variables.nextSubscriptionIndex)
                + " : "
                + Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));


        dbRef = query.getRef();
        Log(TAG,"Dbref starts at "+(mChildToStartFrom + 1));
        dbRef.orderByKey().startAt(Integer.toString(mChildToStartFrom + 1))
                .limitToFirst(Constants.NO_OF_ADS_TO_LOAD2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean canLoad = true;
                if(Variables.constantAmountPerView>3
                        && Variables.getAdTotal(mKey)+Constants.NO_OF_ADS_TO_LOAD2>Constants.MAX_NUMBER_FOR7){
                    canLoad = false;
                }
                if (dataSnapshot.hasChildren()) {
                    Log(TAG, "---More children in dataSnapshot from firebase exist");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Advert ad = snap.getValue(Advert.class);
                        DataSnapshot snpsht = snap.child("pushId");
                        String pushID = snpsht.getValue(String.class);
                        ad.setPushId(pushID);
                        ad.setPushIdNumber(Integer.parseInt(pushID));
                        Log(TAG,"setting push id to : "+ ad.getPushId());
                        if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)){
                            if(Variables.constantAmountPerView>3 && Variables.getAdTotal(mKey)+1>Constants.MAX_NUMBER_FOR7) {
                                Log(TAG,"User cannot see more than "+Constants.MAX_NUMBER_FOR7+" ads.");
                            }else{
                                mAdList.add(ad);
                                Log(TAG,"Loaded ad : "+ad.getPushRefInAdminConsole());
                            }
                        }
                    }
                    Log(TAG, "---All the new ads have been handled.Total is " + mAdList.size());
                    if(mAdList.size()!=0){
                        loadMoreAdsIntoAdvertCard();
                        mChildToStartFrom += (int) dataSnapshot.getChildrenCount();
                        isLoadingMoreAds = false;

                    }else{
                        Log(TAG,"Loaded no ad, loading more ads...");
                        if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                            mChildToStartFrom=0;
                            Variables.nextSubscriptionIndex+=1;
                            loadMoreAds();
                        }else{
                            Log(TAG,"No more ads are available from the rest of the subscriptions");
                            isLoadingMoreAds = false;
                            if(isLoaderShowing) {
                                isLoaderShowing = false;
                                mAviLoadingMoreAds.smoothToHide();
                            }
                            if(mSwipeView.getChildCount()==1){
                                if(!hasShowedToastForNoMoreAds){
                                    hasShowedToastForNoMoreAds = true;
                                    Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                                }
                                loadAnyAnnouncements();
                            }
//                            spinner.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    //no ads were found in the subscription
                    Log(TAG, "----No ads are available in subscription: "+getSubscriptionValue(Variables.nextSubscriptionIndex));
                    if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                        mChildToStartFrom=0;
                        Variables.nextSubscriptionIndex+=1;
                        loadMoreAds();
                    }else{
                        Log(TAG,"No more ads are available from the rest of the subscriptions");
                        isLoadingMoreAds = false;
                        if(isLoaderShowing) {
                            isLoaderShowing = false;
                            mAviLoadingMoreAds.smoothToHide();
                        }
                        if(mSwipeView.getChildCount()==1){
                            if(!hasShowedToastForNoMoreAds){
                                hasShowedToastForNoMoreAds = true;
                                Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                            }
                            loadAnyAnnouncements();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG, "Unable to load more ads for some issue." + databaseError.getMessage());
            }
        });
    }



    private void startLoadingMoreAdsFromSnapshot(){
        if(mAdsSnapshot!=null){
            loadMoreAdsFromSnapshot();
        }else {
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS);
            adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mAdsSnapshot = dataSnapshot;
                    loadMoreAdsFromSnapshot();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void loadMoreAdsFromSnapshot(){
        isLoadingMoreAds = true;
        if(!areViewsHidden){
            if(!isLoaderShowing){
                isLoaderShowing = true;
                mAviLoadingMoreAds.smoothToShow();
            }
        }

        Log(TAG, "Loading more ads since user has seen almost all....");
        String date = isAlmostMidNight() ? getNextDay() : getDate();

        DataSnapshot mySnap = mAdsSnapshot.child(date)
                .child(Integer.toString(Variables.constantAmountPerView))
                .child(getSubscriptionValue(Variables.nextSubscriptionIndex))
                .child(Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));

        Log(TAG, "---Query set up is : " + date + " : "+Variables.constantAmountPerView+ " : "
                + getSubscriptionValue(Variables.nextSubscriptionIndex)
                + " : "
                + Integer.toString(getClusterValue(Variables.nextSubscriptionIndex)));

        Log(TAG,"Dbref starts at "+(mChildToStartFrom + 1));
        List<DataSnapshot> dbSnaps = new ArrayList<>();
        int start = mChildToStartFrom + 1;
        int stop = start+Constants.NO_OF_ADS_TO_LOAD;
        for(int i = start; i<stop; i++){
            String key = Integer.toString(i);
            Log(TAG,"Attempting to load ad "+key+" in category "
                    +getSubscriptionValue(Variables.nextSubscriptionIndex));
            DataSnapshot adSnap = mySnap.child(key);
            if(adSnap.exists()){
                dbSnaps.add(adSnap);
            }
        }

        handleLoadMoreAdsResults(dbSnaps);
    }

    private void handleLoadMoreAdsResults(List<DataSnapshot> dbSnaps) {
        boolean canLoad = true;
        if(Variables.constantAmountPerView>3
                && Variables.getAdTotal(mKey)+Constants.NO_OF_ADS_TO_LOAD2>Constants.MAX_NUMBER_FOR7){
            canLoad = false;
        }
        if (!dbSnaps.isEmpty()) {
            Log(TAG, "---More children in dataSnapshot from firebase exist");
            for (DataSnapshot snap : dbSnaps) {
                Advert ad = snap.getValue(Advert.class);
                if(snap.child("contactdata").exists()){
                    ad.setAdvertiserPhoneNo(snap.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                            .getValue(String.class));
                    if(snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).exists()){
                        List<AdvertiserLocation> advertisersLoc = new ArrayList<>();
                        for(DataSnapshot locSnap:snap.child("contactdata").child(Constants.ADVERTISER_LOCATION).getChildren()){
                            String name = "";
                            if(locSnap.child("name").exists())name = locSnap.child("name").getValue(String.class);
                            double lat = locSnap.child("lat").getValue(double.class);
                            double lng = locSnap.child("lng").getValue(double.class);
                            advertisersLoc.add(new AdvertiserLocation(new myLatLng(lat,lng),name));
                        }
                        ad.setAdvertiserLocations(advertisersLoc);
                    }
                }
                DataSnapshot snpsht = snap.child("pushId");
                String pushID = snpsht.getValue(String.class);
                ad.setPushId(pushID);
                ad.setPushIdNumber(Integer.parseInt(pushID));
                Log(TAG,"setting push id to : "+ ad.getPushId());
                if(!ad.isFlagged() && doesUserMeetCriteria(snap.child("targetdata"),ad)){
                    if(Variables.constantAmountPerView>3 && Variables.getAdTotal(mKey)+1>Constants.MAX_NUMBER_FOR7) {
                        Log(TAG,"User cannot see more than "+Constants.MAX_NUMBER_FOR7+" ads.");
                    }else{
                        mAdList.add(ad);
                        Log(TAG,"Loaded ad : "+ad.getPushRefInAdminConsole());
                    }
                }
            }
            Log(TAG, "---All the new ads have been handled.Total is " + mAdList.size());
            if(mAdList.size()!=0){
                loadMoreAdsIntoAdvertCard();
                mChildToStartFrom += dbSnaps.size();
                isLoadingMoreAds = false;

            }else{
                Log(TAG,"Loaded no ad, loading more ads...");
                if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                    mChildToStartFrom=0;
                    Variables.nextSubscriptionIndex+=1;
                    loadMoreAdsFromSnapshot();
                }else{
                    Log(TAG,"No more ads are available from the rest of the subscriptions");
                    isLoadingMoreAds = false;
                    if(isLoaderShowing) {
                        isLoaderShowing = false;
                        mAviLoadingMoreAds.smoothToHide();
                    }
                    if(mSwipeView.getChildCount()==1){
                        if(!hasShowedToastForNoMoreAds){
                            hasShowedToastForNoMoreAds = true;
                            Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                        }
                        loadAnyAnnouncements();
                    }
//                            spinner.setVisibility(View.VISIBLE);
                }
            }
        } else {
            //no ads were found in the subscription
            Log(TAG, "----No ads are available in subscription: "+getSubscriptionValue(Variables.nextSubscriptionIndex));
            if(Variables.nextSubscriptionIndex+1<Variables.Subscriptions.size()){
                mChildToStartFrom=0;
                Variables.nextSubscriptionIndex+=1;
                loadMoreAdsFromSnapshot();
            }else{
                Log(TAG,"No more ads are available from the rest of the subscriptions");
                isLoadingMoreAds = false;
                if(isLoaderShowing) {
                    isLoaderShowing = false;
                    mAviLoadingMoreAds.smoothToHide();
                }
                if(mSwipeView.getChildCount()==1){
                    if(!hasShowedToastForNoMoreAds){
                        hasShowedToastForNoMoreAds = true;
                        Toast.makeText(mContext, R.string.lastAd, Toast.LENGTH_SHORT).show();
                    }
                    loadAnyAnnouncements();
                }
            }
        }
    }



    private void loadMoreAdsIntoAdvertCard(){
        String date = isAlmostMidNight() ? getNextDay() : getDate();
        for(final Advert ad: mAdList){
            String pushRefInAdminConsole = ad.getPushRefInAdminConsole();
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(date)
                    .child(pushRefInAdminConsole).child("imageUrl");
            adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    mAdList.get(mAdList.indexOf(ad)).setImageUrl(imageUrl);
                    iterations++;
                    if(iterations == mAdList.size()){
                        iterations = 0;
                        loadMoreAdsIntoAdvertCard2();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log(TAG,"An error occured, "+databaseError.getDetails());
                }
            });
        }
    }

    private void loadMoreAdsIntoAdvertCard2() {
        for (Advert ad : mAdList) {
            ad.setNatureOfBanner(Constants.IS_AD);
            Variables.adToVariablesAdList(ad);
            mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.LOAD_MORE_ADS));
            Variables.setIsLastOrNotLast(Constants.NOT_LAST);
        }
        if(Variables.isLockedBecauseOfNoMoreAds){
//            mSwipeView.unlockViews();
            unLockViews();
            Variables.isLockedBecauseOfNoMoreAds = false;
        }
        if(isLoaderShowing){
            isLoaderShowing = false;
            mAviLoadingMoreAds.smoothToHide();
        }
        mAdList.clear();
    }





    private void loadAnyAnnouncements() {
        if(!hasLoadedAnnouncements){
            hasLoadedAnnouncements = true;
            if(!areViewsHidden)mAviLoadingMoreAds.smoothToShow();
            Log("MAIN-ACTIVITY---", "Now loading announcements since there are no more ads....");
            String date = isAlmostMidNight() ? getNextDay() : getDate();
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(date);

            Log(TAG, "---Query set up is : " + Constants.ANNOUNCEMENTS + " : " + date);
            dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Advert ad = snap.getValue(Advert.class);
                            DataSnapshot imgSnap = snap.child("imageUrl");
                            String img = imgSnap.getValue(String.class);
                            ad.setImageUrl(img);
                            mAdList.add(ad);
                        }
                        for (Advert ad : mAdList) {
                            ad.setWebsiteLink(igsNein);
                            ad.setNatureOfBanner(Constants.IS_ANNOUNCEMENT);
                            Variables.adToVariablesAdList(ad);
                            mSwipeView.addView(new AdvertCard(mContext, ad, mSwipeView, Constants.ANNOUNCEMENTS));
                        }
                        if(Variables.isLockedBecauseOfNoMoreAds){
//                            mSwipeView.unlockViews();
                            unLockViews();
                            Log(TAG,"Unlocking views since isLockedBecauseOfNoMoreAds is : "+Variables.isLockedBecauseOfNoMoreAds);
                            Variables.isLockedBecauseOfNoMoreAds = false;
                        }
                        mAviLoadingMoreAds.smoothToHide();
                        mAdList.clear();
                    } else {
                        mAviLoadingMoreAds.smoothToHide();
                        showAnyTextAnnouncements();
                        Log(TAG, "There are no announcements today...");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log(TAG, "Unable to load announcements...");
                    hasLoadedAnnouncements = false;
                }
            });
        }else{
            showAnyTextAnnouncements();
        }
    }

    private void showAnyTextAnnouncements() {
        Log(TAG,"Loading text announcements...");
        boolean hasSeenTodaysAnnouncements  = getSharedPreferences(Constants.TEXT_ANOUNCEMENTS, MODE_PRIVATE)
                .getBoolean(Constants.TEXT_ANOUNCEMENTS, false);

        if(!Variables.announcements.equals("") && !hasSeenTodaysAnnouncements){
            final Dialog d = new Dialog(this);
            d.setTitle("Announcement.");
            d.setContentView(R.layout.dialog99);
            Button b1 = d.findViewById(R.id.okBtn);
            TextView t = d.findViewById(R.id.annText);
            t.setText(Variables.announcements);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    SharedPreferences pref7 = getSharedPreferences(Constants.TEXT_ANOUNCEMENTS, MODE_PRIVATE);
                    SharedPreferences.Editor editor7 = pref7.edit();
                    editor7.clear();
                    editor7.putBoolean(Constants.TEXT_ANOUNCEMENTS, true);
                    Log("DatabaseManager---", "Setting the current announcement boolean to " + true);
                    editor7.apply();
                    resumeTimerByStartingIt();
                }
            });
            d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    SharedPreferences pref7 = getSharedPreferences(Constants.TEXT_ANOUNCEMENTS, MODE_PRIVATE);
                    SharedPreferences.Editor editor7 = pref7.edit();
                    editor7.clear();
                    editor7.putBoolean(Constants.TEXT_ANOUNCEMENTS, true);
                    Log("DatabaseManager---", "Setting the current announcement boolean to " + true);
                    editor7.apply();
                    resumeTimerByStartingIt();
                }
            });
            pauseTimerByStoppingItEntirely();
            d.show();
        }
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }



    public float density() {
        double constant = 0.000046875;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        float relativeScale;

        if (density >= 560) {
            Log("DENSITY---", "HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.005f;
        } else if (density >= 460) {
            Log("DENSITY---", "MEDIUM-HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.009f;
        } else if (density >= 360) {
            Log("DENSITY---", "MEDIUM-LOW... Density is " + String.valueOf(density));
            relativeScale = 0.013f;
        } else if (density >= 260) {
            Log("DENSITY---", "LOW... Density is " + String.valueOf(density));
            relativeScale = 0.015f;
        } else {
            relativeScale = 0.02f;
        }
        return relativeScale;
    }

    private void addToSharedPreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("adTotals", Variables.getAdTotal(mKey));
        Log("MAIN_ACTIVITY--", "Adding 1 to shared preferences adTotal is - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor.putInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, Variables.getMonthAdTotals(mKey));
        Log("MAIN_ACTIVITY--", "Adding 1 to shared preferences Month ad totals is - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.commit();
    }

    private boolean isAlmostMidNight() {
        return TimeManager.isAlmostMidNight();
    }




    private void adDayAndMonthTotalsToFirebase() {
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(Variables.getAdTotal(mKey));

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(Variables.getMonthAdTotals(mKey));

        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_TOTALS);
        adRef3.setValue(Variables.getTotalReimbursementAmount());

    }

    private void setCurrentAdInSubscriptionAndCurrentSubscriptionIndexInFireBase() {
        String uid = User.getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        Log(TAG,"Setting current subscription index in firebase to :"+Variables.getCurrentSubscriptionIndex());
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        Log(TAG,"Setting current ad in subscription index in firebase to : "+Variables.getCurrentAdInSubscription());
        adRef4.setValue(Variables.getCurrentAdInSubscription());
    }

    private String getDate() {
        return TimeManager.getDate();
    }

    private void resetAdTotalSharedPreferencesAndDayAdTotals() {
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        Variables.setAdTotal(0, mKey);
        Variables.setCurrentAdInSubscription(0);
        Variables.setCurrentSubscriptionIndex(0);
        resetAdTotalsInFirebase();

        new DatabaseManager().clearAdsSeenSoFarInFirebase();
    }




    private void setLastUsedDateInFirebaseDate(String uid) {
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.DATE_IN_FIREBASE);
            adRef.setValue(getDate());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void resetAdTotalsInFirebase() {
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (isOffline) {
                    isFirebaseResetNecessary = true;
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isFirebaseResetNecessary = false;
            }
        });

        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);
    }

    private void resetEverything() {
        hideViews();
        resetAdTotalSharedPreferencesAndDayAdTotals();
        Variables.clearAllAdsFromAdList();
        lastAdSeen = null;
        loadAdsFromThread();
    }




    private String getNextDay() {
        return TimeManager.getNextDay();
    }

    private void setCurrentDateToSharedPrefs() {
        Log(TAG, "---Setting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if(isAlmostMidNight()) editor.putString("date",getNextDay());
        else editor.putString("date", getDate());
        editor.apply();
    }

    private String getCurrentDateInSharedPreferences() {
        Log(TAG, "---Getting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences(Constants.DATE, MODE_PRIVATE);
        String date = prefs.getString("date", "0");
        return date;
    }





    @Override public void networkAvailable() {
        Log(TAG, "User is connected to the internet via wifi or cellular data");
        isOffline = false;
        if(doesWindowHaveFocus) resumeTimerByStartingIt();
        if(stage.equals("VIEWING_ADS") && isHiddenBecauseNetworkDropped){
            //Sets these views if activity has already loaded the ads.
            isHiddenBecauseNetworkDropped = false;
            findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
            mBottomNavButtons.setVisibility(View.VISIBLE);
            mSwipeView.setVisibility(View.VISIBLE);
            mAdCounterView.setVisibility(View.VISIBLE);
            findViewById(R.id.easterText).setVisibility(View.VISIBLE);
            if (isFirebaseResetNecessary) {
                resetAdTotalsInFirebase();
            }
        }
//        else{
            //Sets these views if activity has already loaded the ads.
//            findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
//            mBottomNavButtons.setVisibility(View.VISIBLE);
//            mSwipeView.setVisibility(View.VISIBLE);
//            mAdCounterView.setVisibility(View.VISIBLE);
//            if (isFirebaseResetNecessary) {
//                resetAdTotalsInFirebase();
//            }
//        }
//
//        findViewById(R.id.droppedInternetLayout).setVisibility(View.GONE);
//        mBottomNavButtons.setVisibility(View.VISIBLE);
//        mSwipeView.setVisibility(View.VISIBLE);
//        mAdCounterView.setVisibility(View.VISIBLE);
//        if (isFirebaseResetNecessary) {
//            resetAdTotalsInFirebase();
//        }
    }

    @Override public void networkUnavailable() {
        Log(TAG, "User has gone offline...");
        isOffline = true;
        pauseTimerByStoppingItEntirely();
        if(stage.equals("VIEWING_ADS")){
            isHiddenBecauseNetworkDropped = true;
            mBottomNavButtons.setVisibility(View.GONE);
            mSwipeView.setVisibility(View.GONE);
            mAdCounterView.setVisibility(View.GONE);
            findViewById(R.id.droppedInternetLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.easterText).setVisibility(View.GONE);
        }

    }

    @Override public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }





    private void pinAd() {
        Log(TAG, "Pinning ad from main activity");
        Advert ad = Variables.getCurrentAdvert();
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(getDateInDays());

        DatabaseReference pushRef = adRef.push();
        String pushId = pushRef.getKey();

        Log(TAG, "pinning the selected ad.");
        ad.setImageBitmap(null);
        ad.setPushId(pushId);

        long currentTimeMillis = System.currentTimeMillis();
//        long currentDay = -(currentTimeMillis+1000*60*60*3)/(1000*60*60*24);
        long currentDay = Long.parseLong(getDateInDays());
        ad.setDateInDays(currentDay);

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(getDateInDays()).child(ad.getPushRefInAdminConsole()).child("imageUrl");
        adRef2.setValue(ad.getImageUrl());
        setAdsNumberOfPins();


        ad.setImageUrl(null);
        pushRef.setValue(ad).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log(TAG, "Pinning is complete.");
                Variables.hasBeenPinned = true;
            }
        });
    }

    private void setAdsNumberOfPins() {
        Advert ad = Variables.getCurrentAdvert();
        final DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(getDateInDays()).child(ad.getPushRefInAdminConsole()).child(Constants.NO_OF_TIMES_PINNED);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int numberOfPins = dataSnapshot.getValue(int.class);
                    adRef.setValue(numberOfPins+1);
                }else{
                    adRef.setValue(1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void logUser() {
        Crashlytics.setUserIdentifier(User.getUid());
        Crashlytics.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Crashlytics.setUserName("Test User");
    }

    private void getNumberOfTimesAndSetNewNumberOfTimes() {
        Log(TAG, "Getting the current ad's numberOfTimesSeen from firebase");
        final String datte;
        //ad gotten will be current advert
        final Advert ad = Variables.getCurrentAdvert();
        datte = isAlmostMidNight() ? getNextDay() : getDate();

        Log(TAG, "Push ref for current Advert is : " + ad.getPushRefInAdminConsole());
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(datte)
                .child(ad.getPushRefInAdminConsole())
                .child("numberOfTimesSeen");
        Log(TAG, "Query set up is :" + Constants.ADS_FOR_CONSOLE + " : " + datte + " : " + ad.getPushRefInAdminConsole() + " : numberOfTimesSeen");
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    int number = dataSnapshot.getValue(int.class);
                    int newNumber = number + 1;
                    setNewNumberOfTimesSeen(newNumber, datte, ad);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG, "Unable to get number of times seen");
            }
        });
    }

    private void setNewNumberOfTimesSeen(int number, String date, Advert advert) {
        Log(TAG, "Setting the new number of times seen in firebase.");

        final Advert ad = Variables.getCurrentAdvert();
        String dateInDays = isAlmostMidNight()? Long.toString(-(TimeManager.getDateInDays()+1)) : Long.toString(-TimeManager.getDateInDays());

        Query query2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(ad.getAdvertiserUid()).child(Constants.UPLOAD_HISTORY)
                .child(dateInDays)
                .child(ad.getPushRefInAdminConsole()).child("numberOfTimesSeen");
        DatabaseReference dbref2 = query2.getRef();
        dbref2.setValue(number);

        String day = isAlmostMidNight() ? TimeManager.getNextDayDay() : TimeManager.getDay();
        String month = isAlmostMidNight() ? TimeManager.getNextDayMonth() : TimeManager.getMonth();
        String year = isAlmostMidNight() ? TimeManager.getNextDayYear() : TimeManager.getYear();

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(year).child(month).child(day).child(ad.getPushRefInAdminConsole()).child("numberOfTimesSeen");
        adminRef.setValue(number);



        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(date).child(Variables.getCurrentAdvert().getPushRefInAdminConsole()).child("numberOfTimesSeen");

        Log(TAG, "Query set up is :" + Constants.ADS_FOR_CONSOLE + " : " + date + " : " + advert.getPushRefInAdminConsole() + " : numberOfTimesSeen");
        DatabaseReference dbRef = query.getRef();
        dbRef.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log(TAG, "The new number has been set.");
            }
        });
    }





    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                startShareImage();
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            startShareImage();
        }
    }

    private void startShareImage(){
//        shareImage(Variables.currentAdvertImageBitmap);
////        if(Variables.currentAdvertImageBitmap!=null) {
////            shareImage(Variables.currentAdvertImageBitmap);
////        } else
//            if(!Variables.getCurrentAdvert().getImageUrl().equals("")){
//            try{
//                Bitmap image = decodeFromFirebaseBase64(Variables.getCurrentAdvert().getImageUrl());
//                shareImage(image);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }else{
            Intent intent  = new Intent("SET_IMAGE_FOR_SHARING"+Variables.getCurrentAdvert().getPushRefInAdminConsole());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        }
    }

    private void startShareImage2(){
        try{
            Bitmap imageBm = Variables.imageToBeShared;
            shareImage(imageBm);
        }catch (Exception e){
            e.printStackTrace();
        }
    }





    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            startShareImage();
        }
    }

    private void shareImage(Bitmap icon) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(30);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
//        return getResizedBitmap(bitm,700);
        return bitm;
    }







    private void getAndSetAllAdsThatHaveBeenSeenEver() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.TOTAL_ALL_TIME_ADS);
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long number;
                if (dataSnapshot.exists()) number = dataSnapshot.getValue(long.class);
                else number = 0;
                Log(TAG, "number gotten for global ad totals is : " + number);
                long newNumber = number + Variables.constantAmountPerView;
                setNewAllAdsThatHaveBeenSeenEver(newNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG, "Unable to update totals");
            }
        });
    }

    private void setNewAllAdsThatHaveBeenSeenEver(long number) {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.TOTAL_ALL_TIME_ADS);
        DatabaseReference dbRef = query.getRef();
        dbRef.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log(TAG, "New value has been set");
            }
        });
    }

    private void setAlarmForNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(mContext, AlarmReceiver1.class); // AlarmReceiver1 = broadcast receiver

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        try{
            alarmManager.cancel(pendingIntent);
        }catch(Exception e){
            e.printStackTrace();
        }

        Calendar alarmStartTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, Variables.preferredHourOfNotf);
        alarmStartTime.set(Calendar.MINUTE, Variables.preferredMinuteOfNotf);
        alarmStartTime.set(Calendar.SECOND, 0);
        if (now.after(alarmStartTime)) {
            Log(TAG, "Setting alarm to tomorrow morning.");
            alarmStartTime.add(Calendar.DATE, 1);
        }
        try {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            String message = String.format("Alarms set for everyday %s:%s hrs.",Variables.preferredHourOfNotf,Variables.preferredMinuteOfNotf);
            Log.w("Alarm", message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private void getAndSetTheOtherTotals(){
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADCAFE_TOTALS);
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long number;
                if (dataSnapshot.exists()) number = dataSnapshot.getValue(long.class);
                else number = 0;
                Log(TAG, "number gotten for takeout ad totals is : " + number);
                long newNumber = number + Constants.TOTAL_AMOUNT_PER_VIEW_FOR_ADMIN;
                setNewAdminTotals(newNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG, "Unable to update totals");
            }
        });
    }

    private void setNewAdminTotals(long number){
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADCAFE_TOTALS);
        DatabaseReference dbRef = query.getRef();
        dbRef.setValue(number).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log(TAG, "New value has been set");
            }
        });
    }




    private void RateAppIntent() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName()));
            startActivity(rateIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getClusterValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log(TAG, "Cluster gotten from current subscription is : " + cluster);
        return cluster;
    }

    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log(TAG, "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }




    private int getPositionOf(String subscription) {
        LinkedHashMap map = Variables.Subscriptions;
        List<String> indexes = new ArrayList<String>(map.keySet());
        return indexes.indexOf(subscription);
    }

    private String getDateInDays(){
        return Long.toString(-TimeManager.getDateInDays());
    }

    private void informUserOfSubscriptionChanges(){
        String message;
        if(Variables.NSSubs.size()<4) {
            message = "We removed one or more of your interests that we no longer support: ";
            for (String category : Variables.NSSubs) {
                if (Variables.NSSubs.indexOf(category) == Variables.NSSubs.size() - 1) {
                    if(Variables.newSubs.size()==1){
                        message = String.format("%s%s", message, String.format("%s.", category));
                    }else{
                        message = String.format("%s, %s", message, String.format("%s.", category));
                    }
                } else if (Variables.NSSubs.indexOf(category) == 0) {
                    message = message + category;
                } else {
                    message = String.format("%s%s", message, String.format(", %s", category));
                }
            }
        }else{
            message = "We removed one or more of your interests that we no longer support.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Variables.didAdCafeRemoveCategory = false;
                        Variables.NSSubs.clear();
                    }
                })
                .setPositiveButton("Ok.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }




    private void tellUserOfNewSubscription(){
        String message = "We now support a couple more ad categories you may be interested in.";
        if(Variables.newSubs.size()<3) {
            message = "We now support a couple more ad categories you may be interested in: ";
            for (String category : Variables.newSubs) {
                if (Variables.newSubs.indexOf(category) == Variables.newSubs.size() - 1) {
                    if(Variables.newSubs.size()==1){
                        message = String.format("%s%s", message, String.format("%s.", category));
                    }else{
                        message = String.format("%s, %s", message, String.format("%s.", category));
                    }
                } else if (Variables.newSubs.indexOf(category) == 0) {
                    message = message + category;
                } else {
                    message = String.format("%s%s", message, String.format(", %s", category));
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Variables.didAdCafeAddNewCategory = false;
                        Variables.newSubs.clear();
                    }
                })
                .setPositiveButton("Cool.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private String getDateFromDays(long days){
        long currentTimeInMills = -days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log(TAG,"Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }

    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }




    public boolean isOnline() {
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void lockViews(){
        if(!Variables.isLocked){
//        mSwipeView.lockViews();
            mSwipeView.getBuilder()
                    .setWidthSwipeDistFactor(1f)
                    .setHeightSwipeDistFactor(1f);
            Variables.isLocked = true;
            Log.e(TAG,"Locking views");
        }
    }

    private void unLockViews(){
        if(Variables.isLocked && !Variables.hasTimerStarted){
            //        mSwipeView.unlockViews();
            mSwipeView.getBuilder()
                    .setWidthSwipeDistFactor(10f)
                    .setHeightSwipeDistFactor(10f);
            Variables.isLocked = false;
            Log.e(TAG,"Unlocking views");
        }

    }



    private void setBooleanForPausingTimer(){
        Log(TAG,"Setting boolean for pausing timer.");
        if(Variables.isAllClearToContinueCountDown && Variables.timerLevel>1){
            Variables.isAllClearToContinueCountDown = false;
        }

    }

    private void setBooleanForResumingTimer(){
        Log(TAG,"Setting boolean for resuming timer.");
        if(!Variables.isAllClearToContinueCountDown)Variables.isAllClearToContinueCountDown = true;
    }

    private void pauseTimerByStoppingItEntirely(){
        if(Variables.timerLevel>1){
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.PAUSE_TIMER));
        }
    }




    private void resumeTimerByStartingIt(){
        setBooleanForResumingTimer();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.RESUME_TIMER));
    }

    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals(Constants.ADMIN_ACC)) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private boolean canStartFromCustomClass(){
        SharedPreferences prefs4 = getSharedPreferences(Constants.CUSTOM_STARTING_POINT_ENABLED, MODE_PRIVATE);
        return prefs4.getBoolean(Constants.CUSTOM_STARTING_POINT_ENABLED,false);
    }

    private int getTheCustomCategoryToStartFrom(){
        SharedPreferences prefs4 = getSharedPreferences(Constants.CUSTOM_STARTING_POINT_VALUE, MODE_PRIVATE);
        return getPositionOf(prefs4.getString(Constants.CUSTOM_STARTING_POINT_VALUE, getSubscriptionValue(0)));
    }

    private void removeTheCustomCategoryEnabling(){
        SharedPreferences prefs = getSharedPreferences(Constants.CUSTOM_STARTING_POINT_ENABLED, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.CUSTOM_STARTING_POINT_ENABLED, false);
        editor.apply();
    }




//    Font: AR ESSENCE.
    private void nothn(){}
//    No of lns : 30,070 as of 6/05/2018.

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus) {
            doesWindowHaveFocus = false;
            pauseTimerByStoppingItEntirely();
        }else{
            doesWindowHaveFocus = true;
            resumeTimerByStartingIt();
        }
    }

    private boolean doesUserMeetCriteria(DataSnapshot targetDataSnap, Advert ad){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        if(mTargetUsersDataList.child(getDate()).child(ad.getPushRefInAdminConsole()).exists()){
            List<String> targetUids;
            String targetUserListString = mTargetUsersDataList.child(getDate()).child(ad.getPushRefInAdminConsole()).getValue(String.class);

            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<ArrayList<String>>(){}.getType();
            targetUids = gson.fromJson(targetUserListString,type);
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


    private void setIsAppOnline(boolean bol){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.ONLINE_NESS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putBoolean(Constants.ONLINE_NESS, bol);
        Log(TAG, "Setting onlineness - " + bol);
        editor.apply();
    }

    private void setLastUserOfAppInSharedPreferences(String uid){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.USER_ID, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putString(Constants.USER_ID, uid);
        Log(TAG, "Setting uid in shared prefs - " + uid);
        editor.apply();
    }


    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.getKey().equals(Constants.BOI_IS_DA_KEY)){
                String firebasekey = dataSnapshot.getValue(String.class);
                if(!firebasekey.equals(getSessionKey())){
                    PerformShutdown();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public void addListenerForChangeInSessionKey(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference FirstCheckref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.BOI_IS_DA_KEY);
        FirstCheckref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String firebasekey = dataSnapshot.getValue(String.class);
                    if (!firebasekey.equals(getSessionKey())) {
                        PerformShutdown();
                    }else{
                        nowReallyAddLisenerForChangeInSessionKey();
                    }
                }else{
                    PerformShutdown();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void nowReallyAddLisenerForChangeInSessionKey(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SKListener = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid);
        SKListener.addChildEventListener(chil);
    }

    public void removeListenerForChangeInSessionKey(){
        if(SKListener!=null){
            SKListener.removeEventListener(chil);
        }
    }

    public String getSessionKey(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        String sk = prefs2.getString(Constants.BOI_IS_DA_KEY, "NULL");
        Log(TAG, "Loading session key from shared prefs - " + sk);
        return sk;
    }


    public void PerformShutdown(){
        setLastUsedDateInFirebaseDate(User.getUid());
        if (dbRef != null) {
            dbRef.removeEventListener(val);
        }
        User.setID(0, mKey);
        unregisterAllReceivers();
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        setIsUserLoggedOnInSharedPrefs(false);
        clearUserDataFromSharedPreferences();
        Variables.resetAllValues();
        if(!isWindowPaused){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            finish();
        }

    }

    public String getUserDeviceCagegory(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;

        if(density>Constants.HIGH_DENSITY_THRESHOLD){
            return Constants.HIGH_END_DEVICE;
        }else if(density>Constants.MID_DENSITY_THRESHOLD){
            return Constants.MID_RANGE_DEVICE;
        }else return Constants.LOW_END_DEVICE;
    }

    public void setUserDeviceCategoryInFirebaseAndSharedPrefs(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference fbd = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DEVICE_CATEGORY);
        fbd.setValue(getUserDeviceCagegory());

        SharedPreferences pref = getSharedPreferences(Constants.DEVICE_CATEGORY, MODE_PRIVATE);
        pref.edit().clear().putString(Constants.DEVICE_CATEGORY, getUserDeviceCagegory()).apply();
        Log(TAG,"Set user device category in shared pref and firebase");

    }

}