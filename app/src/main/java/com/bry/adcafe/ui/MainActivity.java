package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdvertCard;
import com.bry.adcafe.adapters.AdCounterBar;
import com.bry.adcafe.fragments.ReportDialogFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.ConnectionChecker;
import com.bry.adcafe.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private SwipePlaceHolderView mSwipeView;
    private PlaceHolderView mAdCounterView;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private int mNumberOfAdsSeen;
    private String mKey = "";

    private List<Advert> mAdList;
    private Runnable mViewRunnable;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayout;
    private AVLoadingIndicatorView mAvi;
    private String mTodaysDate;
    private int mMonthTotal = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        registerReceivers();
        loadDateFromFirebase();

        setUpSwipeView();
        loadAdsFromThread();
        StartNetworkChecker(mContext);
    }

    //redundant method for multithreading
    private void loadAdsFromThread(){
        try{
            startGetAds();
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
    }

    private void startGetAds() {
        mViewRunnable = new Runnable() {
            @Override
            public void run() {
                getAds();
            }
        };
        Thread thread =  new Thread(null, mViewRunnable, "Background");
        thread.start();
//        mProgressBar.setVisibility(View.VISIBLE);
        mAvi.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.GONE);
    }

    //method for loading ads onto thread from db
    private void getAds() {
        try{
            mAdList = new ArrayList<>();
            for(Advert ad: Utils.loadProfiles(this.getApplicationContext())){
                 mAdList.add(ad);
            }
            Thread.sleep(3000);
            Log.i("ARRAY", ""+  mAdList.size());
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }

    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            loadAdsFromJSONFile();
            mAvi.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
        }
    };




    @Override
    protected void onStop(){
        super.onStop();
    }

    //deleting saved data when app is stopped
    @Override
    protected void onDestroy(){
        unregisterAllReceivers();
        removeAllViews();
        addToSharedPreferences();
        Variables.clearAdTotal();
        super.onDestroy();
    }

    private void unregisterAllReceivers(){
        Log.d("MAIN_ACTIVITY--","Unregistering all receivers");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddingToSharedPreferences);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOffline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForConnectionOnline);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForLastAd);

        sendBroadcastToUnregisterAllReceivers();
    }

    private void sendBroadcastToUnregisterAllReceivers() {
        Intent intent = new Intent(Constants.UNREGISTER_ALL_RECEIVERS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }




    private void setUpSwipeView() {
        mSwipeView = (SwipePlaceHolderView)findViewById(R.id.swipeView);
//        mProgressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        mLinearLayout = (LinearLayout) findViewById(R.id.bottomNavButtons);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.mainActivityAvi);

        int bottomMargin = Utils.dpToPx(90);
        Point windowSize = Utils.getDisplaySize(getWindowManager());
        float relativeScale = density();

        mSwipeView.getBuilder()
                .setDisplayViewCount(4)
                .setIsUndoEnabled(false)
                .setHeightSwipeDistFactor(10)
                .setWidthSwipeDistFactor(5)
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setSwipeRotationAngle(0)
                        .setSwipeAnimTime(200)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(15)
                        .setRelativeScale(relativeScale));
//        mSwipeView.s;
    }

    private void removeAllViews(){
        if(mSwipeView!=null){
            mSwipeView.removeAllViews();
        }
        if(mAdCounterView!=null){
            mAdCounterView.removeAllViews();
        }
    }

    private void loadAdCounter() {
        mAdCounterView = (PlaceHolderView)findViewById(R.id.adCounterView);
        mAdCounterView.addView(new AdCounterBar(this.getApplicationContext(),mAdCounterView));

    }

    private void loadAdsFromJSONFile(){
        if(mSwipeView == null){
            setUpSwipeView();
        }
        if(mSwipeView.getChildCount()!=0){
            mSwipeView.removeAllViews();
        }
        if(mAdList!=null && mAdList.size()>0){
            for(int i = 0 ; i < mAdList.size() ; i++){
                if(Variables.getAdTotal(mKey)>=mAdList.size()){
                    mSwipeView.addView(new AdvertCard(mContext,mAdList.get(mAdList.size()-1),mSwipeView,Constants.LAST));
                    Variables.setIsLastOrNotLast(Constants.LAST);
                    break;
                } else {
                    if(i>=Variables.getAdTotal(mKey)){
                        mSwipeView.addView(new AdvertCard(mContext,mAdList.get(i),mSwipeView,Constants.NOT_LAST));
                        Variables.setIsLastOrNotLast(Constants.NOT_LAST);
                    }
                }
            }
        }

        loadAdCounter();
        Variables.setNewNumberOfAds(mAdList.size()-Variables.getAdTotal(mKey));
        onclicks();
    }




    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences,new IntentFilter(Constants.ADD_TO_SHARED_PREFERENCES));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline,new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline,new IntentFilter(Constants.CONNECTION_ONLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForLastAd,new IntentFilter(Constants.LAST));

    }

    private void onclicks() {
        findViewById(R.id.logoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        if(findViewById(R.id.bookmark2Btn)!= null){
            findViewById(R.id.bookmark2Btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isNetworkConnected(mContext)){
                        if(!Variables.hasBeenPinned){
                            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.pinning,
                                    Snackbar.LENGTH_SHORT).show();
                            Intent intent = new Intent(Constants.PIN_AD);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        }else{
                            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.hasBeenPinned,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }else {
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.cannotPin,
                                Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
        }

        findViewById(R.id.bookmarkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Bookmarks.class);
                startActivity(intent);
            }
        });

        if(findViewById(R.id.profileImageView)!= null){
            findViewById(R.id.profileImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwipeView.doSwipe(true);
                }
            });

            findViewById(R.id.profileImageView).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    findViewById(R.id.bookmark2Btn).callOnClick();
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


        findViewById(R.id.shareBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext,"This will share the app.",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        if(findViewById(R.id.reportBtn)!=null){
            findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    ReportDialogFragment reportDialogFragment = new ReportDialogFragment();
                    reportDialogFragment.show(fm,"Report dialog fragment.");
                    reportDialogFragment.setfragcontext(mContext);
                }
            });
        }

    }

    private void logoutUser() {
        clearFromSharedPreferences();
        unregisterAllReceivers();
        if(FirebaseAuth.getInstance()!=null){
            FirebaseAuth.getInstance().signOut();
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("COUNTER_BAR_TO_MAIN- ","Broadcast has been received to add to shared preferences.");
            Variables.adAdToTotal(mKey);
            Variables.adToMonthTotals(mKey);
            addToSharedPreferences();
            adDayAndMonthTotalsToFirebase();
            onclicks();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has been dropped");
            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.connectionDropped2,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has come online");
        }
    };

    private BroadcastReceiver mMessageReceiverForLastAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.lastAd,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    };




    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public float density(){
        double constant = 0.000046875;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        float relativeScale;

        if (density >= 560) {
            Log.d("DENSITY---","HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.005f;
        }else if(density >= 460){
            Log.d("DENSITY---","MEDIUM-HIGH... Density is " + String.valueOf(density));
            relativeScale = 0.009f;
        }else if(density >= 360){
            Log.d("DENSITY---","MEDIUM-LOW... Density is " + String.valueOf(density));
            relativeScale = 0.013f;
        }else if(density >= 260){
            Log.d("DENSITY---","LOW... Density is " + String.valueOf(density));
            relativeScale = 0.015f;
        }else{
            relativeScale = 0.02f;
        }
        return relativeScale;
    }

    private void addToSharedPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.AD_TOTAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("adTotals",Variables.getAdTotal(mKey));
        Log.d("MAIN_ACTIVITY--","Adding 1 to shared preferences adTotal is - "+Integer.toString(Variables.getAdTotal(mKey)));
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor.putInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,Variables.getMonthAdTotals(mKey));
        Log.d("MAIN_ACTIVITY--","Adding 1 to shared preferences Month ad totals is - "+Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.commit();
    }



//    private void loadFromSharedPreferences(){
//        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
//        int number = prefs.getInt("adTotals",0);
//        Log.d("MAIN_ACTIVITY-----","NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number);
//        Variables.setAdTotal(number,mKey);
//
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.clear();
//        editor.commit();
//
//        SharedPreferences prefs2 = getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,MODE_PRIVATE);
//        int number2 = prefs2.getInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,0);
//        Log.d("MAIN_ACTIVITY-----","NUMBER GOTTEN FROM MONTHLY SHARED PREFERENCES IS - "+ number2);
//        Variables.setMonthAdTotals(mKey,number);
//
//        SharedPreferences.Editor editor2 = prefs2.edit();
//        editor2.clear();
//        editor2.commit();
//    }

    private void clearFromSharedPreferences(){
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        SharedPreferences prefs2 = getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.clear();
        editor2.commit();
    }

    public void StartNetworkChecker(final Context context){
        Handler handler=new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!isNetworkConnected(context)){
                        Snackbar.make(findViewById(R.id.mainCoordinatorLayout), R.string.connectionDropped2,
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                }
            },10000);
    }



    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void adDayAndMonthTotalsToFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.getUid()!=null){
            String uid = user.getUid();
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
            adRef.setValue(Variables.getAdTotal(mKey));

            DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
            adRef2.setValue(Variables.getMonthAdTotals(mKey));
        }

    }

    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayeString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        final Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH));
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dayeString+":"+MonthString+":"+yearString);

        return todaysDate;
    }

    private void loadDateFromFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.DATE_IN_FIREBASE);
        DatabaseReference mRef = query.getRef();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String FirebaseDate = dataSnapshot.getValue(String.class);
                mTodaysDate = FirebaseDate;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mTodaysDate = getDate();
            }

        });

    }

    private void resetAdTotalSharedPreferencesAndDayAdTotals(){
        SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        Variables.setAdTotal(0,mKey);
    }



    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

}
