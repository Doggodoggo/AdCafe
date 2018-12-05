package com.bry.adcafe.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.ui.Dashboard;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 13/02/2018.
 */

public class ChangeCPVFragment extends DialogFragment implements View.OnClickListener{
    private Button cancelBtn1;
    private Button continueBtn;
    private Button cancelBtn2;
    private Button changeBtn;

    private LinearLayout mainLayout;
    private LinearLayout chooseAmountLayout;
    private Context mContext;
    private View mRootView;

    public void setContext(Context context){
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_change_cpv, container, false);
        mRootView = rootView;
        cancelBtn1 = rootView.findViewById(R.id.cancelBtn);
        continueBtn = rootView.findViewById(R.id.continueBtn);
        cancelBtn2 = rootView.findViewById(R.id.cancelButton);
        changeBtn = rootView.findViewById(R.id. submitButton);

        mainLayout = rootView.findViewById(R.id.mainLayout);
        chooseAmountLayout = rootView.findViewById(R.id.chooseAmountLayout);
        TextView currentCpv = rootView.findViewById(R.id.cuurentCPV);
        currentCpv.setText(String.format("Current charge : %dKsh.", Variables.constantAmountPerView));

        boolean hasChangedPrev =  mContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE)
                .getBoolean(Constants.IS_CHANGING_CPV, false);
        if(hasChangedPrev){
            int hasChangedPrevValue = mContext.getSharedPreferences(Constants.NEW_CPV, MODE_PRIVATE)
                    .getInt(Constants.NEW_CPV, Variables.constantAmountPerView);
            TextView newCPV = rootView.findViewById(R.id.newCPV);
            newCPV.setVisibility(View.VISIBLE);
            newCPV.setText(String.format("New set charge : %dKsh.", hasChangedPrevValue));
        }

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        onclicks();
        return rootView;
    }


    @Override
    public void onClick(View v) {
        if(v == cancelBtn1){
            dismiss();
        } else if(v == cancelBtn2){
            dismiss();
        } else if(v == continueBtn){
//            mainLayout.setVisibility(View.GONE);
            chooseAmountLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
        }else if(v == changeBtn){
            int cpv;
            RadioButton button3 = mRootView.findViewById(R.id.radioButton3);
            RadioButton button5 = mRootView.findViewById(R.id.radioButton5);
            RadioButton button8 = mRootView.findViewById(R.id.radioButton8);
            if(button3.isChecked()){
                cpv = 3;
            }else if(button5.isChecked()){
                cpv = 5;
            }else{
                cpv = 8;
            }
            if(isNetworkConnected(mContext)) makeChanges(cpv);
            else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
        }
    }

    private void onclicks(){
        cancelBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.animate().setDuration(1000).translationX(-200).alpha(0f)
                        .setInterpolator(new LinearOutSlowInInterpolator());
                chooseAmountLayout.setVisibility(View.VISIBLE);
                chooseAmountLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = mRootView.findViewById(R.id.radioButton1);
                RadioButton button5 = mRootView.findViewById(R.id.radioButton3);
                RadioButton button8 = mRootView.findViewById(R.id.radioButton6);
                if(button3.isChecked()) cpv = 1;
                else if(button5.isChecked()) cpv = 3;
                else cpv = 6;
                if(isNetworkConnected(mContext)) makeChanges(cpv);
                else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void makeChanges(int newCpv) {
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        boolean hasChangedPrev = prefs2.getBoolean(Constants.IS_CHANGING_CPV, false);

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.NEW_CPV, MODE_PRIVATE);
        int hasChangedPrevValue = prefs.getInt(Constants.NEW_CPV, Variables.constantAmountPerView);

        if(hasChangedPrev){
            new DatabaseManager().setBooleanForResetSubscriptions(newCpv, mContext);
            Intent intent = new Intent("SHOW_PROMPT");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            dismiss();
        }else{
            if (newCpv != Variables.constantAmountPerView) {
                new DatabaseManager().setBooleanForResetSubscriptions(newCpv, mContext);
                Intent intent = new Intent("SHOW_PROMPT");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dismiss();
            } else {
                Toast.makeText(mContext, "That's already your current charge.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
