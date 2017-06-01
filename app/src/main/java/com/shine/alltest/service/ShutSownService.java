package com.shine.alltest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shine.utilitylib.A64Utility;

/**
 * Created by 123 on 2017/5/19.
 */

public class ShutSownService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
    boolean Continue = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final long shutDownLong = intent.getLongExtra("ShutDownTime", 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (Continue) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("onStartCommand", shutDownLong + "sssssaaaAAZZ");
                    Log.d("onStartCommand", System.currentTimeMillis() + "sssssaaaAAZZ");
                    if (System.currentTimeMillis() > shutDownLong) {
                        Continue = false;
                        A64Utility a64Utility = new A64Utility();
                        a64Utility.Shutdown();
                    }
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

}
