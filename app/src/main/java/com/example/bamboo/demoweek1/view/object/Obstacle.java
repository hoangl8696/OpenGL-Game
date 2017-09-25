package com.example.bamboo.demoweek1.view.object;

import android.opengl.GLES20;

import com.example.bamboo.demoweek1.view.ExtendRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Obstacle implements ExtendRenderer.DrawObject {
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawOrderBuffer;

    private int mProgram;

    private int mPossitionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mMoveHandle;

    private float mMove = 0;

    float mSquareCoords[] = {
            -0.1f,  0.1f, 0.0f,
            -0.1f, -0.1f, 0.0f,
            0.1f, -0.1f, 0.0f,
            0.1f,  0.1f, 0.0f
    };
    short mDrawOrder[] = {0,1,2,0,2,3};
    float[] mColor = {1.0f, 0.2f, 0.2f, 1.0f};

    private float translateX;
    private float translateY;

    private boolean isReady = false;

    private final String mVerTextShaderCode =
            "attribute vec4 vPosition;"
                    + "uniform mat4 uMVPMatrix;"
                    + "uniform vec2 translate;"
                    + "void main() {"
                    + "   gl_Position = uMVPMatrix * vPosition + vec4(translate.x, translate.y, 0.0f, 0.0f);"
                    + "}";

    private final String mFragmentShaderCode =
            "precision mediump float;"
                    + "uniform vec4 vColor;"
                    + "void main() {"
                    + "    gl_FragColor = vColor;"
                    + "}";

    public Obstacle() {
        setUpBuffer();
        setUpProgram();
    }

    private void setUpProgram() {
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, mVerTextShaderCode));
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode));
        GLES20.glLinkProgram(mProgram);
    }

    private static int loadShader (int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void setUpBuffer() {
        ByteBuffer bb1 = ByteBuffer.allocateDirect(mSquareCoords.length*4);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(mSquareCoords.length*2);

        bb1.order(ByteOrder.nativeOrder());
        bb2.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb1.asFloatBuffer();
        mDrawOrderBuffer = bb2.asShortBuffer();

        mVertexBuffer.put(mSquareCoords);
        mDrawOrderBuffer.put(mDrawOrder);

        mVertexBuffer.position(0);
        mDrawOrderBuffer.position(0);
    }

    @Override
    public boolean canRemove() {
        return isReady;
    }

    @Override
    public float getWidth() {
        return 0.2f;
    }

    @Override
    public float getHeight() {
        return 0.2f;
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
    public void draw(float[] matrix, boolean behaviour) {
        GLES20.glUseProgram(mProgram);
        if (behaviour) {
            if (!(mMove > 8.0f)) {
                mMove += 0.05f;
            } else {
                isReady = true;
            }
        }

        translateX = 4.0f - mMove;
        translateY = -1.0f;

        mPossitionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mMoveHandle = GLES20.glGetUniformLocation(mProgram, "translate");

        GLES20.glEnableVertexAttribArray(mPossitionHandle);
        GLES20.glVertexAttribPointer(mPossitionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        GLES20.glUniform2f(mMoveHandle, translateX, translateY);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawOrderBuffer);
        GLES20.glDisableVertexAttribArray(mPossitionHandle);
    }
}
