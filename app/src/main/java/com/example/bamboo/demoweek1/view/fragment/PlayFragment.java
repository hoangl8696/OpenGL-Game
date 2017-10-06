package com.example.bamboo.demoweek1.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendGLSurfaceView;
import com.example.bamboo.demoweek1.view.extended.ExtendRenderer;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static android.content.Context.SENSOR_SERVICE;
import static com.example.bamboo.demoweek1.MainActivity.OFFLINE_FLAG;

public class PlayFragment extends android.app.Fragment implements SensorEventListener, ExtendRenderer.HealthControl {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String CAMERA_REQUEST = "camera";

    public static float mHealthValue = 5;

    private boolean mIsCamera;

    private ExtendGLSurfaceView mSurfaceView;

    private FrameLayout mFrameLayout;

    private FrameLayout mContainer;

    private RatingBar mHealth;

    private TextView mHeartSignals, mRhrText;
    private int mAirflowData = 0;

    private SensorManager mSensorManager;
    private Sensor mGameRotationVectorSensor;

    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;
    private Runnable mTimer2;
    private final Handler mHandler = new Handler();

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private OnPlayFragmentInteractionListener mListener;
    private SoundInterface mActivity;

    public PlayFragment() {
        // Required empty public constructor
    }

    public static PlayFragment newInstance(boolean isCamera) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putBoolean(CAMERA_REQUEST, isCamera);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsCamera = getArguments().getBoolean(CAMERA_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play, container, false);
        mSurfaceView = (ExtendGLSurfaceView) v.findViewById(R.id.glsurfaceview);
        mFrameLayout = (FrameLayout) v.findViewById(R.id.pauseview);
        mContainer = (FrameLayout) v.findViewById(R.id.live_data_container);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mGameRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mHeartSignals = (TextView) v.findViewById(R.id.heart_signals);
        mRhrText = (TextView) v.findViewById(R.id.rhr_text);
        mHealth = (RatingBar) v.findViewById(R.id.health_bar);
        mHealthValue = 5;
        mHealth.setRating(mHealthValue);
        if (mIsCamera) {
            delegateCamera();
        } else {
            ExtendRenderer.setRawData(null);
        }
        GraphView graph2 = (GraphView) v.findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        graph2.getGridLabelRenderer().setTextSize(0.0f);
        if (OFFLINE_FLAG) {
            mContainer.setVisibility(View.GONE);
        }
        mSurfaceView.setSoundInterface(mActivity);
        mSurfaceView.setHealthController(this);
        return v;
    }

    public void setRestingPulse (int data) {
        mRhrText.setText("RHR: "+Integer.toString(data));
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
        if (context instanceof SoundInterface) {
            mActivity = (SoundInterface) context;
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
                mListener.restartService();
                if (data != null) {
                    Bundle extra = data.getExtras();
                    Bitmap bitmap = (Bitmap) extra.get("data");
                    ExtendRenderer.setRawData(bitmap);
                } else {
                    ExtendRenderer.setRawData(null);
                }
                break;
        }
    }

    private void delegateCamera() {
        Intent takePicture = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void pulseData(int data) {
        if (mHeartSignals != null) {
            mHeartSignals.setText(Integer.toString(data));
        }
    }

    public void airflowData(int data) {
        mAirflowData = data;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mListener.pauseService();
        mSensorManager.unregisterListener(this);
        mHandler.removeCallbacks(mTimer2);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        mListener.resumeService();
        mSensorManager.registerListener(this, mGameRotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        mTimer2 = new Runnable() {
            @Override
            public void run(){
                graph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(graph2LastXValue, mAirflowData),true,40);
                mHandler.postDelayed(this,200);
            }
        };
        mHandler.postDelayed(mTimer2, 200);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void goUp () {
        if (mSurfaceView != null) {
            mSurfaceView.goUp();
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
        }
    }

    public void decrease() {
        mHealthValue = mHealthValue - 0.5f;
        if (mHealthValue != 0) {
            if (mHealth != null) {
                mHealth.setRating(mHealthValue);
            }
        } else {
            mHealth.setRating(mHealthValue);
            mListener.youDie();
        }
    }

    public interface OnPlayFragmentInteractionListener {
        void pauseService();
        void resumeService();
        void youDie();
        void restartService();
    }
}
