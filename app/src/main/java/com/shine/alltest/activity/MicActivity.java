package com.shine.alltest.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shine.alltest.R;
import com.shine.alltest.manager.SuClient;
import com.shine.alltest.service.RecordThread;
import com.shine.utilitylib.A64Utility;

public class MicActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecordThread mRecordThread;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int scanCode = intent.getIntExtra("scanCode", 0);

            //scanCode: 185挂机   186摘机
            Log.e(TAG, "Action: " + intent.getAction() + " scanCode: " + scanCode);
            if (scanCode == 139) {
                Toast.makeText(context, "点击 HOME 键", Toast.LENGTH_SHORT).show();
                return;
            }

            if (scanCode == 185) {
                mA64Utility.selectMic(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("tinymix 14 30");
                        mSuClient.execCMD("tinymix 112 0");
                    }
                }).start();
                return;
            }

            if (scanCode == 186) {
                mA64Utility.selectMic(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("tinymix 14 0");
                        mSuClient.execCMD("tinymix 112 1");

                    }
                }).start();
            }
        }
    };
    private A64Utility mA64Utility;
    private SuClient mSuClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);
        mA64Utility = new A64Utility();
        findViewById(R.id.close_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mA64Utility.CloseScreen();
            }
        });
        findViewById(R.id.get_handle_state).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int inValue = mA64Utility.GetGpioInValue(2);
                Toast.makeText(MicActivity.this, String.valueOf(inValue), Toast.LENGTH_SHORT).show();
            }
        });


        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.server.PhoneWindowManager.action.EXTKEYEVENT");
        registerReceiver(broadcastReceiver, filter);
    }
}
