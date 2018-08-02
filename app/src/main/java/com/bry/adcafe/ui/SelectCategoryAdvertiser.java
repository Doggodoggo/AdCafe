package com.bry.adcafe.ui;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    }

    private void setUpTimeIfNeedBe(){
        if(!TimeManager.isTimerOnline()) {
            failedToLoadLayout.setVisibility(View.GONE);
            mainView.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
            TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,
                    new IntentFilter(Constants.LOAD_TIME));
        } else loadUserStatsFirst();
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
            loadUserStatsFirst();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void loadUserStatsFirst(){
        failedToLoadLayout.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Integer cpvValue = Integer.parseInt(snap.getKey());
                    LinkedHashMap<String,Long> categoryStats = new LinkedHashMap<>();
                    for (DataSnapshot categorySnap: snap.getChildren()){
                        String category = categorySnap.getKey();
                        long numberOfUsers = ((categorySnap.getChildrenCount()-1)*1000)
                                +categorySnap.child(Long.toString(categorySnap.getChildrenCount())).getChildrenCount();
                        categoryStats.put(category,numberOfUsers);
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
                    if(!subcategories.isEmpty())placeHolderView.addView(new SelectCategoryAdvertiserContainer(mContext,placeHolderView,category,subcategories));
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
                loadUserStatsFirst();
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
}
