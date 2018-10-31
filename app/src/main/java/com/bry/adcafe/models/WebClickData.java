package com.bry.adcafe.models;

public class WebClickData {
    private String WebClickUserUid;
    private MyTime WebClickTime;
    private String adId;


    public WebClickData(){}

    public WebClickData(String webClickUserUid,MyTime webClickTime,String adId){
        this.WebClickUserUid = webClickUserUid;
        this.WebClickTime = webClickTime;
        this.adId = adId;
    }


    public String getWebClickUserUid() {
        return WebClickUserUid;
    }

    public void setWebClickUserUid(String webClickUserUid) {
        WebClickUserUid = webClickUserUid;
    }

    public MyTime getWebClickTime() {
        return WebClickTime;
    }

    public void setWebClickTime(MyTime webClickTime) {
        WebClickTime = webClickTime;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }
}
