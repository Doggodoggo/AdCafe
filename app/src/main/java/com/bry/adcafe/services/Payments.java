package com.bry.adcafe.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.PaymentResponse;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
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
        Log(TAG,"ipayservice has started....");
        String dataString = Constants.live+orderId+invoiceId+amount+phoneNo+email+Constants.vid+Constants.curr+Constants.p1+
                Constants.p2+Constants.p3+Constants.p4+Constants.cbk+Constants.cst;
        String myGeneratedHash = generateHmac(dataString, Constants.key);
        Log(TAG,dataString);
        Log(TAG,myGeneratedHash);


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
                Log(TAG,"response gotten");
                Log(TAG,""+response.message());
                Log(TAG,""+response.code());
                try {
                    String jsonData = response.body().string();
                    Log(TAG,jsonData);
                    if (response.isSuccessful()){
                        Log(TAG,""+response.body().toString());
                        JSONObject statusJSON = new JSONObject(jsonData);
                        JSONObject dataJSON = statusJSON.getJSONObject("data");
                        String sid = dataJSON.getString("sid");
                        String hash = dataJSON.getString("hash");
                        Log(TAG,"The sid from response "+sid);
                        Log(TAG,"The hash from reponse "+hash);
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
                    Log(TAG,jsonData);
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
            checkIfMpesaPaymentsHaveCompleted(sid);
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
                    Log(TAG,jsonData);
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

        Log(TAG,dataString);
        Log(TAG,myGeneratedHash3);

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
                Log(TAG,"response gotten");
                Log(TAG,""+response.message());
                Log(TAG,""+response.code());
                try {
                    String jsonData = response.body().string();
                    Log(TAG,jsonData);
                    if (response.isSuccessful()){
                        Log(TAG,""+response.body().toString());
                        JSONObject statusJSON = new JSONObject(jsonData);
                        JSONObject dataJSON = statusJSON.getJSONObject("data");
                        String sid = dataJSON.getString("sid");
                        String hash = dataJSON.getString("hash");
                        Log(TAG,"The sid from response "+sid);
                        Log(TAG,"The hash from reponse "+hash);
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
        Log(TAG,myGeneratedHash1);
        Log(TAG,carddataString);
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
                    Log(TAG,jsonData);
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
        String myPayoutDataString = Constants.vid+payoutReference+payoutPhone+payoutAmount;
        String myPayoutGeneratedHash = generateHmac(myPayoutDataString, Constants.key);

        OkHttpClient client  = new OkHttpClient();
        String myUrl = "";
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
                    Log(TAG,jsonData);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }



    //this handles the payment api calls and logic for making payments.
    public void MpesaMakePayments(final String amount, final String partyA ){
        String app_key = Constants.appKey;
        String app_secret = Constants.appSecret;
        String appKeySecret = app_key + ":" + app_secret;
        String shortCode = "550105";
        String passKey  ="102178110d0c3f3a71170a35a7fc85530422a987574e616662a3f77d9d310f69";
        final String timeStamp = TimeManager.getTimeStamp();
        String passWordEncoded = shortCode+passKey+timeStamp;
        byte [] bytesPas = new byte[0];
        try {
            bytesPas = passWordEncoded.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[0];
        try {
            bytes = appKeySecret.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
        final String passEncoded = Base64.encodeToString(bytesPas, Base64.NO_WRAP);
        Log.d("Passencoded :",passEncoded);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic " + encoded)
                .addHeader("cache-control", "no-cache")
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
                    JSONObject aT = new JSONObject(jsonData);
                    String accessToken = aT.getString("access_token");

                    STKPushSimulation("550105",passEncoded, timeStamp,
                            "CustomerPayBillOnline",amount,partyA,
                            partyA,"550105","https://ilovepancake.github.io/PigDice",
                            "https://adcafe.github.io/CBK/","Adpayment","jsjsj",accessToken);
                    Log.d(TAG+"payments",jsonData);
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

    private void STKPushSimulation(final String businessShortCode, final String password, final String timestamp, String transactionType, String amount,
                                     String phoneNumber, String partyA, String partyB, String callBackURL, String queueTimeOutURL,
                                     String accountReference, String transactionDesc, String bearer) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("TransactionType", transactionType);
        jsonObject.put("Amount", amount);
        jsonObject.put("PhoneNumber", phoneNumber);
        jsonObject.put("PartyA", partyA);
        jsonObject.put("PartyB", partyB);
        jsonObject.put("CallBackURL", callBackURL);
        jsonObject.put("AccountReference", accountReference);
        jsonObject.put("QueueTimeOutURL", queueTimeOutURL);
        jsonObject.put("TransactionDesc", transactionDesc);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + bearer)
                .addHeader("cache-control", "no-cache")
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sendIntentForSuccessfulPaymentRequest();
                try {
                    String jsonData = response.body().string();
                    JSONObject aT = new JSONObject(jsonData);
                    String CheckoutRequestID = aT.getString("CheckoutRequestID");
                    startPaymentListeningForMpesaPayment(businessShortCode,password,timestamp,CheckoutRequestID);
                    Log.d(TAG,jsonData);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    private void startPaymentListeningForMpesaPayment(String businessShortCode, String password, String timestamp,
                                                      String checkoutRequestID){
        if(canConfirmPayments){
            checkIfMpesaPaymentHasBeenCompleted(businessShortCode, password, timestamp, checkoutRequestID);
        }
    }

    private void checkIfMpesaPaymentHasBeenCompleted(final String businessShortCode, final String password, final String timestamp,
                                                     final String checkoutRequestID) {
        String app_key = Constants.appKey;
        String app_secret = Constants.appSecret;
        String appKeySecret = app_key + ":" + app_secret;
        String shortCode = "550105";
        String passKey  ="102178110d0c3f3a71170a35a7fc85530422a987574e616662a3f77d9d310f69";
        final String timeStamp = TimeManager.getTimeStamp();
        String passWordEncoded = shortCode+passKey+timeStamp;
        byte [] bytesPas = new byte[0];
        try {
            bytesPas = passWordEncoded.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[0];
        try {
            bytes = appKeySecret.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
        final String passEncoded = Base64.encodeToString(bytesPas, Base64.NO_WRAP);
        Log.d("Passencoded :",passEncoded);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic " + encoded)
                .addHeader("cache-control", "no-cache")
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
                    JSONObject aT = new JSONObject(jsonData);
                    String accessToken = aT.getString("access_token");
                    STKPushTransactionStatus(businessShortCode,password,timestamp,checkoutRequestID,accessToken);
                     Log.d(TAG+"payments",jsonData);
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

    private void STKPushTransactionStatus(final String businessShortCode, final String password, final String timestamp,
                                          final String checkoutRequestID, String accessToken) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("CheckoutRequestID", checkoutRequestID);

        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/mpesa/stkpushquery/v1/query")
                .post(body)
                .addHeader("authorization", "Bearer " + accessToken)
                .addHeader("content-type", "application/json")
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
                    JSONObject aT = new JSONObject(jsonData);
                    if(aT.has("ResponseCode")&& aT.has("MerchantRequestID")) {
                        String ResponseCode = aT.getString("ResponseCode");
                        String ResponseDescription = aT.getString("ResponseDescription");
                        String MerchantRequestID = aT.getString("MerchantRequestID");
                        String CheckoutRequestID = aT.getString("CheckoutRequestID");
                        String ResultCode = aT.getString("ResultCode");
                        String ResultDesc = aT.getString("ResultDesc");
                        if(ResponseCode.equals("0") &&ResultCode.equals("0")){
                            Log.d(TAG,"Payment has been completed,sending brodcast to start upload");
                            Variables.transactionID = checkoutRequestID;
                            Variables.transactionObject = new PaymentResponse(ResponseCode,ResponseDescription,MerchantRequestID
                                    ,CheckoutRequestID,ResultCode,ResultDesc);
                            sendIntentForCompletedPayments();
                        }else if(ResponseCode.equals("0") &&ResultCode.equals("1")){

                        }
                        else{
                            Log.d(TAG,"Payments have failed for some reason..");
                            startPaymentListeningForMpesaPayment(businessShortCode, password, timestamp,checkoutRequestID);
                        }
                    }else startPaymentListeningForMpesaPayment(businessShortCode, password, timestamp,checkoutRequestID);
                    Log.d(TAG,jsonData);
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



    private void sendIntentForCompletedPayments(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(mSuccessfulFilter));
    }

    private void sendIntentForFailedPayments(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(mFailedFilter));
    }

    private void sendIntentForSuccessfulPaymentRequest(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("STK-PUSHED"+mSuccessfulFilter));
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




    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals("bryonyoni@gmail.com")) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
