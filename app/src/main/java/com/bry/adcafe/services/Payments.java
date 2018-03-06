package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Payment.Lipisha.Payment;
import com.bry.adcafe.Variables;
import com.lipisha.sdk.LipishaClient;
import com.lipisha.sdk.api.LipishaAPI;
import com.lipisha.sdk.response.*;


import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by bryon on 27/09/2017.
 */

public class Payments {
    private final String TAG = "Payments";
    private final String BASE_URL = LipishaClient.PROD_BASE_URL;
    private final String API_KEY = "6dfdfda338495033842c60c3ea9fea75";
    private final String API_SIGNATURE = "HRxaYAoEUmgttISeHr+M3DAxLN4j0o0YivAmZHn91fGhIww+ZfjvTvp4TNpdixy1ybEJNhnlJzFPsM2uzTuQjxrszTU9DSv9SYiAlT2UG5LNg+3lIo2X4GeV1ACBtRfkadnBxffzyjqYzR6ULgsu85xudTVem30iiJXf5JuyomQ=";
    private final String mAccountNo = "12663";
    private final String mMpesaAccountNo = "12579";
    private final String mMpesaPayOptionString = "Paybill (M-Pesa)";
    private final String mCurrency = "KES";
    private final String mCountry = "KENYA";
    private LipishaClient lipishaClient;

    private boolean isConfirmingPayments = false;
    private Handler h = new Handler();
    private Runnable r;
    private boolean isStoppingChecker = false;
    private final int delayMills = 3000;


    public Payments(){
//        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
    }

    public  void makeBankPayment(final String failedIntentFilter,final String intentFilter, final Context context,String cardNo, String expiry,
                                 String securityCode, String zipCode, float amount,String name,String address,String state){
       Payment pay = new Payment();
       pay.makeBankPayment(failedIntentFilter,intentFilter,context,cardNo,expiry,securityCode,zipCode,amount,name,address,state);
//        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
//        lipishaClient.authorizeCardTransaction(mAccountNo, cardNo, address, "", expiry, name,
//                state, mCountry, zipCode, securityCode, amount, mCurrency).enqueue(new Callback<CardTransactionResponse>() {
//            @Override
//            public void onResponse(Call<CardTransactionResponse> call, Response<CardTransactionResponse> response) {
//                Log.d(TAG,"RESPONSE: "+response.message());
//
//                CardTransactionResponse requestResponse = response.body();
//                Log.d(TAG,"Status is: "+requestResponse.getStatus());
//                Log.d(TAG,"Transaction transaction index is: "+requestResponse.getTransactionIndex());
//                Log.d(TAG,"Transaction transaction reference is: "+requestResponse.getTransactionReference());
//
//                if(requestResponse.getTransactionReference()!=null && requestResponse.getTransactionIndex()!=null){
//                    completeBankPayments(intentFilter,context,failedIntentFilter,requestResponse.getTransactionReference(),requestResponse.getTransactionIndex());
//                }else{
//                    Log.d(TAG,"There was an error : "+requestResponse.getStatusDescription());
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<CardTransactionResponse> call, Throwable t) {
//                Log.d(TAG,"There was an error : "+t.getMessage());
//                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
//            }
//        });
    }

    private void completeBankPayments(final String intentFilter, final Context context,final String failedIntentFilter,
                                             String transactionReference, String transactionIndex) {
        lipishaClient.completeCardTransaction(transactionIndex,transactionReference).enqueue(new Callback<CardTransactionResponse>() {
            @Override
            public void onResponse(Call<CardTransactionResponse> call, Response<CardTransactionResponse> response) {
                CardTransactionResponse requestResponse = response.body();
                if(requestResponse.getTransactionIndex()!=null && requestResponse.getTransactionReference()!=null){
                    Log.d(TAG,"Transaction has been successfully finished");
                    Variables.transactionID = requestResponse.getTransactionReference();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }else{
                    Log.d(TAG,"There was an error : "+response.message());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                }
            }

            @Override
            public void onFailure(Call<CardTransactionResponse> call, Throwable t) {
                Log.d(TAG,"There was an error"+t.getMessage());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
            }
        });
    }





    public void requestMpesaPayment(final String failedIntentFilter, final String intentFilter, final Context context,
                                           String amount, String phoneNo, final String reference){
//        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
        Log.d(TAG,"Starting mPesa request for money for payments");
        lipishaClient.requestMoney(API_KEY,API_SIGNATURE,mMpesaAccountNo,phoneNo,mMpesaPayOptionString,amount,mCurrency,reference)
                .enqueue(new Callback<RequestResponse>() {
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                if(response.body().getStatus().getStatusCode().equals("0000")){
                    Log.d(TAG,"The call was a success : "+response.body().getStatus().getStatusDescription());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }else{
                    RequestResponse res= response.body();
                    Log.d(TAG,"Request failed : "+response.body().getStatus().getStatusDescription());
                }
            }

            public void onFailure(Call<RequestResponse> call, Throwable t) {
               Log.d(TAG,"There was an error : "+t.getMessage());
                t.printStackTrace();
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
            }
        });
    }

    public void confirmPayments(final String failedIntentFilter, final String intentFilter, final Context context,
                                       String transactionID){
        if(!isConfirmingPayments){
            isConfirmingPayments = true;
            lipishaClient.confirmTransaction(transactionID).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    TransactionResponse res = response.body();
                    if(res.getTransaction().getTransactionStatus().equals("Completed") &&!isStoppingChecker){
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                    }else{
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                    }
                    isConfirmingPayments = false;
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Log.d(TAG,"There was an error : "+t.getMessage());
                    if(!isStoppingChecker) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                        isConfirmingPayments = false;
                    }
                }
            });
        }

    }




    public void startRecursiveCheckerForConfirmingPayments(final String failedIntentFilter, final String intentFilter,
                                                                   final Context context, final String transactionID){

        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Starting checker for confirming payments");
                confirmTransactionBackground(failedIntentFilter,intentFilter,context,transactionID);
                h.postDelayed(r, delayMills);
            }
        };
        h.postDelayed(r, delayMills);
    }

    private void confirmTransactionBackground(final String failedIntentFilter, final String intentFilter, final Context context,
                                                     String transactionID){
        if(!isConfirmingPayments){
            isConfirmingPayments = true;
            lipishaClient.confirmTransaction(transactionID).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    TransactionResponse res = response.body();
                    if(res.getTransaction().getTransactionStatus().equals("Completed") && !isStoppingChecker){
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                        stopRecursiveChecker();
                    }
                    isConfirmingPayments = false;
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Log.d(TAG,"There was an error : "+t.getMessage());
                    if(!isStoppingChecker) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                        isConfirmingPayments = false;
                    }
                }
            });
        }

    }




    public void stopRecursiveChecker(){
        isStoppingChecker = true;
        try{
            h.removeCallbacks(r);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void makePayouts(final String failedIntentFilter, final String intentFilter, final Context context,
                                   String phoneNo, String amount){
       Payment payment = new Payment();
//       payment.makePayouts(failedIntentFilter,intentFilter,context,phoneNo,amount);
//        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
//        lipishaClient.sendMoney(phoneNo,Integer.parseInt(amount),mMpesaAccountNo).enqueue(new Callback<Payout>() {
//            @Override
//            public void onResponse(Call<Payout> call, Response<Payout> response) {
//                Payout res = response.body();
//                if(res.getStatusDescription().equals("Balance Found")){
//                    Variables.transactionID = res.getReference();
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
//                }else{
//                    Log.d(TAG,"Payout failed: "+res.getStatusDescription());
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Payout> call, Throwable t) {
//                Log.d(TAG,"Call: "+call.toString());
//                Log.d(TAG,"Api call failed : "+t.getMessage());
//                t.printStackTrace();
//                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
//            }
//        });
    }



}
