package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by amati on 12/6/2017.
 */

public class RequestResponse {

    @SerializedName("status")
    private Status status;
    @SerializedName("content")
    private Content content;

    /**
     *
     * @return
     *     The status
     */
    public Status getStatus() {
        return status;
    }

    /**
     *
     * @param status
     *     The status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     *
     * @return
     *     The content
     */
    public Content getContent() {
        return content;
    }

    /**
     *
     * @param content
     *     The content
     */
    public void setContent(Content content) {
        this.content = content;
    }
}
