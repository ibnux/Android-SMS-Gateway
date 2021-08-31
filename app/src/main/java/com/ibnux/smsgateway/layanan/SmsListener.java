package com.ibnux.smsgateway.layanan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static com.ibnux.smsgateway.layanan.PushService.writeLog;

public class SmsListener extends BroadcastReceiver {

    SharedPreferences sp;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(sp==null)sp = context.getSharedPreferences("pref",0);
        String url = sp.getString("urlPost",null);
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageFrom = smsMessage.getOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();
                Log.i("SMS From", messageFrom);
                Log.i("SMS Body", messageBody);
                writeLog("SMS: RECEIVED : " + messageFrom + " " + messageBody);
                if(url!=null){
                    sendPOST(url, messageFrom, messageBody,"received");
                }
            }
        }
    }

    public static void sendPOST(String urlPost,String from, String msg,String tipe){
        if(urlPost==null) return;
        if(from.isEmpty()) return;
        try {
            URL url;
            String response = "";
            try {
                url = new URL(urlPost);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(
                        "number="+URLEncoder.encode(from, "UTF-8")+
                                "&message="+URLEncoder.encode(msg, "UTF-8")+
                                "&type="+URLEncoder.encode(tipe, "UTF-8")
                );

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            writeLog("SMS: POST : "+urlPost+" : "+response);
        }catch (Exception e){
            e.printStackTrace();
            writeLog("SMS: POST FAILED : "+urlPost+" : "+e.getMessage());
        }
    }

}
