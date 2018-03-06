package com.bry.adcafe.Payment.response;

import java.util.Map;

/**
 * Created by amati on 12/6/2017.
 */

public class AccountBalance extends BaseResponse {
    private static final String BALANCE_KEY = "balance";
    private static final String CURRENCY_KEY = "currency";

    public AccountBalance(StatusResponse statusResponse, Map<String, String> contentResponse) {
        super(statusResponse, contentResponse);
    }

    public String getBalance(){
        return getContentResponse().get("balance");
    }

    public String getCurrency(){
        return getContentResponse().get("currency");
    }
}
