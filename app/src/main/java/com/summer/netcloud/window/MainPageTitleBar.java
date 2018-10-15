package com.summer.netcloud.window;

import android.app.Activity;
import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.summer.netcore.VpnConfig;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;
import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.traffic.TrafficMgr;
import com.summer.netcloud.utils.ResTools;
import com.summer.netcloud.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 21/06/2018.
 */

public class MainPageTitleBar implements IMsgListener, View.OnClickListener{

    private TitleBar mTitlebar;
    private TextView mStart;


    public MainPageTitleBar(Context context){

        int textSize = (int)ResTools.getDimen(R.dimen.textsize1);
        mTitlebar = new TitleBar(context);
        mTitlebar.setTitle(R.string.app_name);
        mTitlebar.getTitle().setOnClickListener(this);

        mStart = new TextView(context);
        mStart.setOnClickListener(this);
        int hpd = ScreenUtils.dp2px(5);
        int vpd = ScreenUtils.dp2px(2);
        mStart.setPadding(hpd,vpd,hpd,vpd);
        mStart.setGravity(Gravity.CENTER);
        mStart.setBackgroundResource(R.drawable.start_or_pause);
        mStart.setTextColor(ResTools.getColor(R.color.background));
        mStart.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mStart.setMinWidth(ScreenUtils.dp2px(60));
        mTitlebar.addRight(mStart);

        MsgDispatcher.get().registerMsg(Messege.VPN_START,this);
        MsgDispatcher.get().registerMsg(Messege.VPN_STOP,this);

        updateVPNState();
    }

    public View getView(){
        return mTitlebar;
    }

    public void update(){
        updateVPNState();
    }

    private void updateVPNState(){
        if(TrafficMgr.getInstance().isCtrlSetEmpty()){
            mStart.setSelected(true);
            mStart.setText(R.string.add);
        }else{
            if(TrafficMgr.getInstance().isEnable()){
                mStart.setSelected(true);
                mStart.setText(R.string.stop);
            }else{
                mStart.setSelected(false);
                mStart.setText(R.string.start);
            }
        }

    }

    @Override
    public void onMessage(int msgId, Object arg) {
        switch (msgId){
            case Messege.VPN_START:
            case Messege.VPN_STOP:{
                updateVPNState();
                break;
            }
        }
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        return null;
    }

    @Override
    public void onClick(View v) {
        if(v == mStart){
            if(TrafficMgr.getInstance().isCtrlSetEmpty()){
                List<Integer> excludes = new ArrayList<>();
                excludes.add(Process.myUid());
                AppsSelectWindow w = new AppsSelectWindow(v.getContext(), excludes, new AppsSelectWindow.IResultCallback() {
                    @Override
                    public void onSelect(List<Integer> uids) {
                        for(int uid:uids){
                            String suid = String.valueOf(uid);
                            VpnConfig.updateCtrl(VpnConfig.CtrlType.APP, suid, VpnConfig.AVAIL_CTRLS.BASE);
                        }
                    }
                });
                MsgDispatcher.get().dispatch(Messege.PUSH_WINDOW,w);
            }else{
                if(TrafficMgr.getInstance().isEnable()){
                    TrafficMgr.getInstance().stop();
                }else{
                    TrafficMgr.getInstance().start();
                }
            }

        }else if(v == mTitlebar.getTitle()){
            MsgDispatcher.get().dispatchSync(Messege.PUSH_WINDOW, new SettingsWindow(ContextMgr.getContext()));
        }
    }
}
