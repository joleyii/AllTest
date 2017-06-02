package com.shine.alltest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.shine.alltest.R;

/**
 * Created by 123 on 2017/5/18.
 */

public class JiekouActivity extends BaseAvtivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiekou);
    }

    public void clickT(View view) {
        switch (view.getId()) {
            case R.id.tv_1:
                Intent intent = new Intent(this, SetTimeActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_2:
                Intent intent2 = new Intent(this, CloseAndOpenScreenActivity.class);
                startActivity(intent2);
                break;
            case R.id.tv_3:
                Intent intent4 = new Intent(this, TingTongChuangtouActivity.class);
                startActivity(intent4);
                break;
            case R.id.tv_5:
                Intent intent5 = new Intent(this, Open4KeyActivity.class);
                startActivity(intent5);
                break;
            case R.id.tv_6:
                Intent intent6 = new Intent(this, ShutDownAndStartActivity.class);
                startActivity(intent6);
                break;
            case R.id.tv_7:
                Intent intent7 = new Intent(this, CameraActivity.class);
                startActivity(intent7);
                break;
            case R.id.tv_8:
                Intent intent8 = new Intent(this, PlayAudioActivity.class);
                startActivity(intent8);
                break;
        }
    }
}
