package com.favinet.freeorder.data.service;

import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.BuyerReponse;
import com.favinet.freeorder.data.model.ResponseData;
import com.favinet.freeorder.data.model.SellerReponse;
import com.favinet.freeorder.data.model.ShortData;
import com.favinet.freeorder.data.model.UploadCon;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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

    @Multipart
    @POST(Constants.API_URL.API_UPFILE)
    Call<UploadCon> uploadFile(@Part List<MultipartBody.Part> params);

}
