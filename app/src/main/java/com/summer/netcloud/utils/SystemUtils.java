package com.summer.netcloud.utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import com.summer.netcloud.ContextMgr;

/**
 * Created by summer on 02/10/2018.
 */

public class SystemUtils {


    public static boolean batteryOptimizing() {
        Context context = ContextMgr.getApplicationContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.util.Log.d("jjjj", "pm.isIgnoringBatteryOptimizations(context.getPackageName() " + pm.isIgnoringBatteryOptimizations(context.getPackageName()));
            return !pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }

        android.util.Log.d("jjjj", "pm.isIgnoringBatteryOptimizations(context.getPackageName() false");
        return false;
    }
}

