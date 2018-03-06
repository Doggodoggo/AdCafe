package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class TransactionResponse extends BaseStatusResponse {
    @SerializedName("content")
    private Transaction transaction;

    public TransactionResponse(StatusResponse statusResponse, Transaction transaction) {
        super(statusResponse);
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
