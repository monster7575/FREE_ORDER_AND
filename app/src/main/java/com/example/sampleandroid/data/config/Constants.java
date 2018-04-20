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
    public static final String MAIN_URL = "http://" + Constants.DOING_DOMAIN + "/main?uid=%s";
    public static final String SHORT_APIKEY = "AIzaSyB2Di7GVIBLJhURbklprv1B7pcLoRXMChU";
    public static final String FCM_APIKEY = "AIzaSyDEXoZQXqXlgUPq4BjaSJ_RV5ArvK478Gc";


    public static final String INTENT_DATA_KEY = "INTENT_DATA_KEY";
    public static final String LOG_TAG = "FREE_ORDER";


    public final class MENU_LINKS {
        public static final String BUYERLIST_URL = "http://" + Constants.DOING_DOMAIN + "/srv/buyer/mobile/list";
        public static final String SELLER_LOGIN = "http://" + Constants.DOING_DOMAIN + "/srv/seller/mobile/login";
        public static final String SELLER_SETTING = "http://" + Constants.DOING_DOMAIN + "/srv/seller/mobile/main/setting";
        public static final String ORDER_URL = "http://" + Constants.DOING_DOMAIN + "/orders/%s";
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
