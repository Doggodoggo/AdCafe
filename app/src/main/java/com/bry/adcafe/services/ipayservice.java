package com.bry.adcafe.services;

import android.util.Log;

import com.bry.adcafe.Variables;

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

public class ipayservice {
    public static final String TAG = ipayservice.class.getSimpleName();
//    private String key;
    private String data;
    private String cardData;
    public  int live = 1;
    public  String oid = "";
    public  String inv = "";
    public  int amount = 0;
    public int payoutAmount = 0;
    public  String tel = "";
    public String payoutPhone = "";
    public  String eml = "";
    public  String vid = "ctl";
    public  String curr = "KES";
    public  String p1 = "";
    public  String p2 = "";
    public  String p3 = "";
    public  String p4 = "";
    public  String cbk = "";
    public  int cst = 1;
    public  int crl = 2;
    public  String hash = "Tech2548gtRV365";
    public  String cardno = Variables.cardNumber;
    public  String cvv = Variables.cvv;
    public  String month = Variables.expirationMonth;
    public  String year = Variables.expirationYear;
    public  String cust_address = Variables.postalCode;
    public  String cust_city = Variables.cardHolderState;
    public  String cust_country = "Kenya";
    public  String postcode = Variables.postalCode;
    public  String stateprov = Variables.cardHolderState;
    public  String fname = Variables.cardHolderFirstName;
    public  String lname = Variables.cardHolderLastName;
    public String payoutReference = "";
    public String sid;


    public  final String key = "Tech2548gtRV365";
    public String dataString;
    public String carddataString;
    public String payoutDataString;


    public void setMpesaValues(String phone,String email,String transactionid, int amount){
        this.tel = phone;
        this.eml = email;
        this.oid = transactionid;
        this.inv = transactionid;
        this.amount = amount;
        dataString = live+oid+inv+amount+tel+eml+vid+curr+p1+p2+p3+p4+cbk+cst;
    }

    public void setCardValues(int amount,String phone,String email,String cardNo, String myCvv, String myMonth,String myYear, String address,String city,String country,String
            myPostcode,String stateProv,String firstName,String lastName,String transactionid){
        this.amount = amount;
        this.tel = phone;
        this.eml = email;
        this.cardno = cardNo;
        this.cvv = myCvv;
        this.month = myMonth;
        this.year = myYear;
        this.cust_address = address;
        this.cust_city = city;
        this.cust_country = country;
        this.postcode = myPostcode;
        this.stateprov = stateProv;
        this.fname = firstName;
        this.lname = lastName;
        this.oid = transactionid;
        this.inv = transactionid;
        carddataString = vid+tel+eml+cvv+cardno+month+year+cust_address+cust_city+cust_country+postcode+stateprov+fname+lname;;
    }


    public void setPayoutValues(String myReference, String myPhone, int myAmount){
        this.payoutAmount = myAmount;
        this.payoutPhone = myPhone;
        this.payoutReference = myReference;
        payoutDataString = vid+payoutReference+payoutPhone+payoutAmount;

    }

//    String key = hashKey;



    String myGeneratedHash0 = null;
    public String generate_Hmac(String datas,String key) {
            try {
//                key = hashKey;
//                datas = carddataString;
                myGeneratedHash0 = encode(datas, key);
            } catch (Exception e1) {
                e1.printStackTrace();
            }return myGeneratedHash0;
}

    String myGeneratedHash1 = null;
    public String generate_Hmac1(String sid,String vid) {
        try {
            myGeneratedHash1 = encode(sid, vid);
        } catch (Exception e1) {
            e1.printStackTrace();
        }return myGeneratedHash1;
    }





    public void makeipaympesapayment(){
        Log.d(TAG,"ipayservice has started....");

        String myGeneratedHash = generate_Hmac(dataString,key);
        Log.d(TAG,dataString);
        Log.d(TAG,myGeneratedHash);


        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        //Request request = null;
        RequestBody formBody = new FormBody.Builder()
                .add("live",Integer.toString(live))
                .add("oid",oid)
                .add("inv",inv)
                .add("amount",Integer.toString(amount))
                .add("tel",tel)
                .add("eml",eml)
                .add("vid",vid)
                .add("curr",curr)
                .add("p1",p1)
                .add("p2",p2)
                .add("p3",p3)
                .add("p4",p4)
                .add("cbk",cbk)
                .add("cst",Integer.toString(cst))
                .add("crl",Integer.toString(crl))
                .add("hash",myGeneratedHash)
                .build();

        Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    //.header("generatedHash",myGeneratedHash)
                    .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public void makeipayCardPayment(){
        String myGeneratedHash3 = generate_Hmac(dataString,key);

        Log.d(TAG,dataString);
        Log.d(TAG,myGeneratedHash3);

        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("live",Integer.toString(live))
                .add("oid",oid)
                .add("inv",inv)
                .add("amount",Integer.toString(amount))
                .add("tel",tel)
                .add("eml",eml)
                .add("vid",vid)
                .add("curr",curr)
                .add("p1",p1)
                .add("p2",p2)
                .add("p3",p3)
                .add("p4",p4)
                .add("cbk",cbk)
                .add("cst",Integer.toString(cst))
                .add("crl",Integer.toString(crl))
                .add("hash",myGeneratedHash3)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                //.header("generatedHash",myGeneratedHash)
                .build();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processCardResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);

    }

    public void triggerStkCall(){
        String dataString = tel+"ctl"+sid;

        String myGeneratedHash4 = generate_Hmac(dataString,"Tech2548gtRV365");
        OkHttpClient client  = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/push/mpesa";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("phone",tel)
                .add("vid",vid)
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
                processStkCallResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public void mPesaTransact(){
        String dataString = sid+"ctl";

        String myGeneratedHash1 = generate_Hmac1(dataString,"Tech2548gtRV365");
            OkHttpClient client = new OkHttpClient();
            String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/mobilemoney";
            HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
            String url = urlBuilder.build()
                    .toString();
            RequestBody formBody = new FormBody.Builder()
                    .add("sid",sid)
                    .add("vid",vid)
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
                    processMpesaTransactResponse(response);
                }
            };
            Call call = client.newCall(request);
            call.enqueue(cb);
    }



    public void cardTransact(){
//        cardData = carddataString;
        String dataString = sid+carddataString;
        Log.d(TAG,dataString);
        Log.d(TAG,vid);
        Log.d(TAG,tel);
        Log.d(TAG,eml);
        Log.d(TAG,curr);
        Log.d(TAG,cvv);
        Log.d(TAG,cardno);
        Log.d(TAG,month);
        Log.d(TAG,year);
        Log.d(TAG,cust_address);
        Log.d(TAG,postcode);
        Log.d(TAG,cust_city);
        Log.d(TAG,stateprov);
        Log.d(TAG,cust_country);
        Log.d(TAG,fname);
        Log.d(TAG,lname);
        Log.d(TAG,dataString);

        String myGeneratedHash1 = generate_Hmac(dataString,"demo");
        Log.d(TAG,myGeneratedHash1);
        OkHttpClient client = new OkHttpClient();
        String myUrl = "https://apis.ipayafrica.com/payments/v2/transact/cc";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("vid",vid)
                .add("tel",tel)
                .add("eml",eml)
                .add("curr",curr)
                .add("cvv",cvv)
                .add("cardno",cardno)
                .add("month",month)
                .add("year",year)
                .add("cust_address",cust_address)
                .add("cust_postcode",postcode)
                .add("cust_city",cust_city)
                .add("cust_stateprov",stateprov)
                .add("cust_country",cust_country)
                .add("sid",sid)
                .add("fname",fname)
                .add("lname",lname)
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

                processCardTransactResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public void makeIpayPayouts(){
        String myPayoutDataString = payoutDataString;
        String myPayoutGeneratedHash = generate_Hmac(myPayoutDataString, "Tech2548gtRV365");

        OkHttpClient client  = new OkHttpClient();
        String myUrl = "";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("vid",vid)
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
                processPayoutRequest(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public void checkStatusForPayout(){
        String dataString = vid+payoutReference;
        String myGeneratedHash5 = generate_Hmac(dataString,"Tech2548gtRV365");

        OkHttpClient client  = new OkHttpClient();
        String myUrl = "";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        RequestBody formBody = new FormBody.Builder()
                .add("vid",vid)
                .add("reference",payoutReference)
                .add("hash",myGeneratedHash5)
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
                processPayoutStatus(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);

    }

    public void processMpesaTransactResponse(Response response){
        try {
            String jsonData = response.body().string();
            Log.d(TAG,jsonData);
            if (response.isSuccessful()){
                JSONObject statusJSON = new JSONObject(jsonData);
                if (statusJSON.getString("status").equals("aei7p7yrx4ae34")){
                    //Broadcast is made to AdUpload to start the upload
                }else{
                    mPesaTransact();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void processCardTransactResponse(Response response){
        try {
            String jsonData = response.body().string();
            Log.d(TAG,jsonData);
            if (response.isSuccessful()){
                JSONObject statusJSON = new JSONObject(jsonData);
                if (statusJSON.getString("status").equals("aei7p7yrx4ae34")){
//                    LocalBroadcastManager
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void processStkCallResponse(Response response){
        try {
            String jsonData = response.body().string();
            Log.d(TAG,jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void processResponse(Response response){
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
                    sid = dataJSON.getString("sid");
                    String hash = dataJSON.getString("hash");
                    Log.d(TAG,"The sid from response "+sid);
                    Log.d(TAG,"The hash from reponse "+hash);
                    triggerStkCall();
                    mPesaTransact();
                }
            } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void processCardResponse(Response response){
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
                sid = dataJSON.getString("sid");
                String hash = dataJSON.getString("hash");
                Log.d(TAG,"The sid from response "+sid);
                Log.d(TAG,"The hash from reponse "+hash);
                cardTransact();
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void processPayoutRequest(Response response){
        try {
            String jsonData = response.body().string();
            Log.d(TAG,jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processPayoutStatus(Response response){
        try {
            String jsonData = response.body().string();


            Log.d(TAG,jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public String encode(String data, String key) throws Exception{

        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec mySectretKey = new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
        sha256.init(mySectretKey);

        return String.valueOf(Hex.encodeHex(sha256.doFinal(data.getBytes("UTF-8"))));
    }

    }

