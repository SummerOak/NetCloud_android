package com.summer.netcloud.traffic;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.util.SparseArray;

import com.summer.netcore.NetCoreIface;
import com.summer.netcore.VpnConfig;
import com.summer.netcloud.Constants;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.MainActivity;
import com.summer.netcloud.NetWatcherApp;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.Listener;
import com.summer.netcloud.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 14/06/2018.
 */

public class TrafficMgr implements NetCoreIface.IListener{

    private static final String TAG = Constants.TAG + ".TrafficMgr";

    public static final int UNKNOWN_UID = -1;
    private static final TrafficMgr sInstance = new TrafficMgr();
    private Listener<ITrafficListener> mListeners = new Listener<>();

    private SparseArray<ConnInfo> mId2Conn = new SparseArray<>();
    private SparseArray<List<ConnInfo>> mUID2Conns = new SparseArray<>();
    private SparseArray<Integer> mUID2ConnNum = new SparseArray<>();
    private long mTotalConns = 0L;

    private final int MAX_RETAIN_CONN_SIZE = 50;


    private boolean mEnable = false;


    public static final TrafficMgr getInstance(){
        return sInstance;
    }

    public int init(){
        NetCoreIface.init(ContextMgr.getApplicationContext());
        NetCoreIface.setForgroundNotifycation(NetWatcherApp.buildNotification());
        NetCoreIface.setListener(this);
        return 0;
    }

    public int start(Context context){
        if(isCtrlSetEmpty()){
            Intent intent = new Intent(context, MainActivity.class);
//            intent.setAction(MainActivity.ACT_OPEN_WINDOW_STRATEGY_CTRL);
            context.startActivity(intent);

            return 1;
        }
        return NetCoreIface.startVpn(context);
    }

    public int stop(){
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
