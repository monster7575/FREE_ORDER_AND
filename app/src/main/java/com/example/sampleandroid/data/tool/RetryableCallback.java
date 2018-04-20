package com.example.sampleandroid.data.tool;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.sampleandroid.R;
import com.example.sampleandroid.ui.activity.AppActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by KCH on 2018-04-06.
 */

public class RetryableCallback<T> implements Callback<T> {

    private int totalRetries = 3;
    private static final String TAG = RetryableCallback.class.getSimpleName();
    private final Call<T> call;
    private int retryCount = 0;
    private Context context;

    public RetryableCallback(Call<T> call, Context context) {
        this.call = call;
        this.context = context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!DataInterface.isCallSuccess(response) && (context instanceof Activity))
        {
            retryPopup();
        }
//            if (retryCount++ < totalRetries) {
//                Log.v(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
//                retry();
//            } else
//                onFinalResponse(call, response);
        else
            onFinalResponse(call,response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
//        Log.e(TAG, t.getMessage());
        if (context instanceof Activity) retryPopup();
        else onFinalFailure(call, t);

//        if (retryCount++ < totalRetries) {
//            Log.v(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
//            retry();
//        } else
//            onFinalFailure(call, t);
    }

    public void onFinalResponse(Call<T> call, Response<T> response) {

    }

    public void onFinalFailure(Call<T> call, Throwable t) {
    }

    private void retry() {
        call.clone().enqueue(this);
    }

    private void retryPopup()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getString(R.string.app_name)).setMessage(context.getString(R.string.network_data_error)).setCancelable(false).setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retry();
            }
        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(context instanceof AppActivity)
                {
                    ((AppActivity)context).stopIndicator();
                }
//                ((Activity)context).moveTaskToBack(true);
//                ((Activity)context).finish();
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }).create().setCanceledOnTouchOutside(false);

        if(context != null && !((Activity)context).isFinishing()) dialog.show();
    }
}

