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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends AppActivity
{

    @BindView(R.id.toolbar_header) Toolbar toolbar_header;
    @BindView(R.id.toolbar_setting) ImageButton toolbar_setting;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.toolbar_back) ImageButton toolbar_back;
    @BindView(R.id.toolbar_refresh) ImageButton toolbar_refresh;
    @BindView(R.id.toolbar_add) ImageButton toolbar_add;


    public CustomWebView customWebView;
    private Listener mListener = new Listener();
    private JSONObject mToobarData;
    private final static int INTENT_CALL_PROFILE_GALLERY = 3002;
    private List<MainActivity.FileInfo> fileInfoList = new ArrayList<>();

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
       if (resultCode == RESULT_OK) {

            if (requestCode == INTENT_CALL_PROFILE_GALLERY) { // 킷캣.
                startIndicator("");
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();

                File file = Utils.getAlbum(this, result);
                if(file == null)
                {
                    stopIndicator();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(R.string.app_name).setMessage(getString(R.string.gallery_error)).setPositiveButton(getString(R.string.yes), null).create().show();
                }
                else
                {
                    fileInfoList.clear();
                    fileInfoList.add(new MainActivity.FileInfo(result, file));

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
        setContentView(R.layout.activity_main);

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
        toolbar_add.setOnClickListener(mListener);
    }

    private void start() {

        Logger.log(Logger.LogState.E, "start = ");
        String token = FirebaseInstanceId.getInstance().getToken();
        Logger.log(Logger.LogState.E, "start = " + Utils.getStringByObject(token));
        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, token);

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

    public void initToobar(JSONObject jsonObject)
    {
        try
        {
            String backBt = (jsonObject.has("backBt")) ? jsonObject.getString("backBt") : "N";
            String refreshBt = (jsonObject.has("refreshBt")) ? jsonObject.getString("refreshBt") : "N";
            String settingBt = (jsonObject.has("settingBt")) ? jsonObject.getString("settingBt") : "N";
            String addBt = (jsonObject.has("addBt")) ? jsonObject.getString("addBt") : "N";

            toolbar_header.findViewById(R.id.toolbar_back).setVisibility(View.GONE);
            toolbar_header.findViewById(R.id.toolbar_refresh).setVisibility(View.GONE);
            toolbar_header.findViewById(R.id.toolbar_setting).setVisibility(View.GONE);
            toolbar_header.findViewById(R.id.toolbar_add).setVisibility(View.GONE);

            if(backBt.equals("Y"))
                toolbar_header.findViewById(R.id.toolbar_back).setVisibility(View.VISIBLE);
            else
                toolbar_header.findViewById(R.id.toolbar_back).setVisibility(View.GONE);

            if(refreshBt.equals("Y"))
                toolbar_header.findViewById(R.id.toolbar_refresh).setVisibility(View.VISIBLE);
            else
                toolbar_header.findViewById(R.id.toolbar_refresh).setVisibility(View.GONE);

            if(settingBt.equals("Y"))
                toolbar_header.findViewById(R.id.toolbar_setting).setVisibility(View.VISIBLE);
            else
                toolbar_header.findViewById(R.id.toolbar_setting).setVisibility(View.GONE);

            if(addBt.equals("Y"))
                toolbar_header.findViewById(R.id.toolbar_add).setVisibility(View.VISIBLE);
            else
                toolbar_header.findViewById(R.id.toolbar_add).setVisibility(View.GONE);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
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
                    Logger.log(Logger.LogState.E, "toolbar_back = " + customWebView.mView.getUrl());
                    if(customWebView.mView.getUrl().indexOf("/srv/seller/mobile/update") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/seller/mobile/main/setting") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/buyer/mobile/list');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/seller/mobile/update") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/goods/mobile/list") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/sellmsglog/mobile/list") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/seller/mobile/update/pwd") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/goods/mobile/select/") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/goods/mobile/list');");
                    }
                    else
                    {
                        if(customWebView.mView.canGoBack())
                            customWebView.mView.goBack();
                    }
                    break;
                case R.id.toolbar_refresh :
                    customWebView.mView.reload();
                    break;
                case R.id.toolbar_add :
                    customWebView.mView.loadUrl("http://order.favinet.co.kr/srv/goods/mobile/insert");
                    break;
            }
        }
    }
}
