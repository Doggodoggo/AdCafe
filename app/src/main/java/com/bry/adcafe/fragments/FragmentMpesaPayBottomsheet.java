package com.bry.adcafe.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.TargetedUser;
import com.bry.adcafe.services.TimeManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 27/02/2018.
 */

public class FragmentMpesaPayBottomsheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private View mContentView;
    private String mPhoneNo;
    private LinearLayout mEnterPaymentDetailsPart;
    private LinearLayout mConfirmLayout;

    private long mTargetedUsers;
    private long mTargetedUsersBeforeFiltering;
    private String mAdViewingDate;
    private long mConstantAmountPerUserTargeted;
    private String mCategory;
    private String mUploaderEmail;
    private String mName;

    private long mAmountToBePaid;
    private double chargeForPayment;
    private double paymentTotals;
    private double VATamm;

    private List<TargetedUser> targetedUserData;



    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setDetails(long targetedUsers, long constantAmountPerUser, String adViewingDate,
                           String category, String uploaderEmail, String name, List<TargetedUser> targetedUserDatas) {
        this.targetedUserData = targetedUserDatas;
        this.mTargetedUsersBeforeFiltering = targetedUsers;
        this.mTargetedUsers = targetedUsers;
        this.mAdViewingDate = adViewingDate;
        this.mConstantAmountPerUserTargeted = constantAmountPerUser;
        Log.d("ModalbottomSheet","Constant amount per ad: "+constantAmountPerUser);
        this.mCategory = category;
        this.mUploaderEmail = uploaderEmail;
        this.mName = name;

        if(Variables.isTargetingDataSet()) this.mTargetedUsers = targetedUserDatas.size();

        this.mAmountToBePaid = mTargetedUsers*(mConstantAmountPerUserTargeted+Constants.MPESA_CHARGES);
        this.VATamm = getVATAmmount(mTargetedUsers*(Variables.getUserCpvFromTotalPayPerUser((int)mConstantAmountPerUserTargeted)));
        this.paymentTotals = mAmountToBePaid+VATamm;
        this.chargeForPayment = getChargeForTransaction(paymentTotals);
        paymentTotals+=chargeForPayment;
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
        View contentView = View.inflate(getContext(), R.layout.fragment_mpesa_pay_bottomsheett, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        params.setMargins(15,-15,15,15);
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setHideable(false);
        }
        mEnterPaymentDetailsPart = contentView.findViewById(R.id.enterPayoutDetailsPart);
        mConfirmLayout = contentView.findViewById(R.id.confirmLayout);
        loadEnterDetailsPart();
        FrameLayout bottomSheet = dialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.dialog_bg);
    }

    private void loadEnterDetailsPart() {
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
        final EditText emailEdit = mContentView.findViewById(R.id.emailEditText);
        emailEdit.setText(mUploaderEmail);
        setPhoneField();

        phoneEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mContentView.findViewById(R.id.continueButton2).performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });

        mContentView.findViewById(R.id.cancelBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mContentView.findViewById(R.id.continueButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if(phoneEdit.getText().toString().trim().equals("")){
                    phoneEdit.setError("We need your phone number.");
                }else if(emailEdit.getText().toString().trim().equals("")){
                    emailEdit.setError("We need your email address.");
                }else if(phoneEdit.getText().toString().trim().length()<10){
                    phoneEdit.setError("That's not a real phone number.");
                }else{
                    String phoneNo = phoneEdit.getText().toString().trim();
                    String email = emailEdit.getText().toString().trim();
                    try{
                        Integer.parseInt(phoneNo);
                        mPhoneNo = phoneNo;
                        mUploaderEmail = email;
                        updatePhoneNumber(mPhoneNo);
                        mEnterPaymentDetailsPart.setVisibility(View.GONE);
                        loadConfirmDetailsPart();
                    }catch (Exception e){
                        e.printStackTrace();
                        phoneEdit.setError("That's not a real phone number.");
                    }
                }
            }
        });
    }

    private void loadConfirmDetailsPart() {
        mConfirmLayout.setVisibility(View.VISIBLE);
        mConfirmLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());

        TextView targetingView = mContentView.findViewById(R.id.targetingNumber);
        TextView dateView = mContentView.findViewById(R.id.date);
        TextView categoryView = mContentView.findViewById(R.id.category);
        TextView userEmailView = mContentView.findViewById(R.id.userEmail);
        TextView amountToBePaidView = mContentView.findViewById(R.id.amountToBePaid);
        TextView phoneToPayVew = mContentView.findViewById(R.id.payingPhoneNumber);
        TextView transactionCostView = mContentView.findViewById(R.id.transationCost);
        TextView vatCostView = mContentView.findViewById(R.id.vatCost);

        targetingView.setText(Html.fromHtml("Targeting : <b>" + Long.toString(mTargetedUsersBeforeFiltering) + " users.</b>"));
        if(Variables.isTargetingDataSet()){
            targetingView.setText(Html.fromHtml("Targeting : <b>" +
                    Long.toString(mTargetedUsersBeforeFiltering) + " users.</b> However, <b>"+
                    Long.toString(mTargetedUsers)+"</b> will be reached."));
        }
        dateView.setText(Html.fromHtml("Ad Viewing Date : <b>" + mAdViewingDate + "</b> (DD/MM/YYYY)"));
        categoryView.setText(Html.fromHtml("Category : <b>" + mCategory + "</b>"));
        userEmailView.setText(Html.fromHtml("Uploader : <b>" + mUploaderEmail + "</b>"));
        amountToBePaidView.setText(Html.fromHtml("Amount To Be Paid: <b>" + paymentTotals + "Ksh.</b>"));
        phoneToPayVew.setText(Html.fromHtml("Paying phone number : <b>" + mPhoneNo + "</b>"));
        transactionCostView.setText(Html.fromHtml("Transaction cost amount : <b>" + chargeForPayment + "Ksh.</b>"));
        vatCostView.setText(Html.fromHtml("VAT amount : <b>" + VATamm + "Ksh.</b>"));

        mContentView.findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.isTargetingDataSet() && mTargetedUsers==0){
                    Toast.makeText(mActivity,"You can't target 0 users.",Toast.LENGTH_SHORT).show();
                }else {
                    if (isOnline(mActivity)) {
                        dismiss();
                        Variables.phoneNo = mPhoneNo;
                        Variables.mpesaEmail = mUploaderEmail;
                        Variables.amountToPayForUpload = paymentTotals;
                        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYMENTS_INTENT"));
                    } else {
                        Toast.makeText(mActivity, "Please have an internet connection to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }



    private void setPhoneField(){
        String number = mActivity.getSharedPreferences(Constants.PHONE_NUMBER, MODE_PRIVATE).getString(Constants.PHONE_NUMBER, "b");
        if(!number.equals("b")){
            final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
            phoneEdit.setText(number);
        }
    }

    private void updatePhoneNumber(String phone){
        SharedPreferences pref = mActivity.getSharedPreferences(Constants.PHONE_NUMBER, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putString(Constants.PHONE_NUMBER,phone);
        editor.apply();
    }

    private void setPhoneField2(){
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
        TelephonyManager tMgr = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_SMS}, 1);
            return;
        } else {
            if (tMgr != null) {
                String mPhoneNumber = tMgr.getLine1Number();
                phoneEdit.setText(mPhoneNumber);
            }
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
            if (tMgr != null) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String mPhoneNumber = tMgr.getLine1Number();
                phoneEdit.setText(mPhoneNumber);
            }
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private int getChargeForTransaction(double amount){
//        if(amount<2500){
//            return 33;
//        }else if(amount<5000){
//            return 55;
//        }else if(amount<10000){
//            return 83;
//        }else if(amount<35000){
//            return 110;
//        }else if(amount<50000){
//            return 198;
//        }else{
//            return 220;
//        }
        return 0;
    }

    private double getVATAmmount(long amount){
       return amount*Constants.VAT_CONSTANT;
    }


    private long getNumberOfUsersAfterFiltering(){
        List<TargetedUser> usersQualified = new ArrayList<>(targetedUserData);
        if(!Variables.genderTarget.equals("")){
            for(TargetedUser user: targetedUserData){
                if(!user.getGender().equals(Variables.genderTarget)){
                   if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(Variables.ageGroupTarget!=null){
            for(TargetedUser user:targetedUserData){
                if(user.getBirthday()!=0) {
                    Integer userAge = getAge(user.getBirthYear(), user.getBirthMonth(), user.getBirthday());
                    if(userAge < Variables.ageGroupTarget.getStartingAge() || userAge > Variables.ageGroupTarget.getFinishAge()){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
                }
            }
        }
        if(!Variables.locationTarget.isEmpty()){
            for(TargetedUser user:targetedUserData){
                if(locationContained(user.getUserLocations())==0){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        return (long)usersQualified.size();
    }

    private Integer getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = TimeManager.getCal();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return age;
    }

    private int locationContained(List<LatLng> checkLocalLst){
        int locations = 0;
        for(LatLng latlngUser : checkLocalLst){
            for(LatLng latlngAdv: Variables.locationTarget){
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

}
