package com.huajiao.jni;

public class FacePoints {
	static
	{
		System.loadLibrary("face_jni");
	}
	public static native float[] TransformPonits(float mid_x, float mid_y,
			float w, float h, float x, float y, float angle, float scaleRatio,
			float w_ratio);
}
