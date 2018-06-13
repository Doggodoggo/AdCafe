package com.bry.adcafe.models;

import com.bry.adcafe.services.TimeManager;

public class PaymentResponse {
    private String ResponseCode;
    private String ResponseDescription;
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private String ResultCode;
    private String ResultDesc;
    private String timeOfDay;
    private String date;
    private String payingPhoneNumber;
    private String pushrefInAdminConsole;
    private String uploaderId;


    public PaymentResponse(){}

    public PaymentResponse(String ResponseCode,String ResponseDescription,String MerchantRequestID,
                           String CheckoutRequestID,String ResultCode,String ResultDesc){
        this.ResponseCode = ResponseCode;
        this.ResponseDescription = ResponseDescription;
        this.MerchantRequestID = MerchantRequestID;
        this.CheckoutRequestID = CheckoutRequestID;
        this.ResultCode = ResultCode;
        this.ResultDesc = ResultDesc;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public String getResponseDescription() {
        return ResponseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        ResponseDescription = responseDescription;
    }

    public String getMerchantRequestID() {
        return MerchantRequestID;
    }

    public void setMerchantRequestID(String merchantRequestID) {
        MerchantRequestID = merchantRequestID;
    }

    public String getCheckoutRequestID() {
        return CheckoutRequestID;
    }

    public void setCheckoutRequestID(String checkoutRequestID) {
        CheckoutRequestID = checkoutRequestID;
    }

    public String getResultCode() {
        return ResultCode;
    }

    public void setResultCode(String resultCode) {
        ResultCode = resultCode;
    }

    public String getResultDesc() {
        return ResultDesc;
    }

    public void setResultDesc(String resultDesc) {
        ResultDesc = resultDesc;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPayingPhoneNumber() {
        return payingPhoneNumber;
    }

    public void setPayingPhoneNumber(String payingPhoneNumber) {
        this.payingPhoneNumber = payingPhoneNumber;
    }

    public String getPushrefInAdminConsole() {
        return pushrefInAdminConsole;
    }

    public void setPushrefInAdminConsole(String pushrefInAdminConsole) {
        this.pushrefInAdminConsole = pushrefInAdminConsole;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }
}
