package com.summer.netcloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.startup.Starter;
import com.summer.netcloud.traffic.ConnInfo;
import com.summer.netcloud.window.AbsWindow;
import com.summer.netcloud.window.AppConnectionsWindow;
import com.summer.netcloud.window.TCPLogsWindow;
import com.summer.netcloud.window.TrafficCtrlWindow;
import com.summer.netcloud.window.WindowStack;
import com.summer.netcore.VpnConfig;

public class MainActivity extends Activity implements IMsgListener{

    private WindowStack mEnv;

    public static final String ACT_OPEN_WINDOW_STRATEGY_CTRL = "intent_action_openwindow";

    public static final int ACT_REQ_CODE_VPN_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContextMgr.setContext(this);

        mEnv = new WindowStack(this);
        setContentView(mEnv.getView());

        MsgDispatcher.get().registerMsg(Messege.BACK_PRESSED,this);
        MsgDispatcher.get().registerMsg(Messege.PUSH_WINDOW,this);
        MsgDispatcher.get().registerMsg(Messege.PUSH_WINDOW_WITHOUT_ANIM, this);
        MsgDispatcher.get().registerMsg(Messege.POP_WINDOW,this);
        MsgDispatcher.get().registerMsg(Messege.SHOW_APP_CONNS_WINDOW,this);
        MsgDispatcher.get().registerMsg(Messege.SHOW_CONN_LOGS, this);
        MsgDispatcher.get().registerMsg(Messege.START_UP_FINISHED,this);

        Starter.startup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContextMgr.setForeground(false);
        mEnv.onPause();
        MsgDispatcher.get().dispatch(Messege.APP_ON_PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextMgr.setForeground(true);
        mEnv.onResume();
        MsgDispatcher.get().dispatch(Messege.APP_ON_RESUME);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);

        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            MsgDispatcher.get().dispatchSync(Messege.ACTIVITY_RESULT_OK, requestCode);
        }else{
            MsgDispatcher.get().dispatchSync(Messege.ACTIVITY_RESULT_NO, requestCode);
        }

    }

    private void checkIntent(Intent intent){
        if(intent != null){
            if(ACT_OPEN_WINDOW_STRATEGY_CTRL.equals(intent.getAction())){
                if(!(mEnv.getTopWindow() instanceof TrafficCtrlWindow)){
                    TrafficCtrlWindow w = TrafficCtrlWindow.createWindow(this, VpnConfig.CTRL_BITS.BASE);
                    MsgDispatcher.get().dispatch(Messege.PUSH_WINDOW,w);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        MsgDispatcher.get().dispatch(Messege.BACK_PRESSED);
    }

    @Override
    public void onMessage(int msgId, Object arg) {
        switch (msgId){
            case Messege.PUSH_WINDOW:{
                mEnv.push((AbsWindow)arg);
                break;
            }
            case Messege.POP_WINDOW:{
                mEnv.pop();
                break;
            }
            case Messege.SHOW_APP_CONNS_WINDOW:{
                AppConnectionsWindow w = new AppConnectionsWindow(this);
                w.updateUID((int)arg);
                MsgDispatcher.get().dispatch(Messege.PUSH_WINDOW,w);

                break;
            }
            case Messege.SHOW_CONN_LOGS:{
                TCPLogsWindow w = new TCPLogsWindow(this);
                w.update((ConnInfo)arg);
                MsgDispatcher.get().dispatch(Messege.PUSH_WINDOW,w);
                break;
            }
            case Messege.BACK_PRESSED:{
                if(!mEnv.onBackPressed()){
                    finish();
                }

                break;
            }
            case Messege.START_UP_FINISHED:{
                checkIntent(getIntent());
                break;
            }
            case Messege.PUSH_WINDOW_WITHOUT_ANIM:{
                mEnv.push((AbsWindow)arg, false);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        ContextMgr.setContext(null);
        MsgDispatcher.get().unregisterMsg(Messege.BACK_PRESSED,this);
        MsgDispatcher.get().unregisterMsg(Messege.PUSH_WINDOW,this);
        MsgDispatcher.get().unregisterMsg(Messege.PUSH_WINDOW_WITHOUT_ANIM, this);
        MsgDispatcher.get().unregisterMsg(Messege.POP_WINDOW,this);
        MsgDispatcher.get().unregisterMsg(Messege.SHOW_APP_CONNS_WINDOW,this);
        MsgDispatcher.get().unregisterMsg(Messege.SHOW_CONN_LOGS, this);
        MsgDispatcher.get().unregisterMsg(Messege.START_UP_FINISHED, this);

        super.onDestroy();
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        onMessage(msgId, arg);

        return null;
    }
}
