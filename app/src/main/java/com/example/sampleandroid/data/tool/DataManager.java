package com.example.sampleandroid.data.tool;

import android.content.Context;

/**
 * Created by KCH on 2018-04-06.
 */

public class DataManager {

    //ResponseCallback 형태로 구현되어야 한다. api, sql, pref
    private static DataManager mInstance;

    public DataInterface api;
    //private SQLInterface sql;
    //private BasePreference pref;
    //private Gson gson;

    public static DataManager getInstance(Context context) {
        if (DataManager.mInstance == null) {
            synchronized (DataManager.class) {
                DataManager.mInstance = new DataManager(context);
            }
        }

        return DataManager.mInstance;
    }

    public DataManager(Context context)
    {
        api = DataInterface.getInstance();
        //sql = SQLInterface.getInstance(context);
        //pref = BasePreference.getInstance(context);
        //gson = new Gson();
    }

}
