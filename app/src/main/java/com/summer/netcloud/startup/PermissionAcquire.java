package com.summer.netcloud.startup;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Process;

import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.PermissionMgr;

/**
 * Created by summer on 04/10/2018.
 */

public class PermissionAcquire extends Starter.Task implements PermissionMgr.IPermissionListener{

    @Override
    protected int start() {
        PermissionMgr.get().ensureVpnPermission(this);
        return 1;
    }


    @Override
    public void onPermissionGranted() {
        finish();
    }

    @Override
    public void onPermissionDenied() {
        Activity activity = (Activity)ContextMgr.getContext();
        if(activity != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activity.finishAffinity();
            }else{
                activity.finish();
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
            }
        }, 200);
    }
}
