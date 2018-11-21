package com.bry.adcafe.models;

import android.graphics.Bitmap;

public class AdImageData {
    private String adId;
    private String adImage;
    private Bitmap imageBitmap = null;
    private MyTime uploadDate;
    private String dateInDays;

    public AdImageData(){}

    public  AdImageData(String adId,String adImage){
        this.adId = adId;
        this.adImage = adImage;
    }




    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }


    public String getAdImage() {
        return adImage;
    }

    public void setAdImage(String adImage) {
        this.adImage = adImage;
    }


    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }


    public MyTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(MyTime uploadDate) {
        this.uploadDate = uploadDate;
    }


    public String getDateInDays() {
        return dateInDays;
    }

    public void setDateInDays(String dateInDays) {
        this.dateInDays = dateInDays;
    }
}
