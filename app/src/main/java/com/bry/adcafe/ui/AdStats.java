package com.bry.adcafe.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.DateForAdStats;
import com.bry.adcafe.adapters.MyAdStatsItem;
import com.bry.adcafe.adapters.OlderAdsItem;
import com.bry.adcafe.adapters.TomorrowsAdStatItem;
import com.bry.adcafe.fragments.FragmentAdvertiserPayoutBottomsheet;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.PayoutResponse;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.Payments;
import com.bry.adcafe.services.TimeManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdStats extends AppCompatActivity {
    private static final String TAG = "AdStats";
    private List<String> mAdList = new ArrayList<>();
    private List<Advert> mUploadedAds = new ArrayList<>();

    private List<String> mAdList2 = new ArrayList<>();
    private List<Advert> mUploadedAds2 = new ArrayList<>();

    private List<String> mAdList3 = new ArrayList<>();
    private List<Advert> mUploadedAds3= new ArrayList<>();

    private Context mContext;
    @Bind(R.id.PlaceHolderViewInfo) PlaceHolderView DataListsView;

    private int cycleCount = 0;
    private int cycleCount2 = 0;
    private int cycleCount3 = 0;

    private ProgressDialog mAuthProgressDialog;
    private ProgressDialog mProgForPayments;
    private int numberOfClusters =0;
    private int runCount = 0;

    private int numberOfElements = 0;

    Handler h = new Handler();
    Runnable r;
    private boolean doChildrenExist = false;
    private boolean isMakingPayout = false;
    private DatabaseReference dbListener;

    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private boolean isNeedToLoadLogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_stats);
        mContext = this.getApplicationContext();
        ButterKnife.bind(this);
        setCurrentDateToSharedPrefs();

        if(isNetworkConnected(mContext)){
            DataListsView.setVisibility(View.GONE);
            findViewById(R.id.topText).setVisibility(View.GONE);
            findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);

            registerReceivers();
            createProgressDialog();
            setUpTimeIfNeedBe();
        }else{
            showNoConnectionView();
        }
        DataListsView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        addListenerForPaymentSession();
    }

    @Override
    protected void onPause(){
        super.onPause();
        setCurrentDateToSharedPrefs();
        h.removeCallbacks(r);
        removeListenerForChangeInSessionKey();
        isWindowPaused = true;
    }



    @Override
    protected void onResume(){
        isWindowPaused = false;
        super.onResume();
        new DatabaseManager().loadUsersPassword();
        if(TimeManager.isTimerOnline()) handleOnResumeMethodsAndLogic();
        else handleOnResumeMethodsIfTimeIsOffline();

        if(isNeedToLoadLogin){
            Intent intent = new Intent(AdStats.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else addListenerForChangeInSessionKey();
    }

    private void handleOnResumeMethodsAndLogic(){
        if (!getCurrentDateInSharedPreferences().equals("0") && !getCurrentDateInSharedPreferences().equals(getDate())) {
            Log(TAG, "---Date in shared preferences does not match current date,therefore resetting everything.");
            if(isNetworkConnected(mContext)){
                DataListsView.setVisibility(View.GONE);
                findViewById(R.id.topText).setVisibility(View.GONE);
                findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
                createProgressDialog();
                setCurrentDateToSharedPrefs();
                setUpTimeIfNeedBe();
            }else{
                showNoConnectionView();
            }
        }else if(isAlmostMidNight()){
            if(isNetworkConnected(mContext)){
                DataListsView.setVisibility(View.GONE);
                findViewById(R.id.topText).setVisibility(View.GONE);
                findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
                createProgressDialog();
                setUpTimeIfNeedBe();
            }else{
                showNoConnectionView();
            }
        }
        r = new Runnable() {
            @Override
            public void run() {
                Log(TAG, "---started the time checker for when it is almost midnight.");
                if (isAlmostMidNight()) {
                    DataListsView.setVisibility(View.GONE);
                    findViewById(R.id.topText).setVisibility(View.GONE);
                    findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
                    createProgressDialog();
                    setUpTimeIfNeedBe();
                }
                h.postDelayed(r, 60000);
            }
        };
        h.postDelayed(r, 60000);
    }

    private void handleOnResumeMethodsIfTimeIsOffline(){
        TimeManager.setUpTimeManager("AD_STATS_RESET_UP_TIMER",mContext);
        DataListsView.setVisibility(View.GONE);
        findViewById(R.id.topText).setVisibility(View.GONE);
        findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleOnResumeMethodsAndLogic();
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                Log(TAG,"Unhiding views");
                DataListsView.setVisibility(View.VISIBLE);
                findViewById(R.id.topText).setVisibility(View.VISIBLE);
                findViewById(R.id.LoadingViews).setVisibility(View.GONE);
            }
        },new IntentFilter("AD_STATS_RESET_UP_TIMER"));
    }



    @Override
    protected void onDestroy() {
        unregisterReceivers();
        removeLisenerForPaymentSession();
        super.onDestroy();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForTakeDownAd,
                new IntentFilter("TAKE_DOWN"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForStartPayout,
                new IntentFilter("START_PAYOUT_ADVERTISER"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowBottomSheet,
                new IntentFilter("START_ADVERTISER_PAYOUT"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForCantTakeDown,
                new IntentFilter("CANT_TAKE_DOWN_AD"));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForTakeDownAd);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForShowBottomSheet);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForCantTakeDown);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSuccessfulPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForFailedPayout);

        Intent intent = new Intent("REMOVE-LISTENERS");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void showNoConnectionView() {
        DataListsView.setVisibility(View.GONE);
        findViewById(R.id.topText).setVisibility(View.GONE);
        findViewById(R.id.droppedInternetLayoutForAdStats).setVisibility(View.VISIBLE);
    }



    private void setUpTimeIfNeedBe(){
        if(!TimeManager.isTimerOnline()) {
            TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,
                    new IntentFilter(Constants.LOAD_TIME));
        } else loadTomorrowsUploadedAds();
    }

    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG,"Finished setting up time.");
            loadTomorrowsUploadedAds();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };


    private void loadTomorrowsUploadedAds() {
        if(DataListsView.getChildCount()!=0)DataListsView.removeAllViews();
        Log(TAG,"Loading ads uploaded by user for tomorrow.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getNextDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    doChildrenExist = true;
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList3.add(pushValue);
                    }
                    loadNextDaysAds();
                }else{
                    loadAdsThatHaveBeenUploaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadNextDaysAds() {
        for(int i = 0; i<mAdList3.size(); i++){
            String adToBeLoaded = mAdList3.get(i);
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getNextDay()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cycleCount3++;
                    if(dataSnapshot.hasChildren()) {
                        Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                        DataSnapshot clusters = dataSnapshot.child("clustersToUpLoadTo");
                        for (DataSnapshot clusterSnap : clusters.getChildren()) {
                            int cluster = Integer.parseInt(clusterSnap.getKey());
                            int pushId = clusterSnap.getValue(int.class);
                            adUploadedByUser.clusters.put(cluster, pushId);
                        }
                        Double payoutReimbursalAmm = dataSnapshot.child("payoutReimbursalAmount").getValue(Double.class);
                        adUploadedByUser.setPayoutReimbursalAmount(payoutReimbursalAmm);
                        Log(TAG, "Gotten one ad from firebase. : " + adUploadedByUser.getPushRefInAdminConsole());
                        mUploadedAds3.add(adUploadedByUser);
                        numberOfElements++;
                    }
                    if (cycleCount3 == mAdList3.size()) {
                        Log(TAG, "All the ads have been handled.");
                        loadStats3();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadStats3() {
        DataListsView.addView(new DateForAdStats(mContext,"Your Tomorrows Ads.",DataListsView));
        DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        for(int i = 0; i<mUploadedAds3.size();i++){
            DataListsView.addView(new TomorrowsAdStatItem(mContext,DataListsView,mUploadedAds3.get(i)));
        }
        for(int i = 0;i<getNumber(mUploadedAds3.size());i++){
            DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        }
        loadAdsThatHaveBeenUploaded();
    }



    private void loadAdsThatHaveBeenUploaded() {
        Log(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getDate());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    doChildrenExist = true;
                    Log(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList.add(pushValue);
                    }
                    loadAdsUploadedByUser();
                    Log(TAG,"Number of children is : "+mAdList.size());
                }else{
                    loadPreviousDaysAds();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser() {
        for(int i = 0; i<mAdList.size(); i++){
            String adToBeLoaded = mAdList.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getDate()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cycleCount++;
                    if(dataSnapshot.hasChildren()) {
                        Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                        Double payoutReimbursalAmm = dataSnapshot.child("payoutReimbursalAmount").getValue(Double.class);
                        adUploadedByUser.setPayoutReimbursalAmount(payoutReimbursalAmm);
                        Log(TAG, "Gotten one ad from firebase. : " + adUploadedByUser.getPushRefInAdminConsole());
                        mUploadedAds.add(adUploadedByUser);
                        numberOfElements++;
                    }
                    if (cycleCount == mAdList.size()) {
                        Log(TAG, "All the ads have been handled.");
                        loadStats();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    private void loadStats() {
        DataListsView.addView(new DateForAdStats(mContext,"Your Todays Ads.",DataListsView));
        DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        for(int i = 0; i<mUploadedAds.size();i++){
            DataListsView.addView(new MyAdStatsItem(mContext,DataListsView,mUploadedAds.get(i)));
        }
        for(int i = 0;i<getNumber(mUploadedAds.size());i++){
            DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        }
        loadPreviousDaysAds();
    }




    private void loadPreviousDaysAds() {
        Log(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getPreviousDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    doChildrenExist = true;
                    Log(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList2.add(pushValue);
                    }
                    Log(TAG,"Number of children is : "+mAdList2.size());
                    findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
                    loadAdsUploadedByUser2();
                }else{
                    loadUploadHistory();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser2() {
        for(int i = 0; i<mAdList2.size(); i++){
            String adToBeLoaded = mAdList2.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getPreviousDay()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cycleCount2++;
                    if(dataSnapshot.hasChildren()) {
                        Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                        Double payoutReimbursalAmm = dataSnapshot.child("payoutReimbursalAmount").getValue(Double.class);
                        adUploadedByUser.setPayoutReimbursalAmount(payoutReimbursalAmm);
                        Log(TAG, "Gotten one ad from firebase. : " + adUploadedByUser.getPushRefInAdminConsole());
                        mUploadedAds2.add(adUploadedByUser);
                        numberOfElements++;
                    }
                    if (cycleCount2 == mAdList2.size()) {
                        Log(TAG, "All the ads have been handled.");
                        loadStats2();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void loadStats2() {
        DataListsView.addView(new DateForAdStats(mContext,"Your Yesterdays Ads.",DataListsView));
        DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        for(int i = 0; i<mUploadedAds2.size();i++){
            DataListsView.addView(new MyAdStatsItem(mContext,DataListsView,mUploadedAds2.get(i)));
        }

        for(int i = 0;i<getNumber(mUploadedAds2.size());i++){
            DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
        }
        loadUploadHistory();
    }

    private void loadUploadHistory() {
        DatabaseReference dbrefh = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY);
        dbrefh.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log(TAG,"Loading upload history");
                if(dataSnapshot.exists()){
                    doChildrenExist = true;
                    boolean hasTopBeenAdded = false;
                    findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
                    int numberOfUploads = 0;
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        for(DataSnapshot adSnap:snap.getChildren()){
                            numberOfUploads++;
                        }
                    }
                    if(numberOfUploads>0) DataListsView.setItemViewCacheSize(numberOfUploads+6);
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                       String viewingDate = snap.getKey();

                       Log(TAG,"One date has loaded"+viewingDate);
                       long viewingDateInDays = Long.valueOf(viewingDate)*-1;
                       long tomorrowsDateInDays = getDateInDays()+1;
                       long todaysDateInDays = getDateInDays();
                       long yesterdaysDateInDays = getDateInDays()-1;
                       if(viewingDateInDays!=tomorrowsDateInDays
                               && viewingDateInDays!=todaysDateInDays
                               && viewingDateInDays!=yesterdaysDateInDays){
                           Log(TAG,"The viewing date is past the dates for not showing");
                           for(DataSnapshot snapMini:snap.getChildren()){
                               Advert ad = snapMini.getValue(Advert.class);
                               if(!hasTopBeenAdded){
                                   DataListsView.addView(new DateForAdStats(mContext,"Your Upload History.",DataListsView));
                                   DataListsView.addView(new DateForAdStats(mContext,"",DataListsView));
                                   hasTopBeenAdded = true;
                               }
                               DataListsView.addView(new OlderAdsItem(mContext,DataListsView,ad));
                           }
                       }
                    }
                }
                DataListsView.setVisibility(View.VISIBLE);
                findViewById(R.id.topText).setVisibility(View.VISIBLE);
                findViewById(R.id.LoadingViews).setVisibility(View.GONE);
                if(doChildrenExist){
                    findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
            }
        });
    }




    private String getDate(){
        return TimeManager.getDate();
    }

    private String getPreviousDay(){
        return TimeManager.getPreviousDay();
    }

    private Long getDateInDays(){
        return TimeManager.getDateInDays();
    }


    private String getNextDay(){
        return TimeManager.getNextDay();

    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private BroadcastReceiver mMessageReceiverForTakeDownAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Message received for taking down ad.");
            showConfirmSubscribeMessage();
        }
    };

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(R.string.app_name);
        mAuthProgressDialog.setMessage("Taking down the ad...");
        mAuthProgressDialog.setCancelable(false);

        mProgForPayments = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProgForPayments.setTitle(R.string.app_name);
        mProgForPayments.setMessage("This should take a few seconds... ");
        mProgForPayments.setCancelable(false);
        mProgForPayments.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgForPayments.setIndeterminate(true);
    }

    private void showConfirmSubscribeMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafe.");
        builder.setMessage(Variables.areYouSureTakeDownText)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takeDownAd2();
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void takeDownAd() {
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeFlagged;
        if(ad.isFlagged())mAuthProgressDialog.setMessage("Restoring the ad...");
        boolean bol = !ad.isFlagged();

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(ad.getPushRefInAdminConsole())
                .child("flagged");
        mRef.setValue(bol);

        Log(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        for(Integer cluster : ad.clusters.keySet()){
            int pushId = ad.clusters.get(cluster);
            DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                    .child(getNextDay())
                    .child(Integer.toString(ad.getAmountToPayPerTargetedView()-2))
                    .child(ad.getCategory())
                    .child(Integer.toString(cluster))
                    .child(Integer.toString(pushId))
                    .child("flagged");
            mRef3.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    runCount++;
                    if(runCount==numberOfClusters){
                        runCount = 0;
                        numberOfClusters = 0;
                        mAuthProgressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void takeDownAd2(){
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeFlagged;
        if(ad.isFlagged())mAuthProgressDialog.setMessage("Restoring the ad...");
        else mAuthProgressDialog.setMessage("Taking down ad...");
        boolean bol = !ad.isFlagged();

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(ad.getPushRefInAdminConsole())
                .child("flagged");
        mRef.setValue(bol);

        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(TimeManager.getNextDayYear()).child(TimeManager.getNextDayMonth()).child(TimeManager.getNextDayDay())
                .child(ad.getPushRefInAdminConsole()).child("flagged");
        mRef3.setValue(bol);

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(TimeManager.getDateInDays()+1)))
                .child(ad.getPushRefInAdminConsole()).child("flagged");
        mRef2.setValue(bol);

        Log(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        int nextCluster = getClusterValue(runCount,ad);
        int nextPushId = getPushIdValue(runCount,ad);
        flagSpecific(nextCluster,nextPushId,ad,bol);
    }

    private void flagSpecific(int cluster, int pushId, final Advert ad, final boolean bol){
        DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay())
                .child(Integer.toString(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                .child(ad.getCategory())
                .child(Integer.toString(cluster))
                .child(Integer.toString(pushId))
                .child("flagged");
        mRef3.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runCount++;
                if(runCount<numberOfClusters){
                    int nextCluster = getClusterValue(runCount,ad);
                    int nextPushId = getPushIdValue(runCount,ad);
                    flagSpecific(nextCluster,nextPushId,ad,bol);
                }else{
                    runCount = 0;
                    numberOfClusters = 0;
                    mAuthProgressDialog.dismiss();
                }
            }
        });
    }



    private int getClusterValue(int index,Advert ad) {
        LinkedHashMap map = ad.clusters;
        int cluster = (new ArrayList<Integer>(map.keySet())).get(index);
        Log(TAG, "Cluster gotten from ad is : " + cluster);
        return cluster;
    }

    private int getPushIdValue(int index,Advert ad) {
        LinkedHashMap map = ad.clusters;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log(TAG, "Cluster gotten from ad is : " + cluster);
        return cluster;
    }

    private int getNumber(int size){
        int newSize = size;
        int number = 0;
        while (newSize%2!=0){
            newSize++;
            number++;
        }

        return number;
    }




    private BroadcastReceiver mMessageReceiverForShowBottomSheet = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received to show bottom sheet.");
            new DatabaseManager().setIsMakingPayoutInFirebase(true);
            showBottomSheetForReimbursement();
        }
    };

    private BroadcastReceiver mMessageReceiverForStartPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received to start payout.");
            if(isOnline(mContext)){
                if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
                startPayout();
            }
            else Toast.makeText(mContext,"You need internet connection to do that.",Toast.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForCantTakeDown = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received to show cant take down dialog.");
            showPromptForTakenDown();
        }
    };




    //Payout api implementation comes here...
    private void startPayout(){
        mProgForPayments.show();
        Advert ad = Variables.adToBeReimbursed;
        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        double ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                (ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);

        double vat = (ad.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(
                ad.getAmountToPayPerTargetedView())) *Constants.VAT_CONSTANT;

        double reimbursementTotals = (round(ammountToBeRepaid+ad.getPayoutReimbursalAmount()+vat));

//        Toast.makeText(mContext,"payout!",Toast.LENGTH_SHORT).show();
        String payoutPhoneNumber = Variables.phoneNo;
        String totalsToReimburse = Integer.toString((int)reimbursementTotals);

        String PAYOUT_SUCCESSFUL = "PAYOUT_SUCCESSFUL";
        String PAYOUT_FAILED = "PAYOUT_FAILED";

        String newPhoneNo = "254"+payoutPhoneNumber.substring(1);
        Log("Dashboard","new Phone no is: "+newPhoneNo);
        int amount = Integer.parseInt(totalsToReimburse);

        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(Constants.ADMIN_ACC)){
            amount = 10;
            totalsToReimburse = "10";
        }

        Payments mPayments = new Payments(mContext,PAYOUT_SUCCESSFUL,PAYOUT_FAILED);
        mPayments.MpesaMakePayouts(totalsToReimburse,newPhoneNo,mContext);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSuccessfulPayout,
                new IntentFilter(PAYOUT_SUCCESSFUL));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFailedPayout,
                new IntentFilter(PAYOUT_FAILED));
    }


    private BroadcastReceiver mMessageReceiverForSuccessfulPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received that payout is finished.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            if(Variables.isOlderAd){
                setOlderAdsPaymentValue();
            } else SetPaymentValues();
        }
    };


    private BroadcastReceiver mMessageReceiverForFailedPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received that payout has failed.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            mProgForPayments.dismiss();
            showFailedPayoutsView();
            new DatabaseManager().setIsMakingPayoutInFirebase(false);
        }
    };

    //this handles logic when payouts have been completed..
    private void SetPaymentValues() {
        Advert ad = Variables.adToBeReimbursed;
        boolean bol = !ad.isHasBeenReimbursed();

        DatabaseReference mRef2 =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(TimeManager.getDateInDays()-1)))
                .child(ad.getPushRefInAdminConsole()).child("hasBeenReimbursed");
        mRef2.setValue(bol);

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getPreviousDay())
                .child(ad.getPushRefInAdminConsole())
                .child("hasBeenReimbursed");
        mRef.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgForPayments.hide();
                showSuccessfulPayoutPrompt();
            }
        });

        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        double reimbursementTotals = (numberOfUsersWhoDidntSeeAd*(ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(TimeManager.getDateInDays()-1)))
                .child(ad.getPushRefInAdminConsole()).child(Constants.REIMBURSEMENT_HISTORY);

        PayoutResponse myResp = Variables.payoutObject;
        myResp.setDateInDays(TimeManager.getDateInDays());
        myResp.setDate(TimeManager.getDate());
        myResp.setTime(TimeManager.getTime());
        myResp.setPhoneNo(Variables.phoneNo);
        myResp.setUserId(uid);
        myResp.setAmount((int)reimbursementTotals);
        adRef.setValue(myResp);

        DatabaseReference dbrefTrans = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTIONS)
                .child(Constants.AD_PAYOUTS).child(Constants.MPESA_PAYOUTS)
                .child(TimeManager.getYear()).child(TimeManager.getMonth()).child(TimeManager.getDay())
                .child(ad.getPushRefInAdminConsole());
        dbrefTrans.setValue(myResp);

        removeAdminAmm((long)reimbursementTotals);
    }

    private void setOlderAdsPaymentValue() {
        Advert ad = Variables.adToBeReimbursed;
        boolean bol = !ad.isHasBeenReimbursed();

        DatabaseReference mRef2 =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(ad.getDateInDays()+1)))
                .child(ad.getPushRefInAdminConsole()).child("hasBeenReimbursed");
        mRef2.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Variables.isOlderAd = false;
                mProgForPayments.hide();
                showSuccessfulPayoutPrompt();
            }
        });

        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        double reimbursementTotals = (numberOfUsersWhoDidntSeeAd*(ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(ad.getDateInDays()+1)))
                .child(ad.getPushRefInAdminConsole()).child(Constants.REIMBURSEMENT_HISTORY);

        PayoutResponse myResp = Variables.payoutObject;
        myResp.setDateInDays(TimeManager.getDateInDays());
        myResp.setDate(TimeManager.getDate());
        myResp.setTime(TimeManager.getTime());
        myResp.setPhoneNo(Variables.phoneNo);
        myResp.setUserId(uid);
        myResp.setAmount((int)reimbursementTotals);
        adRef.setValue(myResp);

        DatabaseReference dbrefTrans = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTIONS)
                .child(Constants.AD_PAYOUTS).child(Constants.MPESA_PAYOUTS)
                .child(TimeManager.getYear()).child(TimeManager.getMonth()).child(TimeManager.getDay())
                .child(ad.getPushRefInAdminConsole());
        dbrefTrans.setValue(myResp);

        removeAdminAmm((long)reimbursementTotals);
    }

    private void removeAdminAmm(final long amount){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long amm = dataSnapshot.getValue(long.class);
                    long newAmm = amm -= amount;
                    DatabaseReference mewRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
                    mewRef.setValue(newAmm);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void showBottomSheetForReimbursement(){
        Advert ad = Variables.adToBeReimbursed;
        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        double ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                (ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);

        double vat = (ad.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(
                ad.getAmountToPayPerTargetedView())) *Constants.VAT_CONSTANT;

        double reimbursementTotals = ammountToBeRepaid+ad.getPayoutReimbursalAmount()+vat;

        FragmentAdvertiserPayoutBottomsheet fragmentModalBottomSheet = new FragmentAdvertiserPayoutBottomsheet();
        fragmentModalBottomSheet.setActivity(AdStats.this);
        fragmentModalBottomSheet.setDetails(reimbursementTotals,Variables.getPassword());
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }

    private void showPromptForTakenDown(){
        final Dialog d = new Dialog(AdStats.this);
        d.setTitle("Cannot Put Up.");
        d.setContentView(R.layout.dialog91);
        Button b2 =  d.findViewById(R.id.okBtn);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void setCurrentDateToSharedPrefs() {
        Log(TAG, "---Setting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences("AdStatsDate", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if(isAlmostMidNight()) editor.putString("date",getNextDay());
        else editor.putString("date", getDate());
        editor.apply();
    }




    private String getCurrentDateInSharedPreferences() {
        Log(TAG, "---Getting current date in shared preferences.");
        SharedPreferences prefs = getSharedPreferences("AdStatsDate", MODE_PRIVATE);
        return prefs.getString("date", "0");
    }

    private boolean isAlmostMidNight() {
        return TimeManager.isAlmostMidNight();
    }

    private void showFailedPayoutsView() {
        final Dialog d = new Dialog(this);
        d.setTitle("Failed Payout.");
        d.setContentView(R.layout.dialog94);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void showSuccessfulPayoutPrompt() {
        final Dialog d = new Dialog(this);
        d.setTitle("Successful Payout.");
        d.setContentView(R.layout.dialog95);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
        new DatabaseManager().setIsMakingPayoutInFirebase(false);
    }



    private void promptUserForUnableToPayout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payout");
        builder.setMessage("You cant make a payout of 0Ksh.")
                .setCancelable(true)
                .setPositiveButton("Ok.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }


    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals("bryonyoni@gmail.com")) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.getKey().equals(Constants.IS_MAKING_PAYOUT)){
                isMakingPayout = dataSnapshot.getValue(boolean.class);
                if(isMakingPayout)sendBroadcastToHideBtns();
                else sendBroadcastToShowBtns();
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

    private void addListenerForPaymentSession(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbListener = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        dbListener.addChildEventListener(chil);
    }

    private void removeLisenerForPaymentSession(){
        if(dbListener!=null) dbListener.removeEventListener(chil);
    }

    private void sendBroadcastToHideBtns(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.IS_MAKING_PAYOUT+"true"));
    }

    private void sendBroadcastToShowBtns(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.IS_MAKING_PAYOUT+"false"));
    }



    ChildEventListener chil2 = new ChildEventListener() {
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
        SKListener.addChildEventListener(chil2);
    }

    public void removeListenerForChangeInSessionKey(){
        if(SKListener!=null){
            SKListener.removeEventListener(chil2);
        }
    }

    public String getSessionKey(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        String sk = prefs2.getString(Constants.BOI_IS_DA_KEY, "NULL");
        Log.d(TAG, "Loading session key from shared prefs - " + sk);
        return sk;
    }



    public void PerformShutdown(){
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Variables.resetAllValues();
        if(!isWindowPaused){
            Intent intent = new Intent(AdStats.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            isNeedToLoadLogin = true;
        }

    }

    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
