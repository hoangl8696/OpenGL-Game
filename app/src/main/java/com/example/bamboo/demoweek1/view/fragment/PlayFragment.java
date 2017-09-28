package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.view.extended.ExtendGLSurfaceView;
import com.example.bamboo.demoweek1.view.extended.ExtendRenderer;

import static android.content.Context.SENSOR_SERVICE;

public class PlayFragment extends android.app.Fragment implements SensorEventListener{
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ExtendGLSurfaceView mSurfaceView;

//    private boolean isGameBegin;

    private FrameLayout mFrameLayout;

    private SensorManager mSensorManager;
    private Sensor mGameRotationVectorSensor;

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private OnPlayFragmentInteractionListener mListener;

//    private FrameLayout mCalibrationScreen;

//    private int goUp;
//    private boolean isAirFlowMonitoring;
//    private boolean isHeartMonitoring;

//    private ScheduledThreadPoolExecutor mExecutor;

    public PlayFragment() {
        // Required empty public constructor
    }

    public static PlayFragment newInstance() {
        PlayFragment fragment = new PlayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play, container, false);
//        isGameBegin = false;
//        isAirFlowMonitoring = false;
//        isHeartMonitoring = false;
//        goUp = 0;
//        mExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
//        mCalibrationScreen = (FrameLayout) v.findViewById(R.id.calibrating);
        mSurfaceView = (ExtendGLSurfaceView) v.findViewById(R.id.glsurfaceview);
        mFrameLayout = (FrameLayout) v.findViewById(R.id.pauseview);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mGameRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        delegateCamera();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayFragmentInteractionListener) {
            mListener = (OnPlayFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    Bitmap bitmap = (Bitmap) extra.get("data");
                    ExtendRenderer.setRawData(bitmap);
                } else {
                    ExtendRenderer.setRawData(null);
                }
//                calibrate();
                break;
        }
    }

    private void delegateCamera() {
        Intent takePicture = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

//    private void calibrate(){
//        mSurfaceView.onPause();
//        mListener.resumeService();
//        isAirFlowMonitoring = false;
//        isHeartMonitoring = false;
//        isGameBegin = false;
//        mCalibrationScreen.setVisibility(View.VISIBLE);
//        mExecutor.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                if (isHeartMonitoring && isAirFlowMonitoring) {
//                    isGameBegin = true;
//                    endCalibration();
//                }
//            }
//        }, 0, 500, TimeUnit.MILLISECONDS);
//    }
//
//    private void endCalibration() {
//        mExecutor.shutdown();
//        mExecutor = null;
//        mCalibrationScreen.setVisibility(View.GONE);
//        mSurfaceView.onResume();
//        mListener.resumeService();
//    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (isGameBegin) {
            switch(sensorEvent.sensor.getType()) {
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorEvent.values);
                    SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

                    if (15 > Math.abs(mOrientationAngles[2]*180/Math.PI)) {
                        mFrameLayout.setVisibility(View.VISIBLE);
                        mListener.pauseService();
                        mSurfaceView.onPause();
                    } else if (15 < Math.abs(mOrientationAngles[2]*180/Math.PI)) {
                        mFrameLayout.setVisibility(View.GONE);
                        mListener.resumeService();
                        mSurfaceView.onResume();
                    }
                    break;
            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mListener.pauseService();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isGameBegin) {
            mSurfaceView.onResume();
            mListener.resumeService();
            mSensorManager.registerListener(this, mGameRotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
//        } else {
//            calibrate();
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void goUp () {
        if (mSurfaceView != null) {
            mSurfaceView.goUp();
//            goUp++;
//            if (goUp == 1) {
//                isAirFlowMonitoring = true;
//            }
        }
    }

    public void goDown() {
        if (mSurfaceView != null) {
            mSurfaceView.goDown();
        }
    }

    public void addObstacle() {
        if (mSurfaceView != null) {
            mSurfaceView.addObstacle();
//            isHeartMonitoring = true;
        }
    }

    public interface OnPlayFragmentInteractionListener {
        void pauseService();
        void resumeService();
    }
}
