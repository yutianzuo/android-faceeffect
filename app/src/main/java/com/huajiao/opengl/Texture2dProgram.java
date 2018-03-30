/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huajiao.opengl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * GL program and supporting functions for textured 2D shapes.
 */
public class Texture2dProgram {
    private static final String TAG = GlUtil.TAG;

    public enum ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BEAUTY, TEXTURE_EXT_SHAPEN,TEXTURE_EXT_BRIGHTNESS,
    }

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    private static final String VERTEX_BEAUTY_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec2 blurCoord0;\n" +
                    "varying vec2 blurCoord1;\n" +
                    "varying vec2 blurCoord2;\n" +
                    "varying vec2 blurCoord3;\n" +
                    "varying vec2 blurCoord4;\n" +
                    "varying vec2 blurCoord5;\n" +
                    "varying vec2 blurCoord6;\n" +
                    "varying vec2 blurCoord7;\n" +
                    "varying vec2 blurCoord8;\n" +
                    "varying vec2 blurCoord9;\n" +
                    "varying vec2 blurCoord10;\n" +
                    "varying vec2 blurCoord11;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "    blurCoord0 = vTextureCoord.xy + vec2(0.0, -0.008);\n" +
                    "    blurCoord1 = vTextureCoord.xy + vec2(0.0, 0.008);\n" +
                    "    blurCoord2 = vTextureCoord.xy + vec2(-0.014, 0.0);\n" +
                    "    blurCoord3 = vTextureCoord.xy + vec2(0.014, 0.0);\n" +

                    "    blurCoord4 = vTextureCoord.xy + vec2(0.007, -0.006);\n" +
                    "    blurCoord5 = vTextureCoord.xy + vec2(0.007, 0.006);\n" +
                    "    blurCoord6 = vTextureCoord.xy + vec2(-0.007, 0.006);\n" +
                    "    blurCoord7 = vTextureCoord.xy + vec2(-0.007, -0.006);\n" +

                    "    blurCoord8 = vTextureCoord.xy + vec2(0.011, -0.004);\n" +
                    "    blurCoord9 = vTextureCoord.xy + vec2(0.011, 0.004);\n" +
                    "    blurCoord10 = vTextureCoord.xy + vec2(-0.011, 0.004);\n" +
                    "    blurCoord11 = vTextureCoord.xy + vec2(-0.011, -0.004);\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_EXT_BEAUTY2 = "" +
            "#extension GL_OES_EGL_image_external : require                                                                            \n" +
            "precision highp float;                                                                                                    \n"+
            "                                                                                                                          \n"+
            "uniform samplerExternalOES sTexture;                                                                                      \n"+
            "uniform vec2 singleStepOffset;                                                                                            \n"+
            "uniform highp vec4 params;                                                                                                \n"+
            "                                                                                                                          \n"+
            "varying highp vec2 vTextureCoord;                                                                                     \n"+
            "varying vec2 blurCoord0;\n" +
            "varying vec2 blurCoord1;\n" +
            "varying vec2 blurCoord2;\n" +
            "varying vec2 blurCoord3;\n" +
            "varying vec2 blurCoord4;\n" +
            "varying vec2 blurCoord5;\n" +
            "varying vec2 blurCoord6;\n" +
            "varying vec2 blurCoord7;\n" +
            "varying vec2 blurCoord8;\n" +
            "varying vec2 blurCoord9;\n" +
            "varying vec2 blurCoord10;\n" +
            "varying vec2 blurCoord11;\n" +
            "                                                                                                                          \n"+
            "void main(){                                                                                                              \n"+
            "	vec3 sampleColor = texture2D(sTexture, vTextureCoord).rgb;                                          \n"+
            "	sampleColor += texture2D(sTexture, blurCoord0).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord1).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord2).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord3).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord4).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord5).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord6).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord7).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord8).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord9).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoord10).rgb;                                                    \n"+
            "	sampleColor += texture2D(sTexture, blurCoord11).rgb;                                                    \n"+
            "		                                                                                                                   \n"+
            "	sampleColor /= 13.0;                                                                                     \n"+
            "	                                                                                                                       \n"+
            "	float var = 0.0;             \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord0).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord1).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord2).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord3).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord4).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord5).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord6).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord7).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord8).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord9).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord10).g- sampleColor.g));          \n"+
            "	var +=  abs((texture2D(sTexture, blurCoord11).g- sampleColor.g));          \n"+
            "	var /= 12.0;             \n"+
            "	gl_FragColor.rgb = mix(sampleColor, texture2D(sTexture, vTextureCoord).rgb, var / (var + 0.075));               \n"+
            "}";

    private static final String FRAGMENT_SHADER_EXT_BEAUTY = "" +
            "#extension GL_OES_EGL_image_external : require                                                                            \n" +
            "precision highp float;                                                                                                    \n"+
            "                                                                                                                          \n"+
            "uniform samplerExternalOES sTexture;                                                                                      \n"+
            "uniform vec2 singleStepOffset;                                                                                            \n"+
            "uniform highp vec4 params;                                                                                                \n"+
            "                                                                                                                          \n"+
            "varying highp vec2 vTextureCoord;                                                                                     \n"+
            "                                                                                                                          \n"+
            "void main(){                                                                                                              \n"+
            "	vec2 blurCoordinates[12];                                                                                              \n"+
            "	                                                                                                                       \n"+
            "	blurCoordinates[0] = vTextureCoord.xy + vec2(0.0, -0.008);                                       \n"+
            "	blurCoordinates[1] = vTextureCoord.xy + vec2(0.0, 0.008);                                        \n"+
            "	blurCoordinates[2] = vTextureCoord.xy + vec2(-0.014, 0.0);                                       \n"+
            "	blurCoordinates[3] = vTextureCoord.xy + vec2(0.014, 0.0);                                        \n"+
            "	                                                                                                                       \n"+
            "	blurCoordinates[4] = vTextureCoord.xy + vec2(0.007, -0.006);                                      \n"+
            "	blurCoordinates[5] = vTextureCoord.xy + vec2(0.007, 0.006);                                       \n"+
            "	blurCoordinates[6] = vTextureCoord.xy + vec2(-0.007, 0.006);                                      \n"+
            "	blurCoordinates[7] = vTextureCoord.xy + vec2(-0.007, -0.006);                                     \n"+
            "	                                                                                                                       \n"+
            "	blurCoordinates[8] = vTextureCoord.xy + vec2(0.011, -0.004);                                      \n"+
            "	blurCoordinates[9] = vTextureCoord.xy + vec2(0.011, 0.004);                                       \n"+
            "	blurCoordinates[10] = vTextureCoord.xy + vec2(-0.011, 0.004);	                                   \n"+
            "	blurCoordinates[11] = vTextureCoord.xy + vec2(-0.011, -0.004);                                    \n"+
            "	                                                                                                                       \n"+
            "	                                                                                                                       \n"+
            "	vec3 sampleColor = texture2D(sTexture, vTextureCoord).rgb;                                          \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[0]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[1]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[2]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[3]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[4]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[5]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[6]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[7]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[8]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[9]).rgb;                                                     \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[10]).rgb;                                                    \n"+
            "	sampleColor += texture2D(sTexture, blurCoordinates[11]).rgb;                                                    \n"+
            "		                                                                                                                   \n"+
            "	sampleColor /= 13.0;                                                                                     \n"+
            "	                                                                                                                       \n"+
			"	float var = 0.0;             \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[0]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[1]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[2]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[3]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[4]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[5]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[6]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[7]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[8]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[9]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[10]).g- sampleColor.g));          \n"+
			"	var +=  abs((texture2D(sTexture, blurCoordinates[11]).g- sampleColor.g));          \n"+
			"	var /= 12.0;             \n"+
			"	gl_FragColor.rgb = mix(sampleColor, texture2D(sTexture, vTextureCoord).rgb, var / (var + 0.075));               \n"+
            "}";


    private static final String FRAGMENT_SHADER_EXT_SHAPEN = "" +
            "#extension GL_OES_EGL_image_external : require                                                                            \n"+
            "precision highp float;                                                                                                    \n"+
            "                                                                                                                          \n"+
            "uniform samplerExternalOES sTexture;                                                                                      \n"+
            "                                                                                                                          \n"+
            "varying highp vec2 vTextureCoord;                                                                                         \n"+
            "void main() {                                                                                                             \n"+
            "vec2 offset0=vec2(-0.001389,-0.000781); vec2 offset1=vec2(0.0,-0.000781); vec2 offset2=vec2(0.001389,-0.000781);                                   \n"+
            "vec2 offset3=vec2(-0.001389,0.0); vec2 offset4=vec2(0.0,0.0); vec2 offset5=vec2(0.001389,0.0);                                      \n"+
            "vec2 offset6=vec2(-0.001389,0.000781); vec2 offset7=vec2(0.0,0.000781); vec2 offset8=vec2(0.001389,0.000781);                                      \n"+
            "float kernelValue0 = 0.0; float kernelValue1 = -1.0; float kernelValue2 = 0.0;                                            \n"+
            "float kernelValue3 = -1.0; float kernelValue4 = 5.0; float kernelValue5 = -1.0;                                           \n"+
            "float kernelValue6 = 0.0; float kernelValue7 = -1.0; float kernelValue8 = 0.0;                                            \n"+
            "vec4 sum;                                                                                                                 \n"+
            "vec4 cTemp0,cTemp1,cTemp2,cTemp3,cTemp4,cTemp5,cTemp6,cTemp7,cTemp8;                                                      \n"+
            "cTemp0=texture2D(sTexture, vec2(vTextureCoord.s+offset0.x, vTextureCoord.t + offset0.y));                     \n"+
            "cTemp1=texture2D(sTexture, vec2(vTextureCoord.s+offset1.x, vTextureCoord.t + offset1.y));                                                          \n"+
            "cTemp2=texture2D(sTexture, vec2(vTextureCoord.s+offset2.x, vTextureCoord.t + offset2.y));                                                          \n"+
            "cTemp3=texture2D(sTexture, vec2(vTextureCoord.s+offset3.x, vTextureCoord.t + offset3.y));                                                          \n"+
            "cTemp4=texture2D(sTexture, vec2(vTextureCoord.s+offset4.x, vTextureCoord.t + offset4.y));                                                          \n"+
            "cTemp5=texture2D(sTexture, vec2(vTextureCoord.s+offset5.x, vTextureCoord.t + offset5.y));                                                          \n"+
            "cTemp6=texture2D(sTexture, vec2(vTextureCoord.s+offset6.x, vTextureCoord.t + offset6.y));                                                          \n"+
            "cTemp7=texture2D(sTexture, vec2(vTextureCoord.s+offset7.x, vTextureCoord.t + offset7.y));                                                          \n"+
            "cTemp8=texture2D(sTexture, vec2(vTextureCoord.s+offset8.x, vTextureCoord.t + offset8.y));                                                          \n"+
            "sum =kernelValue0*cTemp0+kernelValue1*cTemp1+kernelValue2*cTemp2+                                                         \n"+
            "kernelValue3*cTemp3+kernelValue4*cTemp4+kernelValue5*cTemp5+                                                              \n"+
            "kernelValue6*cTemp6+kernelValue7*cTemp7+kernelValue8*cTemp8;                                                              \n"+
            "gl_FragColor = sum;                                                                                                       \n"+
            "}";

    private static final String FRAGMENT_SHADER_EXT_BRIGHTNESS =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "     vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                    "gl_FragColor = vec4((textureColor.rgb + vec3(0.1)), textureColor.w);\n" +
                    "}\n";


    private ProgramType mProgramType;

    // Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    //private int muKernelLoc;
    //private int muTexOffsetLoc;
    //private int muColorAdjustLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    //beauty
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    //beauty

    private int mTextureTarget;

    //private float[] mKernel = new float[KERNEL_SIZE];
    private float[] mTexOffset;
    private float mColorAdjust;


    /**
     * Prepares the program in the current EGL context.
     */
    public Texture2dProgram(ProgramType programType) {
        mProgramType = programType;

        switch (programType) {
            case TEXTURE_2D:
                mTextureTarget = GLES20.GL_TEXTURE_2D;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
                break;
            case TEXTURE_EXT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
                break;
            case TEXTURE_EXT_BEAUTY:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_BEAUTY_SHADER, FRAGMENT_SHADER_EXT_BEAUTY2);
                break;
            case TEXTURE_EXT_SHAPEN:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_SHAPEN);
                break;
            case TEXTURE_EXT_BRIGHTNESS:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_BRIGHTNESS);
                break;
            default:
                throw new RuntimeException("Unhandled type " + programType);
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
//        LivingLog.d(TAG, "Created program " + mProgramHandle + " (" + programType + ")");

        // get locations of attributes and uniforms

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");

        if (programType == ProgramType.TEXTURE_EXT_BEAUTY)
        {
            mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgramHandle, "singleStepOffset");
            //GlUtil.checkLocation(mSingleStepOffsetLocation, "singleStepOffset");
            mParamsLocation = GLES20.glGetUniformLocation(mProgramHandle, "params");
            //GlUtil.checkLocation(mParamsLocation, "params");
        }
    }

    /**
     * Releases the program.
     * <p>
     * The appropriate EGL context must be current (i.e. the one that was used to create
     * the program).
     */
    public void release() {
//        LivingLog.d(TAG, "deleting program " + mProgramHandle);
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Returns the program type.
     */
    public ProgramType getProgramType() {
        return mProgramType;
    }

    /**
     * Creates a texture object suitable for use with this program.
     * <p>
     * On exit, the texture will be bound.
     */
    public int createTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
        GLES20.glBindTexture(mTextureTarget, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        return texId;
    }

    /**
     * Sets the size of the texture.  This is used to find adjacent texels when filtering.
     */
    public void setTexSize(int width, int height) {
        float rw = 1.0f / width;
        float rh = 1.0f / height;

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = new float[] {
            -rw, -rh,   0f, -rh,    rw, -rh,
            -rw, 0f,    0f, 0f,     rw, 0f,
            -rw, rh,    0f, rh,     rw, rh
        };
        //Log.d(TAG, "filt size: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
    }

    FloatBuffer tmp1 = FloatBuffer.wrap(new float[] {1.0f / 720, 1.0f / 1280});
    FloatBuffer tmp2 = FloatBuffer.wrap(new float[] {0.33f, 0.63f, 0.4f, 0.35f});

    /**
     * Issues the draw call.  Does the full setup on every call.
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param vertexBuffer Buffer with vertex position data.
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the position data for each vertex (often
     *        vertexCount * sizeof(float)).
     * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
     *        for use with SurfaceTexture.)
     * @param texBuffer Buffer with vertex texture data.
     * @param texStride Width, in bytes, of the texture data for each vertex.
     */
    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
            GlUtil.checkGlError("glVertexAttribPointer");

        if (mProgramType == ProgramType.TEXTURE_EXT_BEAUTY && mSingleStepOffsetLocation > 0 && mParamsLocation > 0)
        {
            GLES20.glUniform2fv(mSingleStepOffsetLocation, 1, tmp1);
            GlUtil.checkGlError("mSingleStepOffsetLocation");
            GLES20.glUniform4fv(mParamsLocation, 1, tmp2);
            GlUtil.checkGlError("mParamsLocation");
        }

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
