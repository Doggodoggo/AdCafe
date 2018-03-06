package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by amati on 12/6/2017.
 */

public class MultiTransactionResponse extends BaseStatusResponse {

    @SerializedName("content")
    private List<Transaction> transactions;

    public MultiTransactionResponse(StatusResponse statusResponse, List<Transaction> transactions) {
        super(statusResponse);
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
