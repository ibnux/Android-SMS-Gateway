package com.ibnux.smsgateway;

/**
 * Created by Ibnu Maksum 2020
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ibnux.smsgateway.layanan.BackgroundService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private boolean serviceActive = false;
    TextView txtLogs,info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtLogs = findViewById(R.id.txtLogs);
        txtLogs.setText(Fungsi.readFile(this));

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.WAKE_LOCK
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
        updateInfo();
        checkServices();
    }

    public void updateInfo(){
        SharedPreferences sp = getSharedPreferences("pref",0);
        info = findViewById(R.id.text);
        info.setText("Your Secret \n\n"+sp.getString("secret",null)+
                "\n\nYour Device ID \n\n"+
                sp.getString("token",null)+
                "\n\nhttps://sms.ibnux.net\n");
    }

    public void checkServices(){
        Fungsi.log("checkServices");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("BackgroundService"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Fungsi.log("checkServices "+serviceActive);
                if(!serviceActive){
                    startService(new Intent(MainActivity.this, BackgroundService.class));
                }
            }
        },3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_change_expired:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Change expired, by seconds");
                builder.setMessage("If you use Md5 for secret with time, if time expired, it will not send SMS");
                final EditText input = new EditText(this);
                input.setText(getSharedPreferences("pref",0).getInt("expired",3600)+"");
                input.setMaxLines(1);
                input.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_TEXT_VARIATION_PHONETIC);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ex = input.getText().toString();
                        try{
                            int exi = Integer.parseInt(ex);
                            if(exi<5){
                                exi = 5;
                            }
                            getSharedPreferences("pref",0).edit().putInt("expired", exi).commit();
                            Toast.makeText(MainActivity.this,"Expired changed",Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            //not numeric
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            case R.id.menu_change_secret:
                new AlertDialog.Builder(this)
                        .setTitle("Change Secret")
                        .setMessage("This will denied previous secret, every sms with previous secret ")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getSharedPreferences("pref",0).edit().putString("secret", UUID.randomUUID().toString()).commit();
                                updateInfo();
                                Toast.makeText(MainActivity.this,"Secret changed",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            case R.id.menu_clear_logs:
                new AlertDialog.Builder(this)
                        .setTitle("Clear All Logs")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Fungsi.clearLogs(MainActivity.this);
                                txtLogs.setText(Fungsi.readFile(MainActivity.this));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter("MainActivity"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Fungsi.log("BroadcastReceiver received");
            if(intent.hasExtra("newMessage"))
                txtLogs.setText(Fungsi.readFile(MainActivity.this));
            else if(intent.hasExtra("newToken"))
                updateInfo();
            else
                serviceActive = true;

        }
    };
}
