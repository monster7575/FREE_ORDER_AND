package com.example.sampleandroid.gcm;

import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


/**
 * Created by KCH on 2018-04-10.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Logger.log(Logger.LogState.E, "Refreshed token: " + refreshedToken);

        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, refreshedToken);
    }
}
