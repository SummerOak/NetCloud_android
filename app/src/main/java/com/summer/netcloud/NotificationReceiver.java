package com.summer.netcloud;

import android.app.Activity;
import android.os.Bundle;

import com.summer.netcore.NetCoreIface;
import com.summer.netcloud.traffic.TrafficMgr;

/**
 * Created by summer on 12/06/2018.
 */

public class NotificationReceiver extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        moveTaskToBack(true);

        if(NetCoreIface.isServerRunning()){
            TrafficMgr.getInstance().stop();
        }else{
            TrafficMgr.getInstance().start(this);
        }

        finish();
    }

}
