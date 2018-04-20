package com.example.sampleandroid.data.tool;

import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.data.service.HttpService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by KCH on 2018-04-06.
 */

public class BaseDataInterface {

    public HttpService service;

    public BaseDataInterface() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(client).build();
        service = retrofit.create(HttpService.class);
    }

}
