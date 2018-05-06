package com.bry.adcafe.Payment.Lipisha;

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

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by amati on 12/6/2017.
 */

public interface LipishaAPI {
    @FormUrlEncoded
    @POST("/v2/api/get_balance")
    Call<AccountBalance> getBalance(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                    @Field("api_version") String apiVersion, @Field("api_type") String apiType);

    @FormUrlEncoded
    @POST("/v2/get_float")
    Call<AccountFloat> getFloat(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                @Field("account_number") String accountNumber);

    @FormUrlEncoded
    @POST("/v2/send_money")
    Call<Payout> sendMoney(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                           @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                           @Field("account_number") String accountNumber, @Field("mobile_number") String mobileNumber,
                           @Field("amount") String amount);

    @FormUrlEncoded
    @POST("/index.php/v2/api/send_airtime")
    Call<AirtimeDisbursement> sendAirtime(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                          @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                          @Field("account_number") String accountNumber, @Field("mobile_number") String mobileNumber,
                                          @Field("amount") int airtimeAmount, @Field("network") String network);

    @FormUrlEncoded
    @POST("/index.php/v2/api/send_sms")
    Call<SMSReport> sendSMS(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                            @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                            @Field("account_number") String accountNumber, @Field("mobile_number") String mobileNumber,
                            @Field("message") String message);

    @FormUrlEncoded
    @POST("/v2/acknowledge_transaction")
    Call<TransactionResponse> confirmTransaction(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                                 @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                                 @Field("transaction") String transactionIds);

    @FormUrlEncoded
    @POST("/index.php/v2/api/reverse_transaction")
    Call<MultiTransactionResponse> reverseTransaction(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                                      @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                                      @Field("transaction") String transactionIds);

    @FormUrlEncoded
    @POST("/index.php/v2/api/get_transactions")
    Call<MultiTransactionResponse> getTransactions(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                                   @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                                   @Field("transaction") String transactionIds,
                                                   @Field("transaction_type") String transactionTypes,
                                                   @Field("transaction_method") String transactionMethods,
                                                   @Field("transaction_date_start") String transactionDateStart,
                                                   @Field("transaction_date_end") String transactionDateEnd,
                                                   @Field("transaction_account_name") String transactionAccountNames,
                                                   @Field("transaction_account_number") String transactionAccountNumbers,
                                                   @Field("transaction_reference") String transactionReferences,
                                                   @Field("transaction_amount_minimum") Float transactionAmountMinimum,
                                                   @Field("transaction_amount_maximum") Float transactionAmountMaximum,
                                                   @Field("transaction_status") String transactionStatuses,
                                                   @Field("transaction_name") String transactionName,
                                                   @Field("transaction_mobile_number") String transactionMobileNumber,
                                                   @Field("transaction_email") String transactionEmail,
                                                   @Field("limit") int limit,
                                                   @Field("offset") int offset);

    @FormUrlEncoded
    @POST("/index.php/v2/api/get_customers")
    Call<CustomerResponse> getCustomers(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                        @Field("api_version") String apiVersion, @Field("api_type") String apiType);

    @FormUrlEncoded
    @POST("/index.php/v2/api/create_payment_account")
    Call<TransactionAccountResponse> createTransactionAccount(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                                              @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                                              @Field("transaction_account_type") int accountType,
                                                              @Field("transaction_account_name") String accountName,
                                                              @Field("transaction_account_manager") String accountManager);

    @FormUrlEncoded
    @POST("/index.php/v2/api/create_withdrawal_account")
    Call<WithdrawalAccountResponse> createWithdrawalAccount(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                                            @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                                            @Field("transaction_account_type") int accountType,
                                                            @Field("transaction_account_name") String accountName,
                                                            @Field("transaction_account_manager") String accountManager,
                                                            @Field("transaction_account_number") String accountNumber,
                                                            @Field("transaction_account_bank_name") String bankName,
                                                            @Field("transaction_account_bank_branch") String bankBranch,
                                                            @Field("transaction_account_bank_address") String bankAddress,
                                                            @Field("transaction_account_swift_code") String swiftCode);

    @FormUrlEncoded
    @POST("/index.php/v2/api/create_user")
    Call<UserResponse> createUser(@Field("api_key") String apiKey, @Field("api_signature") String apiSignature,
                                  @Field("api_version") String apiVersion, @Field("api_type") String apiType,
                                  @Field("full_name") String fullName,
                                  @Field("role") String role,
                                  @Field("email") String email,
                                  @Field("mobile_number") String mobileNumber,
                                  @Field("user_name") String userName,
                                  @Field("password") String password);

    @FormUrlEncoded
    @POST("/v2/authorize_card_transaction")
    Call<CardTransactionResponse> authorizeCardTransaction(@Field("api_key") String apiKey,
                                                           @Field("api_signature") String apiSignature,
                                                           @Field("api_version") String apiVersion,
                                                           @Field("api_type") String apiType,
                                                           @Field("account_number") String accountNumber,
                                                           @Field("card_number") String cardNumber,
                                                           @Field("address1") String address1,
                                                           @Field("address2") String address2,
                                                           @Field("expiry") String expiry,
                                                           @Field("name") String name,
                                                           @Field("state") String state,
                                                           @Field("country") String country,
                                                           @Field("zip") String zipCode,
                                                           @Field("security_code") String securityCode,
                                                           @Field("amount") String amount,
                                                           @Field("currency") String currency);

    @FormUrlEncoded
    @POST("/v2/complete_card_transaction")
    Call<CardTransactionResponse> completeCardTransaction(@Field("api_key") String apiKey,
                                                          @Field("api_signature") String apiSignature,
                                                          @Field("api_version") String apiVersion,
                                                          @Field("api_type") String apiType,
                                                          @Field("transaction_index") String accountNumber,
                                                          @Field("transaction_reference") String cardNumber);

    @FormUrlEncoded
    @POST("/index.php/v2/api/reverse_card_transaction")
    Call<CardTransactionResponse> reverseCardTransaction(@Field("api_key") String apiKey,
                                                         @Field("api_signature") String apiSignature,
                                                         @Field("api_version") String apiVersion,
                                                         @Field("api_type") String apiType,
                                                         @Field("transaction_index") String accountNumber,
                                                         @Field("transaction_reference") String cardNumber);


    @FormUrlEncoded
    @POST("/v2/request_money")
    Call<RequestResponse> requestMoney(@Field("api_key") String apiKey,
                                       @Field("api_signature") String apiSignature,
                                       @Field("account_number") String accountNumber,
                                       @Field("mobile_number") String mobile_number,
                                       @Field("method") String method,
                                       @Field("amount") String amount,
                                       @Field("currency") String currency,
                                       @Field("reference") String reference);


}
