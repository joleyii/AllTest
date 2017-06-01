package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.shine.alltest.R;
import com.shine.alltest.manager.SerialMain;

/**
 * Created by 123 on 2017/6/1.
 */

public class SerialScanHeadActivity extends BaseAvtivity {
    private SerialMain serialMain;
    private TextView tv_scan_out;
    private StringBuffer stringBuffer;
    private TextView tv_current_serial;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_scan_head);
        stringBuffer = new StringBuffer();
        tv_scan_out = (TextView) findViewById(R.id.tv_scan_out);
        tv_current_serial = (TextView) findViewById(R.id.tv_current_serial);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialMain != null) {
            serialMain.onDestory();
        }
    }

    public void serialClick(View view) {
        switch (view.getId()) {
            case R.id.tv_3:
                if (serialMain != null) {
                    serialMain.onDestory();
                }
                tv_current_serial.setText("当前选择串口3");
                serialMain = new SerialMain("/dev/ttyS3", 9600, new SerialMain.GetString() {
                    @Override
                    public void getBack(String s) {
                        stringBuffer = stringBuffer.insert(0, s + "\n");
                        Log.d("getString", "stringBuffer:" + stringBuffer);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_scan_out.setText(stringBuffer.toString());
                            }
                        });
                    }
                });
                serialMain.startReadThread();
                break;
            case R.id.tv_4:
                if (serialMain != null) {
                    serialMain.onDestory();
                }
                tv_current_serial.setText("当前选择串口4");
                serialMain = new SerialMain("/dev/ttyS4", 9600, new SerialMain.GetString() {
                    @Override
                    public void getBack(String s) {
                        stringBuffer = stringBuffer.insert(0, s + "\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_scan_out.setText(stringBuffer.toString());
                            }
                        });
                    }
                });
                serialMain.startReadThread();
                break;
        }
    }
}
