package com.shine.alltest.manager;


import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPort;


/**
 * 读取串口使用
 *
 * @author 郭鹏
 * @version 1.0
 * @date 2015.5.12
 */
public class SerialMain {
    GetString getString;

    public SerialMain(String path, int baudrate, GetString getString) {
        this.path = path;
        this.baudrate = baudrate;
        this.getString = getString;

    }

    private String path;
    private int baudrate;
    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private String temp = "";
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {

			/* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public void startReadThread() {
//        try {

            scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 50, TimeUnit.MILLISECONDS);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                mSerialPort = getSerialPort();
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();
                int size;
                if (mInputStream == null) return;
                byte[] buffer = new byte[mInputStream.available()];
                size = mInputStream.read(buffer);
                if (size > 0) {
                    Log.d("stringBuffer", buffer + "");
                    String s = new String(buffer);
                    getString.getBack(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void onDestory() {
        scheduledExecutorService.shutdownNow();
        closeSerialPort();
        mSerialPort = null;
    }

    public interface GetString {
        void getBack(String s);
    }
}
