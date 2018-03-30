package com.huajiao.opengl;

import android.annotation.SuppressLint;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

@SuppressLint("NewApi")
public class SurfaceContextSwap {
	private static final String TAG = "SurfaceContextSwap";

	private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
	private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
	private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

	private EGLDisplay mOldDisplay;
	private EGLSurface mOldDrawSurface;
	private EGLSurface mOldReadSurface;
	private EGLContext mOldContext;

	private int mWidth;
	private int mHeigth;

	public SurfaceContextSwap(int width, int height) {
		mWidth = width;
		mHeigth = height;
	}

	public void release() {
		if (mEGLSurface != EGL14.EGL_NO_SURFACE)
			EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
		if (mEGLContext != EGL14.EGL_NO_CONTEXT)
			EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
		if (mEGLDisplay != EGL14.EGL_NO_DISPLAY)
			EGL14.eglTerminate(mEGLDisplay);
		
		mEGLDisplay = EGL14.EGL_NO_DISPLAY;
		mEGLSurface = EGL14.EGL_NO_SURFACE;
		mEGLContext = EGL14.EGL_NO_CONTEXT;
	}

	private void eglSetup() {
		mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
		if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
			throw new RuntimeException("unable to get EGL14 display");
		}
		// Configure EGL for recording and OpenGL ES 2.0.
		int[] attribList = { EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE,
				8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
				EGL14.EGL_NONE };
		EGLConfig[] configs = new EGLConfig[1];
		int[] numConfigs = new int[1];
	       int[] version = new int[2];
	        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
	            mEGLDisplay = null;
	            throw new RuntimeException("unable to initialize EGL14");
	        }
		EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
		checkEglError("eglCreateContext RGB888+recordable ES2");

		int[] attrib_list_c = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE }; // Create
																						// EGL
																						// context
																						// for
																						// the
																						// encoder
																						// input
																						// surface.
		// This context must share the textures with the context in
		// GLSurfaceView
		mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.eglGetCurrentContext(), attrib_list_c, 0);
		checkEglError("eglCreateContext");

		int[] attrib_list = { EGL14.EGL_WIDTH, mWidth, EGL14.EGL_HEIGHT, mHeigth, EGL14.EGL_NONE };
		mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], attrib_list, 0);

		checkEglError("eglCreateWindowSurface");
	}

	public boolean enterGLContext() {
		// Save the current context
		mOldDisplay = EGL14.eglGetCurrentDisplay();
		mOldDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
		mOldReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
		mOldContext = EGL14.eglGetCurrentContext();
		if (mEGLDisplay == EGL14.EGL_NO_DISPLAY || mEGLSurface == EGL14.EGL_NO_SURFACE
				|| mEGLContext == EGL14.EGL_NO_CONTEXT) {
			eglSetup();
		}
		// Make the context for input surface to be current
		EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
		// //Set OpenGL ES texture parameters
		// GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
		// GLES20.GL_TEXTURE_MIN_FILTER,
		// GLES20.GL_LINEAR_MIPMAP_LINEAR);
		// GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
		// GLES20.GL_TEXTURE_MAG_FILTER,
		// GLES20.GL_LINEAR);
		// GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
		// GLES20.GL_TEXTURE_WRAP_S,
		// GLES20.GL_CLAMP_TO_EDGE);
		// GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
		// GLES20.GL_TEXTURE_WRAP_T,
		// GLES20.GL_CLAMP_TO_EDGE);

		return true;
	}

	public void leaveGLContext() {
		// if (VERBOSE) Log.d(TAG, "render is swaping.");
		// //update the content of the input surface
		// EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
		// checkEglError("eglSwapBuffers");
		// restore the EGL context for GLSurfaceView
		EGL14.eglMakeCurrent(mOldDisplay, mOldDrawSurface, mOldReadSurface, mOldContext);
	}

	private void checkEglError(String msg) {
		int error;
		if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
			throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
		}
	}
}
