package com.ibnux.smsgateway.Utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.ibnux.smsgateway.layanan.PushService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SimUtil {

    public static boolean sendSMS(Context ctx, int simID, String toNum, String centerNum, String smsText, int retry) {
        String name;

        try {
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                PushService.writeLog("can not get service which for sim '" + simID + "', only 0,1 accepted as values", ctx);
                return false;
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            int time = (int) System.currentTimeMillis()/1000;

            Intent is = new Intent(Fungsi.SENT);
            is.putExtra("number",toNum);
            is.putExtra("centerNum",centerNum);
            is.putExtra("simID",simID);
            is.putExtra("smsText",smsText);
            is.putExtra("retry",retry);
            PendingIntent sentPI = PendingIntent.getBroadcast(ctx, time,
                    is, 0);
            Intent id = new Intent(Fungsi.DELIVERED);
            is.putExtra("number",toNum);
            is.putExtra("centerNum",centerNum);
            is.putExtra("simID",simID);
            is.putExtra("smsText",smsText);
            is.putExtra("retry",retry);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, time,
                    id, 0);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            try {
                if (stubObj != null) {
                    if (Build.VERSION.SDK_INT < 18) {
                        method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                        method.invoke(stubObj, toNum, centerNum, smsText, sentPI, deliveredPI);
                    } else {
                        method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                        method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsText, sentPI, deliveredPI);
                    }
                } else {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                    if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                        List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(simID);
                        SmsManager
                                .getSmsManagerForSubscriptionId(simInfo.getSubscriptionId())
                                .sendTextMessage(toNum, null, smsText, sentPI, deliveredPI);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                    List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                    SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(simID);
                    SmsManager
                            .getSmsManagerForSubscriptionId(simInfo.getSubscriptionId())
                            .sendTextMessage(toNum, null, smsText, sentPI, deliveredPI);
                }
            }

            PushService.writeLog("SUBMIT SMS SUCCESS: " + toNum + " SIM" + (simID+1), ctx);

            return true;
        } catch (ClassNotFoundException e) {
            PushService.writeLog("ClassNotFoundException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            PushService.writeLog("NoSuchMethodException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            PushService.writeLog("InvocationTargetException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            PushService.writeLog("IllegalAccessException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (Exception e) {
            PushService.writeLog("Exception:" + e.getMessage(), ctx);
            e.printStackTrace();
        }
        return false;
    }


    public static boolean sendMultipartTextSMS(Context ctx, int simID, String toNum, String centerNum, ArrayList<String> smsTextlist) {
        String name;
        try {
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                PushService.writeLog("can not get service which for sim '" + simID + "', only 0,1 accepted as values", ctx);
                return false;
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            ArrayList<PendingIntent> sentIntentList = new ArrayList<>();
            ArrayList<PendingIntent> deliveryIntentList = new ArrayList<>();
            String sms = "";
            for(int n=0;n<smsTextlist.size();n++) {
                int time = (int) System.currentTimeMillis()/1000;
                sms += smsTextlist.get(n);

                Intent is = new Intent(Fungsi.SENT);
                is.putExtra("number",toNum);
                is.putExtra("centerNum",centerNum);
                is.putExtra("simID",simID);
                is.putExtra("smsText",sms);
                is.putExtra("retry",0);
                sentIntentList.add(PendingIntent.getBroadcast(ctx, time+n,
                        is, 0));

                Intent id = new Intent(Fungsi.DELIVERED);
                is.putExtra("number",toNum);
                is.putExtra("centerNum",centerNum);
                is.putExtra("simID",simID);
                is.putExtra("smsText",sms);
                is.putExtra("retry",0);
                deliveryIntentList.add(PendingIntent.getBroadcast(ctx, time+n,
                        id, 0));
            }

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            try {
                if (stubObj != null) {
                    if (Build.VERSION.SDK_INT < 18) {
                        method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, List.class, List.class, List.class);
                        method.invoke(stubObj, toNum, centerNum, smsTextlist, sentIntentList, deliveryIntentList);
                    } else {
                        method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, String.class, List.class, List.class, List.class);
                        method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsTextlist, sentIntentList, deliveryIntentList);
                    }
                } else {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                    if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                        List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(simID);
                        for(int n=0;n<smsTextlist.size();n++) {
                            SmsManager
                                    .getSmsManagerForSubscriptionId(simInfo.getSubscriptionId())
                                    .sendTextMessage(toNum, null,
                                            smsTextlist.get(n),
                                            sentIntentList.get(n),
                                            deliveryIntentList.get(n));
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                    List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                    SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(simID);
                    for(int n=0;n<smsTextlist.size();n++) {
                        SmsManager
                                .getSmsManagerForSubscriptionId(simInfo.getSubscriptionId())
                                .sendTextMessage(toNum, null,
                                        smsTextlist.get(n),
                                        sentIntentList.get(n),
                                        deliveryIntentList.get(n));
                    }
                }
            }

            PushService.writeLog("SUBMIT SMS SUCCESS: " + toNum + " SIM" + (simID+1), ctx);
            return true;
        } catch (ClassNotFoundException e) {
            PushService.writeLog("ClassNotFoundException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            PushService.writeLog("NoSuchMethodException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            PushService.writeLog("InvocationTargetException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            PushService.writeLog("IllegalAccessException:" + e.getMessage(), ctx);
            e.printStackTrace();
        } catch (Exception e) {
            PushService.writeLog("Exception:" + e.getMessage(), ctx);
            e.printStackTrace();
        }
        return false;
    }


}