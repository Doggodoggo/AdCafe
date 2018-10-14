package com.bry.adcafe.models;

import android.graphics.Bitmap;

public class Message {
    private String mMessageString;
    private String mPushId;
    private String mImageURI;
    private String mSenderId;
    private String mRecipientId;

    private boolean mHasBeenSent;
    private boolean mIsUsersMessage;
    private String mMessageType;

    private Bitmap imageBitmap;
    private String messageUri;

    private MyTime timeStamp;


    public Message(String message, String pushId, String senderId, boolean mIsUsersMessage,String messageType){
        this.mMessageString = message;
        this.mPushId = pushId;
        this.mSenderId = senderId;
        this.mIsUsersMessage = mIsUsersMessage;
        this.mMessageType = messageType;
    }

    public Message(){}



    public String getMessageString() {
        return mMessageString;
    }

    public void setMessageString(String mMessage) {
        this.mMessageString = mMessage;
    }



    public String getPushId() {
        return mPushId;
    }

    public void setPushId(String mPushId) {
        this.mPushId = mPushId;
    }



    public String getImage() {
        return mImageURI;
    }

    public void setImage(String mImage) {
        this.mImageURI = mImage;
    }



    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String mSenderId) {
        this.mSenderId = mSenderId;
    }



    public String getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(String mRecipientId) {
        this.mRecipientId = mRecipientId;
    }



    public boolean HasBeenSent() {
        return mHasBeenSent;
    }

    public void setHasBeenSent(boolean mHasBeenSent) {
        this.mHasBeenSent = mHasBeenSent;
    }



    public boolean IsUsersMessage() {
        return mIsUsersMessage;
    }

    public void setIsUsersMessage(boolean mIsUsersMessage) {
        this.mIsUsersMessage = mIsUsersMessage;
    }


    public String getMessageType() {
        return mMessageType;
    }

    public void setMessageType(String mMessageType) {
        this.mMessageType = mMessageType;
    }


    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }


    public String getMessageUri() {
        return messageUri;
    }

    public void setMessageUri(String messageUri) {
        this.messageUri = messageUri;
    }


    public MyTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(MyTime timeStamp) {
        this.timeStamp = timeStamp;
    }
}
