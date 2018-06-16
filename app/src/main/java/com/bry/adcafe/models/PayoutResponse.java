package com.bry.adcafe.models;

public class PayoutResponse {
    private String Date;
    private String Time;
    private String TransactionID;
    private String PhoneNo;
    private String Amount;
    private String pushID;

    public PayoutResponse(){}

    public PayoutResponse(String date, String time, String transactionID, String phoneNo, String amount, String pushID){
        this.Date = date;
        this.Time = time;
        this.TransactionID = transactionID;
        this.PhoneNo = phoneNo;
        this.Amount = amount;
        this.pushID = pushID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getTransactionID() {
        return TransactionID;
    }

    public void setTransactionID(String transactionID) {
        TransactionID = transactionID;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getPushID() {
        return pushID;
    }

    public void setPushID(String pushID) {
        this.pushID = pushID;
    }
}
