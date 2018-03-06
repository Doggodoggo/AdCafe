package com.bry.adcafe.Payment.response;

import java.util.Map;

/**
 * Created by amati on 12/6/2017.
 */

public class AccountFloat extends BaseResponse {
    private static final String FLOAT_KEY = "float";
    private static final String ACCCOUNT_NUMBER_KEY = "account_number";
    private static final String CURRENCY_KEY = "currency";


    public AccountFloat(StatusResponse statusResponse, Map<String, String> contentResponse) {
        super(statusResponse, contentResponse);
    }

    public String getFloat(){
        return getContentResponse().get(FLOAT_KEY);
    }

    public String getAccountNumber(){
        return getContentResponse().get(ACCCOUNT_NUMBER_KEY);
    }

    public String getCurrency(){
        return getContentResponse().get(CURRENCY_KEY);
    }
}
