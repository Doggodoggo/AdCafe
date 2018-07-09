package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

/**
 * Created by bryon on 19/02/2018.
 */

public class FragmentSelectPaymentOptionBottomSheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private View mContentView;
    private boolean shouldShowOption = false;
    private int number = 0;
    private String mCategory;
    private boolean hasShownOption = false;


    public void setActivity(Activity activity){
        this.mActivity = activity;
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

    public void setTargetOptionData(boolean shouldShowOption, int numberOfPeople, String category){
        this.shouldShowOption = shouldShowOption;
        this.number = numberOfPeople;
        this.mCategory = category;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_select_pay_option_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        params.setMargins(15,-15,15,15);
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setHideable(false);
        }

        final TextView paymentMessage = contentView.findViewById(R.id.paymentMessage);
        final LinearLayout optionImages = contentView.findViewById(R.id.optionImages);
        Button cancelBtn = contentView.findViewById(R.id.cancelBtn);
        Button proceedBtn = contentView.findViewById(R.id.continueButton);
        final RadioButton rbCard = contentView.findViewById(R.id.radioButtonC);
        final RadioButton rbMpesa = contentView.findViewById(R.id.radioButtonM);

        final LinearLayout specificOnlyLayout = contentView.findViewById(R.id.specificOnlyLayout);
        TextView message = contentView.findViewById(R.id.usersInfo);
        final RadioButton yes = contentView.findViewById(R.id.radioButtonYes);
        final RadioButton no = contentView.findViewById(R.id.radioButtonNo);

        if(shouldShowOption){
            yes.setText("Yes, Pay only for the "+number+" users.");
            message.setText(String.format("As of now, there are %d users interested in %s. You can pay for these users only. However if they increase, the new users will not see your advert.", number, mCategory));

            proceedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!hasShownOption){
                        hasShownOption = true;
                        specificOnlyLayout.setVisibility(View.VISIBLE);
                        paymentMessage.setVisibility(View.GONE);
                        optionImages.setVisibility(View.GONE);
                    }else{
                        Variables.isOnlyTargetingKnownUsers = yes.isChecked();
                        setDetailsAndProceed();
                        dismiss();
                    }
                }
            });
        }else{
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            proceedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setDetailsAndProceed();
                    dismiss();
                }
            });
        }

        FrameLayout bottomSheet = dialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.dialog_bg);

    }

    private void setDetailsAndProceed(){
//        if(rbCard.isChecked()) Variables.paymentOption = Constants.BANK_OPTION;
//                else if(rbMpesa.isChecked())Variables.paymentOption = Constants.MPESA_OPTION;
//                else Variables.paymentOption = Constants.BANK_OPTION;

        Variables.paymentOption = Constants.MPESA_OPTION;
        Intent intent = new Intent("PROCEED_CARD_DETAILS_PART");
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }

}
