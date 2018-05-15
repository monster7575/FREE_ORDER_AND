package com.favinet.freeorder.ui.listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.BuyerReponse;
import com.favinet.freeorder.data.model.SellerData;
import com.favinet.freeorder.data.model.SellerReponse;
import com.favinet.freeorder.data.model.SellerVO;
import com.favinet.freeorder.data.service.CallingService;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.activity.MainActivity;

import java.util.HashMap;


/**
 * Created by KCH on 2018-04-02.
 */

public class IncomingCallBroadcastReceiver extends BroadcastReceiver{

    public static final String TAG = "PHONE STATE";
    private static String mLastState;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(final Context context, final Intent intent) {

        final SellerVO sellerVO = BasePreference.getInstance(context).getObject(BasePreference.SELLER_DATA, SellerVO.class);
        if(sellerVO != null)
        {
            String useyn = sellerVO.getUseyn();
            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.e("before  : ", state);
            if(state.equals(mLastState))
            {
                return;
            }
            else
            {
                mLastState = state;
            }
            Log.e("EXTRA_STATE_RINGING  : ", "" + TelephonyManager.EXTRA_STATE_RINGING);
            Log.e("EXTRA_STATE_RINGING  : ", "" + useyn);
            if(TelephonyManager.EXTRA_STATE_RINGING.equals(state) && useyn.equals("Y"))
            {
                new Handler().postDelayed(new Runnable() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void run() {
                        Log.e("EXTRA_STATE_RINGING  222: ", "" + state);
                        Log.e("EXTRA_STATE_RINGING  222: ", "" + mLastState);
                        if(TelephonyManager.EXTRA_STATE_RINGING.equals(mLastState))
                        {
                            final String idx = String.valueOf(sellerVO.getIdx());
                            final String content = sellerVO.getContent();
                            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            final String phoneNumber = PhoneNumberUtils.formatNumber(incomingNumber);
                            Log.e("phoneNumber : ", phoneNumber);

                            HashMap<String, String> params = new HashMap<>();
                            params.put("phonenb", phoneNumber);
                            DataManager.getInstance(context).api.getBuyer(context, params, new DataInterface.ResponseCallback<BuyerReponse>() {
                                @Override
                                public void onSuccess(BuyerReponse response) {

                                    Log.e("getBuyer  : ", "" + Utils.getStringByObject(response));

                                    String bobjid = String.valueOf(response.data.get(0).getIdx());

                                    Intent serviceIntent = new Intent(context, CallingService.class);
                                    serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, phoneNumber);
                                    serviceIntent.putExtra(CallingService.EXTRA_SELLER_IDX, idx);
                                    serviceIntent.putExtra(CallingService.EXTRA_SELLER_CONTENT, content);
                                    serviceIntent.putExtra(CallingService.EXTRA_BUYER_IDX, bobjid);
                                    context.startService(serviceIntent);
                                }

                                @Override
                                public void onError() {
                                    Logger.log(Logger.LogState.E, "savelog fail");
                                }
                            });
                        }
                    }
                }, sellerVO.getSec()*1000);

            }
        }

    }
}
