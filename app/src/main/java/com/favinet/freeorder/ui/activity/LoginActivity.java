package com.favinet.freeorder.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.favinet.freeorder.R;
import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.PermissionHelper;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.BuyerReponse;
import com.favinet.freeorder.data.model.UploadCon;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.view.CustomWebView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

/**
 * Created by KCH on 2018-04-11.
 */

public class LoginActivity extends AppActivity {

    public CustomWebView customWebView;
    private final static int INTENT_CALL_PROFILE_GALLERY = 3002;
    private List<LoginActivity.FileInfo> fileInfoList = new ArrayList<>();
    private LoginActivity.Listener mListener = new LoginActivity.Listener();

    @BindView(R.id.toolbar_header) Toolbar toolbar_header;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.toolbar_back) ImageButton toolbar_back;

    @BindView(R.id.popup_content) TextView popup_content;
    @BindView(R.id.btn_pop_close) ImageView btn_pop_close;
    @BindView(R.id.guide_popup) LinearLayout guide_popup;

    private JSONObject mToobarData;

    public interface headerJsonCallback{
        void onReceive(JSONObject jsonObject);
    }

    public interface titleCallback{
        void onReceive(String title);
    }

    private LoginActivity.headerJsonCallback mHeaderJsonCallback = new LoginActivity.headerJsonCallback() {
        @Override
        public void onReceive(JSONObject jsonObject) {
            Log.e(Constants.LOG_TAG, "mHeaderJsonCallback : " + Utils.getStringByObject(jsonObject));
            initToobar(jsonObject);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkPermission();

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
        btn_pop_close.setOnClickListener(mListener);
        popup_content.setMovementMethod(new ScrollingMovementMethod());
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
        toolbar_header.findViewById(R.id.toolbar_back).setVisibility(View.GONE);
        toolbar_back.setOnClickListener(mListener);
        toolbar_header.findViewById(R.id.toolbar_refresh).setVisibility(View.GONE);
        toolbar_header.findViewById(R.id.toolbar_setting).setVisibility(View.GONE);

        try
        {
            boolean isGuide = (jsonObject.has("guide")) ? jsonObject.getBoolean("guide") : false;
            if(isGuide)
            {
                String obj = (jsonObject.has("obj")) ? jsonObject.getString("obj") : null;
                final String col = (jsonObject.has("col")) ? jsonObject.getString("col") : null;
                if(obj == null || col == null)
                {
                    Toast.makeText(LoginActivity.this, "가이드를 불러 올수 없습니다.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("obj", obj);
                    params.put("col", col);
                    DataManager.getInstance(context).api.getGuide(context, params, new DataInterface.ResponseCallback<BuyerReponse>() {
                        @Override
                        public void onSuccess(BuyerReponse response) {

                            if(response.data.size() > 0)
                            {

                                String content = response.data.get(0).getContent();
                                popup_content.setText(content);

                                try
                                {


                                    Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                                    Matcher matcher = pattern.matcher(content);
                                    Logger.log(Logger.LogState.E, "content = " + Utils.getStringByObject(content));
                                    Spannable spannable = (Spannable) popup_content.getText();
                                    while (matcher.find())
                                    {

                                        String text = spannable.toString();
                                        final String matcherStr = matcher.group(0);
                                        int start = text.indexOf(matcherStr);
                                        int end = start + matcherStr.length();
                                        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                                        spannable.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        spannable.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(View widget) {

                                                Logger.log(Logger.LogState.E, "matcher 1 = ");
                                                customWebView.initContentView("javascript:setGuideText('"+col+"', '"+matcherStr.replace("[", "").replace("]", "")+"');");
                                                guidePopupClose(guide_popup);
                                            }

                                            @Override
                                            public void updateDrawState(TextPaint ds) {
                                                ds.setColor(getResources().getColor(R.color.colorPrimaryDark));
                                                ds.setUnderlineText(true);

                                            }
                                        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


                                    }
                                    popup_content.setMovementMethod(new LinkMovementMethod());
                                    //      popup_content.setText(popup_content.getText().toString().replace("[", "").replace("]", ""));
                                    guidePopupOpen(guide_popup);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                            else
                                Toast.makeText(LoginActivity.this, "가이드를 불러 올수 없습니다.", Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onError() {
                            Logger.log(Logger.LogState.E, "savelog fail");
                        }
                    });
                }
            }
            else
            {
                boolean show = (jsonObject.has("show")) ? jsonObject.getBoolean("show") : false;
                if(show)
                {
                    toolbar_header.setVisibility(View.VISIBLE);
                }
                else
                    toolbar_header.setVisibility(View.GONE);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        stopIndicator();
    }

    private void start() {

        Logger.log(Logger.LogState.E, "start = ");
        String token = FirebaseInstanceId.getInstance().getToken();
        Logger.log(Logger.LogState.E, "start = " + Utils.getStringByObject(token));
        BasePreference.getInstance(getApplicationContext()).put(BasePreference.GCM_TOKEN, token);

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

                        }
                        if(size > 0 && permissions[1].equals(Manifest.permission.SEND_SMS))
                        {
                            if(grantResults[1] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }

                        }
                        if(size > 0 && permissions[2].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        {
                            if(grantResults[2] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }

                        }
                        if(size > 0 && permissions[3].equals(Manifest.permission.CALL_PHONE))
                        {
                            if(grantResults[3] != PackageManager.PERMISSION_GRANTED)
                            {
                                System.exit(0);
                                return;
                            }

                        }

                    }
                });
            }

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
                    if(customWebView.mView.canGoBack())
                        customWebView.mView.goBack();
                    break;
                case R.id.btn_pop_close :
                    guidePopupClose(guide_popup);
                    break;
            }
        }
    }
}
