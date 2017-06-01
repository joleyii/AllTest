package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shine.alltest.R;

/**
 * Created by 123 on 2017/6/1.
 */

public class TingTongChuangtouActivity extends BaseAvtivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tingtong_chuangtouping);
    }

    public void jumpClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tingtong:

                break;
            case R.id.tv_chuangtoupign:

                break;
        }
    }
}
