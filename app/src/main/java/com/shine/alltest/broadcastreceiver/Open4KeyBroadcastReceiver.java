package com.shine.alltest.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shine.utilitylib.A64Utility;

/**
 * Created by 123 on 2017/5/25.
 */

public class Open4KeyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getIntExtra("scanCode", 0) == 190
                || intent.getIntExtra("scanCode", 0) == 191
                || intent.getIntExtra("scanCode", 0) == 192
                || intent.getIntExtra("scanCode", 0) == 193
                ) {
            Log.d("eewre", "qweqweqeq");
            A64Utility a64Utility = new A64Utility();
            a64Utility.SetLameValue(1, 0);
            a64Utility.SetLameValue(2, 0);
            a64Utility.SetLameValue(3, 0);
            a64Utility.SetLameValue(4, 0);
        }
    }
}
