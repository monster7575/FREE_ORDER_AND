package com.favinet.freeorder.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by KCH on 2018-04-26.
 */

public class BuyerVO {

    @SerializedName("idx")
    @Expose
    private Integer idx;
    @SerializedName("phonenb")
    @Expose
    private String phonenb;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("regdate")
    @Expose
    private String regdate;
    @SerializedName("content")
    @Expose
    private String content;

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getPhonenb() {
        return phonenb;
    }

    public void setPhonenb(String phonenb) {
        this.phonenb = phonenb;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
