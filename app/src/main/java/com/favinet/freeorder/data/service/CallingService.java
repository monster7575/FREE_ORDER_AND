package com.favinet.freeorder.data.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;
import com.favinet.freeorder.R;
import com.favinet.freeorder.common.tool.Logger;
import com.favinet.freeorder.data.config.Constants;
import com.favinet.freeorder.data.model.ResponseData;
import com.favinet.freeorder.data.model.SellerData;
import com.favinet.freeorder.data.model.SellerReponse;
import com.favinet.freeorder.data.model.ShortData;
import com.favinet.freeorder.data.tool.DataInterface;
import com.favinet.freeorder.data.tool.DataManager;
import com.favinet.freeorder.ui.activity.MainActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;

/**
 * Created by KCH on 2018-04-02.
 */

public class CallingService extends Service{

    public static final String EXTRA_CALL_NUMBER = "call_number";
    public static final String EXTRA_SELLER_IDX = "call_idx";
    public static final String EXTRA_SELLER_CONTENT = "call_content";
    public static final String EXTRA_BUYER_IDX = "call_bobjid";
    protected View mRootView;
    private WindowManager.LayoutParams params;
    private WindowManager mWindowManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.9);

        params = new WindowManager.LayoutParams( width, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                mRootView = layoutInflater.inflate(R.layout.popup_view, null);
                ButterKnife.bind(this, mRootView);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        mWindowManager.addView(mRootView, params);


        final String phoneNumber = intent.getStringExtra(EXTRA_CALL_NUMBER);
        final String sellerIdx = intent.getStringExtra(EXTRA_SELLER_IDX);
        final String sellerContent = intent.getStringExtra(EXTRA_SELLER_CONTENT);
        final String bobjid = intent.getStringExtra(EXTRA_BUYER_IDX);

        final HashMap<String, String> params = new HashMap<>();
        params.put("content", sellerContent);
        params.put("bobjid", bobjid);
        params.put("sobjid", sellerIdx);
        DataManager.getInstance(this).api.insertSellerMsg(this, params, new DataInterface.ResponseCallback<ResponseData>() {
            @Override
            public void onSuccess(ResponseData response) {
                Logger.log(Logger.LogState.D, "insertSellerMsg success");

                String result = response.getResult();
                if(result.equals("1"))
                {
                    getShortUrl(phoneNumber, sellerIdx, sellerContent);
                }
                else
                    Logger.log(Logger.LogState.E, "insertSellerMsg fail");
            }

            @Override
            public void onError() {
                Logger.log(Logger.LogState.E, "insertSellerMsg fail");
            }
        });

        return START_NOT_STICKY   ;
    }

    private void getShortUrl(final String phoneNumber, String sellerIdx, final String sellerContent)
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("longUrl", String.format(Constants.MENU_LINKS.ORDER_URL, phoneNumber, sellerIdx));
        DataManager.getInstance(this).api.getShortUrl(this, params, new DataInterface.ResponseCallback<ShortData>() {
            @Override
            public void onSuccess(ShortData response) {
                Logger.log(Logger.LogState.D, "savelog success");

                String shortUrl = response.getId();
                sendSms(phoneNumber, sellerContent, shortUrl);

            }

            @Override
            public void onError() {
                Logger.log(Logger.LogState.E, "savelog fail");
            }
        });
    }

    private void sendSms(String phoneNumber, String sellerContent, String shortUrl)
    {
        SmsManager mSmsManager = SmsManager.getDefault();
        Logger.log(Logger.LogState.E, "sendSms");
        String smsText = sellerContent + shortUrl;
        ArrayList<String> smsTextList = mSmsManager.divideMessage(smsText);
        //smsText.add(sellerContent + shortUrl);
        //PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        //PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);
        ArrayList<PendingIntent> sentIntent =  new ArrayList<>();
        sentIntent.add(PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0));
        ArrayList<PendingIntent> deliveredIntent =  new ArrayList<>();
        deliveredIntent.add(PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0));

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
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
                         stopSelf();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(getApplicationContext(), "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(getApplicationContext(), "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(getApplicationContext(), "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(br, new IntentFilter("SMS_SENT_ACTION"));


        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.log(Logger.LogState.E, "BroadcastReceiver broadcastReceiver");

                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(getApplicationContext(), "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(getApplicationContext(), "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("SMS_DELIVERED_ACTION"));


        mSmsManager.sendMultipartTextMessage(phoneNumber, null, smsTextList, sentIntent, deliveredIntent);



    }
}
