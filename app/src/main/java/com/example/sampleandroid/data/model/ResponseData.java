package com.example.sampleandroid.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by KCH on 2018-04-06.
 */

public class ResponseData {


    @SerializedName("result")
    @Expose
    public String result = "";

    @SerializedName("error")
    @Expose
    public String error = "";

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }


}
