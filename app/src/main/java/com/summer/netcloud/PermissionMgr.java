package com.summer.netcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.Log;
import com.summer.netcore.VpnServer;

import java.lang.ref.WeakReference;

/**
 * Created by summer on 04/10/2018.
 */

public class PermissionMgr implements IMsgListener{
    private static final String TAG = Constants.TAG + ".PermissionMgr";

    private WeakReference<IPermissionListener> mVpnListener;

    private static final class HOLDER{
        private static final PermissionMgr sIns = new PermissionMgr();
    }

    private PermissionMgr(){
        MsgDispatcher.get().registerMsg(Messege.ACTIVITY_RESULT_OK, this);
        MsgDispatcher.get().registerMsg(Messege.ACTIVITY_RESULT_NO, this);
    }

    public static final PermissionMgr get(){
        return HOLDER.sIns;
    }

    public int ensureVpnPermission(IPermissionListener listener){
        mVpnListener = null;
        Intent intent = VpnServer.prepare(ContextMgr.getApplicationContext());
        if (intent != null) {
            Log.e(TAG, "requesting vpn permission...");
            mVpnListener = new WeakReference<>(listener);
            Activity activity = (Activity)ContextMgr.getContext();
            if(activity != null){
                activity.startActivityForResult(intent, MainActivity.ACT_REQ_CODE_VPN_PERMISSION);
            }else{
                Context appContext = ContextMgr.getApplicationContext();
                Intent main = new Intent(appContext, MainActivity.class);
                appContext.startActivity(main);
            }

            return 1;
        }else{
            if(listener != null){
                listener.onPermissionGranted();
            }
        }

        return 0;
    }

    @Override
    public void onMessage(int msgId, Object arg) {

    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        switch (msgId){
            case Messege.ACTIVITY_RESULT_OK:{
                int reqCode = (int)arg;
                if(reqCode == MainActivity.ACT_REQ_CODE_VPN_PERMISSION){
                    if(mVpnListener != null){
                        IPermissionListener listener = mVpnListener.get();
                        if(listener != null){
                            listener.onPermissionGranted();
                        }
                    }
                }
                break;
            }
            case Messege.ACTIVITY_RESULT_NO:{
                int reqCode = (int)arg;
                if(reqCode == MainActivity.ACT_REQ_CODE_VPN_PERMISSION){
                    if(mVpnListener != null){
                        IPermissionListener listener = mVpnListener.get();
                        if(listener != null){
                            listener.onPermissionDenied();
                        }
                    }
                }

                break;
            }
        }
        return null;
    }


    public interface IPermissionListener{
        void onPermissionGranted();
        void onPermissionDenied();
    }

}
