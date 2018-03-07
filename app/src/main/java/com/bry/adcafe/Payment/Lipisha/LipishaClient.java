package com.bry.adcafe.Payment.Lipisha;

import com.lipisha.sdk.api.ServiceGenerator;
import com.lipisha.sdk.response.AccountBalance;
import com.lipisha.sdk.response.AccountFloat;
import com.lipisha.sdk.response.AirtimeDisbursement;
import com.lipisha.sdk.response.CardTransactionResponse;
import com.lipisha.sdk.response.CustomerResponse;
import com.lipisha.sdk.response.MultiTransactionResponse;
import com.lipisha.sdk.response.Payout;
import com.lipisha.sdk.response.RequestResponse;
import com.lipisha.sdk.response.SMSReport;
import com.lipisha.sdk.response.TransactionAccountResponse;
import com.lipisha.sdk.response.TransactionResponse;
import com.lipisha.sdk.response.UserResponse;
import com.lipisha.sdk.response.WithdrawalAccountResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;

/**
 * Created by amati on 11/22/2017.
 */

public class LipishaClient {

    public static final String SANDBOX_BASE_URL  = "https://api.lipisha.com";
    public static final String PROD_BASE_URL = "https://api.lipisha.com";
    private static final String API_VERSION = "1.3.0";
    private static final String API_TYPE_CALLBACK = "Callback";
    private static final String API_TYPE_IPN = "Ipn";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    private String apiKey, apiSignature, apiVersion , apiType;
    private LipishaAPI lipishaApi;

    public LipishaClient(String apiKey, String apiSignature, String apiVersion, String apiType , String baseUrl){
        this.apiKey = apiKey;
        this.apiSignature = apiSignature;
        this.apiVersion = apiVersion;
        this.apiType = apiType;
        this.lipishaApi = ServiceGenerator.createService(LipishaAPI.class, baseUrl);


    }


    public LipishaClient(String apiKey, String apiSignature, String baseUrl){
        this.apiVersion = API_VERSION;
        this.apiType = API_TYPE_CALLBACK;
        this.lipishaApi = ServiceGenerator.createService(LipishaAPI.class, baseUrl);
    }

    public Call<AccountBalance> getBalance(){
        return this.lipishaApi.getBalance(this.apiKey, this.apiSignature , this.apiVersion, this.apiType);
    }

    public Call<AccountFloat> getFloat(String accountNumber){
        return this.lipishaApi.getFloat(this.apiKey, this.apiSignature, this.apiVersion, this.apiType, accountNumber);
    }

    public Call<Payout> sendMoney(String mobileNumber, String amount, String payoutAccount){
        return this.lipishaApi.sendMoney(apiKey,apiSignature, apiVersion, apiType, payoutAccount,mobileNumber,amount);
    }




    public Call<AirtimeDisbursement> sendAirtime(String mobileNumber, int airtimeAmount, String airtimeAccount , String networkCode){
        return this.lipishaApi.sendAirtime(apiKey,apiVersion,apiVersion,apiType, airtimeAccount, mobileNumber, airtimeAmount,networkCode);
    }
    public Call<SMSReport> sendSMS(String mobileNumber, String smsAccount, String message){
        return this.lipishaApi.sendSMS(apiKey,apiSignature, apiVersion, apiType, smsAccount, mobileNumber, message);
    }
    public Call<TransactionResponse> confirmTransaction(String[] transactionIds){
        StringBuilder listBuilder = new StringBuilder();
        String delim = "";
        for (String i: transactionIds){
            listBuilder.append(delim).append(i);
            delim = ",";
        }
        return this.lipishaApi.confirmTransaction(apiKey,apiSignature,apiVersion, apiType, listBuilder.toString());
    }

    public Call<MultiTransactionResponse> reverseTransaction(String transactionId){
        return this.lipishaApi.reverseTransaction(apiKey, apiSignature, apiVersion, apiType, transactionId);
    }

    public Call<MultiTransactionResponse> reverseTransaction(String[] transactionIds){
        StringBuilder listBuilder = new StringBuilder();
        String delim = "";
        for (String i : transactionIds){
            listBuilder.append(delim).append(i);
            delim = ",";
        }
        return this.lipishaApi.reverseTransaction(apiKey, apiSignature, apiVersion, apiType, listBuilder.toString());
    }

    public Call<TransactionResponse> confirmTransaction(String transactionId){
        return this.lipishaApi.confirmTransaction(apiKey, apiSignature, apiVersion, apiType, transactionId);

    }

    public Call<MultiTransactionResponse> getTransactions(String transactionIds,
                                                          String transactionTypes,
                                                          String transactionMethods,
                                                          Date transactionDateStart,
                                                          Date transactionDateEnd,
                                                          String transactionAccountNames,
                                                          String transactionAccountNumbers,
                                                          String transactionReferences,
                                                          Float transactionAmountMaximum,
                                                          Float transactionAmountMinimum,
                                                          String transactionStatuses,
                                                          String transactionName,
                                                          String transactionMobileNumber,
                                                          String transactionEmail,
                                                          int limit,
                                                          int offset

                                                          ){
        String strTransactionDateStart = (transactionDateStart == null) ? null: DATE_FORMATTER.format(transactionDateStart);
        String strTransactionDateEnd = (transactionDateEnd == null) ? null: DATE_FORMATTER.format(transactionDateEnd);

        return this.lipishaApi.getTransactions(apiKey, apiSignature, apiVersion, apiType,
                transactionIds, transactionTypes, transactionMethods, strTransactionDateEnd, strTransactionDateStart,
                transactionAccountNames, transactionAccountNumbers, transactionReferences, transactionAmountMaximum,
                transactionAmountMinimum,transactionStatuses, transactionName, transactionMobileNumber, transactionEmail,
                limit,offset
                );
    }
    public Call<MultiTransactionResponse> getTransactions(String transactionIds,
                                                         String transactionTypes,
                                                         String transactionMethods,
                                                         Date transactionDateStart,
                                                         Date transactionDateEnd,
                                                         String transactionAccountNames,
                                                         String transactionAccountNumbers,
                                                         String transactionReferences,
                                                         Float transactionAmountMinimum,
                                                         Float transactionAmountMaximum,
                                                         String transactionStatuses,
                                                         String transactionName,
                                                         String transactionMobileNumber,
                                                         String transactionEmail){
        String strTransactionDateStart = (transactionDateStart == null) ? null : DATE_FORMATTER.format(transactionDateStart);
        String strTransactionDateEnd = (transactionDateEnd == null) ? null : DATE_FORMATTER.format(transactionDateEnd);

        return this.lipishaApi.getTransactions(apiKey, apiSignature, apiVersion, apiType,
                transactionIds, transactionTypes, transactionMethods, strTransactionDateStart, strTransactionDateEnd,
                transactionAccountNames, transactionAccountNumbers, transactionReferences, transactionAmountMinimum,
                transactionAmountMaximum, transactionStatuses, transactionName, transactionMobileNumber,
                transactionEmail, 1000, 0);

    }

    public Call<CustomerResponse> getCustomers(){
        return this.lipishaApi.getCustomers(apiType,apiVersion,apiSignature,apiKey);
    }

    public Call<TransactionAccountResponse> createTransactionAccount(int accountType, String accountName, String accountManager){
        return this.lipishaApi.createTransactionAccount(apiKey,apiSignature,apiType,apiVersion,accountType,accountName,
                accountManager);
    }

    public Call<WithdrawalAccountResponse> createWithdrawalAccount(int accountType, String accountName, String accountManager,
                                                                   String accountNumber, String bankName, String bankBranch,
                                                                   String bankAddress, String swiftCode){
        return this.lipishaApi.createWithdrawalAccount(apiKey, apiSignature, apiVersion, apiType,
                accountType, accountName, accountManager, accountNumber, bankName, bankBranch, bankAddress, swiftCode);

    }

    public Call<UserResponse> createUser(String fullName, String role, String email, String mobileNumber, String userName,
                                         String password){
        return this.lipishaApi.createUser(apiKey,apiSignature,apiVersion,apiType,fullName,
                role,email,mobileNumber,userName,password);

    }

    public Call<CardTransactionResponse> authorizeCardTransaction(String accountNumber, String cardNumber, String address1,
                                                                  String address2, String expiry, String name,
                                                                  String state, String country, String zipCode,
                                                                  String securityCode, String amount, String currency) {
        return this.lipishaApi.authorizeCardTransaction(apiKey, apiSignature, apiVersion, apiType,
                accountNumber, cardNumber, address1, address2, expiry,
                name, state, country, zipCode, securityCode, amount, currency);
    }

    public Call<CardTransactionResponse> completeCardTransaction(String transactionIndex, String transactionReference) {
        return this.lipishaApi.completeCardTransaction(apiKey, apiSignature, apiVersion, apiType,
                transactionIndex, transactionReference);
    }

    public Call<CardTransactionResponse> reverseCardTransaction(String transactionIndex, String transactionReference) {
        return this.lipishaApi.reverseCardTransaction(apiKey, apiSignature, apiVersion, apiType,
                transactionIndex, transactionReference);
    }

    public Call<RequestResponse> requestMoney(
            String apiKey, String apiSignature, String accountNumber,
            String mobile_number, String method, String amount, String currency, String reference) {

        return this.lipishaApi.requestMoney(apiKey, apiSignature, accountNumber, mobile_number, method, amount,
                currency, reference);
    }







}
