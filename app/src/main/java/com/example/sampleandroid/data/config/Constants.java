package com.example.sampleandroid.data.config;

/**
 * Created by KCH on 2018-04-02.
 */

public class Constants {

    public static final boolean IS_DEV = true;

    public static final String REAL_DOMAIN = "order.favinet.co.kr";
    public static final String TEST_DOMAIN = "order.favinet.co.kr";
    public static final String DOING_DOMAIN = IS_DEV ? TEST_DOMAIN : REAL_DOMAIN;
    public static final String BASE_URL = "http://" + Constants.DOING_DOMAIN;
    public static final int CALL_OUT_TIME = 10000;


    public static final String INTENT_DATA_KEY = "INTENT_DATA_KEY";
    public static final String LOG_TAG = "FREE_ORDER";

    public final class API_URL {
        public static final String API_SELLER_LOGIN = "/srv/seller/api/select/phonenb";
        public static final String API_SHORT_URL = "/srv/seller/api/shorturl";
        public static final String API_SELLER_MSG_INSERT = "/srv/sellmsglog/api/insert";
        public static final String API_BUYER_SELECT = "/srv/buyer/api/select";
        public static final String API_UPFILE = "/srv/upfile/api/save";
    }

    public final class MENU_LINKS {
        public static final String BUYERLIST_URL = "http://" + Constants.DOING_DOMAIN + "/srv/buyer/mobile/list";
        public static final String SELLER_LOGIN = "http://" + Constants.DOING_DOMAIN + "/srv/seller/mobile/login";
        public static final String SELLER_SETTING = "http://" + Constants.DOING_DOMAIN + "/srv/seller/mobile/main/setting";
        public static final String ORDER_URL = "http://" + Constants.DOING_DOMAIN + "/srv/buyer/mobile/insert/%s/%s";
    }

    public final class VIEW_ANIMATION {
        public static final int ANI_NONE = 10;
        public static final int ANI_FADE = 11;
        public static final int ANI_FLIP = 12;
        public static final int ANI_END_ENTER = 13;
        public static final int ANI_SLIDE_DOWN_IN = 14;
        public static final int ANI_SLIDE_LEFT_IN = 15;
        public static final int ANI_SLIDE_RIGHT_IN = 16;
        public static final int ANI_SLIDE_UP_IN = 17;
    }

}
