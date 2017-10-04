package com.example.bamboo.demoweek1.view.object;

import android.opengl.GLES20;

import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Ground implements ExtendRenderer.DrawObject {
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawOrderBuffer;

    private int mProgram;

    private int mPossitionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mMoveHandle;

    float mGroundCoords[] = {
            -2.0f,  0.25f, 0.0f,
            -2.0f, -0.25f, 0.0f,
            2.0f, -0.25f, 0.0f,
            2.0f,  0.25f, 0.0f
    };

    short mDrawOrder[] = {0,1,2,0,2,3};
    float[] mColor = {0.235f, 0.722f, 0.471f, 1.0f};

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

    public Ground() {
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
        ByteBuffer bb1 = ByteBuffer.allocateDirect(mGroundCoords.length*4);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(mGroundCoords.length*2);

        bb1.order(ByteOrder.nativeOrder());
        bb2.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb1.asFloatBuffer();
        mDrawOrderBuffer = bb2.asShortBuffer();

        mVertexBuffer.put(mGroundCoords);
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
        return 8.0f;
    }

    @Override
    public float getHeight() {
        return 4.0f;
    }

    @Override
    public float getCenterX() {
        return mGroundCoords[0] + getWidth()/2 + translateX;
    }

    @Override
    public float getCenterY() {
        return mGroundCoords[1] - getHeight()/2 + translateY;
    }

    @Override
    public void draw(float[] matrix, boolean behaviour, SoundInterface context) {
        GLES20.glUseProgram(mProgram);

        translateX = 0.0f;
        translateY = -2.25f;

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
