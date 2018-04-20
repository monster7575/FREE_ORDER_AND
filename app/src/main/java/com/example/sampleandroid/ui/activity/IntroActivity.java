package com.example.sampleandroid.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.sampleandroid.R;
import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.Utils;
import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.data.parcel.IntentData;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by KCH on 2018-04-11.
 */

public class IntroActivity extends AppActivity{

    Handler handler = new Handler();
    Runnable runMain = new Runnable() {
        @Override
        public void run() {

            IntentData indata = new IntentData();
            indata.aniType = Constants.VIEW_ANIMATION.ANI_FLIP;
            indata.link = Constants.MENU_LINKS.SELLER_LOGIN;
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra(Constants.INTENT_DATA_KEY, indata);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Logger.log(Logger.LogState.E, "start = ");
        String token = FirebaseInstanceId.getInstance().getToken();
        Logger.log(Logger.LogState.E, "start = " + Utils.getStringByObject(token));
        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, token);

        handler.postDelayed(runMain, 1500);

    }
}
