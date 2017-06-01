package com.shine.alltest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shine.alltest.R;
import com.shine.alltest.manager.SuClient;
import com.shine.alltest.service.Open4KeyService;
import com.shine.utilitylib.A64Utility;

/**
 * Created by 123 on 2017/5/25.
 */

public class Open4KeyActivity extends BaseAvtivity {
    A64Utility a64Utility;
    private SuClient mSuClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open4key);
        a64Utility = new A64Utility();
        Intent intent = new Intent(this, Open4KeyService.class);
        startService(intent);
    }

    public void closeClick(View view) {
        a64Utility.SetLameValue(1, 1);
        a64Utility.SetLameValue(2, 1);
        a64Utility.SetLameValue(3, 1);
        a64Utility.SetLameValue(4, 1);
    }

    public void prepareClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSuClient == null) {
                    mSuClient = new SuClient();
                    mSuClient.init(null);
                }

                mSuClient.execCMD("echo 198 >  /sys/class/gpio/export");
                mSuClient.execCMD("echo \"out\" > /sys/class/gpio/gpio198/direction");
                mSuClient.execCMD("echo 199 >  /sys/class/gpio/export");
                mSuClient.execCMD("echo \"out\" > /sys/class/gpio/gpio199/direction");
                mSuClient.execCMD("echo 200 >  /sys/class/gpio/export");
                mSuClient.execCMD("echo \"out\" > /sys/class/gpio/gpio200/direction");
                mSuClient.execCMD("echo 201 >  /sys/class/gpio/export");
                mSuClient.execCMD("echo \"out\" > /sys/class/gpio/gpio201/direction");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio198/direction");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio199/direction");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio200/direction");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio201/direction");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio198/value");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio199/value");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio200/value");
                mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio201/value");

            }
        }).start();
    }
}
