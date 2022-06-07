package com.ibnux.smsgateway.layanan;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.ibnux.smsgateway.Utils.Fungsi;

import java.util.Collections;
import java.util.List;

public class UssdService extends AccessibilityService {

    public static String TAG = "USSD";

    @Override
    public void onCreate() {
        Fungsi.log(TAG,"UssdService onCreate");
    }

    @Override
    public void onDestroy() {
        Fungsi.log(TAG,"UssdService onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Fungsi.log(TAG,"UssdService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Fungsi.log(TAG, "onAccessibilityEvent");

        if(!getSharedPreferences("pref",0).getBoolean("gateway_on",true)){
            Fungsi.log(TAG, "gateway_off");
            return;
        }

        AccessibilityNodeInfo source = event.getSource();
        /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !String.valueOf(event.getClassName()).contains("AlertDialog")) {
            Fungsi.log(TAG, "TYPE_WINDOW_STATE_CHANGED");
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals("android.widget.TextView"))) {
            Fungsi.log(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(source.getText())) {
            Fungsi.log(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
            return;
        }

        List<CharSequence> eventText;

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            eventText = event.getText();
        } else {
            eventText = Collections.singletonList(source.getText());
        }

        String text = processUSSDText(eventText);

        if( TextUtils.isEmpty(text) ) return;

        // Close dialog
        performGlobalAction(GLOBAL_ACTION_BACK); // This works on 4.1+ only
        Fungsi.log(TAG, text);
        if(PushService.current!=null){
            PushService.writeLog("USSD Received: " + text, this);
            SmsListener.sendPOST(getSharedPreferences("pref", 0).getString("urlPost", null),
                    PushService.current.to+PushService.current.sim, text, "ussd", this);
        }else {
            PushService.writeLog("USSD Received: " + text, this);
            SmsListener.sendPOST(getSharedPreferences("pref", 0).getString("urlPost", null),
                    "ussd", text, "ussd", this);
        }
        PushService.runUssd();
    }

    private String processUSSDText(List<CharSequence> eventText) {
        for (CharSequence s : eventText) {
            String text = String.valueOf(s);
            // Return text if text is the expected ussd response
            if( true ) {
                return text;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Fungsi.log(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        Toast.makeText(this,"Ready to listen USSD",Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Fungsi.log(TAG, "onAccessibilityEvent");
//        String text = event.getText().toString();
//
//        if (event.getClassName().equals("android.app.AlertDialog")||event.getClassName().equals("com.android.phone.UssdAlertActivity")) {
//            performGlobalAction(GLOBAL_ACTION_BACK);
//            Fungsi.log(TAG, text);
//            PushService.writeLog("USSD: "+text,this);
//            SmsListener.sendPOST(getSharedPreferences("pref",0).getString("urlPost",null),
//                    "ussd", text,"ussd",this);
//            Intent i = new Intent("MainActivity");
//            i.putExtra("newMessage","newMessage");
//            LocalBroadcastManager.getInstance(Aplikasi.app).sendBroadcast(i);
//        }
//
//    }
//
//    @Override
//    public void onInterrupt() {
//    }
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        Fungsi.log(TAG, "onServiceConnected");
//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        info.flags = AccessibilityServiceInfo.DEFAULT;
//        info.packageNames = new String[]{"com.android.phone"};
//        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        setServiceInfo(info);
//    }

}