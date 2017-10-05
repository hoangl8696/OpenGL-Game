package com.example.bamboo.demoweek1.view.extended;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.bamboo.demoweek1.SoundInterface;

import static com.example.bamboo.demoweek1.MainActivity.OFFLINE_FLAG;

public class ExtendGLSurfaceView extends GLSurfaceView {
    private ExtendRenderer mRenderer;
    private Context mContext;
    private SoundInterface mActivity;
    private static Vibrator v;
    private boolean JUMP_FLAG = false;

    public ExtendGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        mRenderer = new ExtendRenderer();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setSoundInterface(SoundInterface activity) {
        mRenderer.setContext(activity);
        mActivity = activity;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (OFFLINE_FLAG) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    mRenderer.goDown();
                    mRenderer.addObstacle();
                    JUMP_FLAG = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                    if (JUMP_FLAG) {
                        JUMP_FLAG = true;
                        mActivity.playJump();
                    }
                    mRenderer.goUp();
                    break;
            }
            return true;
        }
        return false;
    }

    public void goUp() {
        mRenderer.goUp();
    }

    public void goDown() {
        mRenderer.goDown();
    }

    public void addObstacle() {
        mRenderer.addObstacle();
    }

    public static void vibrate(int time) {
        v.vibrate(time);
    }
}
