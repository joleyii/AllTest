package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.shine.alltest.R;
import com.shine.alltest.manager.SerialMain;

/**
 * Created by 123 on 2017/6/1.
 */

public class SerialScanHeadActivity extends BaseAvtivity {
    private SerialMain serialMain;
    private TextView tv_serial;
    private StringBuffer stringBuffer;
    private TextView tv_current_serial;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_scan_head);
        tv_serial = (TextView) findViewById(R.id.tv_serial);
        tv_current_serial = (TextView) findViewById(R.id.tv_current_serial);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialMain.onDestory();
    }

    public void serialClick(View view) {
        switch (view.getId()) {
            case R.id.tv_3:
                //ttyS3 4
                serialMain = new SerialMain("/dev/ttyS1", 9600, new SerialMain.GetString() {
                    @Override
                    public void getBack(String s) {
                        stringBuffer = stringBuffer.append(s, 0, s.length());
                        tv_serial.setText(stringBuffer);
                    }
                });
                serialMain.startReadThread();
                break;
            case R.id.tv_4:
                //ttyS3 4
                serialMain = new SerialMain("/dev/ttyS1", 9600, new SerialMain.GetString() {
                    @Override
                    public void getBack(String s) {
                        stringBuffer = stringBuffer.append(s, 0, s.length());
                        tv_serial.setText(stringBuffer);
                    }
                });
                serialMain.startReadThread();
                break;
        }
    }
}
