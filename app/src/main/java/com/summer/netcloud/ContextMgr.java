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
        if(context != null){
            sContext = new WeakReference<>(context);
        }else{
            sContext = null;
        }
    }

    public static void setApplicationContext(Context context){
        sAppContext = context;
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
