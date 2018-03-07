package com.bry.adcafe.Payment.Lipisha;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Variables;
import com.lipisha.sdk.response.AccountBalance;
import com.lipisha.sdk.response.AccountFloat;
import com.lipisha.sdk.response.AirtimeDisbursement;
import com.lipisha.sdk.response.CardTransactionResponse;
import com.lipisha.sdk.response.MultiTransactionResponse;
import com.lipisha.sdk.response.Payout;
import com.lipisha.sdk.response.RequestResponse;
import com.lipisha.sdk.response.SMSReport;
import com.lipisha.sdk.response.Transaction;
import com.lipisha.sdk.response.TransactionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by amati on 12/22/2017.
 */

public class Payment {
    private final String TAG = "Payment";
    private static final String BASE_URL = LipishaClient.PROD_BASE_URL;
    public static final String API_KEY = "6dfdfda338495033842c60c3ea9fea75";
    public static final String API_SIGNATURE = "HRxaYAoEUmgttISeHr+M3DAxLN4j0o0YivAmZHn91fGhIww+ZfjvTvp4TNpdixy1ybEJNhnlJzFPsM2uzTuQjxrszTU9DSv9SYiAlT2UG5LNg+3lIo2X4GeV1ACBtRfkadnBxffzyjqYzR6ULgsu85xudTVem30iiJXf5JuyomQ=";

    public static final String API_VERSION = "1.3.0";
    public static final String API_TYPE = "Callback";
    public static final String FLOAT_ACCOUNT_NUMBER = "12579";
    public static final String PAYOUT_ACCOUNT_NUMBER = "11818";


    private final String mMpesaAccountNo = "12579";
    private final String mAccountNo = "12663";
    private final String mMpesaPayOptionString = "Paybill (M-Pesa)";
    private final String mCurrency = "KES";
    private final String mCountry = "KENYA";


    public static final String AIRTIME_ACCOUNT_NUMBER = "11819";
    public static final String TEST_MOBILE_NUMBER = "0798075721";
    private LipishaClient lipishaClient;

    private boolean isConfirmingPayments = false;
    private Handler h = new Handler();
    private Runnable r;
    private boolean isStoppingChecker = false;
    private final int delayMills = 3000;

    public Payment() {
        lipishaClient = new LipishaClient(API_KEY, API_SIGNATURE, BASE_URL);
    }

    public void echo(String label, Object object) {
        Log.d(label,object.toString());
        System.out.printf("%s :: %s\n", label, object);
    }

    public void getBalance() {
        lipishaClient.getBalance().enqueue(new Callback<AccountBalance>() {
            public void onResponse(Call<AccountBalance> call, Response<AccountBalance> response) {
                AccountBalance balance = response.body();
                echo("Balance", balance.getBalance());
            }

            public void onFailure(Call<AccountBalance> call, Throwable throwable) {

                echo("Get balance", throwable.getMessage());
            }
        });
    }
    public void getFloatBalance() {
        lipishaClient.getFloat(FLOAT_ACCOUNT_NUMBER).enqueue(new Callback<AccountFloat>() {
            public void onResponse(Call<AccountFloat> call, Response<AccountFloat> response) {
                AccountFloat accountFloat = response.body();
                echo("Float", accountFloat.getFloat());
            }

            public void onFailure(Call<AccountFloat> call, Throwable throwable) {

                echo("Get float balance", throwable.getMessage());
            }
        });

    }

    public void sendMoney() {
        lipishaClient.sendMoney(TEST_MOBILE_NUMBER, 10, mAccountNo).enqueue(new Callback<Payout>() {
            public void onResponse(Call<Payout> call, Response<Payout> response) {
                Payout payout = response.body();
                Log.d(TAG,payout.getStatusResponse().getStatus());
                Log.d(TAG,payout.getStatusResponse().getStatusDescription());
                Log.d(TAG,""+payout.getStatusResponse().getStatusCode());
                try {
                    echo("Payout:Amount", payout.getAmount());
                    echo("Payout:Number", payout.getMobileNumber());
                    echo("Payout:Reference", payout.getReference());
                }catch (Exception e){
                    e.printStackTrace();
                }


            }

            public void onFailure(Call<Payout> call, Throwable throwable) {

                echo("Send Money", throwable.getMessage());
            }
        });
    }
    public void sendAirtime() {
        lipishaClient.sendAirtime(TEST_MOBILE_NUMBER, 100, AIRTIME_ACCOUNT_NUMBER, "SAF")
                .enqueue(new Callback<AirtimeDisbursement>() {
                    public void onResponse(Call<AirtimeDisbursement> call, Response<AirtimeDisbursement> response) {
                        AirtimeDisbursement airtimeDisbursement = response.body();
                        echo("Airtime:Amount", airtimeDisbursement.getAmount());
                        echo("Airtime:Number", airtimeDisbursement.getMobileNumber());
                        echo("Airtime:Reference", airtimeDisbursement.getReference());
                    }

                    public void onFailure(Call<AirtimeDisbursement> call, Throwable throwable) {

                        echo("Send Airtime", throwable.getMessage());
                    }
                });
    }
    public void sendSMS() {
        lipishaClient.sendSMS(TEST_MOBILE_NUMBER, FLOAT_ACCOUNT_NUMBER, "TEST MESSAGE").enqueue(new Callback<SMSReport>() {
            public void onResponse(Call<SMSReport> call, Response<SMSReport> response) {
                SMSReport smsReport = response.body();
                echo("SMS:Message", smsReport.getMessage());
                echo("SMS:Recipient", smsReport.getRecipient());
                echo("SMS:Cost", smsReport.getCost());
            }

            public void onFailure(Call<SMSReport> call, Throwable throwable) {

                echo("Send Sms", throwable.getMessage());
            }
        });
    }
    public void acknowledgeTransaction() {
        lipishaClient.confirmTransaction(new String[]{"B4F16908F"}).enqueue(new Callback<TransactionResponse>() {
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                TransactionResponse transactionResponse = response.body();
                Transaction transaction = transactionResponse.getTransaction();
                echo("Transaction:Id", transaction.getTransactionId());
                echo("Transaction:Status", transaction.getTransactionStatus());
            }

            public void onFailure(Call<TransactionResponse> call, Throwable throwable) {

                echo("Acknowledge Transactions", throwable.getMessage());
            }
        });
    }
    public void reverseTransaction() {
        lipishaClient.reverseTransaction("B4F16908F").enqueue(new Callback<MultiTransactionResponse>() {
            public void onResponse(Call<MultiTransactionResponse> call, Response<MultiTransactionResponse> response) {
                MultiTransactionResponse transactionResponse = response.body();
                Transaction transaction = transactionResponse.getTransactions().get(0);
                echo("Transaction:Id", transaction.getTransactionId());
                echo("Transaction:Status", transaction.getTransactionStatus());
            }

            public void onFailure(Call<MultiTransactionResponse> call, Throwable throwable) {

                echo("Reverse Transactions", throwable.getMessage());
            }
        });
    }
    public void getTransactions() {
        lipishaClient.getTransactions("B4F16908F", null, null, null,
                null, null, null, null, null, null, null, null, null, null, 10, 0).enqueue(new Callback<MultiTransactionResponse>() {
            public void onResponse(Call<MultiTransactionResponse> call, Response<MultiTransactionResponse> response) {
                MultiTransactionResponse transactionResponse = response.body();
                for (Transaction transaction : transactionResponse.getTransactions()) {
                    echo("Transaction:Id", transaction.getTransactionId());
                    echo("Transaction:Type", transaction.getTransactionType());
                    echo("Transaction:Amount", transaction.getTransactionAmount());
                    echo("Transaction:Date", transaction.getTransactionDate());
                }
            }

            public void onFailure(Call<MultiTransactionResponse> call, Throwable throwable) {
                echo("Get Transactions", throwable.getMessage());
            }
        });

    }
    public void requestMoney() {
        lipishaClient.requestMoney(API_KEY, API_SIGNATURE, FLOAT_ACCOUNT_NUMBER, TEST_MOBILE_NUMBER, "Paybill (M-Pesa)", "100",
                "KES", "TEST004").enqueue(new Callback<RequestResponse>() {
            public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                RequestResponse requestResponse = response.body();
                echo("RequestMoney:Reference", requestResponse.getContent().getReference());
                echo("RequestMoney:MobileNumber", requestResponse.getContent().getMobileNumber());
                echo("RequestMoney:Method", requestResponse.getContent().getMethod());
                echo("RequestMoney:Amount", requestResponse.getContent().getAmount());
                echo("RequestMoney:AccountNumber", requestResponse.getContent().getAccountNumber());

                echo("RequestMoney:StatusCode", requestResponse.getStatus().getStatusCode());
                echo("RequestMoney:Status", requestResponse.getStatus().getStatus());
                echo("RequestMoney:StatusDescription", requestResponse.getStatus().getStatusDescription());
            }

            public void onFailure(Call<RequestResponse> call, Throwable throwable) {
                echo("RequestMoney", throwable.getMessage());
            }
        });
    }




    public void requestMpesaPayment(final String failedIntentFilter, final String intentFilter, final Context context,
                                    String amount, String phoneNo, final String reference){
        Log.d(TAG,"Starting mPesa request for money for payments");
        lipishaClient.requestMoney(API_KEY,API_SIGNATURE,mMpesaAccountNo,phoneNo,mMpesaPayOptionString,amount,mCurrency,reference)
                .enqueue(new Callback<RequestResponse>() {
                    public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                        if(response.body().getStatus().getStatusCode().equals("0000") && response.body().getContent().getReference().equals(reference)){
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

    public void makePayouts(final String failedIntentFilter, final String intentFilter, final Context context,
                            String phoneNo, int amount){
        lipishaClient.sendMoney(phoneNo,amount,mAccountNo).enqueue(new Callback<Payout>() {
            @Override
            public void onResponse(Call<Payout> call, Response<Payout> response) {
                Payout res = response.body();
                if(res.getStatusDescription().equals("Balance Found")){
                    Variables.transactionID = res.getReference();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                }else{
                    Log.d(TAG,"Payout failed: "+res.getStatusDescription());
                    Log.d(TAG,res.getStatusResponse().getStatus());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                }
            }

            @Override
            public void onFailure(Call<Payout> call, Throwable t) {
                Log.d(TAG,"Call: "+call.toString());
                Log.d(TAG,"Api call failed : "+t.getMessage());
                t.printStackTrace();
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
            }
        });
    }

    public  void makeBankPayment(final String failedIntentFilter,final String intentFilter, final Context context,String cardNo, String expiry,
                                 String securityCode, String zipCode, float amount,String name,String address,String state){
        lipishaClient.authorizeCardTransaction(mAccountNo, cardNo, address, "", expiry, name,
                state, mCountry, zipCode, securityCode, amount, mCurrency).enqueue(new Callback<CardTransactionResponse>() {
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
                    Log.d(TAG,"There was an error : "+requestResponse.getStatusDescription());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
                }
            }

            @Override
            public void onFailure(Call<CardTransactionResponse> call, Throwable t) {
                Log.d(TAG,"There was an error : "+t.getMessage());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(failedIntentFilter));
            }
        });
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
        if(!isConfirmingPayments && isStoppingChecker){
            isConfirmingPayments = true;
            lipishaClient.confirmTransaction(transactionID).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    TransactionResponse res = response.body();
                    try {
                        if (res.getTransaction().getTransactionStatus().equals("Completed") && !isStoppingChecker) {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(intentFilter));
                            stopRecursiveChecker();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
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

}
