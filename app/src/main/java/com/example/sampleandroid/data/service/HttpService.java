package com.example.sampleandroid.data.service;

import com.example.sampleandroid.data.model.ShortData;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by KCH on 2018-04-06.
 */

public interface HttpService {



    @FormUrlEncoded
    @POST("/srv/orders/api/shorturl")
    Call<ShortData> callShortUrl(@FieldMap Map<String, String> params);


}
