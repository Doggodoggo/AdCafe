package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.lipisha.sdk.LipishaClient;
import com.lipisha.sdk.response.CardTransactionResponse;
import com.lipisha.sdk.response.Payout;
import com.lipisha.sdk.response.RequestResponse;
import com.lipisha.sdk.response.TransactionResponse;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by bryon on 27/09/2017.
 */

public class Payments {
    private static final String TAG = "Payments";
    private static LipishaClient lipishaClient;
    private static final String BASE_URL = LipishaClient.SANDBOX_BASE_URL;
    private static final String API_KEY = "5352b553494510c1a37ecf38af75637e";
    private static final String API_SIGNATURE = "ZrP18KxY5WNYuJ4b4OBfP/+Y93hd5fydy4l/YKuYPKeEZWywFubXIVbhopQ" +
            "HFXB7u9UdVUq8Zs9ItfVS21UDcOsLQ0M7OOgw3jef6QlqkiHy3Hsd+1xtQ2ZFu1qTipVWl82dJVtRDWWzLYazipTMyZVl6S609X5Hxf/OGudvPUA=";
    private static final String mAccountNo = "12345";
    private static final String mMpesaAccountNo = "12345";
    private static final String mMpesaPayOptionString = "Paybill (M-Pesa)";
    private static final String mCurrency = "KES";

    private static boolean isConfirmingPayments = false;
    private static Handler h = new Handler();
    private static Runnable r;
    private static boolean isStoppingChecker = false;
    private static final int delayMills = 3000;


    public static void makeBankPayment(final String failedIntentFilter,final String intentFilter, final Context context,String cardNo,
                                       String expiry, String securityCode, String zipCode, float amount){
        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
        lipishaClient.authorizeCardTransaction(mAccountNo, cardNo, "", "", expiry, "",
                "", "", zipCode, securityCode, amount, mCurrency).enqueue(new Callback<CardTransactionResponse>() {
            @Override
            public void onResponse(Call<CardTransactionResponse> call, Response<CardTransactionResponse> response) {
                Log.d(TAG,"RESPONSE: "+response.message());

                CardTransactionResponse requestResponse = response.body();
                Log.d(TAG,"Status is: "+requestResponse.getStatus());
                Log.d(TAG,"Transaction transaction index is: "+requestResponse.getTransactionIndex());
                Log.d(TAG,"Transaction transaction reference is: "+requestResponse.getTransactionReference());

                if(requestResponse.getTransactionReference()!=null && requestResponse.getTransactionIndex()!=null){
                    completeBankPayments(intentFilter,context,failedIntentFilter,requestResponse.getTransactionReference(),requestResponse.getTransactionIndex());
                }else{
                    Log.d(TAG,"There was an error"+requestResponse.getStatusDescription());
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

    private static void completeBankPayments(final String intentFilter, final Context context,final String failedIntentFilter,
                                             String transactionReference, String transactionIndex) {
        lipishaClient.completeCardTransaction(transactionIndex,transactionReference).enqueue(new Callback<CardTransactionResponse>() {
            @Override
            public void onResponse(Call<CardTransactionResponse> call, Response<CardTransactionResponse> response) {
                CardTransactionResponse requestResponse = response.body();
                if(requestResponse.getTransactionIndex()!=null && requestResponse.getTransactionReference()!=null){
                    Log.d(TAG,"Transaction was successfully done");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }else{
                    Log.d(TAG,"There was an error"+response.message());
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





    public static void requestMpesaPayment(final String failedIntentFilter, final String intentFilter, final Context context,
                                           String amount, String phoneNo, final String reference){
        lipishaClient.requestMoney(API_KEY,API_SIGNATURE,mMpesaAccountNo,phoneNo,mMpesaPayOptionString,
                amount,mCurrency, reference).enqueue(new Callback<RequestResponse>() {
            @Override
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                if(response.body().getStatus().getStatusCode().equals("0000")){
                    Log.d(TAG,"The call was a success : "+response.body().getStatus().getStatusDescription());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }
            }

            @Override
            public void onFailure(Call<RequestResponse> call, Throwable t) {
                Log.d(TAG,"There was an error : "+t.getMessage());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
            }
        });
    }

    public static void confirmPayments(final String failedIntentFilter, final String intentFilter, final Context context,
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




    private static void startRecursiveCheckerForConfirmingPayments(final String failedIntentFilter, final String intentFilter,
                                                                   final Context context, final String transactionID){
        r = new Runnable() {
            @Override
            public void run() {
                confirmTransactionBackground(failedIntentFilter,intentFilter,context,transactionID);
                h.postDelayed(r, delayMills);
            }
        };
        h.postDelayed(r, delayMills);
    }

    private static void confirmTransactionBackground(final String failedIntentFilter, final String intentFilter, final Context context,
                                                     String transactionID){
        if(!isConfirmingPayments){
            isConfirmingPayments = true;
            lipishaClient.confirmTransaction(transactionID).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    TransactionResponse res = response.body();
                    if(res.getTransaction().getTransactionStatus().equals("Completed") && !isStoppingChecker){
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
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




    public static void stopRecursiveChecker(){
        isStoppingChecker = true;
        h.removeCallbacks(r);
    }

    public static void makePayouts(final String failedIntentFilter, final String intentFilter, final Context context,
                                   String phoneNo, String amount){
        lipishaClient.sendMoney(phoneNo,Integer.parseInt(amount),mMpesaAccountNo).enqueue(new Callback<Payout>() {
            @Override
            public void onResponse(Call<Payout> call, Response<Payout> response) {
                Payout res = response.body();
                if(res.getStatusDescription().equals("Balance Found")){
                    String payoutId = res.getReference();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }else{
                    Log.d(TAG,"Payout failed: "+res.getStatusDescription());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                }
            }

            @Override
            public void onFailure(Call<Payout> call, Throwable t) {
                Log.d(TAG,"Api call failed : "+t.getMessage());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
            }
        });
    }


}
