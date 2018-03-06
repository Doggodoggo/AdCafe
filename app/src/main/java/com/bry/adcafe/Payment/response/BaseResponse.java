package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by amati on 12/6/2017.
 */

public class BaseResponse extends BaseStatusResponse {
    @SerializedName("content")
    private Map<String, String> contentResponse;


    public BaseResponse(StatusResponse statusResponse, Map<String, String> contentResponse) {
        super(statusResponse);
        this.contentResponse = contentResponse;
    }

    public Map<String, String> getContentResponse() {
        return contentResponse;
    }
}
