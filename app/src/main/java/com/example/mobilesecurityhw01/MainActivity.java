package com.example.mobilesecurityhw01;

import static android.app.PendingIntent.getActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText edt_user_name;
    private EditText edt_password;
    private Button btn_login;
    private int batteryLevel;
    private Boolean permissionContacts;
    private Boolean isWifiEnabled;
    private static final String PASSWORD = "HomeWork1";
    private static final String USER_NAME = "GuyForSure";
    private ActivityResultLauncher<String[]> permissionResultLauncher;
    private BroadcastReceiver batteryInfoReceiver;
    private BroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
        initPermissionResultLauncher();
        registerBatteryReceiver();
        registerWifiReceiver();
    }



    private boolean checkPhoneContacts() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,null,null,null,null);
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                int position = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                if(position < 0)
                    break;
                String contactName = cursor.getString(position);
                if(contactName.contains("Molly"))
                    return true;
            }
        }
        return false;
    }


    private void login() {

        boolean isPermitted =checkAllPermissions();
        if(!isPermitted){
            Toast.makeText(MainActivity.this, "Permissions were not given.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isMollyAppear = checkPhoneContacts();
        if(!isMollyAppear){
            Toast.makeText(MainActivity.this, "Molly doesn't appear in contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isWifiEnabled){
            Toast.makeText(MainActivity.this, "Wifi is disabled.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(batteryLevel < 50){
            Toast.makeText(MainActivity.this, "Battery level is lower than 50.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!edt_user_name.getText().toString().equals(USER_NAME)) {
            Toast.makeText(MainActivity.this, "User name is wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!edt_password.getText().toString().equals(PASSWORD)) {
            Toast.makeText(MainActivity.this, "Password is wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
    }

    private void initViews() {
        btn_login.setOnClickListener(view -> login());
    }


    private void findViews() {
        edt_password = findViewById(R.id.edt_password);
        edt_user_name = findViewById(R.id.edt_user_name);
        btn_login = findViewById(R.id.btn_login);
    }

    private void requestPermissions() {
        List<String> permissionRequests = new ArrayList<>();

        permissionContacts = ContextCompat.checkSelfPermission(this , Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        isWifiEnabled = ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;

        if(!permissionContacts){
            permissionRequests.add(Manifest.permission.READ_CONTACTS);
        }

        if(!isWifiEnabled){
            permissionRequests.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        if(!permissionRequests.isEmpty()){
            permissionResultLauncher.launch(permissionRequests.toArray(new String[0]));
        }
    }

    private void initPermissionResultLauncher() {
        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if(result.get(Manifest.permission.READ_CONTACTS) != null){
                permissionContacts = result.get(Manifest.permission.READ_CONTACTS);
            }

            if(result.get(Manifest.permission.ACCESS_WIFI_STATE) != null){
                isWifiEnabled = result.get(Manifest.permission.ACCESS_WIFI_STATE);
            }
        });

        requestPermissions();
    }

    private boolean checkAllPermissions(){
        if(permissionContacts)
            return true;
        return false;
    }

    private void registerWifiReceiver(){
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    isWifiEnabled = wifiManager.isWifiEnabled();
            }
        };
        registerReceiver(this.wifiReceiver, new IntentFilter((WifiManager.NETWORK_STATE_CHANGED_ACTION)));
    }

    private void registerBatteryReceiver(){
        batteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                batteryLevel = intent.getIntExtra((BatteryManager.EXTRA_LEVEL), 0);
            }
        };
        registerReceiver(this.batteryInfoReceiver, new IntentFilter((Intent.ACTION_BATTERY_CHANGED)));
    }

    public void onDestroy() {
        // Unregister broadcast listeners
        unregisterReceiver(batteryInfoReceiver);
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
    }


}
