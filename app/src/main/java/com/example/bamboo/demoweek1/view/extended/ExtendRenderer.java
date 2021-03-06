package com.example.bamboo.demoweek1.view.extended;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.bamboo.demoweek1.SoundInterface;
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

//Class in charge of object creation
public class ExtendRenderer implements GLSurfaceView.Renderer {
    private SoundInterface mContext;
    private HealthControl mHealthControl;

    private DrawObject mSquare, mGround;

    private long mLastClickTime;
    private long mLastClickTime2;

    private boolean isTriangleObstacle = true;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private boolean isJumping = false;
    private boolean isObstacle = false;

    private ArrayList<DrawObject> list = new ArrayList<>();

    private int mTextureDataHandle;

    private static Bitmap mRawData;

    public static void setRawData(Bitmap data) {
        if (ExtendRenderer.mRawData == null) {
            ExtendRenderer.mRawData = data;
        }
    }

    private ScheduledThreadPoolExecutor mExecutor;

    private Runnable mPeriodiclyGenerateObstacle;

    private static final int OBSTACLE_GENERATE_PERIOD = 5000;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.31f, 0.765f, 0.969f, 1.0f);
        mTextureDataHandle = 0;

        mTextureDataHandle = loadTexture(mRawData);
        mSquare = new Square(mTextureDataHandle);
        mGround = new Ground();
        mPeriodiclyGenerateObstacle = new Runnable() {
            @Override
            public void run() {
                if (!isObstacle)
                isObstacle = true;
            }
        };

        //Periodically generate obstacle to increase difficulty
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
        mSquare.draw(mMVPMatrix, isJumping, mContext);
        mGround.draw(mMVPMatrix, false, null);

        //This method is deprecated, only use in earlier version of the app, use to check which obstacle type the fragment
        //is requesting
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
                //Remove when out of screen
                list.remove(i);
            } else {
                list.get(i).draw(mMVPMatrix, true, null);
                if (collisionCheck(mSquare, list.get(i))) {
                    //Play collide sound
                    mContext.playCollide();
                    long lastClickTime = mLastClickTime;
                    long now = System.currentTimeMillis();
                    mLastClickTime = now;
                    if (now - lastClickTime < 100) {
                        // Too fast: ignore
                    } else {
                        // Register the click
                        ExtendGLSurfaceView.vibrate(50);
                        mHealthControl.decrease();
                    }
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
        long lastClickTime = mLastClickTime2;
        long now = System.currentTimeMillis();
        mLastClickTime2 = now;
        if (now - lastClickTime < 500) {
            // Too fast: ignore
        } else {
            // Register
            isObstacle = true;
        }
    }

    public boolean collisionCheck(DrawObject o1, DrawObject o2) {
        return ( Math.abs( o1.getCenterX() - o2.getCenterX() ) * 2 < o1.getWidth() + o2.getWidth() )
                && ( Math.abs( o1.getCenterY() - o2.getCenterY() ) < o1.getHeight() + o2.getHeight() );
    }

    public void setContext (SoundInterface context) {
        if (context != null) {
            mContext = context;
        }
    }

    public void setHealthControl (HealthControl context) {
        if (context != null) {
            mHealthControl = context;
        }
    }

    public static int loadTexture (Bitmap data) {
        if (data != null) {
            int[] textureHandle = new int[1];
            GLES20.glGenTextures(1, textureHandle, 0);

            if (textureHandle[0] != 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = data;
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
        } else {
            return -1;
        }
    }

    public interface DrawObject {
        void draw (float[] matrix, boolean behaviour, SoundInterface context);
        boolean canRemove();
        float getWidth();
        float getHeight();
        float getCenterX();
        float getCenterY();
    }

    public interface HealthControl {
        void decrease();
    }
}
