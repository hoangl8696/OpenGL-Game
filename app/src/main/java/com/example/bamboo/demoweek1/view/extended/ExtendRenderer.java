package com.example.bamboo.demoweek1.view.extended;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.bamboo.demoweek1.view.object.Ground;
import com.example.bamboo.demoweek1.view.object.Obstacle;
import com.example.bamboo.demoweek1.view.object.ObstacleTriangle;
import com.example.bamboo.demoweek1.view.object.Square;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ExtendRenderer implements GLSurfaceView.Renderer {
    private Context mContext;

    private DrawObject mSquare, mGround;

    private boolean isTriangleObstacle = true;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private boolean isJumping = false;
    private boolean isObstacle = false;

    private ArrayList<DrawObject> list = new ArrayList<>();

    private int mTextureDataHandle;

    private static Bitmap mRawData;

    public static void setRawData(Bitmap mRawData) {
        ExtendRenderer.mRawData = mRawData;
    }

    private ScheduledThreadPoolExecutor mExecutor;

    private Runnable mPeriodiclyGenerateObstacle;

    private static final int OBSTACLE_GENERATE_PERIOD = 5000;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.31f, 0.765f, 0.969f, 1.0f);
        mTextureDataHandle = 0;
        if (mRawData != null) {
            mTextureDataHandle = loadTexture(mRawData);
        }
        mSquare = new Square(mTextureDataHandle);
        mGround = new Ground();
        mPeriodiclyGenerateObstacle = new Runnable() {
            @Override
            public void run() {
                if (!isObstacle)
                isObstacle = true;
            }
        };
        if (mExecutor == null) {
            mExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);
            mExecutor.scheduleWithFixedDelay(mPeriodiclyGenerateObstacle, 5000, OBSTACLE_GENERATE_PERIOD, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0, width, height);
        float ratio = (float) width/height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1,1,3,7);
        list.clear();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.setLookAtM(mViewMatrix, 0,0,0,3, 0f,0f,0f,0f, 1f,0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mSquare.draw(mMVPMatrix, isJumping);
        mGround.draw(mMVPMatrix, false);

        if (isObstacle) {
            if (isTriangleObstacle) {
                list.add(new ObstacleTriangle());
            } else {
                list.add(new Obstacle());
            }
            isObstacle = false;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).canRemove()) {
                list.remove(i);
            } else {
                list.get(i).draw(mMVPMatrix, true);
                if (collisionCheck(mSquare, list.get(i))) {
                    Log.d("Collision", "true");
                    ExtendGLSurfaceView.vibrate(100);
                }
            }
        }
    }

    public void goUp() {
        isJumping = true;
    }

    public void goDown() {
        isJumping  = false;
    }

    public void addObstacle() {
        isObstacle = true;
        Log.d("New obj", Integer.toString(list.size()));
    }

    public boolean collisionCheck(DrawObject o1, DrawObject o2) {
        return ( Math.abs( o1.getCenterX() - o2.getCenterX() ) * 2 < o1.getWidth() + o2.getWidth() )
                && ( Math.abs( o1.getCenterY() - o2.getCenterY() ) < o1.getHeight() + o2.getHeight() );
    }

    public void setContext (Context context) {
        mContext = context;
    }

    public static int loadTexture (Bitmap data) {
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            Bitmap bitmap  = data;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//            bitmap.recycle();
            return textureHandle[0];
        } else {
            throw new RuntimeException("Error loading texture");
        }
    }

    public interface DrawObject {
        void draw (float[] matrix, boolean behaviour);
        boolean canRemove();
        float getWidth();
        float getHeight();
        float getCenterX();
        float getCenterY();
    }
}
