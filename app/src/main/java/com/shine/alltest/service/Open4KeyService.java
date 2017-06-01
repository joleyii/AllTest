package com.shine.alltest.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.shine.alltest.broadcastreceiver.Open4KeyBroadcastReceiver;

public class Open4KeyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    Open4KeyBroadcastReceiver open4KeyBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        open4KeyBroadcastReceiver = new Open4KeyBroadcastReceiver();
        IntentFilter homeFilter = new IntentFilter("com.android.server.PhoneWindowManager.action.EXTKEYEVENT");
        registerReceiver(open4KeyBroadcastReceiver, homeFilter);
    }
}