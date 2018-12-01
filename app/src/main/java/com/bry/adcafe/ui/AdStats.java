package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.bry.adcafe.models.AdImageData;
import com.bry.adcafe.models.AdPinData;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.ExpressionData;
import com.bry.adcafe.models.PayoutResponse;
import com.bry.adcafe.models.User;
import com.bry.adcafe.models.WebClickData;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.Payments;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mindorks.placeholderview.PlaceHolderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdStats extends AppCompatActivity implements View.OnClickListener{
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

    @Bind(R.id.swipeBackView)View swipeBackView;
    private boolean isSwipingForBack = false;
    private GestureDetector mSwipeBackDetector;
    private int maxSideSwipeLength = 200;
    private int x_delta;
    private List<Integer> SideSwipeRawList = new ArrayList<>();
    private boolean isSideScrolling = false;


    @Bind(R.id.telemetryLayout) RelativeLayout telemetryLayout;
    private int mAnimationTime = 300;
    private boolean isTelemetryCardMinimized = true;
//    @Bind(R.id.telemetryBack) ImageButton telemetryBack;
    @Bind(R.id.normalTelemetryLayout) RelativeLayout normalTelemetryLayout;
    @Bind(R.id.statTelemetryIcon) ImageView statTelemetryIcon;
    @Bind(R.id.viewingDateText) TextView viewingDateText;
    @Bind(R.id.targetNumber) TextView targetNumber;
    @Bind(R.id.userSeenNumber) TextView userSeenNumber;
    @Bind(R.id.adImage) ImageView adImage;
    @Bind(R.id.adImageBackground) ImageView adImageBackground;
    @Bind(R.id.uploaderText) TextView uploaderText;
    @Bind(R.id.statusText) TextView statusText;
    @Bind(R.id.amountPaidNumber) TextView amountPaidNumber;
    @Bind(R.id.reimbursalAmountNumber) TextView reimbursalAmountNumber;
    @Bind(R.id.reimburseCard) CardView reimburseCard;
    @Bind(R.id.takeDownCard) CardView takeDownCard;
    @Bind(R.id.webclickIncentiveText) TextView webclickIncentiveText;
    @Bind(R.id.numberOfPinsText) TextView numberOfPinsText;
    @Bind(R.id.websiteText) TextView websiteText;
    @Bind(R.id.editWebsiteIcon) ImageButton editWebsiteIcon;
    @Bind(R.id.phoneNoText) TextView phoneNoText;
    @Bind(R.id.editPhoneNoIcon) ImageButton editPhoneNoIcon;
    @Bind(R.id.contactLocationText) TextView contactLocationText;
    @Bind(R.id.categoryText) TextView categoryText;
    private Bitmap bm;
    private boolean isEditingWebsiteUrl = false;
    private boolean isEditingPhoneNo = false;
    Bitmap backBl;
    @Bind(R.id.loadingProgressBar) ProgressBar loadingProgressBar;
    private List<Advert> myUploadHistoryAds = new ArrayList<>();
    private List<AdImageData> myHistoryAdImages = new ArrayList<>();
    private int historyCycleCount = 0;

    private ChildEventListener telemetryEventListener;
    private DatabaseReference dbRef;
    @Bind(R.id.takeDownView) LinearLayout takeDownView;
    private boolean isCardCollapsing = false;
    private ChildEventListener expressionTelemetryListener;
    private DatabaseReference dbExpressionRef;
    private ChildEventListener clickTelemetryListener;
    private DatabaseReference dbClickRef;
    private ChildEventListener pinTelemetryListener;
    private DatabaseReference dbPinRef;

    @Bind(R.id.chart) LineChart expressionGraph;
    @Bind(R.id.webpageChart) LineChart webpageChart;
    @Bind(R.id.webpageVisitCount) TextView webpageVisitCount;
    private DataSnapshot pinSnapshot;

    @Bind(R.id.hideButton) ImageButton hideButton;
    @Bind(R.id.starButton) ImageButton starButton;
    @Bind(R.id.hiddenPlaceHolderViewInfo) PlaceHolderView hiddenPlaceHolderViewInfo;
    private List<Advert> myHistoryAds = new ArrayList<>();
    @Bind(R.id.moreButton) ImageButton moreButton;
    @Bind(R.id.moreLayout) RelativeLayout moreLayout;
    @Bind(R.id.showHidden) LinearLayout showHidden;
    private boolean isOptionsShowing = false;
    private boolean isShowingHidden = false;

    private int hiddenNumber = 0;
    private boolean isToRemove = false;
    private  List<String> historyAdIds = new ArrayList<>();
    private  List<String> hiddenAdIds = new ArrayList<>();

    @Bind(R.id.showStarred) LinearLayout showStarred;
    @Bind(R.id.starredTitle) TextView starredTitle;
    @Bind(R.id.showStarredImage) ImageView showStarredImage;
    @Bind(R.id.starredPlaceHolderView) PlaceHolderView starredPlaceHolderView;
    private boolean isShowingStarred = false;
    private List<Advert> starredAds = new ArrayList<>();

    @Bind(R.id.expandImageCard) RelativeLayout expandImageCard;
    @Bind(R.id.adImageBackground2) ImageView adImageBackground2;
    @Bind(R.id.adImage2) ImageView adImage2;



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
        hiddenPlaceHolderViewInfo.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        starredPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        addListenerForPaymentSession();
        addTouchListenerForSwipeBack();

        collapseCard();
        isTelemetryCardMinimized = true;
        moreButton.setOnClickListener(this);

        closeExpandedImage();
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
    protected void onStop(){
        super.onStop();
        if(!isTelemetryCardMinimized){
            hideSelectedAdTelemetries();
        }
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
                moreButton.setVisibility(View.GONE);
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
                moreButton.setVisibility(View.GONE);
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
                    finish();
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
        moreButton.setVisibility(View.GONE);
        findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleOnResumeMethodsAndLogic();
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                Log(TAG,"Unhiding views");
                DataListsView.setVisibility(View.VISIBLE);
                findViewById(R.id.topText).setVisibility(View.VISIBLE);
                moreButton.setVisibility(View.VISIBLE);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForOpenAdvertTelemetries,
                new IntentFilter("VIEW_AD_TELEMETRIES"));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForTakeDownAd);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForShowBottomSheet);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForCantTakeDown);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSuccessfulPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForFailedPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForOpenAdvertTelemetries);

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


    private void loadPassData(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot passwordSnap = dataSnapshot.child(Constants.USER_PASSCODE);
                DataSnapshot isEncryptedSnap = dataSnapshot.child(Constants.IS_PASSWORD_ENCRYPTED);
                if(isEncryptedSnap.exists()){
                    boolean isEncrypted = isEncryptedSnap.getValue(Boolean.class);
                    if(isEncrypted){
                        if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                            Variables.setPassword(Variables.decryptPassword(passwordSnap.getValue(String.class)));
                        }
                    }else{
                        if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                            Variables.setPassword(passwordSnap.getValue(String.class));
                        }
                    }
                }else{
                    if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                        Variables.setPassword(passwordSnap.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadTomorrowsUploadedAds() {
        loadPassData();
        if(DataListsView.getChildCount()!=0)DataListsView.removeAllViews();
        if(hiddenPlaceHolderViewInfo.getChildCount()!=0)hiddenPlaceHolderViewInfo.removeAllViews();
        Log(TAG,"Loading ads uploaded by user for tomorrow.");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.UPLOADED_AD_LIST).child(getNextDay());
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
                        adUploadedByUser.setAdType(Constants.TOMORROWS_ADS);
                        DataSnapshot clusters = dataSnapshot.child("clustersToUpLoadTo");
                        for (DataSnapshot clusterSnap : clusters.getChildren()) {
                            int cluster = Integer.parseInt(clusterSnap.getKey());
                            int pushId = clusterSnap.getValue(int.class);
                            adUploadedByUser.clusters.put(cluster, pushId);
                        }
                        Double payoutReimbursalAmm = dataSnapshot.child("payoutReimbursalAmount").getValue(Double.class);
                        adUploadedByUser.setPayoutReimbursalAmount(payoutReimbursalAmm);
                        if(adUploadedByUser.isStarred()){
                            starredAds.add(adUploadedByUser);
                            starredPlaceHolderView.addView(new OlderAdsItem(mContext,starredPlaceHolderView,adUploadedByUser,starredPlaceHolderView.getChildCount()));
                        }
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
        loadPinData();
    }

    private void loadPinData(){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.AD_PINS);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pinSnapshot = dataSnapshot;
                loadAdsThatHaveBeenUploaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                        if(pinSnapshot!=null){
                            for(DataSnapshot advertPinDataSnap:pinSnapshot.child(adUploadedByUser.getPushRefInAdminConsole()).getChildren()){
                                AdPinData adPin = advertPinDataSnap.getValue(AdPinData.class);
                                if(!adUploadedByUser.adPins.contains(adPin))adUploadedByUser.adPins.add(adPin);
                            }
                        }
                        if(dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).exists()){
                              for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).getChildren()){
                                  adUploadedByUser.expressions.add(expSnap.getValue(ExpressionData.class));
                              }
                        }
                        if(dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).exists()){
                            for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).getChildren()){
                                adUploadedByUser.webclicks.add(expSnap.getValue(WebClickData.class));
                            }
                        }
                        adUploadedByUser.setAdType(Constants.TODAYS_ADS);
                        DataSnapshot clusters = dataSnapshot.child("clustersToUpLoadTo");
                        for (DataSnapshot clusterSnap : clusters.getChildren()) {
                            int cluster = Integer.parseInt(clusterSnap.getKey());
                            int pushId = clusterSnap.getValue(int.class);
                            adUploadedByUser.clusters.put(cluster, pushId);
                        }
                        Double payoutReimbursalAmm = dataSnapshot.child("payoutReimbursalAmount").getValue(Double.class);
                        adUploadedByUser.setPayoutReimbursalAmount(payoutReimbursalAmm);
                        if(adUploadedByUser.isStarred()){
                            starredAds.add(adUploadedByUser);
                            starredPlaceHolderView.addView(new OlderAdsItem(mContext,starredPlaceHolderView,adUploadedByUser,starredPlaceHolderView.getChildCount()));
                        }
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
        Log.e(TAG,"Loading previous day ads uploaded by user.");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.UPLOADED_AD_LIST).child(getPreviousDay());
        Log.d(TAG,"Query sent is: "+Constants.FIREBASE_CHILD_USERS+" : "+uid+" : "+Constants.UPLOADED_AD_LIST+" : "+getPreviousDay());
//        DatabaseReference dbref = query.getRef();
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
                    Log.e(TAG,"No previous day ads were found");
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
                        if(pinSnapshot!=null){
                            for(DataSnapshot advertPinDataSnap:pinSnapshot.child(adUploadedByUser.getPushRefInAdminConsole()).getChildren()){
                                AdPinData adPin = advertPinDataSnap.getValue(AdPinData.class);
                                if(!adUploadedByUser.adPins.contains(adPin))adUploadedByUser.adPins.add(adPin);
                            }
                        }
                        if(dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).exists()){
                            for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).getChildren()){
                                adUploadedByUser.expressions.add(expSnap.getValue(ExpressionData.class));
                            }
                        }
                        if(dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).exists()){
                            for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).getChildren()){
                                adUploadedByUser.webclicks.add(expSnap.getValue(WebClickData.class));
                            }
                        }
                        adUploadedByUser.setAdType(Constants.YESTERDAYS_ADS);
                        if(adUploadedByUser.isStarred()){
                            starredAds.add(adUploadedByUser);
                            starredPlaceHolderView.addView(new OlderAdsItem(mContext,starredPlaceHolderView,adUploadedByUser,starredPlaceHolderView.getChildCount()));
                        }
                        DataSnapshot clusters = dataSnapshot.child("clustersToUpLoadTo");
                        for (DataSnapshot clusterSnap : clusters.getChildren()) {
                            int cluster = Integer.parseInt(clusterSnap.getKey());
                            int pushId = clusterSnap.getValue(int.class);
                            adUploadedByUser.clusters.put(cluster, pushId);
                        }
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
                myUploadHistoryAds.clear();
                historyAdIds.clear();
                hiddenAdIds.clear();
                hiddenAdIds.add("Your Hidden Ads.");hiddenAdIds.add("");
                historyAdIds.add("Your Upload History.");historyAdIds.add("");
                if(dataSnapshot.exists()){
                    doChildrenExist = true;
                    boolean hasTopBeenAdded = false;
                    boolean hasTopBoi = false;
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
                               if(pinSnapshot!=null){
                                   try{
                                       for(DataSnapshot advertPinDataSnap:pinSnapshot.child(ad.getPushRefInAdminConsole()).getChildren()){
                                           AdPinData adPin = advertPinDataSnap.getValue(AdPinData.class);
                                           if(!ad.adPins.contains(adPin))ad.adPins.add(adPin);
                                       }
                                   }catch (Exception e){
                                       e.printStackTrace();
                                   }
                               }
                               if(dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).exists()){
                                   for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_SEEN).getChildren()){
                                       ad.expressions.add(expSnap.getValue(ExpressionData.class));
                                   }
                               }
                               if(dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).exists()){
                                   for(DataSnapshot expSnap : dataSnapshot.child(Constants.USERS_THAT_HAVE_CLICKED_IT).getChildren()){
                                       ad.webclicks.add(expSnap.getValue(WebClickData.class));
                                   }
                               }
                               ad.setAdType(Constants.OLDER_UPLOADS);
                               myHistoryAds.add(ad);
                               if(ad.hasSetBackupImage())myUploadHistoryAds.add(ad);
                               historyAdIds.add(ad.getPushRefInAdminConsole());
                               if(ad.isStarred()){
                                   starredAds.add(ad);
                                   starredPlaceHolderView.addView(new OlderAdsItem(mContext,starredPlaceHolderView,ad,starredPlaceHolderView.getChildCount()));
                               }
                               if(ad.isAdvertiserHidden()){
//                                   if(!hasTopBoi){
//                                       hiddenPlaceHolderViewInfo.addView(new DateForAdStats(mContext,"Your Hidden ads History.",hiddenPlaceHolderViewInfo));
//                                       hiddenPlaceHolderViewInfo.addView(new DateForAdStats(mContext," ",hiddenPlaceHolderViewInfo));
//                                       hasTopBoi = true;
//                                   }
                                   hiddenPlaceHolderViewInfo.addView(new OlderAdsItem(mContext,hiddenPlaceHolderViewInfo,ad,hiddenPlaceHolderViewInfo.getChildCount()));
                                   hiddenAdIds.add(ad.getPushRefInAdminConsole());
                                   hiddenNumber++;
                               }else{
                                   if(!hasTopBeenAdded){
                                       DataListsView.addView(new DateForAdStats(mContext,"Your Upload History.",DataListsView));
                                       DataListsView.addView(new DateForAdStats(mContext," ",DataListsView));
                                       hasTopBeenAdded = true;
                                   }
                                   DataListsView.addView(new OlderAdsItem(mContext,DataListsView,ad,DataListsView.getChildCount()));
                               }
                           }
                       }
                    }
                }
                if(!myUploadHistoryAds.isEmpty()){
                    loadImages();
                } else {
                    DataListsView.setVisibility(View.VISIBLE);
                    findViewById(R.id.topText).setVisibility(View.VISIBLE);
                    moreButton.setVisibility(View.VISIBLE);
                    findViewById(R.id.LoadingViews).setVisibility(View.GONE);
                    if (doChildrenExist) {
                        findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
                    } else {
                        findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
            }
        });
    }




    private void loadImages(){
        historyCycleCount = 0;
        loadSpecificImage();
    }

    private void loadSpecificImage(){
        String adId = myUploadHistoryAds.get(historyCycleCount).getPushRefInAdminConsole();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.ALL_AD_IMAGES).child(adId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String id = dataSnapshot.child("imageId").getValue(String.class);
                    String image = dataSnapshot.child("image").getValue(String.class);
                    AdImageData adImageData = new AdImageData();
                    adImageData.setAdId(id);
                    adImageData.setAdImage(image);

                    myHistoryAdImages.add(adImageData);
                }

                historyCycleCount++;
                String adId = myUploadHistoryAds.get(historyCycleCount).getPushRefInAdminConsole();
                if(adId!=null){
                    loadSpecificImage();
                }else{
                    historyCycleCount=0;
                    setHistoryImagesData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setHistoryImagesData(){
        if(myHistoryAdImages.isEmpty()){
            DataListsView.setVisibility(View.VISIBLE);
            findViewById(R.id.topText).setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            findViewById(R.id.LoadingViews).setVisibility(View.GONE);
            if(doChildrenExist){
                findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
            }else{
                findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
            }
        }else{
            LongOperationHist hx = new LongOperationHist();
            hx.execute("");
        }

    }

    @Override
    public void onClick(View v) {
        if(v.equals(moreButton)){
            openMore();
        }
    }

    private class LongOperationHist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                List<AdImageData> adImgsToLoop = new ArrayList<>(myHistoryAdImages);
                myHistoryAdImages.clear();
                 for(AdImageData adI:adImgsToLoop){
                     generateImageBitmap(adI);
                 }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            myHistoryAdImages.clear();
            DataListsView.setVisibility(View.VISIBLE);
            findViewById(R.id.topText).setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            findViewById(R.id.LoadingViews).setVisibility(View.GONE);
            if(doChildrenExist){
                findViewById(R.id.noAdsUploadedText).setVisibility(View.INVISIBLE);
            }else{
                findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
            }
        }

    }

    private void generateImageBitmap(AdImageData adI){
        try {
            Bitmap bm = getResizedBitmap(decodeFromFirebaseBase64(adI.getAdImage()),1000);
            Log(TAG,"History adImage has been converted to bitmap.");
            adI.setImageBitmap(bm);
            myHistoryAdImages.add(adI);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        Bitmap newBm = getResizedBitmap(bitm,500);
        return newBm;
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
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
//            showSelectedAdTelemetries();
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String date = getDate();
        if(Variables.adToBeFlagged.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(Variables.adToBeFlagged.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(date)
                .child(ad.getPushRefInAdminConsole())
                .child("flagged");
        mRef.setValue(bol);


        String dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        if(Variables.adToBeFlagged.getAdType().equals(Constants.TOMORROWS_ADS)){
            dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        }else if(Variables.adToBeFlagged.getAdType().equals(Constants.TODAYS_ADS)){
            dateChild = TimeManager.getYear()+TimeManager.getMonth()+TimeManager.getDay();
        }


        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(dateChild)
                .child(ad.getPushRefInAdminConsole()).child("flagged");
        mRef3.setValue(bol);


        long dateInDays = ad.getDateInDays()+1;

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(dateInDays)))
                .child(ad.getPushRefInAdminConsole()).child("flagged");
        mRef2.setValue(bol);

        Log(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        int nextCluster = getClusterValue(runCount,ad);
        int nextPushId = getPushIdValue(runCount,ad);
        flagSpecific(nextCluster,nextPushId,ad,bol);
    }

    private void flagSpecific(int cluster, int pushId, final Advert ad, final boolean bol){
        String date = getDate();
        if(Variables.adToBeFlagged.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(Variables.adToBeFlagged.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(date)
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

        double incentiveAmm = 0;
        if(ad.didAdvertiserAddIncentive()){
            incentiveAmm = (ad.getWebClickIncentive()* (ad.getNumberOfUsersToReach()-ad.getWebClickNumber()) );
        }

        double reimbursementTotals = ammountToBeRepaid+ad.getPayoutReimbursalAmount()+vat+incentiveAmm;
        double finaltotal = (double)Math.round(reimbursementTotals * 100d) / 100d;

        FragmentAdvertiserPayoutBottomsheet fragmentModalBottomSheet = new FragmentAdvertiserPayoutBottomsheet();
        fragmentModalBottomSheet.setActivity(AdStats.this);
        fragmentModalBottomSheet.setDetails(finaltotal,Variables.getPassword());
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }

    private void showPromptForTakenDown(){
        final Dialog d = new Dialog(AdStats.this);
        d.setTitle("Cannot Put Up.");
        d.setContentView(R.layout.dialog91);
        Button b2 =  d.findViewById(R.id.okBtn);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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
//        try{
//            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//            if(user.equals("bryonyoni@gmail.com")) Log.d(tag,message);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        Log.d(tag,message);
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




    private void addTouchListenerForSwipeBack() {
        mSwipeBackDetector = new GestureDetector(this, new MySwipebackGestureListener());
        swipeBackView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (mSwipeBackDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isSideScrolling) {
                        Log.d("touchListener", " onTouch ACTION_UP");
                        isSideScrolling = false;

                        int RightScore = 0;
                        int LeftScore = 0;
                        if (SideSwipeRawList.size() > 15) {
                            for (int i = SideSwipeRawList.size() - 1; i > SideSwipeRawList.size() - 15; i--) {
                                int num1 = SideSwipeRawList.get(i);
                                int numBefore1 = SideSwipeRawList.get(i - 1);

                                if (numBefore1 > num1) LeftScore++;
                                else RightScore++;
                            }
                        } else {
                            for (int i = SideSwipeRawList.size() - 1; i > 0; i--) {
                                int num1 = SideSwipeRawList.get(i);
                                int numBefore1 = SideSwipeRawList.get(i - 1);

                                if (numBefore1 > num1) LeftScore++;
                                else RightScore++;
                            }
                        }
                        if (RightScore > LeftScore) {
//                        onBackPressed();
                        }
                        hideNupdateSideSwipeThing();
                        SideSwipeRawList.clear();
                    }
                    ;
                }

                return false;
            }
        });
    }

    class MySwipebackGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG", "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG", "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) swipeBackView.getLayoutParams();
            x_delta = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("TAG", "onSingleTapConfirmed: ");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("TAG", "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();
            if ((X - x_delta) < maxSideSwipeLength) {

            } else if ((X - x_delta) > maxSideSwipeLength) {

            } else {
            }
            showNupdateSideSwipeThing(X - x_delta);

            Log.d("TAG", "the e2.getAction()= " + e2.getAction() + " and the MotionEvent.ACTION_CANCEL= " + MotionEvent.ACTION_CANCEL);
            SideSwipeRawList.add(X);

            isSideScrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int RightScore = 0;
            int LeftScore = 0;
            if (SideSwipeRawList.size() > 15) {
                for (int i = SideSwipeRawList.size() - 1; i > SideSwipeRawList.size() - 15; i--) {
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            } else {
                for (int i = SideSwipeRawList.size() - 1; i > 0; i--) {
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            }
            if (RightScore > LeftScore) {
                onBackPressed();
            }
            SideSwipeRawList.clear();
            return false;

        }
    }

    private void showNupdateSideSwipeThing(int pos) {
        int trans = (int) ((pos - Utils.dpToPx(10)) * 0.9);
        if(isTelemetryCardMinimized){
            RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.setTranslationX(trans);

            View v = findViewById(R.id.swipeBackViewIndicator);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
            params.height = trans;
            v.setLayoutParams(params);

            LinearLayout scrollView = findViewById(R.id.mainViewLayout);
            scrollView.setTranslationX((int)(trans*0.05));
        }else{
            float s = (float)(((50-(trans*0.01))/50)*1f);
            telemetryLayout.setScaleX(s);
            telemetryLayout.setScaleY(s);
            telemetryLayout.setTranslationX((int)(trans*0.1));

            final RelativeLayout blackBack = findViewById(R.id.blackBack);
            final float alph = s-0.3f;
            blackBack.setAlpha(alph);
//            blackBack.animate().alpha(alph).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    blackBack.setAlpha(alph);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            })
//                    .setInterpolator(new LinearOutSlowInInterpolator()).start();
        }
    }

    private void hideNupdateSideSwipeThing() {
        int myDurat = 200;
        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.animate().setDuration(mAnimationTime).translationX(Utils.dpToPx(-40)).start();

        final View v = findViewById(R.id.swipeBackViewIndicator);
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();

        ValueAnimator animatorTop;
        animatorTop = ValueAnimator.ofInt(params.height, 0);
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                v.requestLayout();
            }
        });
        animatorTop.setDuration(myDurat).start();

        if(!isCardCollapsing){
            final LinearLayout mainViewLayout = findViewById(R.id.mainViewLayout);
            telemetryLayout.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0).scaleX(1f).scaleY(1f).start();
            mainViewLayout.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mainViewLayout.setTranslationX(0);
                            telemetryLayout.setScaleX(1f);
                            telemetryLayout.setScaleY(1f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();

            final RelativeLayout blackBack = findViewById(R.id.blackBack);
            final float alph = 0.8f;
            blackBack.setAlpha(alph);
            blackBack.animate().alpha(alph).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    blackBack.setAlpha(alph);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            })
                    .setInterpolator(new LinearOutSlowInInterpolator()).start();
        }


    }




    BroadcastReceiver mMessageReceiverForOpenAdvertTelemetries = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showSelectedAdTelemetries();
        }
    };



    @Override
    public void onBackPressed(){
        if(isExpandedImage){
            closeExpandedImage();
        }else{
            if (!isTelemetryCardMinimized) {
                hideSelectedAdTelemetries();
            } else {
                if(isShowingHidden){
                    hideHiddenAds();
                }else if(isShowingStarred){
                    hideStarred();
                }
                else{
                    super.onBackPressed();
                }
            }
        }

    }

    private void showSelectedAdTelemetries(){
        if(isTelemetryCardMinimized){
            expandCard();
            onTelemetryOpen();
            isTelemetryCardMinimized = false;
        }
    }

    private void hideSelectedAdTelemetries(){
        Advert ad = Variables.adToBeViewedInTelemetries;
        telemetryLayout.setTranslationX(0);
        if(!isTelemetryCardMinimized){
            collapseCard();
            isTelemetryCardMinimized = true;
            if(isToRemove)LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ad.getPushRefInAdminConsole()+"HIDE"));
        }
    }



    private void expandCard(){
        Advert ad = Variables.adToBeViewedInTelemetries;
        telemetryLayout.setVisibility(View.VISIBLE);
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) telemetryLayout.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(200,100,0);
        animatorLeft = ValueAnimator.ofInt(200,100,0);

        animatorTop = ValueAnimator.ofInt(500,300,0);
        animatorBot = ValueAnimator.ofInt(400,300,0);


        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime);
        animatorRight.setDuration(mAnimationTime);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        final float newAlpha = 1f;
        telemetryLayout.animate().alpha(newAlpha).setInterpolator(new LinearInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        telemetryLayout.setAlpha(newAlpha);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
//                onTelemetryOpen();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


        final float telemLayout = 1f;
        normalTelemetryLayout.setVisibility(View.VISIBLE);
        normalTelemetryLayout.animate().alpha(telemLayout).setInterpolator(new FastOutLinearInInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        normalTelemetryLayout.setAlpha(telemLayout);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        statTelemetryIcon.setVisibility(View.GONE);

        if(ad.getImageBitmap()!=null){
            takeDownView.setTranslationX(-Utils.dpToPx(10));
            takeDownView.animate().translationX(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            takeDownView.setTranslationX(0);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
        }

        final RelativeLayout blackBack = findViewById(R.id.blackBack);
        blackBack.setVisibility(View.VISIBLE);
        final float alph = 0.8f;
        blackBack.animate().alpha(alph).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                blackBack.setVisibility(View.VISIBLE);
                blackBack.setAlpha(alph);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        })
                .setInterpolator(new LinearOutSlowInInterpolator()).start();

    }

    private void collapseCard(){
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) telemetryLayout.getLayoutParams();
        isCardCollapsing = true;
        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(0,220,220);
        animatorLeft = ValueAnimator.ofInt(0,280,280);
        animatorTop = ValueAnimator.ofInt(0,450,800);
        animatorBot = ValueAnimator.ofInt(0,450,100);

        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                telemetryLayout.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime+100);
        animatorRight.setDuration(mAnimationTime+100);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        final float newAlpha = 0f;
        telemetryLayout.animate().alpha(newAlpha).setInterpolator(new FastOutLinearInInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        telemetryLayout.setAlpha(newAlpha);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();


        final float telemLayout = 0f;
        normalTelemetryLayout.setVisibility(View.GONE);
        normalTelemetryLayout.animate().alpha(telemLayout).setInterpolator(new FastOutLinearInInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        normalTelemetryLayout.setVisibility(View.GONE);
                        normalTelemetryLayout.setAlpha(telemLayout);
                        isCardCollapsing = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        statTelemetryIcon.setVisibility(View.VISIBLE);

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onTelemetryClose();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        final RelativeLayout blackBack = findViewById(R.id.blackBack);
        final float alph = 0f;
        blackBack.animate().alpha(alph).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                blackBack.setVisibility(View.GONE);
                blackBack.setAlpha(alph);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        })
                .setInterpolator(new LinearOutSlowInInterpolator()).start();

    }

    private void onTelemetryOpen(){
        canSetTime = true;
        setAdImage();
        setTelemetryData();
        setTelemetryEventListeners();
        setTelemetryClickListeners();

        isEditingPhoneNo = false;
        isEditingWebsiteUrl = false;
    }

    private void onTelemetryClose() {
        adImageBackground.setImageBitmap(null);
        adImage.setImageBitmap(null);

        if(dbRef!=null)dbRef.removeEventListener(telemetryEventListener);
        if(dbExpressionRef!=null)dbExpressionRef.removeEventListener(expressionTelemetryListener);
        if(dbClickRef!=null)dbClickRef.removeEventListener(clickTelemetryListener);
        if(dbPinRef!=null)dbPinRef.removeEventListener(pinTelemetryListener);

        telemetryLayout.setScaleY(1f);
        telemetryLayout.setScaleX(1f);
        canSetTime = false;
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("COLLAPSED_CARD"));
    }





    private void setTelemetryEventListeners() {
        telemetryEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals("starred")){
                    Variables.adToBeViewedInTelemetries.setStarred(dataSnapshot.getValue(Boolean.class));
                }else
                if(dataSnapshot.getKey().equals("advertiserHidden")){
                    Variables.adToBeViewedInTelemetries.setAdvertiserHidden(dataSnapshot.getValue(Boolean.class));
                }else
                if(dataSnapshot.getKey().equals("flagged")){
                    Variables.adToBeViewedInTelemetries.setFlagged(dataSnapshot.getValue(Boolean.class));
                }else
                    if(dataSnapshot.getKey().equals("websiteLink")){
                        Variables.adToBeViewedInTelemetries.setWebsiteLink(dataSnapshot.getValue(String.class));
                }else
                    if(dataSnapshot.getKey().equals("advertiserPhoneNo")){
                        Variables.adToBeViewedInTelemetries.setAdvertiserPhoneNo(dataSnapshot.getValue(String.class));
                }else
                    if(dataSnapshot.getKey().equals(Constants.USERS_THAT_HAVE_CLICKED_IT)){

                }else
                    if(dataSnapshot.getKey().equals(Constants.USERS_THAT_HAVE_SEEN)){

                }else
                    if(dataSnapshot.getKey().equals("numberOfPins")){
                        Variables.adToBeViewedInTelemetries.setNumberOfPins(dataSnapshot.getValue(Integer.class));
                }else
                    if(dataSnapshot.getKey().equals("webClickNumber")){
                        Variables.adToBeViewedInTelemetries.setWebClickNumber(dataSnapshot.getValue(Integer.class));
                }else
                    if(dataSnapshot.getKey().equals("payoutReimbursalAmount")){
                        Variables.adToBeViewedInTelemetries.setPayoutReimbursalAmount(dataSnapshot.getValue(double.class));
                }else{
                    if (dataSnapshot.getKey().equals("numberOfTimesSeen")) {
                        try {
                            int newValue = dataSnapshot.getValue(int.class);
                            Log(TAG, "New value gotten from firebase --" + newValue);
                            Variables.adToBeViewedInTelemetries.setNumberOfTimesSeen(newValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dataSnapshot.getKey().equals("hasBeenReimbursed")) {
                        try {
                            boolean newValue = dataSnapshot.getValue(boolean.class);
                            Log(TAG, "New value gotten from firebase --" + newValue);
                            Variables.adToBeViewedInTelemetries.setHasBeenReimbursed(newValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                setTelemetryData();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        expressionTelemetryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ExpressionData exp = dataSnapshot.getValue(ExpressionData.class);
                Variables.adToBeViewedInTelemetries.expressions.add(exp);
                setTelemetryData();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        clickTelemetryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                WebClickData wb = dataSnapshot.getValue(WebClickData.class);
                if(!Variables.adToBeViewedInTelemetries.webclicks.contains(wb))Variables.adToBeViewedInTelemetries.webclicks.add(wb);
                setTelemetryData();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        pinTelemetryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                AdPinData adPin = dataSnapshot.getValue(AdPinData.class);
                if(!Variables.adToBeViewedInTelemetries.adPins.contains(adPin))Variables.adToBeViewedInTelemetries.adPins.add(adPin);
                setTelemetryData();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        Advert ad = Variables.adToBeViewedInTelemetries;
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            String tme = TimeManager.getNextDay();
            dbRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(ad.getPushRefInAdminConsole());

            dbExpressionRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_SEEN);

            dbClickRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_CLICKED_IT);

        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            String tme = TimeManager.getDate();
            dbRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole());

            dbExpressionRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_SEEN);

            dbClickRef =  FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_CLICKED_IT);

        }else if(ad.getAdType().equals(Constants.YESTERDAYS_ADS)){
            String tme = TimeManager.getPreviousDay();
            dbRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole());

            dbExpressionRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_SEEN);

            dbClickRef =  FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(tme).child(Variables.adToBeViewedInTelemetries.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_CLICKED_IT);

        }else{
            //history uploads
            dbRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                    .child(Long.toString(-(ad.getDateInDays()+1))).child(ad.getPushRefInAdminConsole());

            dbExpressionRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                    .child(Long.toString(-(ad.getDateInDays()+1))).child(ad.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_SEEN);

            dbClickRef =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                    .child(Long.toString(-(ad.getDateInDays()+1))).child(ad.getPushRefInAdminConsole())
                    .child(Constants.USERS_THAT_HAVE_CLICKED_IT);
        }
        dbPinRef = FirebaseDatabase.getInstance().getReference(Constants.AD_PINS)
                .child(ad.getPushRefInAdminConsole());

        dbRef.addChildEventListener(telemetryEventListener);
        dbExpressionRef.addChildEventListener(expressionTelemetryListener);
        dbClickRef.addChildEventListener(clickTelemetryListener);
        dbPinRef.addChildEventListener(pinTelemetryListener);
    }

    private void setTelemetryClickListeners() {
        final Advert ad = Variables.adToBeViewedInTelemetries;

//        telemetryBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        reimburseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ad.isHasBeenReimbursed() && getReimbursalAmount() !=0 &&
                        !ad.getAdType().equals(Constants.TOMORROWS_ADS) && !ad.getAdType().equals(Constants.TODAYS_ADS)){
                    startReimbursal();
                }
            }
        });

        takeDownCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ad.getAdType().equals(Constants.TOMORROWS_ADS)
                        ||ad.getAdType().equals(Constants.TODAYS_ADS)){
                    if(!ad.isAdminFlagged()) {
                        startTakeDown();
                    }else{
                        showPromptForTakenDown();
                    }
                }

            }
        });

        editWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditingPhoneNo && !isEditingWebsiteUrl){
                    startEditWebsiteUrl();
                }
            }
        });

        editPhoneNoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditingPhoneNo && !isEditingWebsiteUrl){
                    startEditPhoneNo();
                }
            }
        });

        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areYouSureHide();
            }
        });

        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areYouSureStar();
            }
        });

        adImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExpandedImage();
            }
        });

    }

    private void setTelemetryData() {
        Advert ad = Variables.adToBeViewedInTelemetries;

        viewingDateText.setText(String.format("Uploaded on: %s", getDateFromDays(ad.getDateInDays())));
        userSeenNumber.setText(Integer.toString(ad.getNumberOfTimesSeen()));

        uploaderText.setText(String.format("Uploaded by : %s", ad.getUserEmail()));
        if(ad.isFlagged()){
            statusText.setText("Status: Taken Down.");
            targetNumber.setText("N/A.");

            TextView t = findViewById(R.id.takeDownText);
            t.setText("PUT UP.");
        }else{
            statusText.setText("Status: Online.");

            targetNumber.setText(Integer.toString(ad.getNumberOfUsersToReach()));

            TextView t = findViewById(R.id.takeDownText);
            t.setText("TAKE DOWN.");
        }
        setTotalAmounts();

        if(ad.isHasBeenReimbursed()){
            if(!ad.getAdType().equals(Constants.TOMORROWS_ADS) && !ad.getAdType().equals(Constants.TODAYS_ADS)){
                statusText.setText("Status: Reimbursed.");
            }else{
                statusText.setText("Status: NOT Reimbursed.");
            }
//            reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.grey));
        }else{
//            reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if(!ad.getAdType().equals(Constants.TOMORROWS_ADS) && !ad.getAdType().equals(Constants.TODAYS_ADS)){
            if(!ad.isHasBeenReimbursed()){
                reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }else{
                reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.grey));
                reimburseCard.setOnClickListener(null);
            }
        }else{
            reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.grey));
            reimburseCard.setOnClickListener(null);
        }

        if(!ad.getWebsiteLink().equals("none")){
            websiteText.setText(ad.getWebsiteLink());
        }else{
            websiteText.setText("N/A.");
        }

        if(!ad.getAdvertiserPhoneNo().equals("")&&!ad.getAdvertiserPhoneNo().equals("none")){
            phoneNoText.setText(ad.getAdvertiserPhoneNo());
        }else{
            phoneNoText.setText("N/A.");
        }

        if(ad.getAdvertiserLocations().isEmpty()){
            contactLocationText.setText("N/A.");
        }else{
            contactLocationText.setText(ad.getAdvertiserLocations().size()+" Locations.");
        }

        if(ad.adPins.size()==1){
            numberOfPinsText.setText(ad.adPins.size()+" Pin.");
        }else numberOfPinsText.setText(ad.adPins.size()+" Pins.");

        categoryText.setText(ad.getCategory());



        if((!ad.getAdType().equals(Constants.TOMORROWS_ADS) && !ad.getAdType().equals(Constants.TODAYS_ADS)) && !ad.isHasBeenReimbursed()){
            reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else{
            reimburseCard.setCardBackgroundColor(getResources().getColor(R.color.accent));
        }

        if(ad.getAdType().equals(Constants.TOMORROWS_ADS) || ad.getAdType().equals(Constants.TODAYS_ADS)){
            takeDownCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else{
            takeDownCard.setCardBackgroundColor(getResources().getColor(R.color.accent));
        }


        setExpressionGraph();
        setWebClickGraph();
        setTime();

        if(ad.isStarred()){
            ImageButton starButton = findViewById(R.id.starButton);
            starButton.setBackground(null);
            starButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_starred));
            TextView starredText = findViewById(R.id.starredText);
            starredText.setText("Starred.");
        }else{
            ImageButton starButton = findViewById(R.id.starButton);
            starButton.setBackground(null);
            starButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unstarred));
            TextView starredText = findViewById(R.id.starredText);
            starredText.setText("Star");
        }

        LinearLayout hideLayout = findViewById(R.id.hideLayout);

        if(ad.getAdType().equals(Constants.OLDER_UPLOADS)){
            hideLayout.setVisibility(View.VISIBLE);
            if(ad.isAdvertiserHidden()){
                ImageButton hideButton = findViewById(R.id.hideButton);
                hideButton.setBackground(null);
                hideButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unhide));
                TextView starredText = findViewById(R.id.hideText);
                starredText.setText("Unhide.");
            }else{
                ImageButton hideButton = findViewById(R.id.hideButton);
                hideButton.setBackground(null);
                hideButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_hide));
                TextView starredText = findViewById(R.id.hideText);
                starredText.setText("Hide");
            }
        }else{
            hideLayout.setVisibility(View.GONE);
        }

    }

    private boolean canSetTime = false;
    private void setTime() {
        TextView timeText = findViewById(R.id.timeText);
        int hoursLeft = 23-TimeManager.getHour();
        String hour = TimeManager.getHour()+"";
        if(TimeManager.getHour()<10){
            hour = "0"+TimeManager.getHour();
        }
        String minute = TimeManager.getCal().get(Calendar.MINUTE)+"";
        if(TimeManager.getCal().get(Calendar.MINUTE)<10){
            minute = "0"+TimeManager.getCal().get(Calendar.MINUTE);
        }
        String timeOfDay = hour+":"+minute;

        if(Variables.adToBeViewedInTelemetries.getAdType().equals(Constants.TODAYS_ADS)){
            if(hoursLeft==0){
                timeText.setText(String.format("%s (less than 1 Hour Left).", timeOfDay));
            }else{
                if(hoursLeft<2){
                    timeText.setText(String.format("%s (about %d Hour Left).", timeOfDay, hoursLeft));
                }else{
                    timeText.setText(String.format("%s (about %d Hours Left).", timeOfDay, hoursLeft));
                }
            }

        }else{
            timeText.setText(String.format("%s", timeOfDay));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(canSetTime) setTime();
            }
        },10000);
    }

    private void setExpressionGraph(){
        List<Integer> times = new ArrayList<>();
//        times.add(TimeManager.getCal().get(Calendar.HOUR_OF_DAY));
        if(Variables.adToBeViewedInTelemetries.getAdType().equals(Constants.TODAYS_ADS)){
            for(int i=TimeManager.getHour()-5;i<=TimeManager.getHour();i++){
                if(i>0){
                    times.add(i);
                }
            }
        }else{
            for(int i=0;i<=23;i++){
                if(i%3==0) times.add(i);
            }
        }
        List<Entry> entries = new ArrayList<>();
        int highest = 3;
        for (int data : times) {
            entries.add(new Entry(data, getUsersWhoSawAt(data)));
            if(getUsersWhoSawAt(data)>highest)highest = getUsersWhoSawAt(data);
        }
        LineDataSet dataSet;
        if(Variables.adToBeViewedInTelemetries.getAdType().equals(Constants.TODAYS_ADS)){
            dataSet = new LineDataSet(entries, "Average Views in the ad's last 4 hours.");
        }else{
            dataSet = new LineDataSet(entries, "Average Views in the ad's life cycle.");
        }
        dataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryLight));

        LineData lineData = new LineData(dataSet);
        XAxis xAxis = expressionGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(times.size(), true);

        expressionGraph.getAxisRight().setEnabled(false);
        YAxis yAxis = expressionGraph.getAxisLeft();
        yAxis.setAxisMinimum(-0.1f);
        yAxis.setLabelCount(highest+1, true);

        expressionGraph.setDrawMarkers(false);
        Description desc = new Description();
        desc.setText("");
        expressionGraph.setDescription(desc);
        expressionGraph.setAutoScaleMinMaxEnabled(false);

        expressionGraph.setData(lineData);
        expressionGraph.invalidate();
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private List<Integer> mValues;

        public MyXAxisValueFormatter(List<Integer> values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Log.e(TAG,"val: "+value);
            if(value<mValues.size()){
                return mValues.get((int) value).toString();
            } else{
                return ((mValues.size()+(int)value)+"");
            }
        }
    }

    private int getUsersWhoSawAt(int hour){
        int number = 0;
        List<String> usersCounted = new ArrayList<>();
        for(ExpressionData data:Variables.adToBeViewedInTelemetries.expressions){
            if(data.getViewingTime().getHour()==hour){
                if(!usersCounted.contains(data.getViewerUid())){
                    number++;
                    Log.e(TAG,"Found user who saw at time: "+data.getViewerUid());
                    usersCounted.add(data.getViewerUid());
                }
            }
        }
        return number;
    }



    private void setWebClickGraph(){
        List<Integer> times = new ArrayList<>();
        if(Variables.adToBeViewedInTelemetries.getAdType().equals(Constants.TODAYS_ADS)){
            for(int i=TimeManager.getHour()-5;i<=TimeManager.getHour();i++){
                if(i>0){
                    times.add(i);
                }
            }
        }else{
            for(int i=0;i<=23;i++){
                if(i%3==0) times.add(i);
            }
        }
        List<Entry> entries = new ArrayList<>();
        int highest = 3;
        for (int data : times) {
            entries.add(new Entry(data, getUsersWhoVisitedAt(data)));
            if(getUsersWhoVisitedAt(data)>highest)highest = getUsersWhoVisitedAt(data);
        }
        LineDataSet dataSet;
        if(Variables.adToBeViewedInTelemetries.getAdType().equals(Constants.TODAYS_ADS)){
            dataSet = new LineDataSet(entries, "Average website visits for the ad's last 4 hours.");
        }else {
            dataSet = new LineDataSet(entries, "Average website visits for the ad's life cycle.");
        }
        dataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryLight));

        LineData lineData = new LineData(dataSet);
        XAxis xAxis = webpageChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(times.size(), true);

        webpageChart.getAxisRight().setEnabled(false);
        YAxis yAxis = webpageChart.getAxisLeft();
        yAxis.setAxisMinimum(-0.1f);
        yAxis.setLabelCount(highest+1, true);

        webpageChart.setDrawMarkers(false);
        Description desc = new Description();
        desc.setText("");
        webpageChart.setDescription(desc);

        webpageChart.setAutoScaleMinMaxEnabled(false);

        webpageChart.setData(lineData);
        webpageChart.invalidate();

        webpageVisitCount.setText(Variables.adToBeViewedInTelemetries.webclicks.size()+" Webpage Visits");
    }

    private int getUsersWhoVisitedAt(int hour){
        int number = 0;
        List<String> usersCounted = new ArrayList<>();
        for(WebClickData data:Variables.adToBeViewedInTelemetries.webclicks){
            if(data.getWebClickTime().getHour()==hour){
                if(!usersCounted.contains(data.getWebClickUserUid())) {
                    number++;
                    usersCounted.add(data.getWebClickUserUid());
                }
            }
        }
        return number;
    }



    private void setTotalAmounts(){
        Advert ad = Variables.adToBeViewedInTelemetries;

        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd* (ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
        double vat = (numberOfUsersWhoDidntSeeAd*(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                *Constants.VAT_CONSTANT;
        double incentiveAmm = 0;
        if(ad.didAdvertiserAddIncentive()){
            incentiveAmm = (ad.getWebClickIncentive()* (ad.getNumberOfUsersToReach()-ad.getWebClickNumber()));
        }
        double totalReimbursalPlusPayout = (double)ammountToBeRepaid+ad.getPayoutReimbursalAmount()+vat+incentiveAmm;
        String number = Double.toString(round(totalReimbursalPlusPayout));

        if(ad.isHasBeenReimbursed()){
            reimbursalAmountNumber.setText("0 Ksh");
        }else{
            reimbursalAmountNumber.setText(number+" Ksh");
        }

        int totalAmmPaid = ad.getNumberOfUsersToReach()* (ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
        double vatTotal = (ad.getNumberOfUsersToReach()*(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                *Constants.VAT_CONSTANT;
        double incentiveAmmTotal = 0;
        if(ad.didAdvertiserAddIncentive()){
            incentiveAmmTotal = (ad.getWebClickIncentive()* (ad.getNumberOfUsersToReach()));
        }
        double totalReimbursalPlusPayoutTotal = (double)totalAmmPaid+ad.getPayoutReimbursalAmount()+vatTotal+incentiveAmmTotal;
        String numberTotal = Double.toString(round(totalReimbursalPlusPayoutTotal));

        amountPaidNumber.setText(numberTotal+" Ksh");

        if(ad.getWebClickIncentive()!=0){
            webclickIncentiveText.setText(incentiveAmmTotal+" Ksh ( "+ad.getNumberOfUsersToReach()+" * "+ad.getWebClickIncentive()+"Ksh ).");
        }else{
            webclickIncentiveText.setText("N/A.");
        }

    }


    private void setAdImage() {
        Advert ad = Variables.adToBeViewedInTelemetries;
        if(ad.getImageBitmap()==null){
            int pos = -1;
            for(int i=0 ; i<myHistoryAdImages.size(); i++){
                if(myHistoryAdImages.get(i).getAdId().equals(ad.getPushRefInAdminConsole())){
                    pos=i;
                    break;
                }
            }
            if(pos != -1){
                Bitmap bs = myHistoryAdImages.get(pos).getImageBitmap();
                ad.setImageBitmap(bs);
            }
        }
        adImage.setImageBitmap(ad.getImageBitmap());
        adImage2.setImageBitmap(ad.getImageBitmap());

        bm = ad.getImageBitmap();
        adImageBackground.setImageBitmap(null);
        adImageBackground2.setImageBitmap(null);
        backBl = null;

        loadingProgressBar.setVisibility(View.VISIBLE);
        LongOperationFI op = new LongOperationFI();
        op.execute("");
    }

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                backBl = fastblur(Variables.adToBeViewedInTelemetries.getImageBitmap(),0.7f,27);
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(backBl!=null){
                adImageBackground.setImageBitmap(backBl);
                adImageBackground2.setImageBitmap(backBl);
                RelativeLayout imageLayout = findViewById(R.id.imageLayout);

                imageLayout.setVisibility(View.VISIBLE);
                adImageBackground.setVisibility(View.VISIBLE);
            }else{
                RelativeLayout imageLayout = findViewById(R.id.imageLayout);
                imageLayout.setVisibility(View.GONE);

                takeDownView.setTranslationX(Utils.dpToPx(100));
                takeDownView.animate().translationX(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                takeDownView.setTranslationX(0);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
            }
            loadingProgressBar.setVisibility(View.GONE);
        }

    }

    private Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }




    private Double getReimbursalAmount(){
        Advert ad = Variables.adToBeViewedInTelemetries;

        int numberOfUsersWhoDidntSeeAd = ad.getNumberOfUsersToReach()- ad.getNumberOfTimesSeen();
        int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd* (ad.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
        double vat = (numberOfUsersWhoDidntSeeAd*(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                *Constants.VAT_CONSTANT;
        double incentiveAmm = 0;
        if(ad.didAdvertiserAddIncentive()){
            incentiveAmm = (ad.getWebClickIncentive()* (ad.getNumberOfUsersToReach()-ad.getWebClickNumber()));
        }
        double totalReimbursalPlusPayout = (double)ammountToBeRepaid+ad.getPayoutReimbursalAmount()+vat+incentiveAmm;

        return round(totalReimbursalPlusPayout);

    }


    private void areYouSureHide(){
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_hide);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button b1 = d.findViewById(R.id.okBtn);
        TextView annTitle = d.findViewById(R.id.annTitle);
        TextView annText = d.findViewById(R.id.annText);
        if(Variables.adToBeViewedInTelemetries.isAdvertiserHidden()){
            annTitle.setText("Un-Hide this?");
            annText.setText(R.string.areYouSureUnHide);
        }else{
            annTitle.setText("Hide this?");
            annText.setText(R.string.areYouSureHide);
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                setHidden();
            }
        });
        d.show();
    }

    private void areYouSureStar(){
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_star);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button b1 = d.findViewById(R.id.okBtn);
        TextView annTitle = d.findViewById(R.id.annTitle);
        TextView annText = d.findViewById(R.id.annText);
        if(!Variables.adToBeViewedInTelemetries.isStarred()){
            annTitle.setText("Star this?");
            annText.setText(R.string.areYouSureStar);
        }else{
            annTitle.setText("Un-star this?");
            annText.setText(R.string.areYouSureUnStar);
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                setStarred();
            }
        });
        d.show();
    }

    private void setStarred() {
        Advert ad = Variables.adToBeViewedInTelemetries;
        if(ad.isStarred()){
            Toast.makeText(mContext,"Removing Star",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext,"Starring",Toast.LENGTH_SHORT).show();
        }
        boolean newStarred = false;
        if(!ad.isStarred()) newStarred = true;

        if(!ad.getAdType().equals(Constants.OLDER_UPLOADS)) {
            String date = getDate();
            if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
                date = getNextDay();
            }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
                date = getDate();
            }

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(date)
                    .child(ad.getPushRefInAdminConsole())
                    .child("starred");
            mRef.setValue(newStarred);

            String dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
            if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
                dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
            }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
                dateChild = TimeManager.getYear()+TimeManager.getMonth()+TimeManager.getDay();
            }

            DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                    .child(dateChild)
                    .child(ad.getPushRefInAdminConsole()).child("starred");
            mRef3.setValue(newStarred);
        }

        long dateInDays = ad.getDateInDays()+1;
        if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateInDays = TimeManager.getDateInDays();
        }
        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(dateInDays)))
                .child(ad.getPushRefInAdminConsole()).child("starred");
        mRef2.setValue(newStarred);

        Log(TAG,"Changing starred for ad : "+ad.getPushRefInAdminConsole());
        ad.setStarred(newStarred);
        setTelemetryData();

        if(newStarred){
            starredPlaceHolderView.addView(new OlderAdsItem(mContext,starredPlaceHolderView,ad,starredPlaceHolderView.getChildCount()));
        }else{
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ad.getPushRefInAdminConsole()+"STARRED"));
        }
    }

    private void setHidden(){
        Advert ad = Variables.adToBeViewedInTelemetries;
        if(ad.isAdvertiserHidden()){
            Toast.makeText(mContext,"Un-Hiding.",Toast.LENGTH_SHORT).show();
            hiddenNumber--;
        }else{
            Toast.makeText(mContext,"Hiding.",Toast.LENGTH_SHORT).show();
            hiddenNumber++;
        }
        boolean newHidden = !ad.isAdvertiserHidden();

        long dateInDays = ad.getDateInDays()+1;
        if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateInDays = TimeManager.getDateInDays();
        }
        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(dateInDays)))
                .child(ad.getPushRefInAdminConsole()).child("advertiserHidden");
        mRef2.setValue(newHidden);

        Log(TAG,"Changing hidden for ad : "+ad.getPushRefInAdminConsole());

        ad.setAdvertiserHidden(newHidden);
//        if(newHidden) isToRemove = true;

        if(newHidden){
//            if(hiddenPlaceHolderViewInfo.getChildCount()==0){
//                hiddenPlaceHolderViewInfo.addView(0,new DateForAdStats(mContext,"Your Hidden Ads.",hiddenPlaceHolderViewInfo));
//                hiddenPlaceHolderViewInfo.addView(1,new DateForAdStats(mContext," ",hiddenPlaceHolderViewInfo));
//            }
            hiddenAdIds.add(ad.getPushRefInAdminConsole());
            hiddenPlaceHolderViewInfo.addView(new OlderAdsItem(mContext,hiddenPlaceHolderViewInfo,ad,hiddenAdIds.indexOf(ad.getPushRefInAdminConsole())));
//            DataListsView.removeView(historyAdIds.indexOf(ad.getPushRefInAdminConsole()));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ad.getPushRefInAdminConsole()+"MAIN-HIDE"));
            if(historyAdIds.size()==0){
                DataListsView.removeAllViews();
            }
        }else{
            if(DataListsView.getChildCount()==0){
                DataListsView.addView(0,new DateForAdStats(mContext,"Your Upload History.",DataListsView));
                DataListsView.addView(1,new DateForAdStats(mContext," ",DataListsView));
            }
//            hiddenPlaceHolderViewInfo.removeView(hiddenAdIds.indexOf(ad.getPushRefInAdminConsole()));
            hiddenAdIds.remove(ad.getPushRefInAdminConsole());
            DataListsView.addView(historyAdIds.indexOf(ad.getPushRefInAdminConsole()),
                    new OlderAdsItem(mContext,DataListsView,ad,historyAdIds.indexOf(ad.getPushRefInAdminConsole())));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ad.getPushRefInAdminConsole()+"HIDDEN-HIDE"));
            if(hiddenAdIds.size()==0){
                hiddenPlaceHolderViewInfo.removeAllViews();
            }
        }
        setTelemetryData();

    }



    private void startEditPhoneNo() {
        isEditingPhoneNo = true;
        final Advert ad = Variables.adToBeViewedInTelemetries;

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_change_phone_no);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button b1 = d.findViewById(R.id.okBtn);
        Button RemoveBtn = d.findViewById(R.id.RemoveBtn);
        final EditText phoneE = d.findViewById(R.id.phoneNoEditTextt);
        if(!ad.getAdvertiserPhoneNo().equals("") && !ad.getAdvertiserPhoneNo().equals("none")) phoneE.setText(ad.getAdvertiserPhoneNo());

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String no = phoneE.getText().toString().trim();
                if(no.equals("")){
                    phoneE.setError("Enter your new phone number");
                }else if(phoneE.getText().toString().trim().length()<10){
                    phoneE.setError("That's not a real phone number");
                }else{
                    try{
                        int phoneNo = Integer.parseInt(no);
                        d.dismiss();
                        setNewPhoneNo(Integer.toString(phoneNo));
                        phoneE.setText("");
                    }catch (Exception e){
                        e.printStackTrace();
                        phoneE.setError("That's not a real number");
                    }
                }
            }
        });
        RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                setNewPhoneNo("");
                phoneE.setText("");
            }
        });

        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isEditingPhoneNo = false;
            }
        });
        d.show();
    }

    private void setNewPhoneNo(String phoneNo) {
        isEditingPhoneNo = false;
        mAuthProgressDialog.show();
        if(phoneNo.equals("")){
            phoneNo = "none";
        }
        Advert ad = Variables.adToBeViewedInTelemetries;
        if(phoneNo.equals("none"))mAuthProgressDialog.setMessage("Removing your phone number...");
        else mAuthProgressDialog.setMessage("Updating your phone number...");

        String date = getDate();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(date)
                .child(ad.getPushRefInAdminConsole())
                .child("advertiserPhoneNo");
        mRef.setValue(phoneNo);


        String dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateChild = TimeManager.getYear()+TimeManager.getMonth()+TimeManager.getDay();
        }


        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(dateChild)
                .child(ad.getPushRefInAdminConsole()).child("advertiserPhoneNo");
        mRef3.setValue(phoneNo);


        long dateInDays = ad.getDateInDays()+1;

        if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateInDays = TimeManager.getDateInDays();
        }

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(dateInDays)))
                .child(ad.getPushRefInAdminConsole()).child("advertiserPhoneNo");
        mRef2.setValue(phoneNo);

        Log(TAG,"Changing phone number for ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        if(numberOfClusters!=0){
            int nextCluster = getClusterValue(runCount,ad);
            int nextPushId = getPushIdValue(runCount,ad);
            updateSpecificPhoneNoData(nextCluster,nextPushId,ad,phoneNo);
        }else{
            mAuthProgressDialog.dismiss();
        }

    }

    public void updateSpecificPhoneNoData(int cluster,int pushId,final Advert ad, final String phoneNo){
        String date = getDate();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(date)
                .child(Integer.toString(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                .child(ad.getCategory())
                .child(Integer.toString(cluster))
                .child(Integer.toString(pushId))
                .child("contactdata").child(Constants.ADVERTISER_PHONE_NO);
        mRef3.setValue(phoneNo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runCount++;
                if(runCount<numberOfClusters){
                    int nextCluster = getClusterValue(runCount,ad);
                    int nextPushId = getPushIdValue(runCount,ad);
                    updateSpecificPhoneNoData(nextCluster,nextPushId,ad,phoneNo);
                }else{
                    runCount = 0;
                    numberOfClusters = 0;
                    mAuthProgressDialog.dismiss();
                }
            }
        });
    }



    private void startEditWebsiteUrl() {
        isEditingWebsiteUrl = true;
        final Advert ad = Variables.adToBeViewedInTelemetries;

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_change_website);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button b1 = d.findViewById(R.id.okBtn);
        Button RemoveBtn = d.findViewById(R.id.RemoveBtn);
        final EditText webisteE = d.findViewById(R.id.websiteEditTextt);
        if(!ad.getWebsiteLink().equals("none")) webisteE.setText(ad.getWebsiteLink());

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String web = webisteE.getText().toString().trim();
                if(web.equals("")){
                    web = "none";
                }
                d.dismiss();
                setNewWebsite(web);
                webisteE.setText("");
            }
        });

        RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                setNewWebsite("");
                webisteE.setText("");
            }
        });

        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isEditingWebsiteUrl = false;
            }
        });
        d.show();

    }

    private void setNewWebsite(String web) {
        isEditingWebsiteUrl = false;

        if(web.equals("")){
            web = "none";
        }
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeViewedInTelemetries;
        if(web.equals("none"))mAuthProgressDialog.setMessage("Removing your website url...");
        else mAuthProgressDialog.setMessage("Updating your website url...");

        String date = getDate();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(date)
                .child(ad.getPushRefInAdminConsole())
                .child("websiteLink");
        mRef.setValue(web);


        String dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            dateChild = TimeManager.getNextDayYear()+TimeManager.getNextDayMonth()+TimeManager.getNextDayDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateChild = TimeManager.getYear()+TimeManager.getMonth()+TimeManager.getDay();
        }


        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(dateChild)
                .child(ad.getPushRefInAdminConsole()).child("websiteLink");
        mRef3.setValue(web);


        long dateInDays = ad.getDateInDays()+1;

        if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            dateInDays = TimeManager.getDateInDays();
        }

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(dateInDays)))
                .child(ad.getPushRefInAdminConsole()).child("websiteLink");
        mRef2.setValue(web);

        Log(TAG,"Changing webUrl for ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        if(numberOfClusters==0){
            mAuthProgressDialog.dismiss();
        }else{
            int nextCluster = getClusterValue(runCount,ad);
            int nextPushId = getPushIdValue(runCount,ad);
            updateSpecificWebsiteData(nextCluster,nextPushId,ad,web);
        }


    }

    public void updateSpecificWebsiteData(int cluster,int pushId,final Advert ad, final String url){
        String date = getDate();
        if(ad.getAdType().equals(Constants.TOMORROWS_ADS)){
            date = getNextDay();
        }else if(ad.getAdType().equals(Constants.TODAYS_ADS)){
            date = getDate();
        }

        DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(date)
                .child(Integer.toString(Variables.getUserCpvFromTotalPayPerUser(ad.getAmountToPayPerTargetedView())))
                .child(ad.getCategory())
                .child(Integer.toString(cluster))
                .child(Integer.toString(pushId))
                .child("websiteLink");
        mRef3.setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runCount++;
                if(runCount<numberOfClusters){
                    int nextCluster = getClusterValue(runCount,ad);
                    int nextPushId = getPushIdValue(runCount,ad);
                    updateSpecificWebsiteData(nextCluster,nextPushId,ad,url);
                }else{
                    runCount = 0;
                    numberOfClusters = 0;
                    mAuthProgressDialog.dismiss();
                }
            }
        });
    }



    private void startTakeDown() {
        final Advert ad = Variables.adToBeViewedInTelemetries;
        Variables.adToBeFlagged = ad;

        if (!ad.isFlagged()) {
            Variables.areYouSureTakeDownText = "Are you sure you want to take down your ad?";
        } else {
            Variables.areYouSureTakeDownText = "Are you sure you want to put back up your ad?";
        }
        showConfirmSubscribeMessage();
    }

    private void startReimbursal() {
        Variables.adToBeReimbursed = Variables.adToBeViewedInTelemetries;
        if(Variables.adToBeReimbursed.getAdType().equals(Constants.OLDER_UPLOADS))Variables.isOlderAd = true;

        new DatabaseManager().setIsMakingPayoutInFirebase(true);
        showBottomSheetForReimbursement();
    }




    private String getDateFromDays(long days){
        long currentTimeInMills = days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log("Splash","Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log("My_ad_stat_item","Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log("My_ad_stat_item","Ad was pined last year...");
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

    private boolean isCardForYesterdayAds(){
        return Variables.adToBeViewedInTelemetries.getDateInDays()+1 < getDateInDays();
    }



    private void openMore(){
        isOptionsShowing = true;
        moreLayout.setVisibility(View.VISIBLE);
        moreLayout.animate().translationY(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(150)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        moreLayout.setTranslationY(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        if(isShowingHidden){
            ImageButton unhideOptionImage = findViewById(R.id.unhideOptionImage);
            unhideOptionImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unhide));
            unhideOptionImage.setBackground(null);
            TextView unhideOptionText = findViewById(R.id.unhideOptionText);
            unhideOptionText.setText("Show Regular Uploads.");
        }else{
            ImageButton unhideOptionImage = findViewById(R.id.unhideOptionImage);
            unhideOptionImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unhide));
            unhideOptionImage.setBackground(null);
            TextView unhideOptionText = findViewById(R.id.unhideOptionText);
            unhideOptionText.setText("Show Hidden.");
        }

        if(isShowingStarred){
            showStarredImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unstarred));
            showStarredImage.setBackground(null);
        }else{
            showStarredImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_starred));
            showStarredImage.setBackground(null);
        }

        showHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!isShowingHidden){
                   showHiddenAds();
               } else {
                   hideHiddenAds();
               }
               closeMore();
            }
        });

        showStarred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowingStarred){
                    hideStarred();
                }  else {
                    showStarred();
                }
                closeMore();
            }
        });

        Handler hx = new Handler();
        Runnable rx;

        rx = new Runnable() {
            @Override
            public void run() {
                closeMore();
            }
        };
        hx.postDelayed(rx, 3000);

    }

    private void showHiddenAds() {
        isShowingHidden = true;
        hiddenPlaceHolderViewInfo.setVisibility(View.VISIBLE);
        DataListsView.setVisibility(View.GONE);
        starredPlaceHolderView.setVisibility(View.GONE);

        ImageButton unhideOptionImage = findViewById(R.id.unhideOptionImage);
        unhideOptionImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_hide));
        unhideOptionImage.setBackground(null);

        TextView unhideOptionText = findViewById(R.id.unhideOptionText);
        unhideOptionText.setText("Show Regular Uploads.");

        if(hiddenAdIds.size()==0){
            TextView nothingHiddenText= findViewById(R.id.nothingHiddenText);
            nothingHiddenText.setVisibility(View.VISIBLE);

            TextView hiddenTitle = findViewById(R.id.hiddenTitle);
            hiddenTitle.setVisibility(View.GONE);
        }else{
            TextView nothingHiddenText= findViewById(R.id.nothingHiddenText);
            nothingHiddenText.setVisibility(View.INVISIBLE);

            TextView hiddenTitle = findViewById(R.id.hiddenTitle);
            hiddenTitle.setVisibility(View.VISIBLE);
        }
        TextView hiddenTitle = findViewById(R.id.starredTitle);
        hiddenTitle.setVisibility(View.GONE);

        showStarredImage.setBackground(getResources().getDrawable(R.drawable.ic_action_starred));

        TextView nothingStarredText= findViewById(R.id.nothingStarredText);
        nothingStarredText.setVisibility(View.GONE);

    }

    private void hideHiddenAds(){
        isShowingHidden = false;
        DataListsView.setVisibility(View.VISIBLE);

        hiddenPlaceHolderViewInfo.animate().setDuration(mAnimationTime).translationY(Utils.dpToPx(50)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hiddenPlaceHolderViewInfo.setVisibility(View.GONE);
                        hiddenPlaceHolderViewInfo.setTranslationY(0);
                        hiddenPlaceHolderViewInfo.setAlpha(1f);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        ImageButton unhideOptionImage = findViewById(R.id.unhideOptionImage);
        unhideOptionImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unhide));
        unhideOptionImage.setBackground(null);

        TextView unhideOptionText = findViewById(R.id.unhideOptionText);
        unhideOptionText.setText("Show Hidden.");

        TextView nothingHiddenText= findViewById(R.id.nothingHiddenText);
        nothingHiddenText.setVisibility(View.GONE);
    }



    private void showStarred(){
        isShowingStarred = true;
        starredPlaceHolderView.setVisibility(View.VISIBLE);
        DataListsView.setVisibility(View.GONE);
        hiddenPlaceHolderViewInfo.setVisibility(View.GONE);

        showStarredImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unstarred));
        showStarredImage.setBackground(null);


        if(starredAds.size()==0){
            TextView nothingHiddenText= findViewById(R.id.nothingStarredText);
            nothingHiddenText.setVisibility(View.VISIBLE);

            TextView hiddenTitle = findViewById(R.id.starredTitle);
            hiddenTitle.setVisibility(View.GONE);
        }else{
            TextView nothingHiddenText= findViewById(R.id.nothingStarredText);
            nothingHiddenText.setVisibility(View.INVISIBLE);

            TextView hiddenTitle = findViewById(R.id.starredTitle);
            hiddenTitle.setVisibility(View.VISIBLE);
        }

        TextView hiddenTitle = findViewById(R.id.hiddenTitle);
        hiddenTitle.setVisibility(View.GONE);
    }

    private void hideStarred(){
        isShowingStarred = false;
        starredPlaceHolderView.setVisibility(View.GONE);
        DataListsView.setVisibility(View.VISIBLE);

        showStarredImage.setBackground(getResources().getDrawable(R.drawable.ic_action_starred));
//        showStarredImage.setBackground(null);


        TextView nothingStarredText= findViewById(R.id.nothingStarredText);
        nothingStarredText.setVisibility(View.GONE);

        TextView starredTitle = findViewById(R.id.starredTitle);
        starredTitle.setVisibility(View.GONE);
    }

    private void closeMore(){
        isOptionsShowing = false;
        final int trans = 90;
        moreLayout.animate().translationY(-Utils.dpToPx(trans)).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(150)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        moreLayout.setVisibility(View.GONE);
                        moreLayout.setTranslationY(-Utils.dpToPx(trans));
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


    private boolean isExpandedImage = false;
    private void openExpandedImage(){
        Advert ad = Variables.adToBeViewedInTelemetries;
        expandImageCard.setVisibility(View.VISIBLE);
        final CardView.LayoutParams params = (CardView.LayoutParams) expandImageCard.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

//        animatorRight = ValueAnimator.ofInt((getScreenWidth()-Utils.dpToPx(160)),200,0);
        animatorRight = ValueAnimator.ofInt(Utils.dpToPx(140),getScreenWidth()-Utils.dpToPx(10));
        animatorLeft = ValueAnimator.ofInt(Utils.dpToPx(30),Utils.dpToPx(10),0);

        animatorTop = ValueAnimator.ofInt(Utils.dpToPx(60),Utils.dpToPx(30),0);
//        animatorBot = ValueAnimator.ofInt((getScreenHeight()-Utils.dpToPx(170)),300,0);
        animatorBot = ValueAnimator.ofInt(Utils.dpToPx(160),getScreenHeight()-Utils.dpToPx(10));

//        animatorRight = ValueAnimator.ofInt(Utils.dpToPx(300),Utils.dpToPx(150),0);
//        animatorLeft = ValueAnimator.ofInt(Utils.dpToPx(300),Utils.dpToPx(150),0);
//
//        animatorTop = ValueAnimator.ofInt(Utils.dpToPx(400),Utils.dpToPx(200),Utils.dpToPx(100),0);
//        animatorBot = ValueAnimator.ofInt(Utils.dpToPx(600),Utils.dpToPx(300),Utils.dpToPx(100),0);


        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.width = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime);
        animatorRight.setDuration(mAnimationTime);

        expandImageCard.animate().alpha(1f).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        expandImageCard.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        CardView adImageCard = findViewById(R.id.adImageCard);
        adImageCard.setVisibility(View.INVISIBLE);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        final RelativeLayout blackBackImage = findViewById(R.id.blackBackImage);
        blackBackImage.setVisibility(View.VISIBLE);
        final float alph = 0.8f;
        blackBackImage.animate().alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                blackBackImage.setAlpha(alph);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        isExpandedImage = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void closeExpandedImage(){
        Advert ad = Variables.adToBeViewedInTelemetries;
        expandImageCard.setVisibility(View.VISIBLE);
        isExpandedImage = false;
        final CardView.LayoutParams params = (CardView.LayoutParams) expandImageCard.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

//        animatorRight = ValueAnimator.ofInt(0,100,(getScreenWidth()-Utils.dpToPx(160)));
        animatorRight = ValueAnimator.ofInt(getScreenWidth()-Utils.dpToPx(10),Utils.dpToPx(140));

        animatorLeft = ValueAnimator.ofInt(0,Utils.dpToPx(10),Utils.dpToPx(30));

        animatorTop = ValueAnimator.ofInt(0,Utils.dpToPx(30),Utils.dpToPx(60));
//        animatorBot = ValueAnimator.ofInt(0,100,(getScreenHeight()-Utils.dpToPx(170)));
        animatorBot = ValueAnimator.ofInt(getScreenHeight()-Utils.dpToPx(10),Utils.dpToPx(160));

//        animatorRight = ValueAnimator.ofInt(0,Utils.dpToPx(100),Utils.dpToPx(200));
//        animatorLeft = ValueAnimator.ofInt(0,Utils.dpToPx(100),Utils.dpToPx(200));
//
//        animatorTop = ValueAnimator.ofInt(0,Utils.dpToPx(200),Utils.dpToPx(400));
//        animatorBot = ValueAnimator.ofInt(0,Utils.dpToPx(200),Utils.dpToPx(300),Utils.dpToPx(600));


        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.width = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                expandImageCard.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime);
        animatorRight.setDuration(mAnimationTime);

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                expandImageCard.setVisibility(View.INVISIBLE);
                CardView adImageCard = findViewById(R.id.adImageCard);
                adImageCard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

//        expandImageCard.animate().alpha(0f).setDuration(mAnimationTime+100).setInterpolator(new LinearInterpolator())
//                .setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                expandImageCard.setAlpha(0f);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        }).start();

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        final RelativeLayout blackBackImage = findViewById(R.id.blackBackImage);
        final float alph = 0f;
        blackBackImage.animate().alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        blackBackImage.setAlpha(alph);
                        blackBackImage.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        expandImageCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private int getScreenWidth(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. x;
    }

    private int getScreenHeight(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. y;
    }




}
