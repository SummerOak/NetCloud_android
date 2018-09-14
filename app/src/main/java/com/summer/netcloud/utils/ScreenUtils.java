package com.summer.netcloud.utils;

import android.graphics.Point;
import android.util.DisplayMetrics;

import com.summer.netcloud.ContextMgr;

/**
 * Created by summer on 14/06/2018.
 */

public class ScreenUtils {

    public static final int dp2px(float dp){
        float scale= ContextMgr.getContext().getResources().getDisplayMetrics().density;
        return (int)(dp*scale+0.5f);
    }

    public static final Point getScreenSize(){
        DisplayMetrics dm = ContextMgr.getApplicationContext().getResources().getDisplayMetrics();
        Point p = new Point();
        p.x = dm.widthPixels;
        p.y = dm.heightPixels;
        return p;
    }

}
