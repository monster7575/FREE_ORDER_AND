package com.example.sampleandroid.ui.view;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
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

import com.example.sampleandroid.R;
import com.example.sampleandroid.common.activity.BaseActivity;
import com.example.sampleandroid.common.preference.BasePreference;
import com.example.sampleandroid.common.tool.Logger;
import com.example.sampleandroid.common.tool.Utils;
import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.ui.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
            Logger.log(Logger.LogState.E, "action = " + action);
            if(action != null)
            {

                if(action.equals("get_seller"))
                {
                    String token = BasePreference.getInstance(base).getValue(BasePreference.GCM_TOKEN, "");
                    mView.loadUrl("javascript:setSeller('"+Utils.getPhoneNumber(base)+"', '"+token+"')");
                    return true;
                }
                else if(action.equals("go_main"))
                {
                    Intent intent = new Intent(base, MainActivity.class);
                    base.startActivity(intent);
                    base.finish();
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
}
