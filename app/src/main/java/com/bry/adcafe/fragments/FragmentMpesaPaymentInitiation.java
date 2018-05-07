package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Payment.mpesaApi.Mpesaservice;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.Payments;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by bryon on 28/02/2018.
 */

public class FragmentMpesaPaymentInitiation  extends DialogFragment {
    private final String TAG = "MpesaPaymentInitiation";
    private Context mContext;
    private double mAmount;
    private String mPhoneNo;
    private Payments mPayment;
    private Mpesaservice mpesaService;
    private String mTransactionId;
    private ProgressBar prog;


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

        Button cancelBtn = rootView.findViewById(R.id.cancelBtn);
        prog = rootView.findViewById(R.id.progBr);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Button restartBtn = rootView.findViewById(R.id.restartButton);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prog.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartPaymentRequest();
                    }
                }, 5000);
            }
        });
        prog.setVisibility(View.VISIBLE);
        startPaymentProcess();
        return rootView;
    }

    private void startPaymentProcess() {
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        mTransactionId= "TRANS"+pushRef.getKey();
        Log.d(TAG,"Transaction Id is : "+mTransactionId);
        String SUCCESSFUL_REQUEST = "SUCCESSFUL_REQUEST"+mTransactionId;
        String FAILED_REQUEST = "FAILED_REQUEST"+mTransactionId;


//        String newPhoneNo = "254"+mPhoneNo.substring(1);
//        Log.d(TAG,"new Phone no is: "+newPhoneNo);
        String email = Variables.mpesaEmail;
        int ammount = (int) mAmount;
//        String amount = Integer.toString(ammount);

//        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")) ammount = 20;
//        mPayment = new Payments(mContext,SUCCESSFUL_REQUEST,FAILED_REQUEST);
//        mPayment.startMpesaPayment(mTransactionId,mTransactionId,ammount,mPhoneNo,email);

        mpesaService = new Mpesaservice("IkcJaREeuzdn4Coxg9DvGQLz3CY29KQS","W0UyjgWR7LjJuRog");
        try {
            mpesaService.authenticate();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mpesaService.authenticateThenPayments("20","600323");
//        try {
//            mpesaService.C2BSimulation("601465","CustomerPayBillOnline","20","254708374149","Testingess");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForCompleteTransaction,
                new IntentFilter(SUCCESSFUL_REQUEST));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedSendingRequest,
                new IntentFilter("STK-PUSHED"+SUCCESSFUL_REQUEST));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFailedToSendRequest,
                new IntentFilter(FAILED_REQUEST));
    }

    private void restartPaymentRequest() {
        mPayment.stopRecursiveChecker();
        removeTheseGodDamnReceivers();
        startPaymentProcess();
    }

    @Override
    public void dismiss(){
        mPayment.stopRecursiveChecker();
        removeTheseGodDamnReceivers();
        super.dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        mPayment.stopRecursiveChecker();
        removeTheseGodDamnReceivers();
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }



    private BroadcastReceiver mMessageReceiverForFailedToSendRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast has been received that request has failed.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSendingRequest);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);

        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedSendingRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast has been received that request for pay is successful.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFailedToSendRequest);
            prog.setVisibility(View.INVISIBLE);
//            listenForCompletePayments();
        }
    };




    private void listenForCompletePayments() {
        String SUCCESSFUL_PAYMENTS = "SUCCESSFUL_PAYMENTS"+mTransactionId;
        String FAILED_PAYMENTS = "FAILED_PAYMENTS"+mTransactionId;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForCompleteTransaction,
                new IntentFilter(SUCCESSFUL_PAYMENTS));

//        mPayment.startRecursiveCheckerForConfirmingPayments(FAILED_PAYMENTS,SUCCESSFUL_PAYMENTS,mContext,mTransactionId);
    }

    private BroadcastReceiver mMessageReceiverForCompleteTransaction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast has been received that transaction is complete.");
            Variables.transactionID = mTransactionId;
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("FINISHED_MPESA_PAYMENTS"));
            dismiss();
        }
    };

    private void removeTheseGodDamnReceivers(){//lol
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSendingRequest);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFailedToSendRequest);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForCompleteTransaction);
    }

}
