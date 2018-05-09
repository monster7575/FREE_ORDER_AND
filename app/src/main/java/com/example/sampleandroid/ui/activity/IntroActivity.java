package com.example.sampleandroid.ui.activity;

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

import com.example.sampleandroid.R;
import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.PermissionHelper;
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
            Logger.log(Logger.LogState.E, "runMain = ");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(Logger.LogState.E, "requestCode = " + requestCode);
        if(requestCode == 0)
        {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            checkPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Logger.log(Logger.LogState.E, "onRequestPermissionsResult = " + requestCode);
        checkPermission();
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

        checkPermission();

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PermissionHelper.getInstance().setPermissionAndActivity(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE}, (Activity) context);

            if(!PermissionHelper.getInstance().checkPermission()) {
                PermissionHelper.getInstance().requestPermission(0, new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onPermissionResult(String[] permissions, int[] grantResults) {
                        int size = permissions.length;
                        Logger.log(Logger.LogState.E, "permissions = " + Utils.getStringByObject(permissions));
                        if(size > 0 && permissions[0].equals(Manifest.permission.READ_PHONE_STATE))
                        {
                            Logger.log(Logger.LogState.E, "READ_PHONE_STATE = " + permissions[0].equals(Manifest.permission.READ_PHONE_STATE));
                            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                handler.postDelayed(runMain, 1500);
                            }
                        }
                        if(size > 0 && permissions[1].equals(Manifest.permission.SEND_SMS))
                        {
                            if(grantResults[1] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                handler.postDelayed(runMain, 1500);
                            }
                        }
                        if(size > 0 && permissions[2].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        {
                            if(grantResults[2] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                handler.postDelayed(runMain, 1500);
                            }
                        }
                        if(size > 0 && permissions[3].equals(Manifest.permission.CALL_PHONE))
                        {
                            if(grantResults[3] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                handler.postDelayed(runMain, 1500);
                            }
                        }
                        else
                        {
                            handler.postDelayed(runMain, 1500);
                        }
                    }
                });
            }
            else
            {
                handler.postDelayed(runMain, 1500);
            }
        }
        else
        {
            handler.postDelayed(runMain, 1500);
        }
    }
}
