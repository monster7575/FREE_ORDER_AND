package com.example.sampleandroid.data.tool;

import android.content.Context;

import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.Utils;
import com.example.sampleandroid.data.model.BuyerReponse;
import com.example.sampleandroid.data.model.ResponseData;
import com.example.sampleandroid.data.model.SellerReponse;
import com.example.sampleandroid.data.model.ShortData;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by KCH on 2018-04-06.
 */

public class DataInterface extends BaseDataInterface {

    private static DataInterface instance;

    public interface ResponseCallback<T> {
        void onSuccess(T response);
        void onError();

    }

    public static DataInterface getInstance() {
        if (instance == null) {
            synchronized (DataInterface.class) {
                if (instance == null) {
                    instance = new DataInterface();
                }
            }
        }

        return instance;
    }

    public DataInterface() {
        super();
    }

    public static boolean isCallSuccess(Response response) {
        return response.isSuccessful();
    }

    public void getShortUrl(Context context, Map<String, String> params, final ResponseCallback callback)
    {

        try {
            Logger.log(Logger.LogState.E, "params = " + Utils.getStringByObject(params));
            Call<ShortData> call = service.callShortUrl(params);
            call.enqueue(new RetryableCallback<ShortData>(call, context) {
                @Override
                public void onFinalResponse(Call<ShortData> call, retrofit2.Response<ShortData> response) {
                    Logger.log(Logger.LogState.E, "onFinalResponse = " + Utils.getStringByObject(response));
                    if (callback == null) return;
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError();
                    }
                }

                @Override
                public void onFinalFailure(Call<ShortData> call, Throwable t) {
                    Logger.log(Logger.LogState.E, "onFinalFailure = " + Utils.getStringByObject(t));
                    if (callback == null)
                        return;
                    t.printStackTrace();
                    callback.onError();
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            callback.onError();
        }
    }

    public void loginSeller(Context context, Map<String, String> params, final ResponseCallback callback)
    {

        try {
            Logger.log(Logger.LogState.E, "params = " + Utils.getStringByObject(params));
            Call<SellerReponse> call = service.callSellerLogin(params);
            call.enqueue(new RetryableCallback<SellerReponse>(call, context) {
                @Override
                public void onFinalResponse(Call<SellerReponse> call, retrofit2.Response<SellerReponse> response) {
                    Logger.log(Logger.LogState.E, "onFinalResponse = " + Utils.getStringByObject(response));
                    if (callback == null) return;
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError();
                    }
                }

                @Override
                public void onFinalFailure(Call<SellerReponse> call, Throwable t) {
                    Logger.log(Logger.LogState.E, "onFinalFailure = " + Utils.getStringByObject(t));
                    if (callback == null)
                        return;
                    t.printStackTrace();
                    callback.onError();
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            callback.onError();
        }
    }

    public void insertSellerMsg(Context context, Map<String, String> params, final ResponseCallback callback)
    {

        try {
            Logger.log(Logger.LogState.E, "params = " + Utils.getStringByObject(params));
            Call<ResponseData> call = service.callSellerMsgInsert(params);
            call.enqueue(new RetryableCallback<ResponseData>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData> call, retrofit2.Response<ResponseData> response) {
                    Logger.log(Logger.LogState.E, "onFinalResponse = " + Utils.getStringByObject(response));
                    if (callback == null) return;
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError();
                    }
                }

                @Override
                public void onFinalFailure(Call<ResponseData> call, Throwable t) {
                    Logger.log(Logger.LogState.E, "onFinalFailure = " + Utils.getStringByObject(t));
                    if (callback == null)
                        return;
                    t.printStackTrace();
                    callback.onError();
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            callback.onError();
        }
    }

    public void getBuyer(Context context, Map<String, String> params, final ResponseCallback callback)
    {

        try {
            Logger.log(Logger.LogState.E, "params = " + Utils.getStringByObject(params));
            Call<BuyerReponse> call = service.callSelectBuyer(params);
            call.enqueue(new RetryableCallback<BuyerReponse>(call, context) {
                @Override
                public void onFinalResponse(Call<BuyerReponse> call, retrofit2.Response<BuyerReponse> response) {
                    Logger.log(Logger.LogState.E, "onFinalResponse = " + Utils.getStringByObject(response));
                    if (callback == null) return;
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError();
                    }
                }

                @Override
                public void onFinalFailure(Call<BuyerReponse> call, Throwable t) {
                    Logger.log(Logger.LogState.E, "onFinalFailure = " + Utils.getStringByObject(t));
                    if (callback == null)
                        return;
                    t.printStackTrace();
                    callback.onError();
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            callback.onError();
        }
    }
}
