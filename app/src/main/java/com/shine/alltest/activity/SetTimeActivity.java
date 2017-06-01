package com.shine.alltest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.shine.alltest.R;
import com.shine.alltest.manager.SystemManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by 123 on 2017/5/19.
 */

public class SetTimeActivity extends BaseAvtivity {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMddHHmmyy.ss", Locale.CHINA);
    DatePicker datePicker;
    TimePicker timePicker;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_settime);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        ;
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);

        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker arg0, int year, int month,
                                      int day) {
                SetTimeActivity.this.year = year;
                SetTimeActivity.this.month = month;
                SetTimeActivity.this.day = day;
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker arg0, int hour, int minute) {
                SetTimeActivity.this.hour = hour;
                SetTimeActivity.this.minute = minute;
            }
        });
    }

    public void okClick(View view) {
        String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
        Log.d("timetime", time);
        try {
            if (Math.abs(System.currentTimeMillis() - simpleDateFormat.parse(time).getTime()) > 2000) {
                time = mSimpleDateFormat.format(simpleDateFormat.parse(time));
                SystemManager systemManager = new SystemManager();
                systemManager.RootCommand("date " + time);
                systemManager.RootCommand("busybox hwclock -f /dev/rtc0 -w");
                systemManager.RootCommand("busybox hwclock -f /dev/rtc1 -w");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
