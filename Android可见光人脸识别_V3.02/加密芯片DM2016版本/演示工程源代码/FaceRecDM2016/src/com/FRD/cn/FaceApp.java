package com.FRD.cn;

import java.io.File;

import android.app.Application;
import android.util.Log;

/**
 * ���ڳ�ʼ������Ӧ����ʹ�õ��������㷨�������ļ���
 * @author �޷�
 * @datetime 2016-05-03
 */
public class FaceApp extends Application {
    private final static String TAG = "FaceApplication";
    public static String tempDir = null;
    public static String libDir = null;

    @Override
    public void onCreate() {
        super.onCreate();

        File file = this.getCacheDir();
        tempDir = file.getAbsolutePath();
        libDir = tempDir.replace("cache", "lib");
        log("tempDir:" + tempDir + " libDir:" + libDir);
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
