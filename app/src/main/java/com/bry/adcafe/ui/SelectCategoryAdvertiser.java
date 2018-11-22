package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.SelectCategoryAdvertiserContainer;
import com.bry.adcafe.fragments.GetAmmountPerUserFragment;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectCategoryAdvertiser extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = SelectCategoryAdvertiser.class.getSimpleName();
    @Bind(R.id.selectCategoriesLayout) LinearLayout mainView;
    @Bind(R.id.failedLoadLayout) LinearLayout failedToLoadLayout;
    @Bind(R.id.retryLoading) Button retryLoadingButton;
    @Bind(R.id.loadingLayout) LinearLayout loadingLayout;
    @Bind(R.id.categoryPlaceHolderView) PlaceHolderView placeHolderView;
    private Context mContext;
    private Context acCont;
    private boolean isDialogShowing = false;

    private LinkedHashMap<Integer,LinkedHashMap<String,Long>> userStats = new LinkedHashMap<>();
    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private boolean isNeedToLoadLogin = false;

    @Bind(R.id.swipeBackView2)View swipeBackView2;
    private boolean isSwipingForBack2 = false;
    private GestureDetector mSwipeBackDetector2;
    private int maxSideSwipeLength2 = 200;
    private int x_delta2;
    private List<Integer> SideSwipeRawList2 = new ArrayList<>();
    private boolean isSideScrolling2 = false;
    private int maxSideSwipeLength = 200;

    private long allUsersNumber = 0;
    private LinkedHashMap<String,Long> userLastOnline = new LinkedHashMap<>();
    private List<String> users = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category_advertiser);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();
        acCont = SelectCategoryAdvertiser.this;
        if(isOnline(mContext)) setUpTimeIfNeedBe();
        else{
            mainView.setVisibility(View.GONE);
            failedToLoadLayout.setVisibility(View.VISIBLE);
        }
        retryLoadingButton.setOnClickListener(this);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSelectingCategory,
                new IntentFilter("SELECTED_CATEGORY"));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForStartingNextActivity,
                new IntentFilter("START_NEXT_ACTIVITY"));

        addTouchListenerForSwipeBack2();
    }

    private void setUpTimeIfNeedBe(){
        if(!TimeManager.isTimerOnline()) {
            failedToLoadLayout.setVisibility(View.GONE);
            mainView.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
            TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,
                    new IntentFilter(Constants.LOAD_TIME));
        } else loadAllUserNumber();
    }

    @Override
    protected void onResume(){
        isWindowPaused = false;
        super.onResume();
        if(isNeedToLoadLogin){
            Intent intent = new Intent(SelectCategoryAdvertiser.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else addListenerForChangeInSessionKey();

    }

    @Override
    protected void onPause(){
        removeListenerForChangeInSessionKey();
        isWindowPaused = true;
        super.onPause();
    }


    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished setting up time.");
            loadAllUserNumber();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void loadAllUserNumber(){
        failedToLoadLayout.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        DatabaseReference mAll = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS);
        mAll.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot userSnap:dataSnapshot.getChildren()){
//                    if(userSnap.child(Constants.SUBSCRIPTION_lIST).child(Constants.CATEGORY_EVERYONE).exists()){
//                        allUsersNumber++;
//                    }
//                }
                for(DataSnapshot userSnap:dataSnapshot.getChildren()){
                    String uid = userSnap.getKey();
                    long lastSeenDate = 0L;
                    if(userSnap.child(Constants.LAST_SEEN_DATE_IN_DAYS).exists()){
                        lastSeenDate = userSnap.child(Constants.LAST_SEEN_DATE_IN_DAYS).getValue(long.class);
                    }
                    if(lastSeenDate+ 30L >TimeManager.getDateInDays()){
                        users.add(uid);
                    }
//                    userLastOnline.put(uid,lastSeenDate);

                }
                allUsersNumber= dataSnapshot.getChildrenCount();
                loadUserStatsFirst();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserStatsFirst(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Integer cpvValue = Integer.parseInt(snap.getKey());
                    LinkedHashMap<String,Long> categoryStats = new LinkedHashMap<>();
                    for (DataSnapshot categorySnap: snap.getChildren()){
                        String category = categorySnap.getKey();
                        int numberOfUsers = 0;
                        if(category.equals(Constants.CATEGORY_EVERYONE)){
                            numberOfUsers=users.size();
                        }else{
                            for(DataSnapshot clusterSnap:categorySnap.getChildren()){
                                for(DataSnapshot userSnap:clusterSnap.getChildren()) {
                                    String userId = userSnap.getKey();
                                    if (users.contains(userId)) {
                                        numberOfUsers++;
                                    }
                                }
                            }
                        }
                        categoryStats.put(category,((long)numberOfUsers));
                    }
                    userStats.put(cpvValue,categoryStats);
                }
                loadCategoriesFromFirebase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                failedToLoadLayout.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
            }
        });
    }

    private void loadCategoriesFromFirebase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CATEGORY_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SelectCategoryAdvertiserContainer lastCat = null;
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    String category = snap.getKey();
                    List<String> subcategories = new ArrayList<>();
                    for(DataSnapshot subSnap: snap.getChildren()){
                        String cat = subSnap.getValue(String.class);
                        if(doesCategoryImageExists(cat)){
                            subcategories.add(cat);
                            Variables.allCategories.add(subSnap.getValue(String.class));
                        }
                    }
                    if(!subcategories.isEmpty()){
                        if(category.equals(Constants.CATEGORY_EVERYONE_CONTAINER)){
                            lastCat = new SelectCategoryAdvertiserContainer(mContext,placeHolderView,category,subcategories);
                        }else{
                            placeHolderView.addView(new SelectCategoryAdvertiserContainer(mContext,placeHolderView,category,subcategories));
                        }
                    }
                }
                if(lastCat!=null){
                    placeHolderView.addView(lastCat);
                }
                loadingLayout.setVisibility(View.GONE);
                mainView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                failedToLoadLayout.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
            }
        });
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private BroadcastReceiver mMessageReceiverForSelectingCategory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Selected category : "+ Variables.SelectedCategory);
            if(!isFinishing()) getAmountPerUser();
        }
    };

    private BroadcastReceiver mMessageReceiverForStartingNextActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startAdUpload();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSelectingCategory);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void startAdUpload() {
        Intent intent = new Intent(SelectCategoryAdvertiser.this, AdUpload.class);
        startActivity(intent);
        finish();
    }

    private void getAmountPerUser2(){
        final Dialog d = new Dialog(acCont);
        d.setTitle("Targeted people category.");
        d.setContentView(R.layout.dialog6);
        Button b1 = d.findViewById(R.id.submitButton);
        Button b2 = d.findViewById(R.id.cancelButton);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = d.findViewById(R.id.radioButton3);
                RadioButton button5 = d.findViewById(R.id.radioButton5);
                RadioButton button8 = d.findViewById(R.id.radioButton8);
                if(button3.isChecked()){
                    cpv = 3;
                }else if(button5.isChecked()){
                    cpv = 5;
                }else{
                    cpv = 8;
                }
                Variables.amountToPayPerTargetedView = cpv;
                d.cancel();
                startAdUpload();
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSelectingCategory);
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });
        d.show();
        isDialogShowing = true;
    }

    private void getAmountPerUser(){
        FragmentManager fm = getFragmentManager();
        GetAmmountPerUserFragment getAmmountPerUserFragment = new GetAmmountPerUserFragment();
        getAmmountPerUserFragment.setMenuVisibility(false);
        getAmmountPerUserFragment.setContext(mContext);
        getAmmountPerUserFragment.setStats(userStats);
        getAmmountPerUserFragment.show(fm, "Amount Per User.");

    }


    @Override
    public void onClick(View v) {
        if(v==retryLoadingButton){
            if(isOnline(mContext)) {
                loadAllUserNumber();
            }else{
                Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
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
        Log.d(TAG, "Loading session key from shared prefs - " + sk);
        return sk;
    }



    public void PerformShutdown(){
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Variables.resetAllValues();
        if(!isWindowPaused){
            Intent intent = new Intent(SelectCategoryAdvertiser.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            isNeedToLoadLogin = true;
        }

    }

    private boolean doesCategoryImageExists(String category){
        String filename;
        filename = category.replaceAll(" ","_");
        int res = mContext.getResources().getIdentifier(filename, "drawable", mContext.getPackageName());
        if(res==0)Log.e(TAG,"Category image for "+category+" does not exist");
        return res != 0;
    }


    private void addTouchListenerForSwipeBack2() {
        mSwipeBackDetector2 = new GestureDetector(this, new MySwipebackGestureListener2());
        swipeBackView2.setOnTouchListener(touchListener2);
    }

    View.OnTouchListener touchListener2 = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mSwipeBackDetector2.onTouchEvent(motionEvent)) {
                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (isSideScrolling2) {
                    Log.d("touchListener", " onTouch ACTION_UP");
                    isSideScrolling2 = false;

                    int RightScore = 0;
                    int LeftScore = 0;
                    if (SideSwipeRawList2.size() > 15) {
                        for (int i = SideSwipeRawList2.size() - 1; i > SideSwipeRawList2.size() - 15; i--) {
                            int num1 = SideSwipeRawList2.get(i);
                            int numBefore1 = SideSwipeRawList2.get(i - 1);

                            if (numBefore1 > num1) LeftScore++;
                            else RightScore++;
                        }
                    } else {
                        for (int i = SideSwipeRawList2.size() - 1; i > 0; i--) {
                            int num1 = SideSwipeRawList2.get(i);
                            int numBefore1 = SideSwipeRawList2.get(i - 1);

                            if (numBefore1 > num1) LeftScore++;
                            else RightScore++;
                        }
                    }
                    if (RightScore > LeftScore) {
//                        onBackPressed();
                    }
                    hideNupdateSideSwipeThing2();
                    SideSwipeRawList2.clear();
                }
                ;
            }

            return false;
        }
    };

    class MySwipebackGestureListener2 extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG", "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG", "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) swipeBackView2.getLayoutParams();
            x_delta2 = X - lParams.leftMargin;

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
            if ((X - x_delta2) < maxSideSwipeLength) {

            } else if ((X - x_delta2) > maxSideSwipeLength) {

            } else {
            }
            showNupdateSideSwipeThing2(X - x_delta2);

            Log.d("TAG", "the e2.getAction()= " + e2.getAction() + " and the MotionEvent.ACTION_CANCEL= " + MotionEvent.ACTION_CANCEL);
            SideSwipeRawList2.add(X);

            isSideScrolling2 = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int RightScore = 0;
            int LeftScore = 0;
            if (SideSwipeRawList2.size() > 15) {
                for (int i = SideSwipeRawList2.size() - 1; i > SideSwipeRawList2.size() - 15; i--) {
                    int num1 = SideSwipeRawList2.get(i);
                    int numBefore1 = SideSwipeRawList2.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            } else {
                for (int i = SideSwipeRawList2.size() - 1; i > 0; i--) {
                    int num1 = SideSwipeRawList2.get(i);
                    int numBefore1 = SideSwipeRawList2.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            }
            if (RightScore > LeftScore) {
                onBackPressed();
            }
            SideSwipeRawList2.clear();
            return false;

        }
    }

    private void showNupdateSideSwipeThing2(int pos) {
        int trans = (int) ((pos - Utils.dpToPx(10)) * 0.9);
//        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.setTranslationX(trans);

        View v = findViewById(R.id.swipeBackViewIndicator2);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
        params.height = trans;
        v.setLayoutParams(params);

        mainView.setTranslationX((int)(trans*0.05));

    }

    private void hideNupdateSideSwipeThing2() {
        int myDurat = 200;
//        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.animate().setDuration(mAnimationTime).translationX(Utils.dpToPx(-40)).start();

        final View v = findViewById(R.id.swipeBackViewIndicator2);
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


        mainView.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainView.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }
}
