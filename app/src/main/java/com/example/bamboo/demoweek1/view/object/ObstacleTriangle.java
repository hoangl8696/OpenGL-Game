package com.example.bamboo.demoweek1.view.object;

import android.opengl.GLES20;

import com.example.bamboo.demoweek1.view.extended.ExtendRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ObstacleTriangle implements ExtendRenderer.DrawObject {
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private FloatBuffer mVertexBuffer;

    private int mProgram;

    private int mPossitionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mMoveHandle;

    private float mMove = 0;

    float mTriangleCoords[] = {
            0.0f,  0.1f, 0.0f,
            -0.1f, -0.1f, 0.0f,
            0.1f, -0.1f, 0.0f
    };

    private final int VERTEX_COUNT = mTriangleCoords.length / COORDS_PER_VERTEX;

    float[] mColor = {0.12f, 0.663f, 0.957f, 1.0f};

    private float translateX;
    private float translateY;

    private boolean isReady = false;

    private static final float SPEED = 0.03f;

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

    public ObstacleTriangle() {
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
        ByteBuffer bb = ByteBuffer.allocateDirect(mTriangleCoords.length*4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mTriangleCoords);
        mVertexBuffer.position(0);
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
        return mTriangleCoords[0] + translateX;
    }

    @Override
    public float getCenterY() {
        return mTriangleCoords[1] - getHeight()/2 + translateY;
    }

    @Override
    public void draw(float[] matrix, boolean behaviour) {
        GLES20.glUseProgram(mProgram);
        if (behaviour) {
            if (!(mMove > 8.0f)) {
                mMove += SPEED;
            } else {
                isReady = true;
            }
        }

        translateX = 4.0f - mMove;
        translateY = -1.15f;

        mPossitionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mMoveHandle = GLES20.glGetUniformLocation(mProgram, "translate");

        GLES20.glEnableVertexAttribArray(mPossitionHandle);
        GLES20.glVertexAttribPointer(mPossitionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        GLES20.glUniform2f(mMoveHandle, translateX, translateY);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
        GLES20.glDisableVertexAttribArray(mPossitionHandle);
    }
}
