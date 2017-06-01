package com.shine.timingboot;

public class TimingBootUtils {
    static {
        System.loadLibrary("jni_rtc");
    }
    public native int setRtcTime(String str);
}