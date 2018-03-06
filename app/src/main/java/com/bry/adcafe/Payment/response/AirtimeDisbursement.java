package com.bry.adcafe.Payment.response;

import java.util.Map;

/**
 * Created by amati on 12/6/2017.
 */

public class AirtimeDisbursement extends BaseResponse {
    private static final String MOBILE_NUMBER_KEY = "mobile_number";
    private static final String AMOUNT_KEY = "amount";
    private static final String REFERENCE_KEY = "reference";

    public AirtimeDisbursement(StatusResponse statusResponse, Map<String, String> contentResponse) {
        super(statusResponse, contentResponse);
    }

    public String getAmount() {
        return getContentResponse().get(AMOUNT_KEY);
    }

    public String getMobileNumber() {
        return getContentResponse().get(MOBILE_NUMBER_KEY);
    }

    public String getReference() {
        return getContentResponse().get(REFERENCE_KEY);
    }
}
