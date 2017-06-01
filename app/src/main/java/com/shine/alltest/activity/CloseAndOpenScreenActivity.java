package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shine.alltest.R;
import com.shine.utilitylib.A64Utility;

/**
 * Created by 123 on 2017/5/19.
 */

public class CloseAndOpenScreenActivity extends BaseAvtivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_and_open_screen);
        findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void closeClick(View view) {
        final A64Utility a64Utility = new A64Utility();
        a64Utility.CloseScreen();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a64Utility.OpenScreen();
            }
        }).start();
    }
}
