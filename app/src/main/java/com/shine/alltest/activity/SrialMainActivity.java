package com.shine.alltest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shine.alltest.R;

/**
 * Created by 123 on 2017/6/1.
 */

public class SrialMainActivity extends BaseAvtivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_main);
    }
    public void clickT(View view) {
        switch (view.getId()) {
            case R.id.tv_1:
                Intent intent = new Intent(this, SerialScanHeadActivity.class);
                startActivity(intent);
                break;
        }
    }
}
