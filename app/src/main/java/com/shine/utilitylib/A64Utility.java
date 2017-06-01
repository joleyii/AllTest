package com.shine.utilitylib;

public class A64Utility {

    static {
        System.loadLibrary("A64Utility");
    }

    public native int OpenScreen();
    public native int CloseScreen();
    public native int Shutdown();

    public native int SetLameValue(int nPort, int value);
    // 十寸电话机port永远为1即可 value表示选择mic 1手柄 0板载
    public native int SelectMicDev(int macCode);
    public native int GetGpioInValue(int nIndex);
    //０是开　１是关
    // 十寸电话机port永远为1即可 value表示选择mic 1手柄 0板载
    public void selectMic(int value) {
        this.SetLameValue(1, value);
    }

}
