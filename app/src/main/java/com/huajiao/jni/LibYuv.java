package com.huajiao.jni;

/**
 *yutianzuo, wrapper of google libyuv
 */

public class LibYuv {
    static {
        System.loadLibrary("yuv");
        System.loadLibrary("yuvjni");
    }

    public static native int init();

    public static native int uninit();

    /**
     * rotate and scale src yuv data
     * @param src
     * @param srcW
     * @param srcH
     * @param des
     * @param desW
     * @param desH
     * @param rotation
     * @param mirrorRotation
     * @param type
     * @return
     */
    public static native int turnAndRotation(byte[] src, int srcW, int srcH, byte[] des, int desW, int desH, int rotation, int mirrorRotation, int type);
}