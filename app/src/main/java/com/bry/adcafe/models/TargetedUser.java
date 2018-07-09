package com.bry.adcafe.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TargetedUser {
    private String mUserId;
    private String mGender;
    private int mBirthday;
    private int mBirthMonth;
    private int mBirthYear;
    private List<LatLng> mUserLocations;
    private int mClusterId;
    private String mDeviceCategory;
    private List<String> mSubscriptions;

    public TargetedUser(String uid,String gender,int birthday, int birthMonth, int birthYear,
                        List<LatLng> userLocations, int cluster, String deviceCategory, List<String> subs){
        this.mUserId = uid;
        this.mGender = gender;
        this.mBirthday = birthday;
        this.mBirthMonth = birthMonth;
        this.mBirthYear = birthYear;
        this.mUserLocations = userLocations;
        this.mClusterId = cluster;
        this.mDeviceCategory = deviceCategory;
        this.mSubscriptions = subs;
    }



    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }



    public String getGender() {
        return mGender;
    }

    public void setGender(String mGender) {
        this.mGender = mGender;
    }



    public int getBirthday() {
        return mBirthday;
    }

    public void setBirthday(int mBirthday) {
        this.mBirthday = mBirthday;
    }

    public int getBirthMonth() {
        return mBirthMonth;
    }

    public void setBirthMonth(int mBirthMonth) {
        this.mBirthMonth = mBirthMonth;
    }

    public int getBirthYear() {
        return mBirthYear;
    }

    public void setBirthYear(int mBirthYear) {
        this.mBirthYear = mBirthYear;
    }



    public List<LatLng> getUserLocations() {
        return mUserLocations;
    }

    public void setUserLocations(List<LatLng> mUserLocations) {
        this.mUserLocations = mUserLocations;
    }



    public int getClusterId() {
        return mClusterId;
    }

    public void setClusterId(int mClusterId) {
        this.mClusterId = mClusterId;
    }

    public String getDeviceCategory() {
        return mDeviceCategory;
    }

    public void setDeviceCategory(String mDeviceCategory) {
        this.mDeviceCategory = mDeviceCategory;
    }

    public List<String> getSubscriptions() {
        return mSubscriptions;
    }

    public void setSubscriptions(List<String> mSubscriptions) {
        this.mSubscriptions = mSubscriptions;
    }
}
