package com.bry.adcafe.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by bryon on 6/4/2017.
 */

public class Advert {
    @SerializedName("url")
    @Expose
    private String imageUrl;

    private int numberOfAds = 0 ;
    private String pushId;
    private int pushIdNumber;
    private Bitmap imageBitmap;
    private int numberOfTimesSeen;
    private int numberOfUsersToReach;
    private String pushRefInAdminConsole;
    private String userEmail;
    private String websiteLink;
    private String category;
    private boolean isFlagged = false;
    private boolean isAdminFlagged = false;
    public LinkedHashMap<Integer,Integer> clusters  = new LinkedHashMap<>();
    private boolean hasBeenReimbursed;
    private int dateInDays;
    private String natureOfBanner;
    private int amountToPayPerTargetedView;
    private String advertiserUid;

    private String paymentReference;
    private String paymentMethod;
    private double payoutReimbursalAmount = 0;
    private String downloadImageName;

    private String advertiserPhoneNo;

    private double webClickIncentive = 0;
    private Integer webClickNumber = 0;

    private int numberOfPins = 0;
    private String AdType;
    private boolean hasSetBackupImage = false;

    public List<ExpressionData> expressions = new ArrayList<>();
    public List<WebClickData> webclicks = new ArrayList<>();
    public List<AdPinData> adPins = new ArrayList<>();



    @Exclude
    private List<AdvertiserLocation> advertiserLocations = new ArrayList<>();

    public Advert(String ImageUrl){
        this.imageUrl = ImageUrl;
    }
    public Advert(){}

    public int getNumberOfAds(){
        return numberOfAds;
    }

    public void setNumberOfAds(int setnumberOfAds){
        numberOfAds = setnumberOfAds;
    }



    public void removeAd(){
        numberOfAds -=1;
    }



    public String getPushId(){ return pushId; }

    public void setPushId(String pushId){ this.pushId = pushId; }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public Bitmap getImageBitmap(){
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap bm){
        imageBitmap = bm;
    }


    public void setNumberOfTimesSeen(int number){
        numberOfTimesSeen = number;
    }

    public int getNumberOfTimesSeen(){
        return  numberOfTimesSeen;
    }




    public String getPushRefInAdminConsole() {
        return pushRefInAdminConsole;
    }

    public void setPushRefInAdminConsole(String pushRefInAdminConsole) {
        this.pushRefInAdminConsole = pushRefInAdminConsole;
    }



    public int getNumberOfUsersToReach() {
        return numberOfUsersToReach;
    }

    public void setNumberOfUsersToReach(int numberOfUsersToReach) {
        this.numberOfUsersToReach = numberOfUsersToReach;
    }



    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }



    public String getWebsiteLink() {
        if(websiteLink == null){
            return "none";
        }else{
            return websiteLink;
        }
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public int getPushIdNumber() {
        return pushIdNumber;
    }

    public void setPushIdNumber(int pushIdNumber) {
        this.pushIdNumber = pushIdNumber;
    }

    public boolean isAdminFlagged() {
        return isAdminFlagged;
    }

    public void setAdminFlagged(boolean adminFlagged) {
        isAdminFlagged = adminFlagged;
    }

    public boolean isHasBeenReimbursed() {
        return hasBeenReimbursed;
    }

    public void setHasBeenReimbursed(boolean hasBeenReimbursed) {
        this.hasBeenReimbursed = hasBeenReimbursed;
    }

    public long getDateInDays() {
        return dateInDays;
    }

    public void setDateInDays(long dateInDays) {
        this.dateInDays = (int) dateInDays;
    }

    public String getNatureOfBanner() {
        return natureOfBanner;
    }

    public void setNatureOfBanner(String natureOfBanner) {
        this.natureOfBanner = natureOfBanner;
    }

    public int getAmountToPayPerTargetedView() {
        return amountToPayPerTargetedView;
    }

    public void setAmountToPayPerTargetedView(int amountToPayPerTargetedView) {
        this.amountToPayPerTargetedView = amountToPayPerTargetedView;
    }

    public String getAdvertiserUid() {
        return advertiserUid;
    }

    public void setAdvertiserUid(String advertiserUid) {
        this.advertiserUid = advertiserUid;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }



    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getPayoutReimbursalAmount() {
        return payoutReimbursalAmount;
    }

    public void setPayoutReimbursalAmount(double payoutReimbursalAmount) {
        this.payoutReimbursalAmount = payoutReimbursalAmount;
    }


    public String getDownloadImageName() {
        return downloadImageName;
    }

    public void setDownloadImageName(String downloadImageName) {
        this.downloadImageName = downloadImageName;
    }



    public String getAdvertiserPhoneNo() {
        if(advertiserPhoneNo==null ||advertiserPhoneNo.equals("")) return "none";
        return advertiserPhoneNo;
    }

    public void setAdvertiserPhoneNo(String advertiserPhoneNo) {
        this.advertiserPhoneNo = advertiserPhoneNo;
    }

    public List<AdvertiserLocation> getAdvertiserLocations() {
        return advertiserLocations;
    }

    public void setAdvertiserLocations(List<AdvertiserLocation> advertiserLocations) {
        this.advertiserLocations = advertiserLocations;
    }

    public boolean didAdvertiserSetContactInfo(){
        if(!getWebsiteLink().equals("none"))return true;
        if(!getAdvertiserPhoneNo().equals("none") && !getAdvertiserPhoneNo().equals(""))return true;
        if(!advertiserLocations.isEmpty())return true;
        return false;
    }

    public boolean didAdvertiserAddIncentive(){
        if(getWebClickIncentive()!=0) return true;
        return false;
    }

    public double getWebClickIncentive() {
        return webClickIncentive;
    }

    public void setWebClickIncentive(double webClickIncentive) {
        this.webClickIncentive = webClickIncentive;
    }



    public int getWebClickNumber() {
        return webClickNumber;
    }

    public void setWebClickNumber(int webClickNumber) {
        this.webClickNumber = webClickNumber;
    }

    public int getNumberOfPins() {
        return numberOfPins;
    }

    public void setNumberOfPins(int numberOfPins) {
        this.numberOfPins = numberOfPins;
    }

    public String getAdType() {
        return AdType;
    }

    public void setAdType(String adType) {
        AdType = adType;
    }

    public boolean hasSetBackupImage() {
        return hasSetBackupImage;
    }

    public void setHasSetBackupImage(boolean hasSetBackupImage) {
        this.hasSetBackupImage = hasSetBackupImage;
    }
}
