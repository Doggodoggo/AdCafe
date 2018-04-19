package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bry.adcafe.Constants;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by bryon on 27/09/2017.
 */
public class Payments {
    private final String TAG = "Payments";
    private boolean canConfirmPayments = true;
    private Context mContext;
    private String mSuccessfulFilter;
    private String mFailedFilter;

    public Payments(Context context,String successfulTransactionFilter,String failedTransactionFilter){
        this.mContext= context;
        this.mSuccessfulFilter = successfulTransactionFilter;
        this.mFailedFilter = failedTransactionFilter;
    }



    public void startMpesaPayment(String orderId,String invoiceId,int amount,final String phoneNo,String email){
        Log.d(TAG,"ipayservice has started....");
        String dataString = Constants.live+orderId+invoiceId+amount+phoneNo+email+Constants.vid+Constants.curr+Constants.p1+
                Constants.p2+Constants.p3+Constants.p4+Constants.cbk+Constants.cst;
        String myGeneratedHash = generateHmac(dataString, Constants.key);
        Log.d(TAG,dataString);
        Log.d(TAG,myGeneratedHash);


        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        //Request request = null;
        RequestBody formBody = new FormBody.Builder()
                .add("live",Integer.toString(Constants.live))
                .add("oid",orderId)
                .add("inv",invoiceId)
                .add("amount",Integer.toString(amount))
                .add("tel",phoneNo)
                .add("eml",email)
                .add("vid",Constants.vid)
                .add("curr",Constants.curr)
                .add("p1",Constants.p1)
                .add("p2",Constants.p2)
                .add("p3",Constants.p3)
                .add("p4",Constants.p4)
                .add("cbk",Constants.cbk)
                .add("cst",Integer.toString(Constants.cst))
                .add("crl",Integer.toString(Constants.crl))
                .add("hash",myGeneratedHash)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"response gotten");
                Log.d(TAG,""+response.message());
                Log.d(TAG,""+response.code());
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    if (response.isSuccessful()){
                        Log.d(TAG,""+response.body().toString());
                        JSONObject statusJSON = new JSONObject(jsonData);
                        JSONObject dataJSON = statusJSON.getJSONObject("data");
                        String sid = dataJSON.getString("sid");
                        String hash = dataJSON.getString("hash");
                        Log.d(TAG,"The sid from response "+sid);
                        Log.d(TAG,"The hash from reponse "+hash);
                        triggerStkCall(phoneNo,sid);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    private void triggerStkCall(String telephoneNo,final String sid) {
        String dataString = telephoneNo+"ctl"+sid;

        String myGeneratedHash4 = generateHmac(dataString,"Tech2548gtRV365");
        OkHttpClient client  = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/push/mpesa";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("phone",telephoneNo)
                .add("vid",Constants.vid)
                .add("sid",sid)
                .add("hash",myGeneratedHash4)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("STK-PUSHED"+mSuccessfulFilter));
                    startCheckerForCompletedPayments(sid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    private void startCheckerForCompletedPayments(final String sid){
        if(canConfirmPayments){
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
                    checkIfMpesaPaymentsHaveCompleted(sid);
//                }
//            }, 3000);
        }
    }

    private void checkIfMpesaPaymentsHaveCompleted(final String sid) {
        String dataString = sid+"ctl";

        String myGeneratedHash1 = generateHmac(dataString,"Tech2548gtRV365");
        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/mobilemoney";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("sid",sid)
                .add("vid",Constants.vid)
                .add("hash",myGeneratedHash1)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    if (response.isSuccessful()){
                        JSONObject statusJSON = new JSONObject(jsonData);
                        if (statusJSON.getString("status").equals("aei7p7yrx4ae34")){
                            sendIntentForCompletedPayments();
                        }else{
                            startCheckerForCompletedPayments(sid);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }





    public void startCardPayment(String orderId, String invoiceId, int amount, final String phoneNo, final String email, final String cvv,
                                 final String cardNo, final String month, final String year, final String address, final String city, final String country, final String postcode,
                                 final String stateProv, final String firstName, final String lastName){

        String dataString = Constants.live+orderId+invoiceId+amount+phoneNo+email+Constants.vid+Constants.curr+Constants.p1+
                Constants.p2+Constants.p3+Constants.p4+Constants.cbk+Constants.cst;
        String myGeneratedHash3 = generateHmac(dataString,Constants.key);

        Log.d(TAG,dataString);
        Log.d(TAG,myGeneratedHash3);

        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("live",Integer.toString(Constants.live))
                .add("oid",orderId)
                .add("inv",invoiceId)
                .add("amount",Integer.toString(amount))
                .add("tel",phoneNo)
                .add("eml",email)
                .add("vid",Constants.vid)
                .add("curr",Constants.curr)
                .add("p1",Constants.p1)
                .add("p2",Constants.p2)
                .add("p3",Constants.p3)
                .add("p4",Constants.p4)
                .add("cbk",Constants.cbk)
                .add("cst",Integer.toString(Constants.cst))
                .add("crl",Integer.toString(Constants.crl))
                .add("hash",myGeneratedHash3)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"response gotten");
                Log.d(TAG,""+response.message());
                Log.d(TAG,""+response.code());
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    if (response.isSuccessful()){
                        Log.d(TAG,""+response.body().toString());
                        JSONObject statusJSON = new JSONObject(jsonData);
                        JSONObject dataJSON = statusJSON.getJSONObject("data");
                        String sid = dataJSON.getString("sid");
                        String hash = dataJSON.getString("hash");
                        Log.d(TAG,"The sid from response "+sid);
                        Log.d(TAG,"The hash from reponse "+hash);
                        cardTransact(sid,phoneNo,email,cvv,cardNo,month,year,address,city,
                                country,postcode,stateProv,firstName,lastName,hash);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    private void cardTransact(String sid,String phoneNo,String email,String cvv,String cardNo,String month,String year,String address,
                             String city,String country,String postcode,String stateProv,String firstName,String lastName,String hash){

        String carddataString = sid+Constants.vid+cardNo+cvv+month+year+address+city+country+postcode
                +stateProv+firstName+lastName;

        // String dataString = sid+carddataString;

        String myGeneratedHash1 = generateHmac(carddataString,Constants.key);
        Log.d(TAG,myGeneratedHash1);
        Log.d(TAG,carddataString);
        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/cc";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("vid",Constants.vid)
                .add("tel",phoneNo)
                .add("eml",email)
                .add("curr",Constants.curr)
                .add("cvv",cvv)
                .add("cardno",cardNo)
                .add("month",month)
                .add("year",year)
                .add("cust_address",address)
                .add("cust_postcode",postcode)
                .add("cust_city",city)
                .add("cust_stateprov",stateProv)
                .add("cust_country",country)
                .add("sid",sid)
                .add("fname",firstName)
                .add("lname",lastName)
                .add("hash",myGeneratedHash1)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    if (response.isSuccessful()){
                        JSONObject statusJSON = new JSONObject(jsonData);
                        if (statusJSON.getString("status").equals("aei7p7yrx4ae34")){
                           sendIntentForCompletedPayments();
                        }else{
                            sendIntentForFailedPayments();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }





    public void makePayouts(String payoutReference,String payoutPhone,int payoutAmount){
        String myPayoutDataString = "amount="+payoutAmount+"&phone="+payoutPhone+"&reference="+payoutReference+"&vid="+Constants.vid;
        String myPayoutGeneratedHash = generateHmac(myPayoutDataString, Constants.key);
        Log.d(TAG,myPayoutDataString);
        Log.d(TAG,myPayoutGeneratedHash);

        OkHttpClient client  = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/b2c/v3/mobile/mpesa";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("vid",Constants.vid)
                .add("reference",payoutReference)
                .add("phone",payoutPhone)
                .add("hash",myPayoutGeneratedHash)
                .add("amount",Integer.toString(payoutAmount))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    String jsonData = response.body().string();
                    Log.d(TAG,"Payouts:"+jsonData);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }






    private void sendIntentForCompletedPayments(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(mSuccessfulFilter));
    }

    private void sendIntentForFailedPayments(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(mFailedFilter));
    }

    private String generateHmac(String data,String key){
        String myGeneratedHash = null;
        try{
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec mySectretKey = new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
            sha256.init(mySectretKey);
            myGeneratedHash =  String.valueOf(Hex.encodeHex(sha256.doFinal(data.getBytes("UTF-8"))));
        }catch (Exception e){
            e.printStackTrace();
        }
        return myGeneratedHash;
    }

    public void stopRecursiveChecker(){
        canConfirmPayments = false;
    }


}
