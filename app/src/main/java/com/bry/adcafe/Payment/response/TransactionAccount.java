package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class TransactionAccount {
    @SerializedName("transaction_account_type")
    private int accountType;
    @SerializedName("transaction_account_number")
    private String accountNumber;
    @SerializedName("transaction_account_name")
    private String accountName;
    @SerializedName("transaction_account_manager")
    private String accountManager;

    public TransactionAccount(int accountType, String accountNumber, String accountName, String accountManager) {
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountManager = accountManager;
    }

    public int getAccountType() {
        return accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountManager() {
        return accountManager;
    }
}
