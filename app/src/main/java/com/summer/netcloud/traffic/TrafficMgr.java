package com.summer.netcloud.traffic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.Toast;

import com.summer.netcloud.Constants;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.MainActivity;
import com.summer.netcloud.NetWatcherApp;
import com.summer.netcloud.PermissionMgr;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.JobScheduler;
import com.summer.netcloud.utils.Listener;
import com.summer.netcloud.utils.Log;
import com.summer.netcloud.utils.PackageUtils;
import com.summer.netcloud.utils.SystemUtils;
import com.summer.netcore.Config;
import com.summer.netcore.NetCoreIface;
import com.summer.netcore.VpnConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by summer on 14/06/2018.
 */

public class TrafficMgr implements NetCoreIface.IListener, PermissionMgr.IPermissionListener{

    private static final String TAG = Constants.TAG + ".TrafficMgr";

    public static final int UNKNOWN_UID = -1;
    private static final TrafficMgr sInstance = new TrafficMgr();
    private Listener<ITrafficListener> mListeners = new Listener<>();

    private SparseArray<ConnInfo> mId2Conn = new SparseArray<>();
    private SparseArray<List<ConnInfo>> mUID2Conns = new SparseArray<>();
    private SparseArray<Integer> mUID2ConnNum = new SparseArray<>();
    private long mTotalConns = 0L;
    private boolean mHadRequestIgnoreBatteryOpt = false;

    private final int MAX_RETAIN_CONN_SIZE = 50;


    private boolean mEnable = false;

    private boolean mPendingStart = false;


    public static final TrafficMgr getInstance(){
        return sInstance;
    }

    public int init(){
        NetCoreIface.init(ContextMgr.getApplicationContext());
        NetCoreIface.setForgroundNotifycation(NetWatcherApp.NOTIFICATION_ID, NetWatcherApp.buildNotification());
        NetCoreIface.setListener(this);

        VpnConfig.addListener(new VpnConfig.IListener() {
            @Override
            public void onVpnConfigLoaded() {
                if(NetWatcherApp.isFirstLaunch()){
                    initDefaultSettings();
                }
            }

            @Override
            public void onVpnConfigItemUpdated(int i, String s) {

            }
        });


        return 0;
    }

    private void initDefaultSettings(){
        JobScheduler.scheduleBackground(new JobScheduler.Job("init-default-settings") {
            @Override
            public void run() {
                VpnConfig.updateCtrl(VpnConfig.CtrlType.APP, String.valueOf(0), Constants.DEFAULT_SYSTEM_CTRL);
                VpnConfig.updateCtrl(VpnConfig.CtrlType.APP, String.valueOf(-1), Constants.DEFAULT_UNKNOWN_CTRL);
                if(!Constants.DEFAULT_APP_CTRLS.isEmpty()){
                    List<PackageUtils.AppInfo> installedApps = PackageUtils.getAllInstallApps();
                    if (installedApps != null) {
                        for(PackageUtils.AppInfo ai:installedApps){
                            if(Constants.DEFAULT_APP_CTRLS.containsKey(ai.pkg)){
                                VpnConfig.updateCtrl(VpnConfig.CtrlType.APP, String.valueOf(ai.uid), Constants.DEFAULT_APP_CTRLS.get(ai.pkg));
                            }
                        }
                    }
                }

                if(!Constants.DEFAULT_IP_CTRLS.isEmpty()){
                    Iterator<Map.Entry<String, VpnConfig.AVAIL_CTRLS>> itr = Constants.DEFAULT_IP_CTRLS.entrySet().iterator();
                    while(itr.hasNext()){
                        Map.Entry<String, VpnConfig.AVAIL_CTRLS> entry = itr.next();
                        VpnConfig.updateCtrl(VpnConfig.CtrlType.IP, entry.getKey(), entry.getValue());
                    }

                }

                if(Constants.USE_DEFAULT_PROXY){
                    VpnConfig.setConfig(Config.PROXY_IPVER, String.valueOf(Constants.DEFAULT_PROXY_IPVER));
                    VpnConfig.setConfig(Config.PROXY_ADDR, Constants.DEFAULT_PROXY_ADDR);
                    VpnConfig.setConfig(Config.PROXY_PORT, Constants.DEFAULT_PROXY_PORT);
                }

                if(Constants.USE_DEFAULT_DNS){
                    VpnConfig.setConfig(Config.DNS_SERVER, Constants.DEFAULT_DNS);
                }


            }
        });
    }

    public int start(){

        mPendingStart = true;
        return PermissionMgr.get().ensureVpnPermission(this);
    }

    public int stop(){
        mPendingStart = false;
        return NetCoreIface.stopVpn(ContextMgr.getApplicationContext());
    }

    public boolean isCtrlSetEmpty(){
        List<Pair<String,VpnConfig.AVAIL_CTRLS>> ctrls = VpnConfig.getCtrls(VpnConfig.CtrlType.APP, VpnConfig.CTRL_BITS.BASE);
        return ctrls == null || ctrls.isEmpty();
    }

    public ConnInfo getConn(int id){
        synchronized (mId2Conn){
            return mId2Conn.get(id);
        }
    }

    public int getUid(int id){
        ConnInfo conn = getConn(id);
        if(conn != null){
            return conn.uid;
        }

        return UNKNOWN_UID;
    }

    public int getConnNum(int uid){
        Integer r = mUID2ConnNum.get(uid);
        return r == null? 0:r;
    }

    public long getTotalConnNum(){
        return mTotalConns;
    }

    public SparseArray<List<ConnInfo>> getConnsCategoryByUid(){
        SparseArray<List<ConnInfo>> r = new SparseArray<>();
        synchronized (mUID2Conns){
            for (int i = 0; i < mUID2Conns.size(); i++) {
                int key = mUID2Conns.keyAt(i);
                List<ConnInfo> value = mUID2Conns.valueAt(i);
                r.append(key, new ArrayList<>(value));
            }
        }

        return r;
    }

    public List<ConnInfo> getConnsOfUid(int uid){
        List<ConnInfo> r = null;
        synchronized (mUID2Conns){
            r = new ArrayList<>(mUID2Conns.get(uid));
        }

        return r;
    }

    public boolean isEnable(){
        return mEnable;
    }

    public void addListener(ITrafficListener listener){
        mListeners.add(listener);
    }

    public void removeListener(ITrafficListener listener){
        mListeners.remove(listener);
    }


    @Override
    public void onEnable() {
        Log.d(TAG,"onEnable");
        mEnable = true;
        MsgDispatcher.get().dispatch(Messege.VPN_START);
    }

    @Override
    public void onDisable() {
        Log.d(TAG,"onDisable");
        mEnable = false;
        MsgDispatcher.get().dispatch(Messege.VPN_STOP);
    }

    @Override
    public void onConnectCreate(final int id, final int uid, final byte b, String dest, int destPort) {
        Log.d(TAG,"onConnectCreate id="+id+","+uid+","+b);

        ConnInfo conn = null;
        synchronized (mId2Conn){
            if(mId2Conn.get(id) != null){
                Log.e(TAG,"recreate conn: " + id);
                return;
            }


            conn = new ConnInfo();
            conn.id = id;
            conn.uid = uid;
            conn.protocol = b;
            conn.dest = dest;
            conn.destPort = destPort;
            conn.born_time = System.currentTimeMillis();
            conn.alive = true;
            mId2Conn.put(conn.id,conn);
        }

        synchronized (mUID2Conns){
            List<ConnInfo> conns = mUID2Conns.get(conn.uid);
            if(conns == null){
                conns = new ArrayList<>();
                mUID2Conns.put(conn.uid, conns);
            }

            Integer connNum = mUID2ConnNum.get(conn.uid);
            mUID2ConnNum.put(conn.uid, connNum==null?1:++connNum);
            mTotalConns++;

            conns.add(conn);

            int curSize = conns.size();
            if(curSize >= MAX_RETAIN_CONN_SIZE){
                List<ConnInfo> del = new ArrayList<>();
                for(ConnInfo ci:conns){
                    if(!ci.alive){
                        del.add(ci);
                        mId2Conn.remove(ci.id);
                    }

                    if(curSize-del.size() < MAX_RETAIN_CONN_SIZE){
                        break;
                    }
                }

                conns.removeAll(del);
            }
        }

        for(ITrafficListener l: mListeners.alive()){
            l.onConnectCreate(id,uid,b);
        }

    }

    @Override
    public void onConnectDestroy(final int i, final int i1) {
        Log.d(TAG,"onConnectDestroy id="+i+", " + i1);

        ConnInfo conn = getConn(i);
        if(conn == null){
            Log.e(TAG,"wrong onTrafficAccept: " + i);
            return;
        }

        conn.alive = false;

        for(ITrafficListener l: mListeners.alive()){
            l.onConnectDestroy(i,i1);
        }
    }

    @Override
    public void onConnectState(final int i, final byte b) {
        Log.d(TAG,"onConnectState id="+i+", " +b);

        ConnInfo conn = getConn(i);
        if(conn != null){
            conn.state = b;
        }

        for(ITrafficListener l: mListeners.alive()){
            l.onConnectState(i,b);
        }

    }

    @Override
    public void onTrafficAccept(final int i, final int l, long total, int flag, int seq, int ack) {
        Log.d(TAG,"onTrafficAccept id=" + i + " , " + l);

        ConnInfo conn = getConn(i);
        if(conn == null){
            Log.e(TAG,"wrong onTrafficAccept: " + i);
            return;
        }

        conn.accept = total;
        if(conn.protocol == IP.TCP){
            conn.tcp_logs.add(new TCPLog(IP.DIRECT.OUT, l, flag, seq, ack));
        }

        for(ITrafficListener lr: mListeners.alive()){
            lr.onTrafficAccept(i,l, total, flag);
        }
    }

    @Override
    public void onTrafficBack(final int i, final int l, long total, int flag, int seq, int ack) {
        Log.d(TAG,"onTrafficBack id=" + i + " , " + l);

        ConnInfo conn = getConn(i);
        if(conn == null){
            Log.e(TAG,"wrong onTrafficBack: " + i);
            return;
        }

        conn.back = total;

        if(conn.protocol == IP.TCP){
            conn.tcp_logs.add(new TCPLog(IP.DIRECT.IN, l, flag, seq, ack));
        }

        for(ITrafficListener lr: mListeners.alive()){
            lr.onTrafficBack(i,l, total, flag);
        }
    }

    @Override
    public void onTrafficSent(int i, int l, long total, int flag) {
        Log.d(TAG,"onTrafficSent id=" + i + " , " + l);

        ConnInfo conn = getConn(i);
        if(conn == null){
            Log.e(TAG,"wrong onTrafficSent: " + i);
            return;
        }

        conn.sent = total;

        for(ITrafficListener lr: mListeners.alive()){
            lr.onTrafficSent(i,l, total, flag);
        }
    }

    @Override
    public void onTrafficRecv(int i, int l, long total, int flag) {
        Log.d(TAG,"onTrafficRecv id=" + i + " , " + l);

        ConnInfo conn = getConn(i);
        if(conn == null){
            Log.e(TAG,"wrong onTrafficRecv: " + i);
            return;
        }

        conn.recv = total;

        for(ITrafficListener lr: mListeners.alive()){
            lr.onTrafficRecv(i,l, total, flag);
        }
    }

    @Override
    public void onPermissionGranted() {
        if(mPendingStart){
            mPendingStart = false;

            Context context = ContextMgr.getApplicationContext();
            if(context != null){
                if(isCtrlSetEmpty()){
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);

                    return;
                }

                if (!mHadRequestIgnoreBatteryOpt && SystemUtils.batteryOptimizing()){
                    final Intent doze = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    doze.setData(Uri.parse("package:" + context.getPackageName()));
                    if(context.getPackageManager().resolveActivity(doze, 0) != null){
                        context.startActivity(doze);
                    }

                    mHadRequestIgnoreBatteryOpt = true;
                }

                NetCoreIface.startVpn(context);
            }

        }
    }

    @Override
    public void onPermissionDenied() {
        if(mPendingStart){
            Toast.makeText(ContextMgr.getApplicationContext(), "start failed, vpn permission is necessary.", Toast.LENGTH_LONG).show();
            mPendingStart = false;
        }
    }


    public interface ITrafficListener{
        void onConnectCreate(int id,int uid, byte protocol);
        void onConnectDestroy(int id, int uid);
        void onConnectState(int id, byte state);
        void onTrafficAccept(int id, int bytes, long total, int flag);
        void onTrafficBack(int id, int bytes, long total, int flag);
        void onTrafficSent(int id, int bytes, long total, int flag);
        void onTrafficRecv(int id, int bytes, long total, int flag);
    }
}
