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

import java.nio.FloatBuffer;

/**
 * Base class for stuff we like to draw.
 * yutianzuo,add comments
 * 这个类定义了基本的一些顶点着色器用要用到的坐标
 * 也定义了纹理所需要的坐标
 * 具体看下面具体注释
 */
public class Drawable2d {
    private static final int SIZEOF_FLOAT = 4;

    /**
     * Simple equilateral triangle (1.0 per side).  Centered on (0,0).
     */
    private static final float TRIANGLE_COORDS[] = {
         0.0f,  0.577350269f,   // 0 top
        -0.5f, -0.288675135f,   // 1 bottom left
         0.5f, -0.288675135f    // 2 bottom right
    };
    private static final float TRIANGLE_TEX_COORDS[] = {
        0.5f, 0.0f,     // 0 top center
        0.0f, 1.0f,     // 1 bottom left
        1.0f, 1.0f,     // 2 bottom right
    };
    private static final FloatBuffer TRIANGLE_BUF =
            GlUtil.createFloatBuffer(TRIANGLE_COORDS);
    private static final FloatBuffer TRIANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(TRIANGLE_TEX_COORDS);

    /**
     * Simple square, specified as a triangle strip.  The square is centered on (0,0) and has
     * a size of 1x1.
     * <p>
     * Triangles are 0-1-2 and 2-1-3 (counter-clockwise winding).
     */
    private static final float RECTANGLE_COORDS[] = {
        -0.5f, -0.5f,   // 0 bottom left
         0.5f, -0.5f,   // 1 bottom right
        -0.5f,  0.5f,   // 2 top left
         0.5f,  0.5f,   // 3 top right
    };
    private static final float RECTANGLE_TEX_COORDS[] = {
        0.0f, 1.0f,     // 0 bottom left
        1.0f, 1.0f,     // 1 bottom right
        0.0f, 0.0f,     // 2 top left
        1.0f, 0.0f      // 3 top right
    };
    private static final FloatBuffer RECTANGLE_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_COORDS);
    private static final FloatBuffer RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_TEX_COORDS);

    /**
     * A "full" square, extending from -1 to +1 in both dimensions.  When the model/view/projection
     * matrix is identity, this will exactly cover the viewport.
     * <p>
     * The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out
     * right with external textures from SurfaceTexture.)
     *
     * yutianzuo add comments
     * 上面的英文注释的意思是在解释两种坐标系，一种是OPENGL的世界坐标系，即FULL_RECTANGLE_COORDS，它的边界是+-1.0
     * 以（0.0，0.0）原点为正中心。如下面这个坐标就是充满整个坐标系，可以理解为充满整个OPENGL要绘制的区域（即我们这个demo中填充满GLSurfaceview的控件）
     * 上面所说的刚好充满还有一个前提就是MVP矩阵是一个单位阵（这里如果不清楚，找书，google吧。。。）
     * 下面的坐标代表的是纹理坐标，即Texture的坐标。这个坐标系在pc平台是左下角（0，0）右上角（1，1）即以（0，0）为坐标起点，向右x轴正增长到1.0，向上y轴正增长到1.0
     * 但是在android平台y轴是向下的即即以（0，0）为坐标起点，向右x轴正增长到1.0，向下y轴正增长到1.0（跟android屏幕的坐标系相同）
     * 这里可以在上面RECTANGLE_COORDS和RECTANGLE_TEX_COORDS看的很清楚
     * 但是这里的FULL_RECTANGLE_TEX_COORDS是很不一样的，为什么？因为配合surfacetexture的矩阵乘以这个坐标，最后得到的结果刚好是对的。
     * 至于为什么这样，我现在也没有太搞清楚。
     */
    private static final float FULL_RECTANGLE_COORDS[] = {
        -1.0f, -1.0f,   // 0 bottom left
         1.0f, -1.0f,   // 1 bottom right
        -1.0f,  1.0f,   // 2 top left
         1.0f,  1.0f,   // 3 top right
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
        0.0f, 0.0f,     // 0 bottom left
        1.0f, 0.0f,     // 1 bottom right
        0.0f, 1.0f,     // 2 top left
        1.0f, 1.0f      // 3 top right
    };

    private static final float FULL_RECTANGLE_TEX_MIRRO_COORDS[] = {
            1.0f, 0.0f,     // 0 bottom left
            0.0f, 0.0f,     // 1 bottom right
            1.0f, 1.0f,     // 2 top left
            0.0f, 1.0f      // 3 top right
    };
//    private static final FloatBuffer RECTANGLE_BUF =
//            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

    private static final FloatBuffer FULL_RECTANGLE_TEX_MIRRORBUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_MIRRO_COORDS);

    private FloatBuffer mVertexArray;
    private FloatBuffer mTexCoordArray;
    private FloatBuffer mTexMirroCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;
    private int mTexCoordStride;
    private Prefab mPrefab;

    /**
     * Enum values for constructor.
     */
    public enum Prefab {
        TRIANGLE, RECTANGLE, FULL_RECTANGLE
    }

    /**
     * Prepares a drawable from a "pre-fabricated" shape definition.
     * <p>
     * Does no EGL/GL operations, so this can be done at any time.
     */
    public Drawable2d(Prefab shape) {
        switch (shape) {
            case TRIANGLE:
                mVertexArray = TRIANGLE_BUF;
                mTexCoordArray = TRIANGLE_TEX_BUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = TRIANGLE_COORDS.length / mCoordsPerVertex;
                break;
            case RECTANGLE:
                mVertexArray = RECTANGLE_BUF;
                mTexCoordArray = RECTANGLE_TEX_BUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = RECTANGLE_COORDS.length / mCoordsPerVertex;
                break;
            case FULL_RECTANGLE:
                mVertexArray = FULL_RECTANGLE_BUF;
                mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
                mTexMirroCoordArray = FULL_RECTANGLE_TEX_MIRRORBUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
                break;
            default:
                throw new RuntimeException("Unknown shape " + shape);
        }
        mTexCoordStride = 2 * SIZEOF_FLOAT;
        mPrefab = shape;
    }

    /**
     * Returns the array of vertices.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer getVertexArray() {
        return mVertexArray;
    }

    /**
     * Returns the array of texture coordinates.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer getTexCoordArray() {
        return mTexCoordArray;
    }

    public FloatBuffer getTexMirroCoordArray()
    {
        return mTexMirroCoordArray;
    }

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    public int getVertexCount() {
        return mVertexCount;
    }

    /**
     * Returns the width, in bytes, of the data for each vertex.
     */
    public int getVertexStride() {
        return mVertexStride;
    }

    /**
     * Returns the width, in bytes, of the data for each texture coordinate.
     */
    public int getTexCoordStride() {
        return mTexCoordStride;
    }

    /**
     * Returns the number of position coordinates per vertex.  This will be 2 or 3.
     */
    public int getCoordsPerVertex() {
        return mCoordsPerVertex;
    }

    @Override
    public String toString() {
        if (mPrefab != null) {
            return "[Drawable2d: " + mPrefab + "]";
        } else {
            return "[Drawable2d: ...]";
        }
    }
}
