package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/6/2017.
 */

public class BaseStatusResponse {

    @SerializedName("status")
    private StatusResponse statusResponse;


    public BaseStatusResponse(StatusResponse statusResponse) {
        this.statusResponse = statusResponse;
    }

    public StatusResponse getStatusResponse() {
        return statusResponse;
    }

    public String getStatus() {
        return getStatusResponse().getStatus();
    }

    public String getStatusDescription() {
        return getStatusResponse().getStatusDescription();
    }

    private int getStatusCode() {
        return getStatusResponse().getStatusCode();
    }

    public boolean isSuccessful() {
        return getStatusResponse().isSuccessful();
    }
}
