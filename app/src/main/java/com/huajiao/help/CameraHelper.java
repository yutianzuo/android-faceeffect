package com.huajiao.help;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;

public class CameraHelper {

    public CameraHelper() {
    }

    public static int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public Camera openCamera(final int id) {
        if (id < 0 || id >= getNumberOfCameras()) {
            return null;
        }
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Throwable e) {
            e.printStackTrace();
            camera = null;
        }
        return camera;
    }

    public Camera openDefaultCamera() {
        Camera camera = null;
        try {
            camera = Camera.open(getCameraId(CameraInfo.CAMERA_FACING_BACK));
        } catch (Throwable e) {
            e.printStackTrace();
            camera = null;
        }
        return camera;
    }

    public Camera openFrontCamera() {
        Camera camera = null;
        try {
            camera = Camera.open(getCameraId(CameraInfo.CAMERA_FACING_FRONT));
        } catch (Throwable e) {
            e.printStackTrace();
            camera = null;
        }
        return camera;
    }

    public Camera openBackCamera() {
        Camera camera = null;
        try {
            camera = Camera.open(getCameraId(CameraInfo.CAMERA_FACING_BACK));
        } catch (Throwable e) {
            camera = null;
        }
        return camera;
    }

    public boolean hasFrontCamera() {
        return getCameraId(CameraInfo.CAMERA_FACING_FRONT) != -1;
    }

    public boolean hasBackCamera() {
        return getCameraId(CameraInfo.CAMERA_FACING_BACK) != -1;
    }

    public boolean isFrontCamera(final int id) {
        if (id < 0 || id >= getNumberOfCameras()) {
            return false;
        }
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(id, info);
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            return true;
        }
        return false;
    }

    public synchronized static void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        if (cameraId < 0 || cameraId >= getNumberOfCameras() || cameraInfo == null) {
            return;
        }
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        cameraInfo.facing = info.facing;
        cameraInfo.orientation = info.orientation;
    }

    public void setCameraDisplayOrientation(final Activity activity,
                                            final int cameraId, final Camera camera) {
        int result = getCameraDisplayOrientation(activity, cameraId);
        camera.setDisplayOrientation(result);
    }

    public synchronized static int getCameraDisplayOrientation(final Activity activity, final int cameraId) {
        if (cameraId < 0 || cameraId >= getNumberOfCameras() || activity == null) {
            return 0;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public int getVideoOrientation(final int cameraId) {
        if (cameraId < 0 || cameraId >= getNumberOfCameras()) {
            return 0;
        }
        int result;
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);
        result = info.orientation;
        return result;
    }

    public static class CameraInfo2 {
        public int facing;
        public int orientation;
    }

    public int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo info = new CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }

}
