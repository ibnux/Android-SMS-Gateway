package com.ibnux.smsgateway;

/**
 * Created by Ibnu Maksum 2020
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class Fungsi {
    private static NotificationManager mNotificationManager;
    public static String SENT = "SMS_SENT";
    public static String DELIVERED = "SMS_DELIVERED";


    public static void sendSMS(final String number, String message, final Context cx){

        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(message))
        {

            Intent is = new Intent(SENT);
            is.putExtra("number",number);
            PendingIntent sentPI = PendingIntent.getBroadcast(cx, 0,
                    is, 0);
            Intent id = new Intent(DELIVERED);
            id.putExtra("number",number);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(cx, 0,
                    id, 0);

            try
            {
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> parts = smsManager.divideMessage(message);
                if (parts.size() > 1)
                {
                    try {
                        ArrayList<PendingIntent> spi = new ArrayList<>();
                        ArrayList<PendingIntent> dpi = new ArrayList<>();
                        for (int n = 0; n < parts.size(); n++) {
                            spi.add(sentPI);
                            dpi.add(deliveredPI);
                        }
                        smsManager.sendMultipartTextMessage(number, null, parts, spi, dpi);
                    }catch (Exception e){
                        smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
                    }
                }
                else
                {
                    smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
                }

                String result = number + ": " + message;
                Log.i(TAG, result);

                sendNotification(number, message);

                ContentValues values = new ContentValues();
                values.put("address", number);
                values.put("body", message);
                Aplikasi.app.getContentResolver()
                        .insert(Uri.parse("content://sms/sent"), values);
            }
            catch (Exception ex)
            {
                Log.e(TAG, ex.toString());
            }
        }
    }

    public static void sendNotification(String to, String msg) {
        if(mNotificationManager==null)
            mNotificationManager = (NotificationManager) Aplikasi.app.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>25) {

            NotificationChannel androidChannel = new NotificationChannel("com.ibnux.smsgateway",
                    "SMS Notifikasi", NotificationManager.IMPORTANCE_LOW);
            androidChannel.enableLights(false);
            androidChannel.enableVibration(false);
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            mNotificationManager.createNotificationChannel(androidChannel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(Aplikasi.app, 0, new Intent(Aplikasi.app, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Aplikasi.app,"com.ibnux.smsgateway")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Aplikasi.app.getText(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText("sent to "+to).setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static void writeToFile(String data,Context context) {
        try {
            FileOutputStream stream = new FileOutputStream(new File(context.getCacheDir(), "sms.txt"),true);
            try {
                stream.write((data+"\n").getBytes());
            } finally {
                stream.close();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void clearLogs(Context context) {
        try {
            FileOutputStream stream = new FileOutputStream(new File(context.getCacheDir(), "sms.txt"));
            try {
                stream.write(("").getBytes());
            } finally {
                stream.close();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFile(Context context) {
        try {
            File file = new File(context.getCacheDir(), "sms.txt");
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            return new String(bytes);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return "";
    }

    public static void log(String txt){
        Log.d("SMSin","-------------------------------");
        Log.d("SMSin",txt+"");
        Log.d("SMSin","-------------------------------");
    }

    public static void log(String tag, String txt){
        Log.d(tag,"-------------------------------");
        Log.d(tag,txt+"");
        Log.d(tag,"-------------------------------");
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
