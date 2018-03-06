package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class UserResponse extends BaseStatusResponse {
    @SerializedName("content")
    private User user;

    public UserResponse(StatusResponse statusResponse, User user) {
        super(statusResponse);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
