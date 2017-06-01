package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.shine.alltest.R;

/**
 * Created by 123 on 2017/6/1.
 */

public class SerialMagneticStripeCardActivity extends BaseAvtivity {

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_magnetic_stripe_card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("keyCode", keyCode + "");
        return super.onKeyDown(keyCode, event);
    }

}
