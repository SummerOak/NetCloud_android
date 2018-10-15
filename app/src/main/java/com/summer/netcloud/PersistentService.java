package com.summer.netcloud;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.ResTools;
import com.summer.netcore.NetCoreIface;

/**
 * Created by summer on 22/09/2018.
 */

public class PersistentService extends Service implements IMsgListener{

    public static boolean sRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        MsgDispatcher.get().registerMsg(Messege.APP_ON_RESUME, this);

        updateNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sRunning = true;
        updateNotification();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MsgDispatcher.get().unregisterMsg(Messege.APP_ON_RESUME, this);

        sRunning = false;
    }

    private void updateNotification(){
        int notificationID = NetWatcherApp.NOTIFICATION_ID;
        Notification notification = NetWatcherApp.getNotification();
        RemoteViews content = notification.contentView;
        if(content != null){
            content.setCharSequence(R.id.netcloud_notify_desc, "setText", ResTools.getString(NetCoreIface.isServerRunning()? R.string.vpn_running:R.string.vpn_not_running));
            content.setCharSequence(R.id.netcloud_notify_button, "setText", ResTools.getString(NetCoreIface.isServerRunning()? R.string.stop:R.string.start));
        }

        startForeground(notificationID, notification);
    }

    @Override
    public void onMessage(int msgId, Object arg) {
        onSyncMessage(msgId, arg);
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        if(msgId == Messege.APP_ON_RESUME){
            updateNotification();
        }
        return null;
    }
}
