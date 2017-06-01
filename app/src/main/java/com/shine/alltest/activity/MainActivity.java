package com.shine.alltest.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shine.alltest.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermission();

    }

    private void setPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.INTERNET
                        , Manifest.permission.ACCESS_NETWORK_STATE
                        , Manifest.permission.ACCESS_WIFI_STATE
                        , Manifest.permission.READ_PHONE_STATE
                        , Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.WAKE_LOCK
                        , Manifest.permission.CAMERA
                        , Manifest.permission.MODIFY_AUDIO_SETTINGS
                )
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                });
    }

    public void upCLick(View view) {
        Intent intent = new Intent(this, JiekouActivity.class);
        startActivity(intent);
    }

    public void downClick(View view) {
        Intent intent = new Intent(this, SrialMainActivity.class);
        startActivity(intent);
    }
}
