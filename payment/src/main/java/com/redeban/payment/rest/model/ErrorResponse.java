package com.redeban.payment.rest.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    @SerializedName("error")
    @Expose
    private RedebanError error;

    public RedebanError getError() {
        return error;
    }

    public void setError(RedebanError error) {
        this.error = error;
    }

}
