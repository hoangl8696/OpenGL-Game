package com.example.bamboo.demoweek1.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bamboo.demoweek1.R;

public class CalibrationFragment extends android.app.Fragment {
    private OnCalibrationFragmentInteractionListener mListener;
    private boolean mHeartMonitoring;
    private boolean mAirflowMonitoring;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration, container, false);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void isHeartMonitoring (boolean monitoring) {
        mHeartMonitoring = monitoring;
        if (mAirflowMonitoring) {
            mListener.calibrate();
        }
    }

    public void isAirflowMonitoring (boolean monitoring) {
        mAirflowMonitoring = monitoring;
        if (mHeartMonitoring) {
            mListener.calibrate();
        }
    }

    public interface OnCalibrationFragmentInteractionListener {
        void calibrate();
    }
}
