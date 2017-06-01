package com.shine.alltest.manager;

import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;


public class SystemManager {
    //给USB读写权限
    public void chmodUSB() {
        RootCommand("chmod -R 777 /dev/bus/usb");
        Log.i("info", "加USB权限");
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @param command 命令：String apkRoot="chmod 777 "+getPackageCodePath(); RootCommand(apkRoot);
     */
    public void RootCommand(final String command) {
        Log.i("SystemManager", "command:" + command);
        new Thread() {
            @Override
            public void run() {
                // 918上处理与6369,801上不同
                if (Build.VERSION.SDK_INT >= 18) {
                    SuClient client = new SuClient();
                    client.init(null);
                    client.execCMD(command);
                    client.close();
                    return;
                }

                Process process = null;
                DataOutputStream os = null;
                try {
                    process = Runtime.getRuntime().exec("shinesu");
                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes(command + "\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    process.waitFor();
                } catch (Exception e) {
                    Log.i("info", "ROOT REE" + e.getMessage());
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        assert process != null;
                        process.destroy();
                    } catch (Exception ignored) {
                    }
                }
            }
        }.start();
    }

    public void mkDir(final String path) {
        RootCommand("mkdir " + path);
        RootCommand("chmod -R 777 " + path);
    }

    public void createNewFile(final String path) {
        RootCommand("touch " + path);
        RootCommand("chmod -R 777 " + path);
    }
}