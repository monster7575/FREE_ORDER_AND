package com.favinet.freeorder.ui.view;

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
import android.opengl.Visibility;
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
import com.favinet.freeorder.R;
import com.favinet.freeorder.common.activity.BaseActivity;
import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.ResponseData;
import com.favinet.freeorder.data.model.SellerData;
import com.favinet.freeorder.data.model.SellerReponse;
import com.favinet.freeorder.data.model.SellerVO;
import com.favinet.freeorder.data.model.ShortData;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.activity.IntroActivity;
import com.favinet.freeorder.ui.activity.LoginActivity;
import com.favinet.freeorder.ui.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
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
    private MainActivity.loadingCallback callbackLoading;
    private LoginActivity.headerJsonCallback callbackLogin;
    private LoginActivity.titleCallback callbackTitleLogin;
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

            setWebViewLoading(false, "");

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

            final String action = Utils.queryToMap(url).get("name");
            final String uobjid = Utils.queryToMap(url).get("uobjid");
            final String link = Utils.queryToMap(url).get("link");
            String content = Utils.queryToMap(url).get("content");
            final String bobjid = Utils.queryToMap(url).get("bobjid");
            final String phonenb = Utils.queryToMap(url).get("phonenb");
            final String idx = (Utils.queryToMap(url).get("idx") == null) ? "" : Utils.queryToMap(url).get("idx");
            String msg = "";
            try
            {
                msg = URLDecoder.decode((Utils.queryToMap(url).get("msg") == null) ? "" : Utils.queryToMap(url).get("msg"), "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            final String obj = (Utils.queryToMap(url).get("obj") == null) ? "" : Utils.queryToMap(url).get("obj");
            final String col = (Utils.queryToMap(url).get("col") == null) ? "" : Utils.queryToMap(url).get("col");
            Logger.log(Logger.LogState.E, "action = " + action);
            Logger.log(Logger.LogState.E, "uobjid = " + uobjid);
            Logger.log(Logger.LogState.E, "bobjid = " + bobjid);
            Logger.log(Logger.LogState.E, "link = " + link);
            Logger.log(Logger.LogState.E, "phonenb = " + phonenb);
            Logger.log(Logger.LogState.E, "idx = " + idx);
            Logger.log(Logger.LogState.E, "msg = " + msg);
            Logger.log(Logger.LogState.E, "obj = " + obj);
            Logger.log(Logger.LogState.E, "col = " + col);

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
                    Logger.log(Logger.LogState.E, "setTopMenu url = " + url);
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
                else if(action.equals("showHeader"))
                {
                    Logger.log(Logger.LogState.E, "showHeader url = " + url);
                    try
                    {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("show", true);
                        jsonObject.put("obj", obj);
                        jsonObject.put("col", col);
                        setWebViewLoginHeaderJson(jsonObject);
                        return true;
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        return false;
                    }

                }
                else if(action.equals("go_main") ||  action.equals("update"))
                {
                    if(uobjid != null)
                    {
                        String gcmtoken = BasePreference.getInstance(base).getValue(BasePreference.GCM_TOKEN, "");
                        HashMap<String, String> params = new HashMap<>();
                        params.put("uobjid", uobjid);
                        params.put("gcmtoken", gcmtoken);
                        DataManager.getInstance(base).api.loginSeller(base, params, new DataInterface.ResponseCallback<SellerReponse>() {
                            @Override
                            public void onSuccess(SellerReponse response) {
                                Logger.log(Logger.LogState.D, "savelog success");

                                if(response.data.size() > 0)
                                {
                                    SellerData.getInstance().setCurrentSellerVO(base, response.data.get(0));

                                    Logger.log(Logger.LogState.E, "savelog success" + Utils.getStringByObject(response.data.get(0)));

                                    if(action.equals("go_main"))
                                    {
                                        Intent intent = new Intent(base, MainActivity.class);
                                        base.startActivity(intent);
                                        base.finish();
                                    }
                                    else if(action.equals("update"))
                                    {
                                        mView.loadUrl("javascript:goSetting();");
                                    }

                                }
                                else
                                {
                                    Toast.makeText(base, "오류가 발생하였습니다. 관리자에게 문의하여 주세요.", Toast.LENGTH_SHORT).show();
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
                    {
                        final String decodedString = URLDecoder.decode(content, "UTF-8");
                        final String smsSendNum = Utils.queryToMap(url).get("phonenb");
                        SellerVO sellerVO = BasePreference.getInstance(base).getObject(BasePreference.SELLER_DATA, SellerVO.class);
                        final String sellerIdx = String.valueOf(sellerVO.getIdx());

                        getShortUrl(smsSendNum, sellerIdx, decodedString, bobjid, link, idx);

                    }
                    catch (Exception e)
                    {
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
                else if(action.equals("call"))
                {
                    String tel = "tel: " + phonenb.replace("-", "");
                    Logger.log(Logger.LogState.E, "TEL :" + tel);
                    base.startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                    return true;
                }
                else if(action.equals("smslog"))
                {
                    setWebViewLoading(true, msg);
                    return true;
                }
                else if(action.equals("guide"))
                {
                    try
                    {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("guide", true);
                        jsonObject.put("obj", obj);
                        jsonObject.put("col", col);
                        setWebViewHeaderJson(jsonObject);
                        setWebViewLoginHeaderJson(jsonObject);
                        return true;
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        return false;
                    }
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
                Logger.log(Logger.LogState.E, "onPageFinished : " + url);
                setWebViewTitle(titleArr.get(url));
                setWebViewLoginTitle(titleArr.get(url));
            }
            super.onPageFinished(view, url);
        }
    }

    private void getShortUrl(final String phoneNumber, final String sellerIdx, final String sellerContent, final String bobjid, final String link, final String idx)
    {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        HashMap<String, String> params = new HashMap<>();
        if(idx.equals(""))
            params.put("longUrl", String.format(Constants.MENU_LINKS.ORDER_URL, phoneNumber, sellerIdx, ts));
        else
            params.put("longUrl", String.format(Constants.MENU_LINKS.STATE_URL, idx));


        DataManager.getInstance(base).api.getShortUrl(base, params, new DataInterface.ResponseCallback<ShortData>() {
            @Override
            public void onSuccess(ShortData response) {

                String shortUrl = response.getId();
                if(link.equals("N"))
                {
                    shortUrl = "";
                }
                insertSellerMsg(sellerContent+ "\n\n" + shortUrl, bobjid, sellerIdx, phoneNumber, shortUrl);

            }

            @Override
            public void onError() {
                Logger.log(Logger.LogState.E, "savelog fail");
            }
        });
    }

    private void insertSellerMsg(final String sellerContent, String bobjid, String sellerIdx, final String phoneNumber, final String url)
    {
        final HashMap<String, String> params = new HashMap<>();
        params.put("content", sellerContent);
        params.put("bobjid", bobjid);
        params.put("sobjid", sellerIdx);
        params.put("url", url);
        DataManager.getInstance(base).api.insertSellerMsg(base, params, new DataInterface.ResponseCallback<ResponseData>() {
            @Override
            public void onSuccess(ResponseData response) {
                Logger.log(Logger.LogState.D, "insertSellerMsg success");

                String result = response.getResult();
                if(result.equals("1"))
                {
                    sendSms(phoneNumber, sellerContent);
                }
                else
                    Logger.log(Logger.LogState.E, "insertSellerMsg fail");
            }

            @Override
            public void onError() {
                Logger.log(Logger.LogState.E, "insertSellerMsg fail");
            }
        });

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
                if(title.equals("title"))
                {
                    SellerVO sellerVO = BasePreference.getInstance(base).getObject(BasePreference.SELLER_DATA, SellerVO.class);
                    title = sellerVO.getTitle();
                    Logger.log(Logger.LogState.E, "타이틀 : " + title);
                    titleArr.put(view.getUrl(), title);
                }
                else
                {
                    titleArr.remove("http://order.favinet.co.kr/srv/buyer/mobile/list");
                    titleArr.put(view.getUrl(), title);
                }

            }
            else
            {
                if(title.equals("title"))
                {
                    SellerVO sellerVO = BasePreference.getInstance(base).getObject(BasePreference.SELLER_DATA, SellerVO.class);
                    title = sellerVO.getTitle();
                    Logger.log(Logger.LogState.E, "타이틀 : " + title);
                    titleArr.put(view.getUrl(), title);
                }
            }

            try
            {
                JSONObject jsonObject = new JSONObject();

                if(view.getUrl().indexOf("/srv/seller/mobile/menu") > -1)
                {
                    jsonObject.put("show", true);
                    setWebViewLoginHeaderJson(jsonObject);
                }
                else if(view.getUrl().indexOf("/srv/goods/mobile/insert") > -1)
                {
                    jsonObject.put("show", true);
                    setWebViewLoginHeaderJson(jsonObject);
                }
                else if(view.getUrl().indexOf("/srv/seller/mobile/insert") > -1)
                {
                    jsonObject.put("show", true);
                    setWebViewLoginHeaderJson(jsonObject);
                }
                else
                {
                    jsonObject.put("show", false);
                    setWebViewLoginHeaderJson(jsonObject);
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
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

    private void setWebViewLoginHeaderJson(JSONObject jsonObject) {
        if(callbackLogin != null) callbackLogin.onReceive(jsonObject);

    }

    public void setWebHeaderLoginCallback(LoginActivity.headerJsonCallback listener)
    {
        callbackLogin = listener;
    }

    private void setWebViewLoading(boolean isShow, String msg) {
        if(callbackLoading != null) callbackLoading.onReceive(isShow, msg);

    }

    private void setWebViewLoginTitle(String title) {
        if(callbackTitleLogin != null) callbackTitleLogin.onReceive(title);
    }

    public void setWebTitleLoginCallback(LoginActivity.titleCallback listener)
    {
        callbackTitleLogin = listener;
    }

    public void setWebViewLoading(MainActivity.loadingCallback loading) {
        callbackLoading = loading;

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

    public void sendSms(String phoneNumber, String sellerContent)
    {
        SmsManager mSmsManager = SmsManager.getDefault();

        SellerVO sellerVO = SellerData.getInstance().getCurrentSellerVO(base);
        String smsText =  "<" + sellerVO.getTitle() + ">\r\n" +  sellerContent + "\r\n* 주문 감사드립니다.";

        ArrayList<String> smsTextList = mSmsManager.divideMessage(smsText);
        int  numPart = smsTextList.size();
        ArrayList<PendingIntent> sentIntent =  new ArrayList<>();
        ArrayList<PendingIntent> deliveredIntent =  new ArrayList<>();

        for(int i = 0; i < numPart; i++)
        {
            sentIntent.add(PendingIntent.getBroadcast(base, 0, new Intent("SMS_SENT_ACTION"), 0));
            deliveredIntent.add(PendingIntent.getBroadcast(base, 0, new Intent("SMS_DELIVERED_ACTION"), 0));
        }

        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.log(Logger.LogState.E, "BroadcastReceiver br");
                //전화 끊기
                try{

                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    Class<?> cls = Class.forName(telephonyManager.getClass().getName());
                    Method method = cls.getDeclaredMethod("getITelephony");
                    method.setAccessible(true);
                    ITelephony iTelephony = (ITelephony) method.invoke(telephonyManager);
                    iTelephony.endCall();
                    Logger.log(Logger.LogState.E, "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");

                }catch (Exception e)
                {
                    Logger.log(Logger.LogState.E, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
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
        };

        base.registerReceiver(br, new IntentFilter("SMS_SENT_ACTION"));

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.log(Logger.LogState.E, "BroadcastReceiver broadcastReceiver");

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
        };
        base.registerReceiver(broadcastReceiver, new IntentFilter("SMS_DELIVERED_ACTION"));
        mSmsManager.sendMultipartTextMessage(phoneNumber, null, smsTextList, sentIntent, deliveredIntent);

    }
}
