package com.huajiao.jni;

public class LibYuv {
    static {
        System.loadLibrary("yuv");
        System.loadLibrary("yuvjni");
    }

    public static native int init();

    public static native int uninit();

    public static native int turnAndRotation(byte[] src, int srcW, int srcH, byte[] des, int desW, int desH, int rotation, int mirrorRotation, int type);
}