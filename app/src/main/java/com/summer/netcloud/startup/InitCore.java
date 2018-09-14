package com.summer.netcloud.startup;

import com.summer.netcloud.Constants;
import com.summer.netcloud.traffic.TrafficMgr;
import com.summer.netcloud.utils.Log;

/**
 * Created by summer on 13/06/2018.
 */

public class InitCore extends Starter.Step {
    private static final String TAG = Constants.TAG + ".InitCore";

    @Override
    protected int start() {
        Log.d(TAG,"init net core...");

        TrafficMgr.getInstance().init();


        return 0;
    }
}
