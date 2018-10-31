package com.bry.adcafe.models;

public class ExpressionData {
    private String viewerUid;
    private MyTime viewingTime;
    private String adId;


    public ExpressionData(){}

    public ExpressionData(String ViewerUId,MyTime viewingTime, String advert){
        this.viewerUid = ViewerUId;
        this.viewingTime = viewingTime;
        this.adId = advert;
    }

    public String getViewerUid() {
        return viewerUid;
    }

    public void setViewerUid(String viewerUid) {
        this.viewerUid = viewerUid;
    }



    public MyTime getViewingTime() {
        return viewingTime;
    }

    public void setViewingTime(MyTime viewingTime) {
        this.viewingTime = viewingTime;
    }
}
