package com.bry.adcafe.Payment.response;

import java.util.Map;

/**
 * Created by amati on 12/6/2017.
 */

public class CardTransactionResponse extends BaseResponse {
    public static final String TRANSACTION_INDEX_KEY = "transaction_index";
    public static final String TRANSACTION_REFERENCE_KEY = "transaction_reference";

    public CardTransactionResponse(StatusResponse statusResponse, Map<String, String> contentResponse) {
        super(statusResponse, contentResponse);
    }

    public String getTransactionIndex(){
        return this.getContentResponse().get(TRANSACTION_INDEX_KEY);
    }

    public String getTransactionReference() {
        return this.getContentResponse().get(TRANSACTION_REFERENCE_KEY);
    }
}
