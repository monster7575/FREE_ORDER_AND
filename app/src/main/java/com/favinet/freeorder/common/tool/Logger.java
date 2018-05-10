package com.favinet.freeorder.common.tool;

import android.util.Log;

import com.favinet.freeorder.data.config.Constants;

/**
 * Created by KCH on 2018-04-06.
 */

public class Logger {

    private static String TAG = Constants.LOG_TAG;
    private static boolean DEBUG_ON = true;

    public enum LogState{
        D, I, W, E
    }

    public static void log(LogState ls, String msg){
        if(DEBUG_ON){
            switch (ls){
                case D:
                    Log.d(TAG, msg);
                    break;
                case I:
                    Log.i(TAG, msg);
                    break;
                case W:
                    Log.w(TAG, msg);
                    break;
                case E:
                    Log.e(TAG, msg);
                    break;
                default:
                    break;
            }
        }
    }

    public static void log(Class<?> cls, LogState ls, String tag, String msg){
        if(DEBUG_ON){
            switch (ls){
                case D:
                    Log.d(tag, cls.getName() + " "+ msg);
                    break;
                case I:
                    Log.i(tag, cls.getName() + " "+ msg);
                    break;
                case W:
                    Log.w(tag, cls.getName() + " "+ msg);
                    break;
                case E:
                    Log.e(tag, cls.getName() + " "+ msg);
                    break;
                default:
                    break;
            }
        }
    }

    public static void log(Class<?> cls, LogState ls, String msg){
        if(DEBUG_ON){
            switch (ls){
                case D:
                    Log.d(TAG, cls.getName() + " "+ msg);
                    break;
                case I:
                    Log.i(TAG, cls.getName() + " "+ msg);
                    break;
                case W:
                    Log.w(TAG, cls.getName() + " "+ msg);
                    break;
                case E:
                    Log.e(TAG, cls.getName() + " "+ msg);
                    break;
                default:
                    break;
            }
        }
    }

}
