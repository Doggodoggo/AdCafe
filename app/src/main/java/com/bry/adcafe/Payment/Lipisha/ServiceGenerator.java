package com.bry.adcafe.Payment.Lipisha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Created by amati on 12/6/2017.
 */

public class ServiceGenerator {

    private static final String DEFAULT_API_TYPE = "Callback";
    private static final String DEFAULT_API_VERSION = "1.3.0";

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static <S> S createService(Class<S> serviceClass, String baseURL) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(serviceClass);
    }
}
