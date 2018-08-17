package com.favinet.freeorder.common.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.favinet.freeorder.common.activity.BaseActivity;
import com.google.gson.Gson;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KCH on 2018-04-02.
 */

public class Utils {

    public static final int NETWORK_WIFI = 1;    // wifi network
    public static final int NETWORK_4G = 4;    // "4G" networks
    public static final int NETWORK_3G = 3;    // "3G" networks
    public static final int NETWORK_2G = 2;    // "2G" networks
    public static final int NETWORK_UNKNOWN = 5;    // unknown network
    public static final int NETWORK_NO = -1;   // no network

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * Object convert to json String
     *
     * @param obj
     * @return
     */
    public static String getStringByObject(Object obj) {
        Gson gson = new Gson();
        String json = gson.toJson(obj);

        return json;
    }

    /**
     * 이메일 포맷 체크
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {

        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;
    }

    /**
     * 비밀번호 포맷 체크(6자리이상, 숫자, 영문자 1자리 이상 포함)
     *
     * @param password
     * @return
     */
    public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{6,16}$");

    public static boolean validatePassword(String pwStr) {
        Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(pwStr);
        return matcher.matches();
    }

    public static String getParseUrl(String url) {
        //Logger.log(Logger.LogState.E, "getParseUrl  = " + url);
        String result = "";
        /*
        String regex = "((http(s?))\\:\\/\\/)([0-9a-zA-Z\\-]+\\.)+[a-zA-Z]{2,6}(\\:[0-9]+)?(\\/\\S*)?(\\?([^#\\s]*))?$";
        Logger.log(Logger.LogState.E, "getParseUrl regex = " + regex);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        Logger.log(Logger.LogState.E, "getParseUrl m.matches() = " + m.matches());
        if(m.matches())
        {
            for(int i=0;i<=m.groupCount();i++)
            {
                result = m.group(i);
                Logger.log(Logger.LogState.E, "getParseUrl  = " + result);
            }
        }
        */
        if (url.indexOf("http") > -1) {
            String[] splitUrl = url.split("http");
            if (splitUrl.length > 0) {
                //Logger.log(Logger.LogState.E, "getParseUrl  = " + splitUrl[1]);
                result = "http" + splitUrl[1];
            }
        }
        if (url.indexOf("https") > -1) {
            String[] splitUrl = url.split("https");
            if (splitUrl.length > 0) {
                //Logger.log(Logger.LogState.E, "getParseUrl  = " + splitUrl[1]);
                result = "https" + splitUrl[1];
            }
        }
        //Logger.log(Logger.LogState.E, "getParseUrl  result = " + result);
        return result;

    }

    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static File getAlbum(Context context, Uri uri) {
        // TODO Auto-generated method stub
        File file = null;
        Cursor pCursor = context.getContentResolver().query(uri, null, null, null, null);
        String pDisplay = "";

        if (pCursor == null)
            return null;

        while (pCursor.moveToNext()) {
            pDisplay = pCursor.getString(1);

        }
        pCursor.close();
        return file = new File(pDisplay);
    }

    //check network status
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static int getNetWorkType(Context context) {
        int netType = NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NETWORK_2G;
                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NETWORK_3G;
                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NETWORK_4G;
                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NETWORK_3G;
                        } else {
                            netType = NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    public static void changeStatusColor(BaseActivity activity, int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
    }

    public static Map<String, String> queryToMap(String url) {
        Map<String, String> result = new HashMap<String, String>();
        String[] queryArray = url.split("\\?");
        String query = (queryArray.length > 1) ? queryArray[1] : "";
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public static String decode(String value, String typ) {
        String result = "";
        try {
            result = URLDecoder.decode(value, typ);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String encode(String value) {
        String result = "";
        try {
            result = URLEncoder.encode(value, "EUC-KR");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getKeyHash(BaseActivity base) {
        String keyHash = "";
        try {
            PackageInfo info = base.getPackageManager().getPackageInfo(base.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("내패키지",base.getPackageName());
                Log.e("KeyHash:", keyHash);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return keyHash;
    }

    @SuppressLint("MissingPermission")
    public static String getPhoneNumber(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = "";

        try {
            if (telephony.getLine1Number() != null) {
                phoneNumber = telephony.getLine1Number();
            } else {
                if (telephony.getSimSerialNumber() != null) {
                    phoneNumber = telephony.getSimSerialNumber();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return phoneNumber;
    }


    /**
     * 핸드폰 번호 유효성 검사
     */
    public static boolean isValidCellPhoneNumber(String cellphoneNumber) {

        boolean returnValue = false;

        Log.i("cell", cellphoneNumber);

        String regex = "^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";

        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(cellphoneNumber);

        if (m.matches()) {

            returnValue = true;

        }

        return returnValue;

    }

    /**
     * dayOfWeek
     */
    public static int  getDayOfWeek()
    {
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return week-1;
    }

    public static SpannableString getUnderLineColorText(String string, String targetString, int color) {
        SpannableString spannableString = new SpannableString(string);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        int targetStartIndex = string.indexOf(targetString);
        int targetEndIndex = targetStartIndex + targetString.length();
        Logger.log(Logger.LogState.E, "targetStartIndex = " + Utils.getStringByObject(targetStartIndex));
        Logger.log(Logger.LogState.E, "targetEndIndex = " + Utils.getStringByObject(targetEndIndex));
        spannableString.setSpan(boldSpan, targetStartIndex, targetStartIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), targetStartIndex, targetEndIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), targetStartIndex, targetEndIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }


}



