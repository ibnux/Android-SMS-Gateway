package com.ibnux.smsgateway;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

public class Aplikasi extends Application {

    public static Application app;
    public static String secret;
    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = this;

        sp = getSharedPreferences("pref",0);
        secret = sp.getString("secret",null);
        if(secret==null){
            secret = UUID.randomUUID().toString();
            sp.edit().putString("secret", secret).apply();
        }
    }

//    public void registerTopic(){
//        FirebaseMessaging.getInstance().subscribeToTopic(channel)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            sp.edit().putBoolean("subscribe",true).apply();
//                        }else{
//                            //ulang jika gagal
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    registerTopic();
//                                }
//                            },5000);
//                        }
//                    }
//                });
//    }
}
