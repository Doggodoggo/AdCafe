package com.bry.adcafe.models;

public class PayoutResponse {
    private String Date;
    private String Time;
    private Long DateInDays;
    private String PhoneNo;
    private int Amount;
    private String pushID;
    private String ConversationID;
    private String OriginatorConversationID;
    private String ResponseCode;
    private String ResponseDescription;
    private String userId;

    public PayoutResponse(){}

    public PayoutResponse(String conversationID,String originatorConversationID,String responseCode,String responseDescription){
        this.ConversationID = conversationID;
        this.OriginatorConversationID = originatorConversationID;
        this.ResponseCode = responseCode;
        this.ResponseDescription = responseDescription;
    }

    public PayoutResponse(String date, String time, String phoneNo, int amount, String pushID){
        this.Date = date;
        this.Time = time;
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

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        Amount = amount;
    }

    public String getPushID() {
        return pushID;
    }

    public void setPushID(String pushID) {
        this.pushID = pushID;
    }

    public Long getDateInDays() {
        return DateInDays;
    }

    public void setDateInDays(Long dateInDays) {
        DateInDays = dateInDays;
    }

    public String getConversationID() {
        return ConversationID;
    }

    public void setConversationID(String conversationID) {
        ConversationID = conversationID;
    }

    public String getOriginatorConversationID() {
        return OriginatorConversationID;
    }

    public void setOriginatorConversationID(String originatorConversationID) {
        OriginatorConversationID = originatorConversationID;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
