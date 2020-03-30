package com.yuriyk_israelb.finalProject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class CallRecordService extends Service {

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallRecordReceiver callReceiver;
    public static NotificationManager notificationManager;
    public static String CHANNEL_ID = "channel1";
    public static String CHANNEL_NAME = "Records";
    public static final String CHANNEL1_ID = "FGServiceChannel";
    public static final int FG_NOTIFICATION_ID = 111;
    public static int notificationId = 1;




    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(callReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotificationChannel();
        createFGNotification();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.callReceiver = new CallRecordReceiver();
        this.registerReceiver(this.callReceiver, filter);

        return START_NOT_STICKY;
    }


    private void createFGNotification()
    {
        // create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL1_ID,
                    "FG Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(serviceChannel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // create Notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Recording Running...")
                .setContentText("Tap to Stop this FG Service!")
//                .setSmallIcon(R.drawable.ic_fg_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(FG_NOTIFICATION_ID, notification);
    }


    private void setupNotificationChannel()
    {
        // 1. Get reference to Notification Manager
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 2. Create Notification Channel ONLY ONEs.
        //    Need for Android 8.0 (API level 26) and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            //Create channel only if it is not already created
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null)
            {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT); // NotificationManager.IMPORTANCE_HIGH
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

}
