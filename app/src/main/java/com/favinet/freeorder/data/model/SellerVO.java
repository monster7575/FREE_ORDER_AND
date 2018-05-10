package com.favinet.freeorder.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by KCH on 2018-04-25.
 */

public class SellerVO extends ResponseData{

    @SerializedName("idx")
    @Expose
    private Integer idx;
    @SerializedName("sns")
    @Expose
    private String sns;
    @SerializedName("snsid")
    @Expose
    private String snsid;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("passwd")
    @Expose
    private String passwd;
    @SerializedName("auth")
    @Expose
    private String auth;
    @SerializedName("cat")
    @Expose
    private String cat;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("phonenb")
    @Expose
    private String phonenb;
    @SerializedName("attaches")
    @Expose
    private Object attaches;
    @SerializedName("hidate")
    @Expose
    private String hidate;
    @SerializedName("useyn")
    @Expose
    private String useyn;
    @SerializedName("byeyn")
    @Expose
    private String byeyn;
    @SerializedName("byedate")
    @Expose
    private Object byedate;
    @SerializedName("ceonm")
    @Expose
    private Object ceonm;
    @SerializedName("ceophonenb")
    @Expose
    private Object ceophonenb;
    @SerializedName("address")
    @Expose
    private Object address;
    @SerializedName("area")
    @Expose
    private Object area;
    @SerializedName("gcmtoken")
    @Expose
    private String gcmtoken;

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getSns() {
        return sns;
    }

    public void setSns(String sns) {
        this.sns = sns;
    }

    public String getSnsid() {
        return snsid;
    }

    public void setSnsid(String snsid) {
        this.snsid = snsid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhonenb() {
        return phonenb;
    }

    public void setPhonenb(String phonenb) {
        this.phonenb = phonenb;
    }

    public Object getAttaches() {
        return attaches;
    }

    public void setAttaches(Object attaches) {
        this.attaches = attaches;
    }

    public String getHidate() {
        return hidate;
    }

    public void setHidate(String hidate) {
        this.hidate = hidate;
    }

    public String getUseyn() {
        return useyn;
    }

    public void setUseyn(String useyn) {
        this.useyn = useyn;
    }

    public String getByeyn() {
        return byeyn;
    }

    public void setByeyn(String byeyn) {
        this.byeyn = byeyn;
    }

    public Object getByedate() {
        return byedate;
    }

    public void setByedate(Object byedate) {
        this.byedate = byedate;
    }

    public Object getCeonm() {
        return ceonm;
    }

    public void setCeonm(Object ceonm) {
        this.ceonm = ceonm;
    }

    public Object getCeophonenb() {
        return ceophonenb;
    }

    public void setCeophonenb(Object ceophonenb) {
        this.ceophonenb = ceophonenb;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Object getArea() {
        return area;
    }

    public void setArea(Object area) {
        this.area = area;
    }

    public String getGcmtoken() {
        return gcmtoken;
    }

    public void setGcmtoken(String gcmtoken) {
        this.gcmtoken = gcmtoken;
    }

}
