package com.summer.netcloud;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Created by summer on 13/06/2018.
 */

public class ContextMgr {

    private static WeakReference<Context> sContext;
    private static Context sAppContext;
    private static boolean sForeground = false;


    public static void setContext(Context context){
        sContext = new WeakReference<>(context);
        sAppContext = context.getApplicationContext();
    }

    public static Context getContext(){
        return sContext.get();
    }

    public static Context getApplicationContext(){
        return sAppContext;
    }

    public static void setForeground(boolean foreground){
        sForeground = foreground;
    }

    public static boolean isForeground(){
        return sForeground;
    }

}
