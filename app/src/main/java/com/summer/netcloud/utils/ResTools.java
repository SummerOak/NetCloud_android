package com.summer.netcloud.utils;

import com.summer.netcloud.ContextMgr;

/**
 * Created by summer on 14/06/2018.
 */

public class ResTools {

    public static final float getDimen(int resId){
        return ContextMgr.getContext().getResources().getDimension(resId);
    }

    public static final int getColor(int resId){
        return ContextMgr.getContext().getResources().getColor(resId,null);
    }

}
