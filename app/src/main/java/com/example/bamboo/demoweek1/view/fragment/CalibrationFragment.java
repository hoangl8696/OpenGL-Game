package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class CalibrationFragment extends android.app.Fragment {
    private OnCalibrationFragmentInteractionListener mListener;
    private boolean mHeartMonitoring;
    private boolean mAirflowMonitoring;
    private boolean isCalled = false;

    private ImageButton btn;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;
    private Runnable mTimer2;
    private final Handler mHandler = new Handler();

    private int mAirflowData = 0;
    private int mPulseData = 0;

    private TextView mPulse;

    public CalibrationFragment() {
        // Required empty public constructor
    }

    public static CalibrationFragment newInstance() {
        CalibrationFragment fragment = new CalibrationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calibration, container, false);

        // graph creation with bounds
        GraphView graph2 = (GraphView) v.findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(100);

        // back button
        btn = (ImageButton) v.findViewById(R.id.back_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.calibrationBackPressed();
            }
        });

        mPulse = (TextView) v.findViewById(R.id.pulse_view);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalibrationFragmentInteractionListener) {
            mListener = (OnCalibrationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

//    //TODO: remember to delete this after testing
//    @Override
//    public void onResume() {
//        super.onResume();
//        mListener.calibrate();
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer2 = new Runnable() {
            @Override
            public void run(){
                graph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(graph2LastXValue, mAirflowData),true,100);
                mHandler.postDelayed(this,200);
            }
        };
        mHandler.postDelayed(mTimer2, 200);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }


    public void isHeartMonitoring (boolean monitoring) {
        mHeartMonitoring = monitoring;
        if (mAirflowMonitoring) {
            if (!isCalled) {
                isCalled = true;
                mListener.calibrate();
            }
        }
    }

    public void isAirflowMonitoring (boolean monitoring) {
        mAirflowMonitoring = monitoring;
        if (mHeartMonitoring) {
            if (!isCalled) {
                isCalled = true;
                mListener.calibrate();
            }
        }
    }

    public void heartData (int data) {
        if (mPulse != null) {
            mPulse.setText("Pulse: "+Integer.toString(data));
        }
    }

    public void airflowData (int data) {
        Log.d("DEBUG", "AIRFLOWDATA: " + Integer.toString(data));
        mAirflowData = (int) Math.floor(data / 10);
    }

    public interface OnCalibrationFragmentInteractionListener {
        void calibrate();
        void calibrationBackPressed();
    }
}
