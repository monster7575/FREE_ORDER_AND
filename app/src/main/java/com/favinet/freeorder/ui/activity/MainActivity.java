package com.favinet.freeorder.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.favinet.freeorder.R;
import com.favinet.freeorder.common.preference.BasePreference;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.common.tool.Utils;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.BuyerReponse;
import com.favinet.freeorder.data.model.BuyerVO;
import com.favinet.freeorder.data.model.SellerVO;
import com.favinet.freeorder.data.model.UploadCon;
import com.favinet.freeorder.data.service.CallingService;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.view.CustomWebView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

public class MainActivity extends AppActivity implements SpeechRecognizeListener
{

    @BindView(R.id.toolbar_header) Toolbar toolbar_header;
    @BindView(R.id.toolbar_setting) ImageButton toolbar_setting;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.toolbar_back) ImageButton toolbar_back;
    @BindView(R.id.toolbar_refresh) ImageButton toolbar_refresh;
    @BindView(R.id.toolbar_add) ImageButton toolbar_add;
    @BindView(R.id.box_progress) RelativeLayout box_progress;
    @BindView(R.id.btn_cancel) BootstrapButton btn_cancel;

    @BindView(R.id.popup_content) TextView popup_content;
    @BindView(R.id.btn_pop_close) ImageView btn_pop_close;
    @BindView(R.id.guide_popup) LinearLayout guide_popup;


    public CustomWebView customWebView;
    private Listener mListener = new Listener();
    private JSONObject mToobarData;
    private final static int INTENT_CALL_PROFILE_GALLERY = 3002;
    private List<MainActivity.FileInfo> fileInfoList = new ArrayList<>();
    private  ArrayList<String> smsList = new ArrayList<>();
    private SpeechRecognizerClient client;
    public static int VOICE_RESULT = 1;


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

    public interface loadingCallback{
        void onReceive(boolean isShow, String msg);
    }

    private loadingCallback mLoadingCallback = new loadingCallback() {
        @Override
        public void onReceive(boolean isShow, final String msg) {
            if(isShow)
            {
                smsList.clear();
                SellerVO sellerVO = BasePreference.getInstance(context).getObject(BasePreference.SELLER_DATA, SellerVO.class);
                final String idx = String.valueOf(sellerVO.getIdx());

                HashMap<String, String> params = new HashMap<>();
                params.put("uobjid", idx);
                DataManager.getInstance(context).api.getSmsReceiverBuyer(context, params, new DataInterface.ResponseCallback<BuyerReponse>() {
                    @Override
                    public void onSuccess(BuyerReponse response) {

                        Log.e("getSmsReceiverBuyer  : ", "" + Utils.getStringByObject(response));


                        for ( BuyerVO buyerVO : response.data) {
                            smsList.add(buyerVO.getPhonenb());
                            if(smsList.size() == response.data.size())
                            {
                                SmsSendAsync smsSendAsync = new SmsSendAsync();
                                smsSendAsync.execute(smsList, msg);
                            }
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
                box_progress.setVisibility(View.GONE);
            }
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
        Logger.log(Logger.LogState.E, "requestCode :" + requestCode);
        Logger.log(Logger.LogState.E, "resultCode :" + resultCode);
        Logger.log(Logger.LogState.E, "RESULT_OK :" + RESULT_OK);
       // Toast.makeText(getApplicationContext(), "requestCode + RESULT_OK : " + requestCode + " : " + RESULT_OK, Toast.LENGTH_LONG).show();
       if (resultCode == RESULT_OK) {

            if (requestCode == INTENT_CALL_PROFILE_GALLERY) {
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
            else
            {
                if(requestCode == VOICE_RESULT)
                {
                    Logger.log(Logger.LogState.E, "data :" + data);

                    String obj = data.getStringExtra("obj");
                    String col = data.getStringExtra("col");

                    Logger.log(Logger.LogState.E, "obj :" + obj);
                    Logger.log(Logger.LogState.E, "col :" + col);

                    ArrayList<String> results = data.getStringArrayListExtra(VoiceRecoActivity.EXTRA_KEY_RESULT_ARRAY);

                    customWebView.initContentView("javascript:setGuideText('"+col+"', '"+results.get(0).trim()+"');");


                }
                else if (requestCode == RESULT_CANCELED) {
                    // 음성인식의 오류 등이 아니라 activity의 취소가 발생했을 때.
                    if (data == null) {
                        return;
                    }
                    int errorCode = data.getIntExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_CODE, -1);
                    String errorMsg = data.getStringExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_MESSAGE);

                    if (errorCode != -1 && !TextUtils.isEmpty(errorMsg)) {
                        new android.app.AlertDialog.Builder(this).
                                setMessage(errorMsg).
                                setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).
                                show();
                    }
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

        Utils.getKeyHash(this);

        // SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this);

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
        customWebView.setWebViewLoading(mLoadingCallback);
        customWebView.initContentView(Constants.MENU_LINKS.BUYERLIST_URL);
    }

    public void initScreen()
    {
        setSupportActionBar(toolbar_header);
        toolbar_setting.setOnClickListener(mListener);
        toolbar_back.setOnClickListener(mListener);
        toolbar_refresh.setOnClickListener(mListener);
        toolbar_add.setOnClickListener(mListener);
        btn_cancel.setOnClickListener(mListener);
        btn_pop_close.setOnClickListener(mListener);
        popup_content.setMovementMethod(new ScrollingMovementMethod());
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
            boolean isGuide = (jsonObject.has("guide")) ? jsonObject.getBoolean("guide") : false;
            if(isGuide)
            {
                final String obj = (jsonObject.has("obj")) ? jsonObject.getString("obj") : null;
                final String col = (jsonObject.has("col")) ? jsonObject.getString("col") : null;
                if(obj == null || col == null)
                {
                    Toast.makeText(MainActivity.this, "가이드를 불러 올수 없습니다.", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(MainActivity.this, "가이드를 불러 올수 없습니다.", Toast.LENGTH_LONG).show();

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

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    class SmsSendAsync extends AsyncTask<Object, String, String>
    {

        RelativeLayout box_progress	= null;
        ProgressBar progressBar= null;
        TextView txt_now = null;
        //TextView txt_total = null;
        //boolean isBreak = false;
        int result = 0;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            box_progress = (RelativeLayout) findViewById(R.id.box_progress);
            progressBar = (ProgressBar) findViewById(R.id.progress_starlist);
            txt_now = (TextView) findViewById(R.id.txt_now);
            box_progress.setVisibility(View.VISIBLE);
            progressBar.setMax(smsList.size());
            progressBar.setClickable(true);
        }

        @SuppressLint("SdCardPath")
        @Override
        protected String doInBackground(Object... params)
        {

            Logger.log(Logger.LogState.E, "params = " + Utils.getStringByObject(params));
            String resultStr = "true";
            ArrayList<String> phonenbs = (ArrayList<String>)params[0];
            String msg = (String) params[1];

            int index = 0;
            for(String phonenb  : phonenbs)
            {
                if(this.isCancelled())
                {
                    resultStr = "false";
                    break;
                }

                try{

                    customWebView.sendSms(phonenb, msg);
                    index++;
                    publishProgress("" + index);
                    Thread.sleep(300);


                }
                catch(Exception e)
                {
                    //resultStr = "false";
                    //isBreak = true;
                    //isRunDownTask = false;
                    e.printStackTrace();
                    continue;
                }
                finally
                {

                }


            }

            return resultStr;
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            //downTask.cancel(true);

            //JSONParser.init(getApplicationContext()).InitDown();
            box_progress.setVisibility(View.GONE);
            txt_now.setText("0 / 0");
            //txt_total.setText("0");
            //if(progressBar != null)
            progressBar.setProgress(0);

            super.onCancelled();
        }

        protected void onProgressUpdate(String... progress)
        {
            txt_now.setText(progress[0]+" / " + smsList.size());
            //if(mProgressDialog != null)
            progressBar.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String unused)
        {

            if(this.isCancelled())
                return;
            if(unused.equals("false"))
            {
                box_progress.setVisibility(View.GONE);
                txt_now.setText("0 / 0");
                //txt_total.setText("0");
                //if(mProgressDialog != null)
                progressBar.setProgress(0);
            }
            else
            {
                box_progress.setVisibility(View.GONE);
                txt_now.setText("0 / 0");
                //txt_total.setText("0");
                //if(mProgressDialog != null)
                progressBar.setProgress(0);

                customWebView.initContentView("javascript:sendSmsEnd();");
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
                    else if(customWebView.mView.getUrl().indexOf("/srv/smslog/mobile/list") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/seller/mobile/main/setting');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/smslog/mobile/insert") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/smslog/mobile/list');");
                    }
                    else if(customWebView.mView.getUrl().indexOf("/srv/smslog/mobile/select/") > -1)
                    {
                        customWebView.mView.loadUrl("javascript:location.replace('http://order.favinet.co.kr/srv/smslog/mobile/list');");
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
                    if(customWebView.mView.getUrl().indexOf("/srv/smslog/") > -1)
                        customWebView.mView.loadUrl(Constants.BASE_URL+"/srv/smslog/mobile/insert/");
                    else
                        customWebView.mView.loadUrl(Constants.BASE_URL+"/srv/goods/mobile/insert");
                    break;
                case R.id.btn_cancel :
                    box_progress.setVisibility(View.GONE);
                    break;
                case R.id.btn_pop_close :
                    guidePopupClose(guide_popup);
                    break;
            }
        }
    }

    @Override
    public void onReady() {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Log.e("SpeechSampleActivity", "onError");
        client = null;
    }

    @Override
    public void onPartialResult(String partialResult) {

    }

    @Override
    public void onResults(Bundle results) {
        final StringBuilder builder = new StringBuilder();
        Log.i("SpeechSampleActivity", "onResults");

        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);

        for (int i = 0; i < texts.size(); i++) {
            builder.append(texts.get(i));
            builder.append(" (");
            builder.append(confs.get(i).intValue());
            builder.append(")\n");
        }

        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // finishing일때는 처리하지 않는다.
                if (activity.isFinishing()) return;

                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(activity).
                        setMessage(builder.toString()).
                        setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();

            }
        });

        client = null;
    }

    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }
}

