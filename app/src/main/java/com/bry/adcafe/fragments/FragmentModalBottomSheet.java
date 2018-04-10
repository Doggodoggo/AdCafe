package com.bry.adcafe.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 04/02/2018.
 */

public class FragmentModalBottomSheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private LinearLayout mEnterCardDetailsPart;
    private Button mContinueButton;
    private LinearLayout ConfirmDetailsPart;
    private Button mStartTransactionThenUpload;
    private LinearLayout CardHolderDetailsPart;

    private long mTargetedUsers;
    private long mConstantAmountPerUserTargeted;
    private String mAdViewingDate;
    private String mCategory;
    private String mUploaderEmail;
    private String mName;
    private long mAmountToBePaid;
    private View mContentView;
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
        this.chargeForPayment = mAmountToBePaid*Constants.TOTAL_PAYOUT_PERCENTAGE;

        paymentTotals = mAmountToBePaid+chargeForPayment;
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
        View contentView = View.inflate(getContext(), R.layout.fragment_modal_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        mEnterCardDetailsPart = contentView.findViewById(R.id.enterCardDetailsPart);
        mContinueButton = contentView.findViewById(R.id.continueButton);
        ConfirmDetailsPart = contentView.findViewById(R.id.confirmLayout);
        mStartTransactionThenUpload = contentView.findViewById(R.id.startButton);
        CardHolderDetailsPart = contentView.findViewById(R.id.cardHolderDetailsLayout);

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setHideable(false);
        }
        setUpPaymentsCard(contentView);
    }

    private void setUpPaymentsCard(View contentView) {
        final CardForm cardForm = contentView.findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .mobileNumberRequired(false)
                .actionLabel("Purchase")
                .setup(mActivity);
        cardForm.getPostalCodeEditText().setText("00200");

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardForm.isValid()){
                    mEnterCardDetailsPart.setVisibility(View.GONE);
                    showNextSlide();
                    setDetailsForConfirmation(cardForm);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    Variables.cvv = cardForm.getCvv();
                    Variables.cardNumber = cardForm.getCardNumber();
                    Variables.postalCode = cardForm.getPostalCode();
                    Variables.expiration = ""+cardForm.getExpirationMonth()+cardForm.getExpirationYear();
                    Variables.amountToPayForUpload = paymentTotals;
                }
                else{
                    if(cardForm.getCardNumber().isEmpty()){
                        cardForm.getCardEditText().performClick();
                        cardForm.getCardEditText().setError("Your card number!");
                    }else
                    if(cardForm.getExpirationYear().isEmpty()){
                        cardForm.getExpirationDateEditText().performClick();
                        cardForm.getExpirationDateEditText().setError("Your card's expiration date!");
                    }else
                    if(cardForm.getCvv().isEmpty()){
                        cardForm.getCvvEditText().performClick();
                        cardForm.getCvvEditText().setError("Your card's cvv.");
                    }
                    else Toast.makeText(mActivity.getApplication(),"Some of your details might be wrong.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mContentView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        cardForm.getPostalCodeEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mContinueButton.performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });

    }

    private void showNextSlide(){
        CardHolderDetailsPart.setVisibility(View.VISIBLE);
        CardHolderDetailsPart.animate().setDuration(140).translationX(0);
        final EditText firstName =  mContentView.findViewById(R.id.firstNameEditText);
        final EditText lastName = mContentView.findViewById(R.id.lastnameEditText);
        final EditText email = mContentView.findViewById(R.id.emailEditText);
        final EditText state = mContentView.findViewById(R.id.stateEditText);
        final EditText phone = mContentView.findViewById(R.id.phoneEditText);

        setPhoneField();

        email.setText(mUploaderEmail);
        try{
            firstName.setText(mName.toUpperCase());
        }catch (Exception e){
            e.printStackTrace();
        }
        state.setText("NAIROBI");

        mContentView.findViewById(R.id.cancelCHDBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardHolderDetailsPart.setVisibility(View.GONE);
                CardHolderDetailsPart.setTranslationX(500);
                mEnterCardDetailsPart.setVisibility(View.VISIBLE);
            }
        });
        phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mContentView.findViewById(R.id.continueCHDBtn).performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });
        mContentView.findViewById(R.id.continueCHDBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                String FirstNameString = firstName.getText().toString().trim();
                String LastNameString = lastName.getText().toString().trim();
                String emailString = email.getText().toString().trim();
                String stateText = state.getText().toString().trim();
                String phoneText = phone.getText().toString().trim();

                if(LastNameString.equals("")){
                    lastName.setError("Your last name is needed.");
                }if(FirstNameString.equals("")){
                    firstName.setError("Your first name is needed.");
                }else if(emailString.equals("")){
                    email.setError("We need your email.");
                }else if(stateText.equals("")){
                    state.setError("We need your Province/County.");
                }else if(phoneText.equals("")){
                    phone.setError("We need your Phone Number.");
                }else{
                    try{
                        Integer.parseInt(phoneText);
                        Variables.cardHolderName = LastNameString;
                        Variables.cardHolderFirstName = FirstNameString;
                        Variables.cardHolderLastName = LastNameString;
                        Variables.cardHolderEmail = emailString;
                        Variables.phoneNo = phoneText;
                        Variables.cardHolderState = stateText;
                        updatePhoneNumber(phoneText);
                        CardHolderDetailsPart.setVisibility(View.GONE);
                        showNextSlide2();
                    }catch (Exception e){
                        e.printStackTrace();
                        phone.setError("That's not a real phone number.");
                    }

                }
            }
        });
    }




    private void showNextSlide2() {
        startNextAnimation();
    }

    private void startNextAnimation(){
        ConfirmDetailsPart.setVisibility(View.VISIBLE);
        ConfirmDetailsPart.animate().setDuration(140).translationX(0);
        mStartTransactionThenUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYMENTS_INTENT"));
            }
        });
        mContentView.findViewById(R.id.backConfBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmDetailsPart.setVisibility(View.GONE);
                ConfirmDetailsPart.setTranslationX(400);
                CardHolderDetailsPart.setVisibility(View.VISIBLE);
            }
        });

    }

    private void setDetailsForConfirmation(CardForm card) {
        TextView targetingView = mContentView.findViewById(R.id.targetingNumber);
        TextView dateView = mContentView.findViewById(R.id.date);
        TextView categoryView = mContentView.findViewById(R.id.category);
        TextView userEmailView = mContentView.findViewById(R.id.userEmail);
        TextView amountToBePaidView = mContentView.findViewById(R.id.amountToBePaid);
        TextView cardToPayVew = mContentView.findViewById(R.id.cardNumber);
        TextView transactionCostView = mContentView.findViewById(R.id.transationCost);

        String strLastFourDi = card.getCardNumber().length() >= 4 ? card.getCardNumber().substring(card.getCardNumber().length() - 4): "";

        targetingView.setText(Html.fromHtml("Targeting : <b>" + Long.toString(mTargetedUsers) + " users.</b>"));
        dateView.setText(Html.fromHtml("Ad Viewing Date : <b>" + mAdViewingDate + "</b> (DD/MM/YYYY)"));
        categoryView.setText(Html.fromHtml("Category : <b>" + mCategory + "</b>"));
        userEmailView.setText(Html.fromHtml("Uploader : <b>" + mUploaderEmail + "</b>"));
        amountToBePaidView.setText(Html.fromHtml("Amount To Be Paid: <b>" + mAmountToBePaid + "Ksh.</b>"));
        cardToPayVew.setText(Html.fromHtml("Paying card number : <b>****" + strLastFourDi + "</b>"));
        transactionCostView.setText(Html.fromHtml("Transaction cost amount : <b>" + chargeForPayment + "Ksh.</b>"));


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
        TelephonyManager tMgr = (TelephonyManager) mActivity.getSystemService(mActivity.TELEPHONY_SERVICE);
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
            TelephonyManager tMgr = (TelephonyManager) mActivity.getSystemService(mActivity.TELEPHONY_SERVICE);
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

}
