package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class WithdrawalAccountResponse extends BaseStatusResponse {
    @SerializedName("content")
    private WithdrawalAccount withdrawalAccount;

    public WithdrawalAccountResponse(StatusResponse statusResponse, WithdrawalAccount withdrawalAccount) {
        super(statusResponse);
        this.withdrawalAccount = withdrawalAccount;
    }

    public WithdrawalAccount getWithdrawalAccount() {
        return withdrawalAccount;
    }
}
