package com.example.sampleandroid.data.service;

import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.data.model.BuyerReponse;
import com.example.sampleandroid.data.model.ResponseData;
import com.example.sampleandroid.data.model.SellerReponse;
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
    @POST(Constants.API_URL.API_SHORT_URL)
    Call<ShortData> callShortUrl(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constants.API_URL.API_SELLER_LOGIN)
    Call<SellerReponse> callSellerLogin(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constants.API_URL.API_SELLER_MSG_INSERT)
    Call<ResponseData> callSellerMsgInsert(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constants.API_URL.API_BUYER_SELECT)
    Call<BuyerReponse> callSelectBuyer(@FieldMap Map<String, String> params);
}
