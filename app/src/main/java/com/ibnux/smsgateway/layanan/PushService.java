package com.ibnux.smsgateway.layanan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibnux.smsgateway.Aplikasi;
import com.ibnux.smsgateway.Fungsi;

import java.util.Calendar;

public class PushService extends FirebaseMessagingService {
    private String TAG = "SMSin";

    BroadcastReceiver deliveredReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String msg = null;
            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    msg = "SMS delivered";
                    break;
                case Activity.RESULT_CANCELED:
                    msg = "SMS not delivered";
                    break;
            }
            if(msg!=null) {
                Calendar cal = Calendar.getInstance();
                Fungsi.writeToFile(msg + " " +
                                cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + arg1.getStringExtra("number"),
                        Aplikasi.app
                );

                Intent i = new Intent("MainActivity");
                i.putExtra("newMessage","newMessage");
                LocalBroadcastManager.getInstance(Aplikasi.app).sendBroadcast(i);
            }
        }
    };

    BroadcastReceiver sentReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String msg = null;
            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    msg = "SENT";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    msg = "Generic failure";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    msg = "No service";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    msg = "Null PDU";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    msg = "Radio off";
                    break;
            }
            if(msg!=null) {
                Calendar cal = Calendar.getInstance();
                Fungsi.writeToFile(msg + " " +
                                cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + arg1.getStringExtra("number"),
                        Aplikasi.app
                );

                Intent i = new Intent("MainActivity");
                i.putExtra("newMessage","newMessage");
                LocalBroadcastManager.getInstance(Aplikasi.app).sendBroadcast(i);
            }
        }
    };

    @Override
    public void onCreate() {
        registerReceiver(sentReceiver, new IntentFilter(Fungsi.SENT));
        registerReceiver(deliveredReceiver, new IntentFilter(Fungsi.DELIVERED));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveredReceiver);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Fungsi.log(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String to = remoteMessage.getData().get("to");
            String message = remoteMessage.getData().get("message");
            String secret = remoteMessage.getData().get("secret");
            String time = remoteMessage.getData().get("time");

            String scrt =  getSharedPreferences("pref",0).getString("secret","");
            Fungsi.log("Local Secret "+scrt);
            Fungsi.log("received Secret "+secret);
            Fungsi.log("Time "+time);
            Fungsi.log("To "+to);
            Fungsi.log("Message "+message);

            if(!TextUtils.isEmpty(to) && !TextUtils.isEmpty(message) && !TextUtils.isEmpty(secret) && !TextUtils.isEmpty(time)){
                Calendar cal = Calendar.getInstance();
                if(System.currentTimeMillis()-(Long.parseLong(time)*1000L)<3600) {
                    //cek dulu secret vs secret, jika oke, berarti tidak diHash
                    if(scrt.equals(secret)){
                        Fungsi.sendSMS(to, message, this);
                        Fungsi.writeToFile("SEND " +
                                        cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                        cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + to + " " + message,
                                this
                        );
                    }else {
                        //hash dulu
                        scrt = Fungsi.md5(scrt.trim() + "" + time.trim());
                        Fungsi.log("MD5 : " + scrt);
                        if (scrt.toLowerCase().equals(secret.toLowerCase())) {
                            Fungsi.sendSMS(to, message, this);
                            Fungsi.writeToFile("SEND " +
                                            cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                            cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + to + " " + message,
                                    this
                            );
                        } else {
                            Fungsi.writeToFile("SECRET INVALID " +
                                            cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                            cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + to + " " + message,
                                    this
                            );
                        }
                    }
                }else{
                    Fungsi.writeToFile("TIMEOUT "+
                                    cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " " +
                                    cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + to + " " + message,
                            this
                    );
                }
                Intent i = new Intent("MainActivity");
                i.putExtra("newMessage","newMessage");
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            }

        }

    }

    @Override
    public void onNewToken(String s) {
        Fungsi.log("onNewToken "+s);
        getSharedPreferences("pref",0).edit().putString("token",s).apply();
        Intent i = new Intent("MainActivity");
        i.putExtra("newToken","newToken");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        super.onNewToken(s);
    }
}
