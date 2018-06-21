package com.favinet.freeorder.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.favinet.freeorder.R;
import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.PermissionHelper;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.SellerData;
import com.favinet.freeorder.data.model.SellerReponse;
import com.favinet.freeorder.data.model.SellerVO;
import com.favinet.freeorder.data.parcel.IntentData;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

/**
 * Created by KCH on 2018-04-11.
 */

public class IntroActivity extends AppActivity{

    Handler handler = new Handler();
    Runnable runMain = new Runnable() {
        @Override
        public void run() {
            Logger.log(Logger.LogState.E, "runMain = ");

            SellerVO sellerVO = BasePreference.getInstance(IntroActivity.this).getObject(BasePreference.SELLER_DATA, SellerVO.class);
            if(sellerVO == null)
            {
                IntentData indata = new IntentData();
                indata.aniType = Constants.VIEW_ANIMATION.ANI_FLIP;
                indata.link = Constants.MENU_LINKS.SELLER_LOGIN;
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra(Constants.INTENT_DATA_KEY, indata);
                startActivity(intent);
                finish();
            }
            else
            {
                String gcmtoken = BasePreference.getInstance(IntroActivity.this).getValue(BasePreference.GCM_TOKEN, "");
                HashMap<String, String> params = new HashMap<>();
                params.put("uobjid", sellerVO.getIdx().toString());
                params.put("gcmtoken", gcmtoken);
                params.put("phonenb", Utils.getPhoneNumber(IntroActivity.this));
                DataManager.getInstance(IntroActivity.this).api.loginSeller(IntroActivity.this, params, new DataInterface.ResponseCallback<SellerReponse>() {
                    @Override
                    public void onSuccess(SellerReponse response) {
                        Logger.log(Logger.LogState.D, "savelog success");

                        if(response.data.size() > 0)
                        {
                            SellerData.getInstance().setCurrentSellerVO(IntroActivity.this, response.data.get(0));

                            Logger.log(Logger.LogState.E, "savelog success" + Utils.getStringByObject(response.data.get(0)));


                                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                        }
                        else
                        {
                            Toast.makeText(IntroActivity.this, "오류가 발생하였습니다. 관리자에게 문의하여 주세요.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError() {
                        Logger.log(Logger.LogState.E, "savelog fail");
                    }
                });
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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
