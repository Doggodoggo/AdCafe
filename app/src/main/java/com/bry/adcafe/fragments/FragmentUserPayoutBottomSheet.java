package com.bry.adcafe.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.AdCoin;
import com.bry.adcafe.models.MyTime;
import com.bry.adcafe.services.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 20/02/2018.
 */

public class FragmentUserPayoutBottomSheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private View mContentView;
    private LinearLayout mPayoutOptionsLayout;
    private LinearLayout mEnterPayoutDetailsPart;
    private String mPhoneNo;
    private LinearLayout mConfirmLayout;
    private int mTotals;
    private String mPassword;
    private String mTransactionId;

    private boolean isMakingPayout = false;

    private int duration = 300;

    private int payoutOptionHeight = 0;
    private int payoutLayoutsHeight = 0;
    private int confirmLayoutHeight = 0;

    boolean isUserUsingCoinsInsteadOfNumber = false;
    DataSnapshot historyUploadsSnap;
    List<AdCoin> myLegitCoins;



    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setDetails(int totals,String password){
        mTotals = totals;
        mPassword = password;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:{
                    Log.d("BSB","collapsed") ;
                }
                case BottomSheetBehavior.STATE_SETTLING:{
                    Log.d("BSB","settling") ;
                }
                case BottomSheetBehavior.STATE_EXPANDED:{
                    Log.d("BSB","expanded") ;
                }
                case BottomSheetBehavior.STATE_HIDDEN: {
                    Log.d("BSB" , "hidden") ;
                    dismiss();
                }
                case BottomSheetBehavior.STATE_DRAGGING: {
                    Log.d("BSB","dragging") ;
                }
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            Log.d("BSB","sliding " + slideOffset ) ;
        }
    };



    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_payout_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView = contentView;

        mPayoutOptionsLayout = contentView.findViewById(R.id.payoutOptions);
        mEnterPayoutDetailsPart = contentView.findViewById(R.id.enterPayoutDetailsPart);
        mConfirmLayout = contentView.findViewById(R.id.confirmLayout);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
//        params.setMargins(15,-15,15,15);
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setHideable(false);
        }

        contentView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        contentView.findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPayoutDetailsPart();
            }
        });

        contentView.findViewById(R.id.cancelBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        FrameLayout bottomSheet = dialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.dialog_bg_trans);

        performBackgroundCheckupOnAdCoins();
    }

    private void showPayoutDetailsPart(){
        mEnterPayoutDetailsPart.setVisibility(View.VISIBLE);
        mPayoutOptionsLayout.animate().translationX(-100).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(Constants.ANIMATION_DURATION).alpha(0f);
        mEnterPayoutDetailsPart.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).setInterpolator(new LinearOutSlowInInterpolator());
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
        final EditText passwordEdit = mContentView.findViewById(R.id.passwordEditText);

        setPhoneField();

        final Button continueBtn = mContentView.findViewById(R.id.continueButton2);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneEdit.getText().toString().trim().equals("")){
                    phoneEdit.setError("We need your phone number.");
                }else if(passwordEdit.getText().toString().trim().equals("")){
                    passwordEdit.setError("We need your password.");
                }else if(phoneEdit.getText().toString().trim().length()<10){
                    phoneEdit.setError("That's not a real phone number.");
                }else{
                    String phoneNo = phoneEdit.getText().toString().trim();
                    try{
                        Integer.parseInt(phoneNo);
                        String password = passwordEdit.getText().toString().trim();
                        if(!password.equals(mPassword)){
                            passwordEdit.setError("That's not your password!");
                        }else{
//                            mEnterPayoutDetailsPart.setVisibility(View.GONE);
                            mPhoneNo = phoneNo;
                            updatePhoneNumber(mPhoneNo);
                            showConfirmDetailsPart();

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        phoneEdit.setError("That's not a real phone number.");
                    }

                }
            }
        });
        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    continueBtn.performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });
    }

    private void showConfirmDetailsPart(){
        mConfirmLayout.setVisibility(View.VISIBLE);
        mEnterPayoutDetailsPart.animate().translationX(-50).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(Constants.ANIMATION_DURATION).alpha(0f);
        mConfirmLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new LinearOutSlowInInterpolator());

        TextView amountToBeSentView = mContentView.findViewById(R.id.amountToBeSent);
        TextView phoneNumberView = mContentView.findViewById(R.id.phoneNumber);
        Button continueBtn = mContentView.findViewById(R.id.startButton);

        if(isUserUsingCoinsInsteadOfNumber){
            mTotals = getTotalValueFromEachCoin(myLegitCoins);
        }

        amountToBeSentView.setText("Amount To Be Sent: "+mTotals+" Ksh.");
        phoneNumberView.setText("Phone number: "+mPhoneNo);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.phoneNo = mPhoneNo;
                isMakingPayout = true;
                dismiss();
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYOUT"));

            }
        });

    }

    @Override
    public void dismiss(){
        super.dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if(!isMakingPayout) new DatabaseManager().setIsMakingPayoutInFirebase(false);
        super.onDismiss(dialog);
    }

    private void setPhoneField(){
        String number = mActivity.getSharedPreferences(Constants.PHONE_NUMBER2, MODE_PRIVATE).getString(Constants.PHONE_NUMBER2, "b");
        if(!number.equals("b")){
            final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
            phoneEdit.setText(number);
        }
    }

    private void updatePhoneNumber(String phone){
        SharedPreferences pref = mActivity.getSharedPreferences(Constants.PHONE_NUMBER2, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putString(Constants.PHONE_NUMBER2,phone);
        editor.apply();
    }



    private void setPhoneField2(){
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
        TelephonyManager tMgr = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_SMS}, 1);
        } else {
            Log.d("UserPpay","Attempting to get users phone numebr");
            String mPhoneNumber = tMgr.getLine1Number();
            Log.d("UserPpay","Users phone numebr"+mPhoneNumber);
            phoneEdit.setText(mPhoneNumber);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("AdvertiserPayout", "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
            TelephonyManager tMgr = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
                Log.d("UserPpay","Attempting to get users phone numebr");
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    String mPhoneNumber = tMgr.getLine1Number();
                    Log.d("UserPpay","Users phone numebr"+mPhoneNumber);
                    phoneEdit.setText(mPhoneNumber);
                }

        }
    }


    private void showLoadingScreen(){
        TextView PayoutTitle = mContentView.findViewById(R.id.PayoutTitle);
        TextView PayoutExp = mContentView.findViewById(R.id.PayoutExp);
        Button continueButton = mContentView.findViewById(R.id.continueButton);

        PayoutTitle.setVisibility(View.INVISIBLE);
        PayoutExp.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);

        mContentView.findViewById(R.id.loadingProgressBar).setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen(){
        TextView PayoutTitle = mContentView.findViewById(R.id.PayoutTitle);
        TextView PayoutExp = mContentView.findViewById(R.id.PayoutExp);
        Button continueButton = mContentView.findViewById(R.id.continueButton);

        PayoutTitle.setVisibility(View.VISIBLE);
        PayoutExp.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.VISIBLE);

        mContentView.findViewById(R.id.loadingProgressBar).setVisibility(View.INVISIBLE);
    }

    private void performBackgroundCheckupOnAdCoins(){
        showLoadingScreen();

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyUploadsSnap = dataSnapshot;
                checkIfUserIsUsingOldSystem();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG","error: "+databaseError.getMessage());
            }
        });

    }

    private void checkIfUserIsUsingOldSystem(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid)
                .child(Constants.COIN_VALUE_SYSTEM);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Variables.isUsingNewCoinValueSystem = false;
                if(dataSnapshot.exists()){
                    isUserUsingCoinsInsteadOfNumber = true;
                    Variables.isUsingNewCoinValueSystem = true;
                }
                ConfirmEachCoinExists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG","error: "+databaseError.getMessage());
            }
        });
    }

    private void ConfirmEachCoinExists(){
        List<AdCoin> coins = loadUsersCoins();
        List<AdCoin> legitCoins = new ArrayList<>();

        for(AdCoin coin:coins){
            MyTime coinTime = coin.getTimeCreated();
            DataSnapshot adSnap = historyUploadsSnap.child(Integer.toString(coinTime.getYear())).child(Integer.toString(coinTime.getMonth()))
                    .child(Integer.toString(coinTime.getDay())).child(coin.getAdvertPushRefInAdminConsole());
            if(adSnap.exists()){
                legitCoins.add(coin);
            }
        }
        Variables.legitCoins = legitCoins;
        myLegitCoins = legitCoins;
        hideLoadingScreen();
    }

    private List<AdCoin> loadUsersCoins(){
        List<AdCoin> myAdCoins = new ArrayList<>();

        Gson gson = new Gson();
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants.USERS_COIN_LIST, MODE_PRIVATE);
        String storedHashMapString = prefs.getString(Constants.USERS_COIN_LIST, "nil");

        if(!storedHashMapString.equals("nil")) {
            java.lang.reflect.Type type = new TypeToken<List<AdCoin>>() {}.getType();
            myAdCoins = gson.fromJson(storedHashMapString, type);
        }
        return myAdCoins;
    }

    private int getTotalValueFromEachCoin(List<AdCoin> legitCoinsList){
        int amount = 0;
        for(AdCoin coin: legitCoinsList){
            amount+=coin.getValue();
        }
        return amount;
    }



}
