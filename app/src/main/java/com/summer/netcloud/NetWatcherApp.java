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
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import com.summer.crashsdk.CrashSDK;
import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.traffic.TrafficMgr;
import com.summer.netcloud.utils.JobScheduler;

/**
 * Created by summer on 25/06/2018.
 */

public class NetWatcherApp extends Application implements IMsgListener{

    private static Notification sNotification = null;
    public static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "NetCloud";

    private static final int FIRST_LAUNCH_NOT_INIT_YET = 0;
    private static final int FIRST_LAUNCH_YES = 1;
    private static final int FIRST_LAUNCH_NO = 2;
    private static byte sFirstLaunch = FIRST_LAUNCH_NOT_INIT_YET;//0: not init yet; 1: first launch; 2: not first launch;

    private static final String SP_FIRST_LAUNCH = "first_launch";



    @Override
    public void onCreate() {
        super.onCreate();

        CrashSDK.init(getApplicationContext());
        ContextMgr.setApplicationContext(getApplicationContext());
        JobScheduler.init();
        TrafficMgr.getInstance().init();

        MsgDispatcher.get().registerMsg(Messege.VPN_STOP, this);
        MsgDispatcher.get().registerMsg(Messege.VPN_START, this);
        MsgDispatcher.get().registerMsg(Messege.START_UP_FINISHED, this);
    }

    public static final boolean isFirstLaunch(){

        if(sFirstLaunch == FIRST_LAUNCH_NOT_INIT_YET){
            Context context = ContextMgr.getApplicationContext();
            SharedPreferences settings = context.getSharedPreferences(SP_FIRST_LAUNCH, 0);
            sFirstLaunch = (byte)settings.getInt(SP_FIRST_LAUNCH, FIRST_LAUNCH_YES);
        }

        return sFirstLaunch == FIRST_LAUNCH_YES;
    }

    @Override
    public void onMessage(int msgId, Object arg) {
        if(msgId == Messege.VPN_STOP){
            startPersistentService(this);
        }else if(msgId == Messege.VPN_START){
            startPersistentService(this);
        }else if(msgId == Messege.START_UP_FINISHED){
            SharedPreferences settings = getSharedPreferences(SP_FIRST_LAUNCH, 0);
            settings.edit().putInt(SP_FIRST_LAUNCH, FIRST_LAUNCH_NO).commit();

            startPersistentService(this);
        }
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        onMessage(msgId, arg);

        return null;
    }

    public static void startPersistentService(Context context){
        Intent intent = new Intent(context, PersistentService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

    }

    public static Notification getNotification(){
        if(sNotification != null){
            return sNotification;
        }

        Context ctx = ContextMgr.getApplicationContext();
        RemoteViews content = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);

        Notification.Builder builder = getNotificationBuilder(ctx, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        Intent notificationIntent = new Intent(ctx.getApplicationContext(), MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

        builder.setSmallIcon(R.drawable.icon_ntf)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOnlyAlertOnce(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sNotification = builder.build();
        }else{
            sNotification = builder.getNotification();
        }


        Intent ntfReceiver = new Intent(ctx, NotificationReceiver.class);
        ntfReceiver.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ntfReceiver.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, ntfReceiver, 0);
        content.setOnClickPendingIntent(R.id.netcloud_notify_button, pendingIntent);

        sNotification.contentView = content;
        sNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

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
