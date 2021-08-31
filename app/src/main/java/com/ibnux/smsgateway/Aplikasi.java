package com.ibnux.smsgateway;

/**
 * Created by Ibnu Maksum 2020
 */

import android.app.Application;
import android.content.SharedPreferences;

import java.util.UUID;

public class Aplikasi extends Application {

    public static Application app;
    public static String secret;
    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = this;
        ObjectBox.init(this);
        sp = getSharedPreferences("pref",0);
        secret = sp.getString("secret",null);
        if(secret==null){
            secret = UUID.randomUUID().toString();
            sp.edit().putString("secret", secret).apply();
        }
    }

}
