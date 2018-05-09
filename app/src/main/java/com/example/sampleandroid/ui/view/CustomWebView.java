package com.example.sampleandroid.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.example.sampleandroid.R;
import com.example.sampleandroid.common.activity.BaseActivity;
import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.Utils;
import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.data.model.ResponseData;
import com.example.sampleandroid.data.model.SellerData;
import com.example.sampleandroid.data.model.SellerReponse;
import com.example.sampleandroid.data.model.SellerVO;
import com.example.sampleandroid.data.tool.DataInterface;
import com.example.sampleandroid.data.tool.DataManager;
import com.example.sampleandroid.ui.activity.IntroActivity;
import com.example.sampleandroid.ui.activity.LoginActivity;
import com.example.sampleandroid.ui.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by KCH on 2018-04-10.
 */

public class CustomWebView {

    private BaseActivity base;
    public WebView mView, mWebviewPop;
    private ProgressBar progressBar;
    public String curUrl;
    public FrameLayout mContainer;
    public Map<String, String> titleArr = new HashMap<>();
    private MainActivity.headerJsonCallback callback;
    private MainActivity.titleCallback callbackTitle;
    private final static int INTENT_CALL_PROFILE_GALLERY = 3002;


    @SuppressLint("NewApi")
    public CustomWebView(BaseActivity baseActivity, View v) {
        base = baseActivity;
        mView = (WebView) v.findViewById(R.id.webview);
        mContainer = (FrameLayout) v.findViewById(R.id.webview_frame);

        mView.setFocusable(true);
        mView.setVerticalScrollbarOverlay(true);
        mView.getSettings().setSupportZoom(true);
        mView.getSettings().setJavaScriptEnabled(true);
        mView.setVerticalScrollBarEnabled(true);
        mView.getSettings().setDomStorageEnabled(true);
        mView.getSettings().setSupportMultipleWindows(true);

        if (18 < Build.VERSION.SDK_INT) {
            mView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            mView.getSettings().setTextZoom(100);

        // 롤리팝이상 https->http 데이타 전송 block됨
        if (20 < Build.VERSION.SDK_INT) {
            mView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mView, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mView, true);
            mView.setWebContentsDebuggingEnabled(true);
        }

        // request check
        mView.setWebViewClient(new MyCustomWebViewClient());
        // alert check
        mView.setWebChromeClient(new MyCustomWebChromeClient());
    }

    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.e(Constants.LOG_TAG, "shouldOverrideUrlLoading : " +url);
            if (Utils.getNetWorkType(base) == Utils.NETWORK_NO) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(base);
                dialog.setTitle(R.string.app_name).setMessage(base.getString(R.string.network_error)).setPositiveButton(base.getString(R.string.yes), null).create().show();

                return true;
            }

            if (url.startsWith("intent")) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(url, 0);
                    base.startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    ex.printStackTrace();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage()));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    base.startActivity(i);

                    return true;
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
                }
            }

            String action = Utils.queryToMap(url).get("name");
            final String phonenb = Utils.queryToMap(url).get("phonenb");
            String content = Utils.queryToMap(url).get("content");
            String bobjid = Utils.queryToMap(url).get("bobjid");
            Logger.log(Logger.LogState.E, "action = " + action);
            Logger.log(Logger.LogState.E, "phonenb = " + phonenb);
            Logger.log(Logger.LogState.E, "bobjid = " + bobjid);

            if(action != null)
            {

                if(action.equals("get_seller"))
                {
                    String token = BasePreference.getInstance(base).getValue(BasePreference.GCM_TOKEN, "");
                    mView.loadUrl("javascript:setSeller('"+Utils.getPhoneNumber(base)+"', '"+token+"')");
                    return true;
                }
                else if(action.equals("setTopMenu"))
                {
                    String json = Utils.queryToMap(url).get("params");
                    try
                    {
                        JSONObject jsonObject = new JSONObject(json);
                        setWebViewHeaderJson(jsonObject);
                        return true;
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        return false;
                    }
                }
                else if(action.equals("go_main"))
                {
                    if(phonenb != null)
                    {
                        String gcmtoken = BasePreference.getInstance(base).getValue(BasePreference.GCM_TOKEN, "");
                        HashMap<String, String> params = new HashMap<>();
                        params.put("phonenb", phonenb);
                        params.put("gcmtoken", gcmtoken);
                        DataManager.getInstance(base).api.loginSeller(base, params, new DataInterface.ResponseCallback<SellerReponse>() {
                            @Override
                            public void onSuccess(SellerReponse response) {
                                Logger.log(Logger.LogState.D, "savelog success");

                                if(response.data.size() > 0)
                                {
                                    SellerData.getInstance().setCurrentSellerVO(base, response.data.get(0));

                                    Intent intent = new Intent(base, MainActivity.class);
                                    base.startActivity(intent);
                                    base.finish();
                                }
                                else
                                {
                                    Toast.makeText(base, "매장 승인 전입니다. 관리자에게 문의하여 주세요." + phonenb, Toast.LENGTH_SHORT).show();
                                    BasePreference.getInstance(base).removeAll();
                                    Intent intent = new Intent(base, IntroActivity.class);
                                    base.startActivity(intent);
                                    base.finish();
                                }

                            }

                            @Override
                            public void onError() {
                                Logger.log(Logger.LogState.E, "savelog fail");
                            }
                        });
                    }
                    else
                    {
                        Intent intent = new Intent(base, MainActivity.class);
                        base.startActivity(intent);
                        base.finish();
                    }

                    return true;
                }
                else if(action.equals("sendsms"))
                {
                    try
                    {final String decodedString = URLDecoder.decode(content, "UTF-8");
                        Logger.log(Logger.LogState.E, "content = " + decodedString);

                        SellerVO sellerVO = BasePreference.getInstance(base).getObject(BasePreference.SELLER_DATA, SellerVO.class);
                        String sellerIdx = String.valueOf(sellerVO.getIdx());
                        final HashMap<String, String> params = new HashMap<>();
                        params.put("content", decodedString);
                        params.put("bobjid", bobjid);
                        params.put("sobjid", sellerIdx);
                        DataManager.getInstance(base).api.insertSellerMsg(base, params, new DataInterface.ResponseCallback<ResponseData>() {
                            @Override
                            public void onSuccess(ResponseData response) {
                                Logger.log(Logger.LogState.D, "insertSellerMsg success");

                                String result = response.getResult();
                                Logger.log(Logger.LogState.E, "result = " + Utils.getStringByObject(result));
                                sendSms(phonenb, decodedString);
                            }

                            @Override
                            public void onError() {
                                Logger.log(Logger.LogState.E, "insertSellerMsg fail");
                            }
                        });


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                    return true;
                }
                else if(action.equals("start_loading"))
                {
                    base.startIndicator("");
                    return true;
                }
                else if(action.equals("stop_loading"))
                {
                    base.stopIndicator();
                    return true;
                }
                else if(action.equals("gallery"))
                {
                    callGallery();
                    return true;
                }
                else if(action.equals("finish"))
                {
                    base.finish();
                    return true;
                }

            }
            curUrl = url;
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            CookieSyncManager.getInstance().sync();
            if(titleArr.get(url) != null && !titleArr.get(url).equals(""))
            {
                setWebViewTitle(titleArr.get(url));
            }
            super.onPageFinished(view, url);
        }
    }

    private class MyCustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(view.getContext());
            mWebviewPop.setFocusable(true);
            mWebviewPop.setVerticalScrollbarOverlay(true);
            mWebviewPop.getSettings().setSupportZoom(true);
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.setVerticalScrollBarEnabled(true);
            mWebviewPop.getSettings().setSupportMultipleWindows(true);

            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.setWebChromeClient(new MyCustomWebChromeClient());
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
            if(mWebviewPop != null)
            {
                mWebviewPop.setVisibility(View.GONE);
                mContainer.removeView(mWebviewPop);
                mWebviewPop = null;
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            // TODO Auto-generated method stub
			Log.e(Constants.LOG_TAG, "web title : " + title+ ", url : " + view.getUrl());
            if(!titleArr.containsKey(view.getUrl()))
            {
                titleArr.put(view.getUrl(), title);
            }
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if(progressBar != null)
            {
                progressBar.setProgress(progress);

                if(progress == 100)
                {
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    if(!progressBar.isShown())
                    {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(base);
            dialog.setTitle(R.string.app_name).setMessage(message).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).create().setCanceledOnTouchOutside(false);

            if(!base.isFinishing()) dialog.show();

            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(base);
            dialog.setTitle(R.string.app_name).setMessage(message).setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).create().setCanceledOnTouchOutside(false);

            if(!base.isFinishing()) dialog.show();

            return true;
        }
    }

    private void setWebViewHeaderJson(JSONObject jsonObject) {
        if(callback != null) callback.onReceive(jsonObject);

    }

    public void setWebHeaderCallback(MainActivity.headerJsonCallback listener)
    {
        callback = listener;
    }

    private void setWebViewTitle(String title) {
        if(callbackTitle != null) callbackTitle.onReceive(title);
    }

    public void setWebTitleCallback(MainActivity.titleCallback listener)
    {
        callbackTitle = listener;
    }

    public void initContentView(String link) {
        curUrl = link;
        mView.loadUrl(link);
    }

    public void callGallery() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        base.startActivityForResult(Intent.createChooser(intent, "File Chooser"), INTENT_CALL_PROFILE_GALLERY);
    }

    private void sendSms(String phoneNumber, String sellerContent)
    {
        String smsText = sellerContent;
        PendingIntent sentIntent = PendingIntent.getBroadcast(base, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(base, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        base.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //전화 끊기
                try{

                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    Class<?> cls = Class.forName(telephonyManager.getClass().getName());
                    Method method = cls.getDeclaredMethod("getITelephony");
                    method.setAccessible(true);
                    ITelephony iTelephony = (ITelephony) method.invoke(telephonyManager);
                    // iTelephony.endCall();

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(base, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(base, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(base, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(base, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(base, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        base.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(base, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(base, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(phoneNumber, null, smsText, sentIntent, deliveredIntent);



    }
}
