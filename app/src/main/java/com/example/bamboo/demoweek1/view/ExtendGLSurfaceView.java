package com.example.bamboo.demoweek1.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class ExtendGLSurfaceView extends GLSurfaceView {
    private ExtendRenderer mRenderer;
    private Context mContext;
    private static Vibrator v;

    public ExtendGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        mRenderer = new ExtendRenderer();
        setRenderer(mRenderer);
        mRenderer.setContext(context);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_UP:
//                mRenderer.goDown();
//                mRenderer.addObstacle();
//                break;
//            case MotionEvent.ACTION_DOWN:
//                mRenderer.goUp();
//                break;
//        }
//        return true;
//    }

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
