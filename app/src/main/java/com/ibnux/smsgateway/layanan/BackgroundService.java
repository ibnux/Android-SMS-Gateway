package com.ibnux.smsgateway.layanan;

/**
 * Created by Ibnu Maksum 2020
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ibnux.smsgateway.Aplikasi;
import com.ibnux.smsgateway.Fungsi;
import com.ibnux.smsgateway.MainActivity;
import com.ibnux.smsgateway.R;

public class BackgroundService extends Service {

    public BackgroundService() {

    }

    @Override
    public void onCreate() {
        Fungsi.log("BackgroundService onCreate");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter("BackgroundService"));
    }

    @Override
    public void onDestroy() {
        Fungsi.log("BackgroundService onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Fungsi.log("BackgroundService onStartCommand");
        NotificationManager mNotificationManager = (NotificationManager) Aplikasi.app.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>25) {
            NotificationChannel androidChannel = new NotificationChannel("com.ibnux.smsgateway.bg",
                    "Background", NotificationManager.IMPORTANCE_LOW);
            androidChannel.enableLights(false);
            androidChannel.enableVibration(false);
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            mNotificationManager.createNotificationChannel(androidChannel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(Aplikasi.app, 0, new Intent(Aplikasi.app, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Aplikasi.app,"com.ibnux.smsgateway")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Aplikasi.app.getText(R.string.app_name))
                .setOngoing(true)
                .setContentText("Listening for push")
                .setAutoCancel(false);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(2, mBuilder.build());
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Fungsi.log("BackgroundService onBind");
        return null;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Fungsi.log("BackgroundService BroadcastReceiver received");
            LocalBroadcastManager.getInstance(BackgroundService.this).sendBroadcast(new Intent("MainActivity"));
        }
    };
}
