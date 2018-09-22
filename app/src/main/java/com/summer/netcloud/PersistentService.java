package com.summer.netcloud;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.summer.netcore.NetCoreIface;

/**
 * Created by summer on 22/09/2018.
 */

public class PersistentService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int notificationID = NetWatcherApp.NOTIFICATION_ID;
        Notification notification = NetWatcherApp.buildNotification();
        RemoteViews content = notification.contentView;
        if(content != null){
            content.setCharSequence(R.id.netcloud_notify_desc, "setText", NetCoreIface.isServerRunning()?"VPN service is running":"VPN service is not running");
            content.setCharSequence(R.id.netcloud_notify_button, "setText", NetCoreIface.isServerRunning()?"STOP":"START");
        }

        startForeground(notificationID, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
