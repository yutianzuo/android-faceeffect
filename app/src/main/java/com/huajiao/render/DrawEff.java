package com.huajiao.render;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLUtils;

import com.huajiao.opengl.Sprite2d;
import com.huajiao.opengl.Texture2dProgram;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glTexParameterf;

/**
 * yutianzuo，calculate and draw decoration on face
 */

final public class DrawEff {

    float deltaTimeRender = 0.0f;
    float deltaTimeRenderEncoder = 0.0f;


    public Stack<PointF[]> stackFacePonits = new Stack<>();
    final public Object stackLock = new Object();

    final private Object cacheLock = new Object();
    private Map<String, Bitmap> mBitmapCache = new TreeMap<>();
    private int mCacheNum = 51; //hardcode


    public void setCacheNum(int num) {
        synchronized (cacheLock) {
            deltaTimeRender = 0.0f;
            deltaTimeRenderEncoder = 0.0f;
            mCacheNum = num;
        }
    }

    private void mirror(PointF[] points, int width, int height) {
        for (int i = 0; i < points.length; ++i) {
            points[i].x = width - points[i].x;
        }
    }

    public void drawEffect(PointF[] points, int face_det_width, int face_det_height, int width, int height, float[] matrix,
                           float w_ratio, float h_ratio, EffectManager effectManager,
                           Texture2dProgram mTextureProgram, Sprite2d mRect, boolean is_render_encoder, boolean b_mirror) {
        if (effectManager == null || effectManager.getTextureNum() <= 0) {
            return;
        }

        if (mTextureProgram == null || mRect == null) {
            return;
        }

        int LEFT_EYE = 0;
        int RIGHT_EYE = 0;
        int NOSE = 0;
        int TOP_LIP = 0;

        if (true) {
            LEFT_EYE = 39;
            RIGHT_EYE = 57;
            NOSE = 69;

            //修正点坐标，兼容senstime，然后直接把计算修正后的点赋值给39,57,69点。
            points[39].x = (points[39].x + points[45].x) / 2;
            points[39].y = (points[39].y + points[45].y) / 2;

            points[57].x = (points[51].x + points[57].x) / 2;
            points[57].y = (points[51].y + points[57].y) / 2;

            points[69].x = (points[66].x + points[71].x) / 2;
            points[69].y = (points[66].y + points[71].y) / 2;

            TOP_LIP = 78;
        }
//        else {
//            LEFT_EYE = 77;
//            RIGHT_EYE = 74;
//            NOSE = 46;
//            TOP_LIP = 87;
//        }


        float mid_x = 0;
        float mid_y = 0;

        float ratio_w = w_ratio;
        float ratio_h = h_ratio;

        //PointF[] points = faces[0].getPointsArray();
        if (b_mirror) {
            mirror(points, face_det_width, face_det_height);
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Bitmap bitmap = null;
        int textureNum = effectManager.getTextureNum();
        int[] textures = new int[textureNum];
        glGenTextures(textureNum, textures, 0);

        int maxImageNum = effectManager.getTextureFrameCount(0);
        for (int i = 0; i < textureNum; i++) {
            maxImageNum = Math.max(maxImageNum, effectManager.getTextureFrameCount(i));
        }

        float eyeDistance = (float) Math.sqrt((float) ((points[RIGHT_EYE].x - points[LEFT_EYE].x) * (points[RIGHT_EYE].x - points[LEFT_EYE].x)
                + (points[RIGHT_EYE].y - points[LEFT_EYE].y) * (points[RIGHT_EYE].y - points[LEFT_EYE].y)));

//		float angle = (float) -((float) 180.0f * Math.atan((points[0].y - points[32].y) / (points[0].x - points[32].x))
//				/ Math.PI);

        float angle = (float) -((float) 180.0f * Math.atan((points[LEFT_EYE].y - points[RIGHT_EYE].y) / (points[LEFT_EYE].x - points[RIGHT_EYE].x))
                / Math.PI); //向右转角度为负，向左为正，为了后面设置opengl旋转参数


        for (int i = 0; i < textureNum; i++) {
            float radius = 0.0f;

            int midType = effectManager.getTextureMidType(i);

            float x = (float) effectManager.getTextureX(i);
            float y = (float) effectManager.getTextureY(i);
            float w = (float) effectManager.getTextureW(i);
            float h = (float) effectManager.getTextureH(i);


            switch (midType) {
                case 0:
                    // 2.1 head type
                    mid_x = ((points[RIGHT_EYE].x + points[LEFT_EYE].x) / 2.0f) * ratio_w;
                    mid_y = height - ((points[RIGHT_EYE].y + points[LEFT_EYE].y) / 2.0f) * ratio_h;
                    break;
                case 1:
                    // 2.2 nose
                    mid_x = points[NOSE].x * ratio_w;
                    mid_y = height - (points[NOSE].y) * ratio_h;
                    break;
                case 2:
                    // 2.3 fixed type
                    mid_x = (w - effectManager.getTextureMidX(i)) / 2.0f;
                    mid_y = (h - effectManager.getTextureMidY(i)) / 2.0f;
                    break;
                case 3:
                    // 2.4 mouse type
                    mid_x = points[TOP_LIP].x * ratio_w;
                    mid_y = height - points[TOP_LIP].y * ratio_h;
                    break;
                default:
                    //if(LiveCloudRecorder.bDebug)
                    return;
            }

            float scaleRatio;
            int scaleType = effectManager.getTextureScaleType(i);
            if (scaleType == 1) {
                scaleRatio = (float) effectManager.getTextureScaleRation(i);
            } else {
                scaleRatio = eyeDistance / (float) effectManager.getTextureScaleRation(i);
            }

            if (midType == 2) {
                mid_x = mid_x * scaleRatio * 2;
                mid_y = mid_y * scaleRatio * 2;
            }

            if (midType != 2) {
                if (y == 0.0f) {
                    y = 0.0000001f;
                }
                if (x == 0.0f) {
                    x = 0.0000001f;
                }
                float x_dis_from_mid = -(w / 2 + x);
                float y_dis_from_mid = -(h / 2 + y);
                if (x_dis_from_mid == 0.0f) {
                    x_dis_from_mid = 0.0000001f;
                }
                float dis_from_point = (float) Math.sqrt(x_dis_from_mid * x_dis_from_mid + y_dis_from_mid * y_dis_from_mid);
                float angle_pic_align_hor_abs = (float) ((float) 180.0f * Math.atan(Math.abs(y_dis_from_mid / x_dis_from_mid))
                        / Math.PI);

                float angle_sum;

                float cos_angle_in_radians;
                float sin_angle_in_radians;

                float mid_y_dis;
                float mid_x_dis;

                if (x_dis_from_mid > .0f && y_dis_from_mid >= .0f) //第一象限
                {
                    angle_sum = angle_pic_align_hor_abs + (angle);
                    cos_angle_in_radians = (float) Math.cos((double) Math.abs(angle_sum) * Math.PI / 180);
                    sin_angle_in_radians = (float) Math.sin((double) Math.abs(angle_sum) * Math.PI / 180);
                    mid_y_dis = Math.abs(dis_from_point * sin_angle_in_radians);
                    mid_x_dis = Math.abs(dis_from_point * cos_angle_in_radians);

                    if (angle_sum >= .0f && angle_sum <= 90.0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum > 90.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum < .0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    }
                } else if (x_dis_from_mid < .0f && y_dis_from_mid >= .0f) //第二象限
                {
                    angle_sum = (180.0f - angle_pic_align_hor_abs) + (angle);
                    cos_angle_in_radians = (float) Math.cos((double) Math.abs(angle_sum) * Math.PI / 180);
                    sin_angle_in_radians = (float) Math.sin((double) Math.abs(angle_sum) * Math.PI / 180);
                    mid_y_dis = Math.abs(dis_from_point * sin_angle_in_radians);
                    mid_x_dis = Math.abs(dis_from_point * cos_angle_in_radians);

                    if (angle_sum > 180.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum >= 90.0f && angle_sum <= 180.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum < 90.0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    }
                } else if (x_dis_from_mid < .0f && y_dis_from_mid < .0f) //第三象限
                {
                    angle_sum = 180.0f + angle_pic_align_hor_abs + (angle);
                    cos_angle_in_radians = (float) Math.cos((double) Math.abs(angle_sum) * Math.PI / 180);
                    sin_angle_in_radians = (float) Math.sin((double) Math.abs(angle_sum) * Math.PI / 180);
                    mid_y_dis = Math.abs(dis_from_point * sin_angle_in_radians);
                    mid_x_dis = Math.abs(dis_from_point * cos_angle_in_radians);

                    if (angle_sum >= 180.0f && angle_sum <= 270.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum > 270.0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum < 180.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    }
                } else if (x_dis_from_mid > .0f && y_dis_from_mid < .0f) //第四象限
                {
                    angle_sum = (360.0f - angle_pic_align_hor_abs) + (angle);
                    cos_angle_in_radians = (float) Math.cos((double) Math.abs(angle_sum) * Math.PI / 180);
                    sin_angle_in_radians = (float) Math.sin((double) Math.abs(angle_sum) * Math.PI / 180);
                    mid_y_dis = Math.abs(dis_from_point * sin_angle_in_radians);
                    mid_x_dis = Math.abs(dis_from_point * cos_angle_in_radians);

                    if (angle_sum < 270.0f) {
                        mid_x = mid_x - mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum >= 270.0f && angle_sum <= 360.0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y - mid_y_dis * scaleRatio * w_ratio;
                    } else if (angle_sum > 360.0f) {
                        mid_x = mid_x + mid_x_dis * scaleRatio * w_ratio;
                        mid_y = mid_y + mid_y_dis * scaleRatio * w_ratio;
                    }
                }
            }

            int radiusType = effectManager.getTextureRadiusType(i);

            if (radiusType == 1) {
                radius = (float) effectManager.getTextureRadius(i);
            } else {
                radius = angle;
            }

            if (is_render_encoder) {
                if (deltaTimeRenderEncoder >= maxImageNum) {
                    deltaTimeRenderEncoder = 0;
                }
            } else {
                if (deltaTimeRender >= maxImageNum) {
                    deltaTimeRender = 0;
                }
            }


            int chooseNum = 0;
            if (is_render_encoder) {
                chooseNum = (int) ((deltaTimeRenderEncoder >= (float) effectManager.getTextureFrameCount(i))
                        ? (effectManager.getTextureFrameCount(i) - 1) : deltaTimeRenderEncoder);
            } else {
                chooseNum = (int) ((deltaTimeRender >= (float) effectManager.getTextureFrameCount(i))
                        ? (effectManager.getTextureFrameCount(i) - 1) : deltaTimeRender);
            }

            long l1 = System.currentTimeMillis();
            String str_png_name = effectManager.GetPngName(i, chooseNum);
            synchronized (cacheLock) {
                if (mBitmapCache.containsKey(str_png_name)) {
                    bitmap = mBitmapCache.get(str_png_name);
                } else {
                    bitmap = effectManager.GetBitmap(i, chooseNum);
                    if (bitmap != null) {
                        if (mBitmapCache.size() > mCacheNum) {
                            mBitmapCache.clear();
                        }
                        mBitmapCache.put(str_png_name, bitmap);
                    } else {
                        mBitmapCache.clear();
                    }
                }
            }

            glBindTexture(GL_TEXTURE_2D, textures[i]);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            if (bitmap != null) {
                GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
                if (scaleType == 1) {
                    mRect.setScale(w * scaleRatio * 2, h * scaleRatio * 2);
                } else {
                    mRect.setScale(w * scaleRatio * w_ratio, h * scaleRatio * h_ratio);
                }
                mRect.setPosition(mid_x, mid_y);
                mRect.setTexture(textures[i]);
                mRect.setRotation(radius);
                glGetError();
                mRect.draw(mTextureProgram, matrix);
            }
        }
        if (is_render_encoder) {
            deltaTimeRenderEncoder += 1.0f;
        } else {
            deltaTimeRender += 1.0f;
        }

        glDeleteTextures(textureNum, textures, 0);
        glDisable(GL_BLEND);
    }
}
