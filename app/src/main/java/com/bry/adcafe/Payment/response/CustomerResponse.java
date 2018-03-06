package com.bry.adcafe.Payment.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by amati on 12/6/2017.
 */

public class CustomerResponse extends BaseStatusResponse {
    @SerializedName("content")
    private List<Customer> customers;

    public CustomerResponse(StatusResponse statusResponse, List<Customer> customers) {
        super(statusResponse);
        this.customers = customers;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
