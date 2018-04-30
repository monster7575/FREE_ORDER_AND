package com.example.sampleandroid.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.sampleandroid.R;
import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.PermissionHelper;
import com.example.sampleandroid.common.tool.Utils;
import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.ui.view.CustomWebView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import butterknife.BindView;

public class MainActivity extends AppActivity
{

    @BindView(R.id.toolbar_header) Toolbar toolbar_header;
    @BindView(R.id.toolbar_setting) BootstrapButton toolbar_setting;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.toolbar_back) ImageButton toolbar_back;
    @BindView(R.id.toolbar_refresh) BootstrapButton toolbar_refresh;


    public CustomWebView customWebView;
    private Listener mListener = new Listener();
    private JSONObject mToobarData;

    public interface headerJsonCallback{
        void onReceive(JSONObject jsonObject);
    }

    public interface titleCallback{
        void onReceive(String title);
    }

    private headerJsonCallback mHeaderJsonCallback = new headerJsonCallback() {
        @Override
        public void onReceive(JSONObject jsonObject) {
            mToobarData = jsonObject;
            initToobar(jsonObject);
        }
    };

    private titleCallback mTitleCallback = new titleCallback() {
        @Override
        public void onReceive(String title) {
            toolbar_title.setText(title);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0)
        {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            checkPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(n.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }else{
            // Ask the user to grant access
            Intent intent2 = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
             startActivityForResult(intent2, 0);
        }


        // 네트워크 상태체크
        int networkStatus = Utils.getNetWorkType(context);

        if (networkStatus == Utils.NETWORK_NO) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(getString(R.string.app_name)).setMessage("네트워크 상태를 확인해 주세요.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            }).create().show();

            return;
        }

        checkPermission();
        initScreen();
        init();

    }

    private void init()
    {
        customWebView = new CustomWebView(this, this.findViewById(R.id.content).getRootView());
        customWebView.setWebHeaderCallback(mHeaderJsonCallback);
        customWebView.setWebTitleCallback(mTitleCallback);
        customWebView.initContentView(Constants.MENU_LINKS.BUYERLIST_URL);
    }

    public void initScreen()
    {
        setSupportActionBar(toolbar_header);
        toolbar_setting.setOnClickListener(mListener);
        toolbar_back.setOnClickListener(mListener);
        toolbar_refresh.setOnClickListener(mListener);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PermissionHelper.getInstance().setPermissionAndActivity(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS}, (Activity) context);

            if(!PermissionHelper.getInstance().checkPermission()) {
                PermissionHelper.getInstance().requestPermission(0, new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onPermissionResult(String[] permissions, int[] grantResults) {
                        int size = permissions.length;

                        if(size > 0 && permissions[0].equals(Manifest.permission.READ_PHONE_STATE))
                        {
                            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                start();
                            }
                        }
                        else if(size > 0 && permissions[1].equals(Manifest.permission.SEND_SMS))
                        {
                            if(grantResults[1] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }
                            else
                            {
                                start();
                            }
                        }
                        else
                        {
                            start();
                        }
                    }
                });
            }
            else
            {
                start();
            }
        }
        else
        {
            start();
        }
    }

    private void start() {

        Logger.log(Logger.LogState.E, "start = ");
        String token = FirebaseInstanceId.getInstance().getToken();
        Logger.log(Logger.LogState.E, "start = " + Utils.getStringByObject(token));
        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, token);

    }

    public void initToobar(JSONObject jsonObject)
    {

    }

    private class Listener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.toolbar_setting:
                    customWebView.initContentView(Constants.MENU_LINKS.SELLER_SETTING);
                    break;
                case R.id.toolbar_back :
                    if(customWebView.mView.canGoBack())
                        customWebView.mView.goBack();
                    break;
                case R.id.toolbar_refresh :
                    customWebView.mView.reload();
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(customWebView.mView.canGoBack())
            {
                customWebView.mView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
