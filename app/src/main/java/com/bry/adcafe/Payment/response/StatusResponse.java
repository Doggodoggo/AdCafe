package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/7/2017.
 */

public class StatusResponse {

    private static final int STATUS_SUCCESSFUL = 0;

    @SerializedName("status")
    private String status;
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_description")
    private String statusDescription;

    public StatusResponse(String status, int statusCode, String statusDescription) {
        this.status = status;
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }

    public String getStatus() {
        return status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean isSuccessful(){
        return getStatusCode() == STATUS_SUCCESSFUL;
    }
}
