package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class User {

    @SerializedName("user_name")
    private String userName;
    @SerializedName("email")
    private String email;

    public User(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }
}
