package com.example.sampleandroid.ui.listener;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.sampleandroid.data.service.CallingService;



/**
 * Created by KCH on 2018-04-02.
 */

public class IncomingCallBroadcastReceiver extends BroadcastReceiver{

    public static final String TAG = "PHONE STATE";
    private static String mLastState;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state.equals(mLastState))
        {
            return;
        }
        else
        {
            mLastState = state;
        }

        if(TelephonyManager.EXTRA_STATE_RINGING.equals(state))
        {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            final String phoneNumber = PhoneNumberUtils.formatNumber(incomingNumber);
            Log.e("phoneNumber : ", phoneNumber);
            Intent serviceIntent = new Intent(context, CallingService.class);
            serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, phoneNumber);
            context.startService(serviceIntent);
        }
    }
}
