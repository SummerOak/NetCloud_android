package com.summer.netcloud.message;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.summer.netcloud.Constants;
import com.summer.netcloud.utils.Listener;
import com.summer.netcloud.utils.ObjectPool;

/**
 * Created by summer on 12/06/2018.
 */

public class MsgDispatcher {
    private static final String TAG = Constants.TAG + ".MsgDispatcher";

    SparseArray<Listener<IMsgListener>> mListeners;
    private MyHandler mH;
    private ObjectPool<MsgData> mMsgDataPool;

    static MsgDispatcher sInstance = new MsgDispatcher();


    private MsgDispatcher(){
        mListeners = new SparseArray<>();
        mH = new MyHandler(Looper.getMainLooper());
        mMsgDataPool = new ObjectPool<>(new ObjectPool.IConstructor<MsgData>() {
            @Override
            public MsgData newInstance(Object... params) {
                return new MsgData();
            }

            @Override
            public void initialize(MsgData e, Object... params) {
                e.l = (IMsgListener)params[0];
                e.arg = params[1];
            }
        },20);
    }

    public static MsgDispatcher get(){
        return sInstance;
    }

    public void registerMsg(int msgId,IMsgListener l){
        synchronized (mListeners){
            Listener<IMsgListener> ls = mListeners.get(msgId);
            if(ls == null){
                ls = new Listener<>();
                mListeners.put(msgId, ls);
            }

            ls.add(l);
        }
    }

    public void unregisterMsg(int msgId,IMsgListener l){
        synchronized (mListeners){
            Listener<IMsgListener> ls = mListeners.get(msgId);
            if(ls == null){
                return;
            }

            ls.remove(l);
        }
    }

    public void dispatch(int msgId){
        dispatch(msgId,null);
    }

    public void dispatch(final int msgId,final Object arg){
        Listener<IMsgListener> ls = mListeners.get(msgId);
        if(ls != null){
            for(IMsgListener l:ls.alive()){
                mH.obtainMessage(msgId,mMsgDataPool.obtain(l,arg)).sendToTarget();
            }
        }
    }

    public Object dispatchSync(int msgId){
        return dispatchSync(msgId,null);
    }

    public Object dispatchSync(final int msgId, final Object arg){
        Object ret = new Object();
        Listener<IMsgListener> ls = mListeners.get(msgId);
        if(ls != null){

            for(IMsgListener l: ls.alive()){
                ret = l.onSyncMessage(msgId,arg);
            }
        }

        return ret;
    }


    private static class MsgData{
        IMsgListener l;
        Object arg;
    }


    private class MyHandler extends Handler{

        private MyHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MsgData e = (MsgData)msg.obj;
            int w = msg.what;
            IMsgListener l = e.l;
            Object arg = e.arg;
            mMsgDataPool.recycle(e);

            l.onMessage(w,arg);

        }
    }


}
