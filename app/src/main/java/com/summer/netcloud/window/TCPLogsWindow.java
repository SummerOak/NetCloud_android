package com.summer.netcloud.window;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summer.netcloud.R;
import com.summer.netcloud.traffic.ConnInfo;
import com.summer.netcloud.traffic.IP;
import com.summer.netcloud.traffic.TCPLog;
import com.summer.netcloud.traffic.TrafficMgr;
import com.summer.netcloud.utils.PackageUtils;
import com.summer.netcloud.utils.ResTools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by summer on 07/07/2018.
 */

public class TCPLogsWindow extends AbsListContentWindow<TCPLog,TCPLogsWindow.ItemView> implements TrafficMgr.ITrafficListener{

    private ConnInfo mConnInfo;
    private LinearLayout mTitleBar;
    private TextView mTitle;
    private ImageView mAppIcon;

    private Date mDate;
    private SimpleDateFormat mFormat;

    public TCPLogsWindow(Context context) {
        super(context);

        mDate = new Date();
        mFormat = new SimpleDateFormat("HH:mm:ss:SSS");
    }

    public void update(int id){
        update(TrafficMgr.getInstance().getConn(id));
    }

    public void update(ConnInfo connInfo){
        mConnInfo = connInfo;
        if(mConnInfo != null){
            PackageUtils.AppInfo appInfo = PackageUtils.getAppInfo(mConnInfo.uid);

            if(appInfo != null){
                mAppIcon.setImageDrawable(appInfo.icon);
            }else{
                mAppIcon.setImageResource(PackageUtils.getDefaultAppIcon(mConnInfo.uid));
            }

        }
        updateTitle();
    }

    private void updateTitle(){
        if(mConnInfo != null){
            mTitle.setText(IP.getProtocolName(mConnInfo.protocol) + "("+IP.getStateName(mConnInfo.protocol, mConnInfo.state) + ")");
            updateData(mConnInfo.tcp_logs);
        }
    }

    @Override
    protected View getTitleBar() {
        if(mTitleBar == null){
            mTitleBar = new LinearLayout(getContext());
            mTitleBar.setGravity(Gravity.CENTER_VERTICAL);
            mTitleBar.setMinimumHeight((int) ResTools.getDimen(R.dimen.title_bar_height));
            mTitleBar.setOrientation(LinearLayout.HORIZONTAL);
            mTitleBar.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            mTitleBar.setBackgroundResource(R.color.black);

            mAppIcon = new ImageView(getContext());
            int iconSize = (int)ResTools.getDimen(R.dimen.icon_size);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(iconSize, iconSize);
            lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
            mAppIcon.setLayoutParams(lp);
            mTitleBar.addView(mAppIcon, lp);


            int textSize = (int)ResTools.getDimen(R.dimen.textsize1);
            mTitle = new TextView(getContext());
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTitle.setTextColor(ResTools.getColor(R.color.background));
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
            mTitleBar.addView(mTitle,lp);
        }

        return mTitleBar;
    }

    @Override
    protected void preSwitchIn() {
        super.preSwitchIn();
        TrafficMgr.getInstance().addListener(this);
    }

    @Override
    protected void postSwitchOut() {
        super.postSwitchOut();
        TrafficMgr.getInstance().removeListener(this);
    }

    @Override
    protected int getItemId(TCPLog item) {
        return mConnInfo==null?0:mConnInfo.tcp_logs.indexOf(item);
    }

    @Override
    protected TCPLogsWindow.ItemView createItemView(int position) {
        return new ItemView(getContext());
    }

    @Override
    protected void bindItem(TCPLog item, TCPLogsWindow.ItemView view) {
        view.bind(item);
    }

    @Override
    public void onConnectCreate(int id, int uid, byte protocol) {

    }

    @Override
    public void onConnectDestroy(int id, int uid) {
        if(mConnInfo != null && id == mConnInfo.id){
            updateTitle();
        }
    }

    @Override
    public void onConnectState(int id, byte state) {
        if(mConnInfo != null && id == mConnInfo.id){
            updateTitle();
        }
    }

    @Override
    public void onTrafficAccept(int id, int bytes, long total, int flag) {
        if(mConnInfo != null && id == mConnInfo.id){
            updateData(mConnInfo.tcp_logs);
        }
    }

    @Override
    public void onTrafficBack(int id, int bytes, long total, int flag) {
        if(mConnInfo != null && id == mConnInfo.id){
            updateData(mConnInfo.tcp_logs);
        }
    }

    @Override
    public void onTrafficSent(int id, int bytes, long total, int flag) {

    }

    @Override
    public void onTrafficRecv(int id, int bytes, long total, int flag) {

    }

    private final String getTime(long time){
        mDate.setTime(time);
        return mFormat.format(mDate);
    }

    private final String getTCPFlag(int flag){
        String ret = "";
        if((flag&IP.TCPF.TCPF_FIN) > 0){
            ret += "FIN|";
        }

        if((flag&IP.TCPF.TCPF_SYN) > 0){
            ret += "SYN|";
        }

        if((flag&IP.TCPF.TCPF_RST) > 0){
            ret += "RST|";
        }

        if((flag&IP.TCPF.TCPF_PSH) > 0){
            ret += "PSH|";
        }

        if((flag&IP.TCPF.TCPF_ACK) > 0){
            ret += "ACK|";
        }

        if((flag&IP.TCPF.TCPF_URG) > 0){
            ret += "URG|";
        }

        if((flag&IP.TCPF.TCPF_ECE) > 0){
            ret += "ECE|";
        }

        if((flag&IP.TCPF.TCPF_CWR) > 0){
            ret += "CWR|";
        }

        return ret;
    }

    public class ItemView extends LinearLayout{

        private TextView mDetails;
        private TextView mTime;

        public ItemView(Context context) {
            super(context);

            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);

            int hp = (int)ResTools.getDimen(R.dimen.hor_padding);
            int vp = (int)ResTools.getDimen(R.dimen.vtl_padding);

            int textsize = (int)ResTools.getDimen(R.dimen.textsize2);
            int textsize1 = (int)ResTools.getDimen(R.dimen.textsize3);
            mDetails = new TextView(context);
            mDetails.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
            mDetails.setTextColor(ResTools.getColor(R.color.text));
            mDetails.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;
            lp.weight = 1;
            lp.topMargin = lp.bottomMargin = vp;
            addView(mDetails, lp);
            mTime = new TextView(context);
            mTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize1);
            mTime.setTextColor(ResTools.getColor(R.color.text1));
            mTime.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL; lp.weight = 2;
            lp.topMargin = lp.bottomMargin = vp;
            addView(mTime,lp);

            setPadding(hp,vp,hp,vp);
            setBackgroundResource(R.drawable.list_item_normal);
        }

        public void bind(TCPLog item){
            mDetails.setText((item.direct== IP.DIRECT.IN?"<<< ":">>> ") + item.seq + "/" + item.ack + ": " + getTCPFlag(item.flag) + " " + (item.size>0? item.size+"bytes":""));
            mTime.setText(getTime(item.time));
        }


    }
}
