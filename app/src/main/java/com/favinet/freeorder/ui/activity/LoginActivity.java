package com.favinet.freeorder.ui.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.favinet.freeorder.R;
import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.UploadCon;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.view.CustomWebView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by KCH on 2018-04-11.
 */

public class LoginActivity extends AppActivity {

    public CustomWebView customWebView;
    private final static int INTENT_CALL_PROFILE_GALLERY = 3002;
    private List<LoginActivity.FileInfo> fileInfoList = new ArrayList<>();
    @BindView(R.id.toolbar_header) Toolbar toolbar_header;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.toolbar_back) ImageButton toolbar_back;
    private LoginActivity.Listener mListener = new LoginActivity.Listener();


    private JSONObject mToobarData;

    public interface headerJsonCallback{
        void onReceive(boolean isShow);
    }

    public interface titleCallback{
        void onReceive(String title);
    }

    private LoginActivity.headerJsonCallback mHeaderJsonCallback = new LoginActivity.headerJsonCallback() {
        @Override
        public void onReceive(boolean isShow) {
            Log.e(Constants.LOG_TAG, "mHeaderJsonCallback : " + isShow);
            initToobar(isShow);
        }
    };

    private LoginActivity.titleCallback mTitleCallback = new LoginActivity.titleCallback() {
        @Override
        public void onReceive(String title) {
            toolbar_title.setText(title);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode == RESULT_OK) {

            if (requestCode == INTENT_CALL_PROFILE_GALLERY) { // 킷캣.
                startIndicator("");
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();

                File file = Utils.getAlbum(this, result);
                if(file == null)
                {
                    stopIndicator();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                    dialog.setTitle(R.string.app_name).setMessage(getString(R.string.gallery_error)).setPositiveButton(getString(R.string.yes), null).create().show();
                }
                else
                {
                    fileInfoList.clear();
                    fileInfoList.add(new LoginActivity.FileInfo(result, file));

                    File fileImg = (fileInfoList.size() > 0) ? fileInfoList.get(0).file : null;

                    DataManager.getInstance(this).api.uploadFile(this, fileImg, new DataInterface.ResponseCallback<UploadCon>() {
                        @Override
                        public void onSuccess(UploadCon response) {
                            stopIndicator();
                            customWebView.initContentView("javascript:setImg('"+response.data.get(0).getPath()+"');");
                        }

                        @Override
                        public void onError() {

                            stopIndicator();
                        }
                    });
                }

                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(n.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
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


        initScreen();
        init();
        start();
    }

    private void init()
    {
        customWebView = new CustomWebView(this, this.findViewById(R.id.content).getRootView());
        customWebView.setWebHeaderLoginCallback(mHeaderJsonCallback);
        customWebView.setWebTitleLoginCallback(mTitleCallback);
        customWebView.initContentView(Constants.MENU_LINKS.SELLER_LOGIN);
    }

    public void initScreen()
    {
        setSupportActionBar(toolbar_header);
        toolbar_back.setOnClickListener(mListener);
    }

    private class FileInfo{
        Uri uri;
        File file;

        public FileInfo(Uri uri, File file)
        {
            this.uri = uri;
            this.file = file;
        }
    }

    public void initToobar(boolean isShow)
    {
        toolbar_header.findViewById(R.id.toolbar_back).setVisibility(View.GONE);
        toolbar_back.setOnClickListener(mListener);
        toolbar_header.findViewById(R.id.toolbar_refresh).setVisibility(View.GONE);
        toolbar_header.findViewById(R.id.toolbar_setting).setVisibility(View.GONE);
        if(isShow)
        {
            toolbar_header.setVisibility(View.VISIBLE);
        }
        else
            toolbar_header.setVisibility(View.GONE);

        stopIndicator();
    }

    private void start() {

        Logger.log(Logger.LogState.E, "start = ");
        String token = FirebaseInstanceId.getInstance().getToken();
        Logger.log(Logger.LogState.E, "start = " + Utils.getStringByObject(token));
        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, token);

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
            }
        }
    }
}
