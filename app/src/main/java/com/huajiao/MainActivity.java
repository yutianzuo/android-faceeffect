package com.huajiao;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.huajiao.help.CameraHelper;
import com.huajiao.help.WeakHandler;
import com.huajiao.help.third.FaceTrackerManager;
import com.huajiao.jni.LibYuv;
import com.huajiao.opengl.Drawable2d;
import com.huajiao.opengl.FullFrameRect;
import com.huajiao.opengl.Sprite2d;
import com.huajiao.opengl.Texture2dProgram;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback, WeakHandler.IHandler {
    public static final boolean bAutoFocus;

    static {
        if (((Build.MODEL.contains("GT-I9505")) || (Build.MODEL.contains("GT-I9506")) ||
                (Build.MODEL.contains("GT-I9500")) || (Build.MODEL.contains("SGH-I337")) ||
                (Build.MODEL.contains("SGH-M919")) || (Build.MODEL.contains("SCH-I545")) ||
                (Build.MODEL.contains("SPH-L720")) || (Build.MODEL.contains("GT-I9508")) ||
                (Build.MODEL.contains("SHV-E300")) || (Build.MODEL.contains("SCH-R970")) ||
                (Build.MODEL.contains("SM-N900")) || (Build.MODEL.contains("LG-D801"))) &&
                (!Build.MODEL.contains("SM-N9008"))) {
            bAutoFocus = true;
        } else {
            bAutoFocus = false;
        }
    }

    private static final int GLVIEW_WIDTH = 1080; //控件大小
    private static final int GLVIEW_HEIGHT = 1920;

    public static final int MSG_SET_SURFACE_TEXTURE = 0;

    private DisplayMetrics mDm;
    private GLSurfaceView mGLView;
    private boolean mFirstAvailable = false;
    private CameraSufaceRender mRender = new CameraSufaceRender();
    private SurfaceTexture mCurrentSurfaceTexture;
    private WeakHandler mHandler = new WeakHandler(this);

    private Camera mCamera = null;
    private CameraHelper mCameraHelper = new CameraHelper();
    private int mCameraId = -1;
    private int mRotationDegree = 0;
    private List<Camera.Size> mSizesForCamera = null;
    private boolean mSupportFrontFalsh;


    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MSG_SET_SURFACE_TEXTURE) {
            if (mCamera == null) {
                return;
            }
            mCurrentSurfaceTexture = (SurfaceTexture) msg.obj;
            mCurrentSurfaceTexture.setOnFrameAvailableListener(this);
            if (mCamera == null) {
                return;
            }
            startCameraPreview();
        }
    }

    private void InitCamera() {
        if (mCamera == null) {
            if (mCameraId != -1) {
                mCamera = mCameraHelper.openCamera(mCameraId);
            } else {
                mCamera = mCameraHelper.openFrontCamera();
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            if (mCamera == null) {
                return;
            }

            try {
                mRotationDegree = mCameraHelper.getCameraDisplayOrientation(
                        this, mCameraId);
                mCamera.setDisplayOrientation(mRotationDegree);
            } catch (Throwable e) {
                UninitCamera();
            }
            mCamera.setPreviewCallback(this);
        }

        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            try {
                List<String> list_ret = mCamera.getParameters().getSupportedFlashModes();
                for (int i = 0; i < list_ret.size(); ++i) {
                    if (list_ret.get(i).equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO) ||
                            list_ret.get(i).equalsIgnoreCase(Camera.Parameters.FLASH_MODE_TORCH)) {
                        mSupportFrontFalsh = true;
                        break;
                    }
                }
            } catch (Throwable e) {
                mSupportFrontFalsh = false;
            }
        }

        Camera.Parameters parameters = mCamera.getParameters();// 获得相机参数
        if (mSizesForCamera == null || mSizesForCamera.size() == 0) {
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            if (sizes == null) {
                return;
            }
            mSizesForCamera = new ArrayList<>();
            for (Camera.Size size : sizes) {
                mSizesForCamera.add(size);
            }
            if (mSizesForCamera.size() == 0) { // 摄像头不支持
                return;
            }
        }

        Camera.Size cameraSize = initCameraSizeFitPreviewSize(GLVIEW_WIDTH, GLVIEW_HEIGHT,
                0.5625f/*16：9*/);
        if (cameraSize == null) {
            return;
        }
        setCameraFocusMode(mCamera, parameters);
        parameters.setPreviewSize(cameraSize.width, cameraSize.height);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);


        //根据摄像头比利缩放控件，防止变形
        setPreviewSize(cameraSize);
    }

    private void UninitCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Throwable e) {
            }
            try {
                mCamera.release();
            } catch (Throwable e) {
            }
            mCamera = null;
        }
    }

    private void startCameraPreview() {
        Log.e("ytz", "startCameraPreview");
        try {
            mCamera.setPreviewTexture(mCurrentSurfaceTexture);
            mCamera.startPreview();
        } catch (Throwable e) {
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean setCameraFocusMode(Camera paramCamera, Camera.Parameters paramParameters) {
        List localList = paramParameters.getSupportedFocusModes();
        if (localList == null) {
            return false;
        }
        String focusMode = null;
        if (bAutoFocus && localList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
        } else if (localList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        } else if (localList.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            focusMode = Camera.Parameters.FOCUS_MODE_INFINITY;
        }
        try {
            if (!TextUtils.isEmpty(focusMode)) {
                paramParameters.setFocusMode(focusMode);
                paramCamera.setParameters(paramParameters);
                return true;
            }
        } catch (Exception localException) {
            paramParameters.setFocusMode(paramParameters.getFocusMode());
        }
        return false;
    }

    private Camera.Size initCameraSizeFitPreviewSize(int textureviewWidth, int textureHeight, float ratio) {
        Camera.Size sizeRet = null;
        if (mSizesForCamera == null || mSizesForCamera.size() == 0) {
            return sizeRet;
        }

        for (int i = 0; i < mSizesForCamera.size(); i++) {
            Camera.Size size = mSizesForCamera.get(i);
            if (Math.min(size.width, size.height) == 720 &&
                    Float.compare((float) Math.min(size.width, size.height) / Math.max(size.width, size.height), ratio) == 0
                    ) { //first round find 720p

                sizeRet = size;
                return sizeRet;
            }
        }

        for (int i = 0; i < mSizesForCamera.size(); i++) { //second roud find 16:9 && size > preview size
            Camera.Size size = mSizesForCamera.get(i);
            if (Math.min(size.width, size.height) >= textureviewWidth &&
                    Math.max(size.width, size.height) >= textureHeight &&
                    Float.compare((float) Math.min(size.width, size.height) / Math.max(size.width, size.height), ratio) == 0) {
                sizeRet = size;
                return sizeRet;
            }
        }

        return sizeRet;
    }

    private void setPreviewSize(Camera.Size cameraSize) {
        int textureWidth = GLVIEW_WIDTH;
        int textureHeight = GLVIEW_HEIGHT;
        float ratioView = (float) textureWidth / textureHeight;
        float ratioCamera = (float) Math.min(cameraSize.width, cameraSize.height) / Math.max(cameraSize.width, cameraSize.height);

        int textureWidthfinal = textureWidth;
        int textureHeightfinal = textureHeight;

        if (Float.compare(ratioView, ratioCamera) <= 0) { //控件的比例小于选定的相机比例，即需要横向拉伸控件
            textureWidthfinal = (int) (textureHeightfinal * ratioCamera);
        } else {
            textureHeightfinal = (int) (textureWidthfinal / ratioCamera);
        }
        RelativeLayout.LayoutParams rlPreview = new RelativeLayout.LayoutParams(
                textureWidthfinal,
                textureHeightfinal);
        mGLView.setLayoutParams(rlPreview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLView = (GLSurfaceView) findViewById(R.id.camera_glview);
        mGLView.setEGLContextClientVersion(2); //select opengles 2.0
        mGLView.setRenderer(mRender);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        ImageButton btnSwitch = (ImageButton) findViewById(R.id.switch_btn);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UninitCamera();
                    if (mCameraId == mCameraHelper
                            .getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)) {
                        mCameraId = mCameraHelper
                                .getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    } else {
                        mCameraId = mCameraHelper
                                .getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                    }
                    mSizesForCamera = null;
                    InitCamera();
                    startCameraPreview();

                } catch (Throwable e) {

                }
            }
        });

        LibYuv.init();
        FaceTrackerManager.copyAndUnzipModelFiles(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ytz", "onResume1");
        InitCamera();
        Log.e("ytz", "onResume2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        UninitCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibYuv.uninit();
        FaceTrackerManager.unInitFaceSDK();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // The SurfaceTexture uses this to signal the availability of a new frame.  The
        // thread that "owns" the external texture associated with the SurfaceTexture (which,
        // by virtue of the context being shared, *should* be either one) needs to call
        // updateTexImage() to latch the buffer.
        //
        // Once the buffer is latched, the GLSurfaceView thread can signal the encoder thread.
        // This feels backward -- we want recording to be prioritized over rendering -- but
        // since recording is only enabled some of the time it's easier to do it this way.
        //
        // Since GLSurfaceView doesn't establish a Looper, this will *probably* execute on
        // the main UI thread.  Fortunately, requestRender() can be called from any thread,
        // so it doesn't really matter.
        //Log.e("ytz", "onFrameAvailable");
        mGLView.requestRender();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Renderer object for our GLSurfaceView.
     * Do not call any methods here directly from another thread -- use the
     * GLSurfaceView#queueEvent() call.
     */
    class CameraSufaceRender implements GLSurfaceView.Renderer {
        private FullFrameRect mFullScreen;
        private Drawable2d mRectDrawable;
        private Sprite2d mRect;
        private Texture2dProgram mTextureProgram;
        private int mTextureId;
        private boolean mNeedUpdateEgl = false;
        private SurfaceTexture mSurfaceTexture;
        private final float[] mSTMatrix = new float[16];


        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.e("ytz", "onSurfaceCreated");
            mNeedUpdateEgl = true;
            // We're starting up or coming back.  Either way we've got a new EGLContext that will
            // need to be shared with the video encoder, so figure out if a recording is already
            // in progress.

            // Set up the texture blitter that will be used for on-screen display.  This
            // is *not* applied to the recording, because that uses a separate shader.
            mFullScreen = new FullFrameRect(
                    new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

            if (mRectDrawable == null) {
                mRectDrawable = new Drawable2d(Drawable2d.Prefab.RECTANGLE);
            }
            if (mRect == null) {
                mRect = new Sprite2d(mRectDrawable);
            }

            if (mTextureProgram == null) {
                mTextureProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);
            }

            mTextureId = mFullScreen.createTextureObject();

            // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
            // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
            // available messages will arrive on the main thread.
            mSurfaceTexture = new SurfaceTexture(mTextureId);

            // Tell the UI thread to enable the camera preview.
            mHandler.sendMessage(mHandler.obtainMessage(
                    MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.e("ytz", "onSurfaceChanged");
            mHandler.sendMessage(mHandler.obtainMessage(
                    MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
        }

        @Override
        public void onDrawFrame(GL10 gl) {
//            Log.e("ytz", "onDrawFrame");
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mFullScreen.drawFrame(mTextureId, mSTMatrix, false);
        }
    }
}





