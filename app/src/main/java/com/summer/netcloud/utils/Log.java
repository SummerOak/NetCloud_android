package com.summer.netcloud.utils;

import com.summer.netcloud.Constants;

/**
 * Created by summer on 13/06/2018.
 */

public class Log {

    public static final int d(String tag,String log){
        if(Constants.DEBUG_LEV != Constants.DEBUG_LEV_RELEASE){
            return android.util.Log.d(tag,log);
        }

        return 0;
    }

    public static final int e(String tag,String log){
        return android.util.Log.e(tag,log);
    }

    public static final int i(String tag,String log){

        if(Constants.DEBUG_LEV != Constants.DEBUG_LEV_RELEASE){
            return android.util.Log.i(tag,log);
        }

        return 0;
    }

    public static final String getStackTraceString(Throwable t){
        return android.util.Log.getStackTraceString(t);
    }

}
