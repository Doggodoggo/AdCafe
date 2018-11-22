package com.bry.adcafe.models;

public class AdPinData {
    private String pinUserUid;
    private String adId;
    private MyTime timeOfPin;

    public AdPinData(){}

    public AdPinData(String pinUserUid,String adId,MyTime time){
        this.pinUserUid = pinUserUid;
        this.adId = adId;
        this.timeOfPin = time;
    }

    public String getPinUserUid() {
        return pinUserUid;
    }

    public void setPinUserUid(String pinUserUid) {
        this.pinUserUid = pinUserUid;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public MyTime getTimeOfPin() {
        return timeOfPin;
    }

    public void setTimeOfPin(MyTime timeOfPin) {
        this.timeOfPin = timeOfPin;
    }
}
