package com.summer.netcloud;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.summer.crashsdk.CrashSDK;
import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.traffic.TrafficMgr;
import com.summer.netcloud.utils.JobScheduler;
import com.summer.netcore.NetCoreIface;

/**
 * Created by summer on 25/06/2018.
 */

public class NetWatcherApp extends Application implements IMsgListener{

    private static Notification sNotification = null;
    public static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "NetCloud";

    @Override
    public void onCreate() {
        super.onCreate();

        CrashSDK.init(getApplicationContext());
        ContextMgr.setContext(getApplicationContext());
        JobScheduler.init();
        TrafficMgr.getInstance().init();

        MsgDispatcher.get().registerMsg(Messege.VPN_STOP, this);
        MsgDispatcher.get().registerMsg(Messege.VPN_START, this);

        showNotification(this);
    }

    @Override
    public void onMessage(int msgId, Object arg) {
        if(msgId == Messege.VPN_STOP){
            showNotification(this);
        }else if(msgId == Messege.VPN_START){
            RemoteViews content = sNotification.contentView;
            if(content != null){
                showNotification(this);
            }
        }
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        if(msgId == Messege.VPN_STOP){
            showNotification(this);
        }else if(msgId == Messege.VPN_START){
            showNotification(this);
        }

        return null;
    }


    public static void showNotification(Context ctx){
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        sNotification = buildNotification();
        RemoteViews content = sNotification.contentView;
        if(content != null){
            content.setCharSequence(R.id.netcloud_notify_desc, "setText", NetCoreIface.isServerRunning()?"VPN service is running":"VPN service is not running");
            content.setCharSequence(R.id.netcloud_notify_button, "setText", NetCoreIface.isServerRunning()?"STOP":"START");
        }

        nMgr.notify(NOTIFICATION_ID, sNotification);
    }

    public static Notification buildNotification(){
        if(sNotification != null){
            return sNotification;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Context ctx = ContextMgr.getApplicationContext();
            RemoteViews content = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);

            Notification.Builder builder = getNotificationBuilder(ctx, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            Intent notificationIntent = new Intent(ctx.getApplicationContext(), MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent contentPendingIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

            sNotification =  builder
                    .setSmallIcon(R.drawable.icon_ntf)
                    .setContentIntent(contentPendingIntent)
                    .setOngoing(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setOnlyAlertOnce(true)
                    .build();

            Intent ntfReceiver = new Intent(ctx, NotificationReceiver.class);
            ntfReceiver.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ntfReceiver.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, ntfReceiver, 0);
            content.setOnClickPendingIntent(R.id.netcloud_notify_button, pendingIntent);


            sNotification.contentView = content;
            sNotification.flags = sNotification.flags | Notification.FLAG_NO_CLEAR;

        }

        return sNotification;
    }

    private static Notification.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(com.summer.netcore.R.string.app_name);
        String description = "NetCloud";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nChannel.setSound(null, null);
                nm.createNotificationChannel(nChannel);
            }
        }
    }

}
