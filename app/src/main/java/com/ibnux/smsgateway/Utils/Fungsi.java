package com.ibnux.smsgateway.Utils;

/**
 * Created by Ibnu Maksum 2020
 */

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ibnux.smsgateway.Aplikasi;
import com.ibnux.smsgateway.MainActivity;
import com.ibnux.smsgateway.R;
import com.ibnux.smsgateway.layanan.PushService;
import com.ibnux.smsgateway.layanan.UssdService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Fungsi {
    private static NotificationManager mNotificationManager;
    public static String SENT = "SMS_SENT";
    public static String DELIVERED = "SMS_DELIVERED";


    public static void sendSMS(final String number, String message, final Context cx){

        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(message))
        {
            int time = (int) System.currentTimeMillis()/1000;
            Intent is = new Intent(SENT);
            is.putExtra("number",number);
            PendingIntent sentPI = PendingIntent.getBroadcast(cx, time,
                    is, 0);
            Intent id = new Intent(DELIVERED);
            id.putExtra("number",number);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(cx, time,
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
                PushService.writeLog("SUBMIT SMS SUCCESS: " + number, cx);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                PushService.writeLog("SEND FAILED: " + number + " " + message+"\n\n"+ex.getMessage(), cx);
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

    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + UssdService.class.getCanonicalName();
        log("USSD",service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v("USSD", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("USSD", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v("USSD", "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v("USSD", "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v("USSD", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v("USSD", "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public static Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if(!ussd.startsWith("tel:"))
            uriString += "tel:";

        for(char c : ussd.toCharArray()) {

            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }

    public static List<SimInfo> getSIMInfo(Context context) {
        List<SimInfo> simInfoList = new ArrayList<>();
        Uri URI_TELEPHONY = Uri.parse("content://telephony/siminfo/");
        Cursor c = context.getContentResolver().query(URI_TELEPHONY, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                int slot = c.getInt(c.getColumnIndex("slot"));
                String display_name = c.getString(c.getColumnIndex("display_name"));
                String icc_id = c.getString(c.getColumnIndex("icc_id"));
                SimInfo simInfo = new SimInfo(id, display_name, icc_id, slot);
                Log.d("apipas_sim_info", simInfo.toString());
                simInfoList.add(simInfo);
            } while (c.moveToNext());
        }
        c.close();

        return simInfoList;
    }


}
