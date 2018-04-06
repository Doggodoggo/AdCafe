package com.bry.adcafe.services;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.security.auth.callback.Callback;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ipayservice {
    public static void makeipaypayment(String dataString ,Callback callback){
        OkHttpClient client = new OkHttpClient();
        String myUrl = "";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(myUrl).newBuilder();
        String url = urlBuilder.build()
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue((okhttp3.Callback) callback);



    }
    private static void processResponse(Response response, String dataString, Context context){
        try {
            String jsonData = response.body().string();
            if (response.isSuccessful()){
                JSONObject statusJSON = new JSONObject(jsonData);
                if (statusJSON.getString("status").equals("aei7p7yrx4ae34")){

                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }
}
