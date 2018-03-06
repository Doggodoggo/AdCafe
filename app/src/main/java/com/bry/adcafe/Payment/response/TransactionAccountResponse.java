package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class TransactionAccountResponse extends BaseStatusResponse {
    @SerializedName("content")
    private TransactionAccount transactionAccount;

    public TransactionAccountResponse(StatusResponse statusResponse, TransactionAccount transactionAccount) {
        super(statusResponse);
        this.transactionAccount = transactionAccount;
    }

    public TransactionAccount getTransactionAccount() {
        return transactionAccount;
    }
}
