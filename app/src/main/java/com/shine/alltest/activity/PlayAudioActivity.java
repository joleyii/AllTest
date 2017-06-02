package com.shine.alltest.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.shine.alltest.R;
import com.shine.alltest.service.AudioService;

/**
 * Created by 123 on 2017/6/2.
 */

public class PlayAudioActivity extends BaseAvtivity {
    private AudioService audioService;

    //使用ServiceConnection来监听Service状态的变化
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            audioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //这里我们实例化audioService,通过binder来实现
            audioService = ((AudioService.AudioBinder) binder).getService();

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playaudio);
    }

    public void onClick(View v) {
        int id = v.getId();
        Intent intent = new Intent();
        intent.setClass(this, AudioService.class);
        if (id == R.id.btn_start) {
            //启动Service，然后绑定该Service，这样我们可以在同时销毁该Activity，看看歌曲是否还在播放
            startService(intent);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } else if (id == R.id.btn_end) {
            //结束Service
            unbindService(conn);
            stopService(intent);
        }
    }
}
