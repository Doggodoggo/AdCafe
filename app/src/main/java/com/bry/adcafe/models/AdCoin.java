package com.bry.adcafe.models;

public class AdCoin {
    private String AdvertPushRefInAdminConsole;
    private double Value;
    private String AdvertiserId;
    private MyTime timeCreated;
    private String coinType;
    private String ownerUid;



    public AdCoin(){}

    public AdCoin(String advertPushRefInAdminConsole,double val, String advertiserId, MyTime timeOfCreation,String CoinType, String OwnerUid){
        this.AdvertPushRefInAdminConsole = advertPushRefInAdminConsole;
        this.Value = val;
        this.AdvertiserId = advertiserId;
        this.timeCreated = timeOfCreation;
        this.coinType = CoinType;
        this.ownerUid = OwnerUid;
    }



    public String getAdvertPushRefInAdminConsole() {
        return AdvertPushRefInAdminConsole;
    }

    public void setAdvertPushRefInAdminConsole(String advertPushRefInAdminConsole) {
        AdvertPushRefInAdminConsole = advertPushRefInAdminConsole;
    }



    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }



    public String getAdvertiserId() {
        return AdvertiserId;
    }

    public void setAdvertiserId(String advertiserId) {
        AdvertiserId = advertiserId;
    }



    public MyTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(MyTime timeCreated) {
        this.timeCreated = timeCreated;
    }



    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }



    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String currentOwnerUid, String newOwnerUid) {
        if(currentOwnerUid.equals(ownerUid)){
            this.ownerUid = newOwnerUid;
        }
    }
}
