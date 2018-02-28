package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

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
    private String mAdViewingDate;
    private long mConstantAmountPerUserTargeted;
    private String mCategory;
    private String mUploaderEmail;
    private String mName;
    private long mAmountToBePaid;
    private double chargeForPayment;
    private double paymentTotals;

    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setDetails(long targetedUsers, long constantAmountPerUser, String adViewingDate,
                           String category, String uploaderEmail, String name){
        this.mTargetedUsers = targetedUsers;
        this.mAdViewingDate = adViewingDate;
        this.mConstantAmountPerUserTargeted = constantAmountPerUser;
        Log.d("ModalbottomSheet","Constant amount per ad: "+constantAmountPerUser);
        this.mCategory = category;
        this.mUploaderEmail = uploaderEmail;
        this.mName = name;
        this.mAmountToBePaid = mTargetedUsers*mConstantAmountPerUserTargeted;
        this.chargeForPayment = mAmountToBePaid* Constants.TOTAL_MPESA_PAYOUT_PERCENTAGE;

        this.paymentTotals = mAmountToBePaid+chargeForPayment;
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
        View contentView = View.inflate(getContext(), R.layout.fragment_mpesa_pay_bottmsheet, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        mEnterPaymentDetailsPart = contentView.findViewById(R.id.enterPayoutDetailsPart);
        mConfirmLayout = contentView.findViewById(R.id.confirmLayout);
        loadEnterDetailsPart();
    }

    private void loadEnterDetailsPart() {
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);

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
                }else if(phoneEdit.getText().toString().trim().length()<10){
                    phoneEdit.setError("That's not a real phone number.");
                }else{
                    String phoneNo = phoneEdit.getText().toString().trim();
                    try{
                        Integer.parseInt(phoneNo);
                        mPhoneNo = phoneNo;
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
        mConfirmLayout.animate().translationX(0).setDuration(150);

        TextView targetingView = mContentView.findViewById(R.id.targetingNumber);
        TextView dateView = mContentView.findViewById(R.id.date);
        TextView categoryView = mContentView.findViewById(R.id.category);
        TextView userEmailView = mContentView.findViewById(R.id.userEmail);
        TextView amountToBePaidView = mContentView.findViewById(R.id.amountToBePaid);
        TextView phoneToPayVew = mContentView.findViewById(R.id.payingPhoneNumber);
        TextView transactionCostView = mContentView.findViewById(R.id.transationCost);

        targetingView.setText(Html.fromHtml("Targeting : <b>" + Long.toString(mTargetedUsers) + " users.</b>"));
        dateView.setText(Html.fromHtml("Ad Viewing Date : <b>" + mAdViewingDate + "</b> (DD/MM/YYYY)"));
        categoryView.setText(Html.fromHtml("Category : <b>" + mCategory + "</b>"));
        userEmailView.setText(Html.fromHtml("Uploader : <b>" + mUploaderEmail + "</b>"));
        amountToBePaidView.setText(Html.fromHtml("Amount To Be Paid: <b>" + mAmountToBePaid + "Ksh.</b>"));
        phoneToPayVew.setText(Html.fromHtml("Paying phone number : <b>" + mPhoneNo + "</b>"));
        transactionCostView.setText(Html.fromHtml("Transaction cost amount : <b>" + chargeForPayment + "Ksh.</b>"));

        mContentView.findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Variables.phoneNo = mPhoneNo;
                Variables.amountToPayForUpload = paymentTotals;
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYMENTS_INTENT"));
            }
        });
    }

}
