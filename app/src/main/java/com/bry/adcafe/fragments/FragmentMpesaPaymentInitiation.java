package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bry.adcafe.R;

/**
 * Created by bryon on 28/02/2018.
 */

public class FragmentMpesaPaymentInitiation  extends DialogFragment {
    private Context mContext;
    private double mAmount;
    private String mPhoneNo;


    public void setContext(Context context){
        this.mContext = context;
    }

    public void setDetails(double amount,String phoneNo){
        this.mAmount = amount;
        this.mPhoneNo = phoneNo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_mpesa_payment_initiation, container, false);

        return rootView;
    }

}
