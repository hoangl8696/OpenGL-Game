package com.example.bamboo.demoweek1.view.object;

import android.opengl.GLES20;

import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

//Class handle drawing the player and player behavior relative to input
public class Square implements ExtendRenderer.DrawObject {
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    static final int TEXTURE_COORDS_DATA_SIZE = 2;
    private static boolean LAND_FLAG = false;

    private long mLastClickTime;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawOrderBuffer;
    private FloatBuffer mTextureBuffer;

    private int mProgram;

    private float jump = 0;

    private final float mGrace = 0.05f;

    private int mPossitionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mJumpHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordHandle;
    private int mTextureDataHandle;

    float mSquareCoords[] = {
            -0.15f,  0.15f, 0.0f,
            -0.15f, -0.15f, 0.0f,
            0.15f, -0.15f, 0.0f,
            0.15f,  0.15f, 0.0f
    };
    short mDrawOrder[] = {0,1,2,0,2,3};
    float[] mColor = {0.004f, 0.341f, 0.608f, 1.0f};
    float[] mTextureCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private float translateX;
    private float translateY;

    private final String mVerTextShaderCode =
            "attribute vec4 vPosition;"
                    + "uniform mat4 uMVPMatrix;"
                    + "uniform vec2 translate;"
                    + "attribute vec2 aTexCoordinate;"
                    + "varying vec2 vTexCoordinate;"
                    + "void main() {"
                    + "   gl_Position = uMVPMatrix * vPosition + vec4(translate.x, translate.y, 0.0f, 0.0f);"
                    + "   vTexCoordinate = aTexCoordinate;"
                    + "}";

    private final String mFragmentShaderCode =
            "precision mediump float;"
                    + "uniform vec4 vColor;"
                    + "uniform sampler2D uTexture;"
                    + "varying vec2 vTexCoordinate;"
                    + "void main() {"
                    + "    gl_FragColor = texture2D(uTexture, vTexCoordinate);"
//                    + "    gl_FragColor = vColor;"
                    + "}";

    private final String mFragmentShaderCode2 =
            "precision mediump float;"
                    + "uniform vec4 vColor;"
                    + "uniform sampler2D uTexture;"
                    + "varying vec2 vTexCoordinate;"
                    + "void main() {"
//                    + "    gl_FragColor = texture2D(uTexture, vTexCoordinate);"
                    + "    gl_FragColor = vColor;"
                    + "}";

    public Square(int textureHandle) {
        if (textureHandle == -1) {
            setUpProgram(false);
        } else {
            setUpProgram(true);
        }
        mTextureDataHandle = textureHandle;
        setUpBuffer();
    }

    private void setUpProgram(boolean which) {
        mProgram = GLES20.glCreateProgram();
        if (which) {
            GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, mVerTextShaderCode));
            GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode));
            GLES20.glLinkProgram(mProgram);
        } else {
            GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, mVerTextShaderCode));
            GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode2));
            GLES20.glLinkProgram(mProgram);
        }
    }

    private void setUpBuffer() {
        ByteBuffer bb1 = ByteBuffer.allocateDirect(mSquareCoords.length*4);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(mSquareCoords.length*2);
        ByteBuffer bb3 = ByteBuffer.allocateDirect(mTextureCoord.length*4);

        bb1.order(ByteOrder.nativeOrder());
        bb2.order(ByteOrder.nativeOrder());
        bb3.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb1.asFloatBuffer();
        mDrawOrderBuffer = bb2.asShortBuffer();
        mTextureBuffer = bb3.asFloatBuffer();

        mVertexBuffer.put(mSquareCoords);
        mDrawOrderBuffer.put(mDrawOrder);
        mTextureBuffer.put(mTextureCoord);

        mVertexBuffer.position(0);
        mDrawOrderBuffer.position(0);
        mTextureBuffer.position(0);
    }


    private static int loadShader (int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public float getWidth() {
        return 0.3f;
    }

    @Override
    public float getHeight() {
        return 0.3f;
    }

    @Override
    public float getCenterX() {
        return mSquareCoords[0] + getWidth()/2 + translateX;
    }

    @Override
    public float getCenterY() {
        return mSquareCoords[1] - getHeight()/2 + translateY;
    }

    @Override
    public void draw(float[] matrix, boolean behaviour, SoundInterface context) {
        GLES20.glUseProgram(mProgram);

        if (behaviour) {
            if (jump < 1.5) {
                jump += mGrace;
                if (context != null) {
                    long lastClickTime = mLastClickTime;
                    long now = System.currentTimeMillis();
                    mLastClickTime = now;
                    if (now - lastClickTime < 100) {
                        // Too fast: ignore
                    } else {
                        // Register the click
                        context.playJump();
                        LAND_FLAG = false;
                    }
                }
            }
        } else {
            if (jump > 0) {
                jump -= mGrace;
            } else {
                if (context != null) {
                    if (!LAND_FLAG) {
                        context.playLand();
                        LAND_FLAG = true;
                    }
                }
            }
        }

        translateX = -2.0f;
        translateY = -1.0f + (float) Math.sin(jump)*2.0f;

        mPossitionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mJumpHandle = GLES20.glGetUniformLocation(mProgram, "translate");
        if (mTextureDataHandle != -1) {
            mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
            mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoordinate");

            GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
            GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_COORDS_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        }


        if (mTextureDataHandle != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            GLES20.glUniform1i(mTextureUniformHandle, 0);
        }

        GLES20.glEnableVertexAttribArray(mPossitionHandle);
        GLES20.glVertexAttribPointer(mPossitionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        GLES20.glUniform2f(mJumpHandle, translateX, translateY);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawOrderBuffer);
        GLES20.glDisableVertexAttribArray(mPossitionHandle);
        if (mTextureDataHandle != -1) {
            GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        }
    }
}
