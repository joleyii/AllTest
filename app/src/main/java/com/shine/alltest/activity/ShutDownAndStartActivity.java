package com.shine.alltest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.shine.alltest.R;
import com.shine.alltest.service.ShutSownService;
import com.shine.timingboot.TimingBootUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by 123 on 2017/5/19.
 */

public class ShutDownAndStartActivity extends BaseAvtivity {
    DatePicker dp_restart;
    TimePicker tp_restart;
    private int year_re;
    private int month_re;
    private int day_re;
    private int hour_re;
    private int minute_re;

    DatePicker dp_shutdown;
    TimePicker tp_shutdown;
    private int year_sh;
    private int month_sh;
    private int day_sh;
    private int hour_sh;
    private int minute_sh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_shutdown_and_restart);
        dp_restart = (DatePicker) findViewById(R.id.dp_restart);
        tp_restart = (TimePicker) findViewById(R.id.tp_restart);
        tp_restart.setIs24HourView(true);
        ;
        Calendar c_re = Calendar.getInstance();
        year_re = c_re.get(Calendar.YEAR);
        month_re = c_re.get(Calendar.MONTH);
        day_re = c_re.get(Calendar.DAY_OF_MONTH);
        hour_re = c_re.get(Calendar.HOUR);
        minute_re = c_re.get(Calendar.MINUTE);

        dp_restart.init(year_re, month_re, day_re, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker arg0, int year, int month,
                                      int day) {
                ShutDownAndStartActivity.this.year_re = year;
                ShutDownAndStartActivity.this.month_re = month;
                ShutDownAndStartActivity.this.day_re = day;
                // 显示当前日期、时间
            }
        });

        tp_restart.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker arg0, int hour, int minute) {
                ShutDownAndStartActivity.this.hour_re = hour;
                ShutDownAndStartActivity.this.minute_re = minute;
            }
        });

        dp_shutdown = (DatePicker) findViewById(R.id.dp_shutdown);
        tp_shutdown = (TimePicker) findViewById(R.id.tp_shutdown);
        tp_shutdown.setIs24HourView(true);
        Calendar c_sh = Calendar.getInstance();
        year_sh = c_sh.get(Calendar.YEAR);
        month_sh = c_sh.get(Calendar.MONTH);
        day_sh = c_sh.get(Calendar.DAY_OF_MONTH);
        hour_sh = c_sh.get(Calendar.HOUR);
        minute_sh = c_sh.get(Calendar.MINUTE);
        dp_shutdown.init(year_sh, month_sh, day_sh, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker arg0, int year, int month,
                                      int day) {
                ShutDownAndStartActivity.this.year_sh = year;
                ShutDownAndStartActivity.this.month_sh = month;
                ShutDownAndStartActivity.this.day_sh = day;
                // 显示当前日期、时间
            }
        });

        tp_shutdown.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker arg0, int hour, int minute) {
                ShutDownAndStartActivity.this.hour_sh = hour;
                ShutDownAndStartActivity.this.minute_sh = minute;
            }
        });
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    public void okClick(View view) {
        int realMonth_sh = month_sh + 1;
        String shutDownTime = year_sh + "-" + realMonth_sh + "-" + day_sh + " " + hour_sh + ":" + minute_sh + ":" + "00";
        Log.d("shutDownTime", shutDownTime);
        long shutDownLong = 0;
        try {
            shutDownLong = simpleDateFormat.parse(shutDownTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(this, ShutSownService.class);
        i.putExtra("ShutDownTime", shutDownLong);
        startService(i);
        int realMonth_re = month_re + 1;
        String restartTime = year_re + "-" + realMonth_re + "-" + day_re + " " + hour_re + ":" + minute_re + ":" + "00";
        Log.d("restartTime", restartTime);
        new TimingBootUtils().setRtcTime(restartTime);
    }
}
