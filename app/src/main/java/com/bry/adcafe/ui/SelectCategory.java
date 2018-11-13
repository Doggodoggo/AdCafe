package com.bry.adcafe.ui;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
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
import com.bry.adcafe.adapters.SelectCategoryContainer;
import com.bry.adcafe.fragments.SetSignupUserPersonalInfo;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectCategory extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = SelectCategory.class.getSimpleName();
    @Bind(R.id.submitCategoriesButton) Button mSubmitButton;
    @Bind(R.id.selectCategoriesLayout) LinearLayout mainView;
    @Bind(R.id.failedLoadLayout) LinearLayout failedToLoadLayout;
    @Bind(R.id.retryLoading) Button retryLoadingButton;
    @Bind(R.id.loadingLayout) LinearLayout loadingLayout;
    @Bind(R.id.categoryPlaceHolderView) PlaceHolderView placeHolderView;
    private Context mContext;
    private boolean isUserInActivity;
    private boolean hasDataBeenLoaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedCreatingUserSubscriptionList,
                new IntentFilter(Constants.SET_UP_USERS_SUBSCRIPTION_LIST));
       LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedSettingUserPersonalinfo,
               new IntentFilter("SETTING_USER_PERSONAL_CONTENT"));
        if (isOnline(mContext)) {
            loadCategoriesFromFirebase();
        }else{
            mainView.setVisibility(View.GONE);
            failedToLoadLayout.setVisibility(View.VISIBLE);
        }
        mSubmitButton.setOnClickListener(this);
        retryLoadingButton.setOnClickListener(this);
    }

    private void loadCategoriesFromFirebase() {
        failedToLoadLayout.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CATEGORY_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int numberOfSubs = 0;
                Variables.selectedCategoriesToSubscribeTo.clear();
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    String category = snap.getKey();
                    List<String> subcategories = new ArrayList<>();
                    for(DataSnapshot subSnap: snap.getChildren()){
                        String cat = subSnap.getValue(String.class);
                        if(doesCategoryImageExists(cat)){
                            subcategories.add(cat);
                            numberOfSubs++;
                        }
                    }
                   if(!subcategories.isEmpty()){
                        if(!category.equals(Constants.CATEGORY_EVERYONE_CONTAINER)){
                            placeHolderView.addView(new SelectCategoryContainer(mContext,placeHolderView,category,subcategories));
                        }
                   }
                }
                new DatabaseManager().setNumberOfSubscriptionsUserKnowsAbout(numberOfSubs);
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



    @Override
    protected void onResume() {
        if(hasDataBeenLoaded) startMainActivity();
        isUserInActivity = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isUserInActivity = false;
        super.onPause();
    }

    @Override
    protected  void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedCreatingUserSubscriptionList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.STOP_LISTENING));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSettingUserPersonalinfo);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v==mSubmitButton) {
            if (Variables.selectedCategoriesToSubscribeTo.size() == 0) {
                Snackbar.make(findViewById(R.id.select_Categories), "At least choose one category.",
                        Snackbar.LENGTH_LONG).show();
            }else{
                if(isOnline(mContext)) {
                    if(!isFinishing()) getAmountPerUser();
                }else{
                    Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
                }
            }
        }else if(v==retryLoadingButton){
            if(isOnline(mContext)) {
                loadCategoriesFromFirebase();
            }else{
                Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAmountPerUser(){
        final Dialog d = new Dialog(this);
        d.setTitle("Amount to receive.");
        d.setContentView(R.layout.dialog7);
        final LinearLayout selectCpv = d.findViewById(R.id.selectCpvLayout);
        Button b1 = d.findViewById(R.id.submitButton);
        Button b2 = d.findViewById(R.id.cancelButton);

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        final LinearLayout setPersonalContent = d.findViewById(R.id.personalisedContentOption);
        final Button skip = d.findViewById(R.id.skip);
        final Button cont = d.findViewById(R.id.okBtn);

        final LinearLayout whyLayout = d.findViewById(R.id.whyLayout);
        final Button okBtn207 = d.findViewById(R.id.okBtn207);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button1 = d.findViewById(R.id.radioButton1);
                RadioButton button3 = d.findViewById(R.id.radioButton3);
                RadioButton button6 = d.findViewById(R.id.radioButton6);
                if(button1.isChecked()){
                    cpv = 1;
                }else if(button3.isChecked()){
                    cpv = 3;
                }else{
                    cpv = 6;
                }
                Variables.constantAmountPerView = cpv;

                selectCpv.setVisibility(View.GONE);
                setPersonalContent.setVisibility(View.VISIBLE);
                setPersonalContent.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                        .setInterpolator(new FastOutSlowInInterpolator());
//                startSetUp();
//                d.dismiss();
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetUp();
                d.dismiss();
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPersonalContent.setVisibility(View.GONE);
                whyLayout.setVisibility(View.VISIBLE);
                whyLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                        .setInterpolator(new FastOutSlowInInterpolator());
            }
        });

        okBtn207.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
                startSetPersonalInfoFragment();
            }
        });

        d.show();
    }

    private void startSetPersonalInfoFragment() {
        FragmentManager fm = getFragmentManager();
        SetSignupUserPersonalInfo cpvFragment = new SetSignupUserPersonalInfo();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.setCancelable(false);
        cpvFragment.show(fm,"Edit User Data.");
        cpvFragment.setfragcontext(mContext);
        cpvFragment.setActivity(this);
    }

    private BroadcastReceiver mMessageReceiverForFinishedSettingUserPersonalinfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished setting user personal info");
            startSetUp();
        }
    };

    private void startSetUp(){
        mainView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        hasDataBeenLoaded = false;
        DatabaseManager dbm = new DatabaseManager();
        dbm.setContext(mContext);
        if(!Variables.selectedCategoriesToSubscribeTo.contains(Constants.CATEGORY_EVERYONE)) {
            Variables.selectedCategoriesToSubscribeTo.add(Constants.CATEGORY_EVERYONE);
        }
        dbm.setUpUserSubscriptions(Variables.selectedCategoriesToSubscribeTo);
    }

    private BroadcastReceiver mMessageReceiverForFinishedCreatingUserSubscriptionList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished creating user subscription list");
            hasDataBeenLoaded = true;
            if(isUserInActivity) startMainActivity();
        }
    };

    private void startMainActivity(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        User.setUid(uid);
        Variables.setCurrentSubscriptionIndex(0);
        Variables.setCurrentAdInSubscription(0);
        Variables.setAdTotal(0,"");
        Variables.setMonthAdTotals("",0);
        Variables.setTotalReimbursementAmount(0);
        Variables.isStartFromLogin = true;
        loadingLayout.setVisibility(View.GONE);
        Intent intent = new Intent(SelectCategory.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private boolean doesCategoryImageExists(String category){
        String filename;
        filename = category.replaceAll(" ","_");
        int res = mContext.getResources().getIdentifier(filename, "drawable", mContext.getPackageName());
        if(res==0)Log.d(TAG,"Category image for "+category+" does not exist");
        return res != 0;
    }
}
