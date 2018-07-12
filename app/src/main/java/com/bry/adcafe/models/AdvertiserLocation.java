package com.bry.adcafe.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

public class AdvertiserLocation {
    private myLatLng myLatLng;
    private String placeName;

    public AdvertiserLocation(){}

    public AdvertiserLocation(myLatLng theLatLng, String name){
        this.myLatLng = theLatLng;
        this.placeName = name;
    }



    public myLatLng getMyLatLng() {
        return myLatLng;
    }

    public void setMyLatLng(myLatLng myLatLng) {
        this.myLatLng = myLatLng;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
