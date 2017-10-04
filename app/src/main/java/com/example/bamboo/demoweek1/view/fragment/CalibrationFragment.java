package com.example.bamboo.demoweek1.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.SoundInterface;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class CalibrationFragment extends android.app.Fragment {
    private static final int WAIT_TIME_MILLISECONDS = 60000;

    private OnCalibrationFragmentInteractionListener mListener;
    private SoundInterface mActivity;
    private boolean mHeartMonitoring;
    private boolean mAirflowMonitoring;
    private boolean isCalled = false;

    private ImageButton btn;

    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;
    private Runnable mTimer2;
    private final Handler mHandler = new Handler();

    private int mAirflowData = 0;

    private TextView mPulse;

    private Button mButton;

    private Runnable mCheckStatus;
    private final Handler mCheckStatusHandler = new Handler();

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
                mActivity.playClick();
            }
        });

        mPulse = (TextView) v.findViewById(R.id.pulse_view);

        mButton = (Button) v.findViewById(R.id.offline_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.calibrate(false);
                mActivity.playClick();
            }
        });
        return v;
    }

    private void showOfflineModeDialog () {
        final String DIALOG_TITLE = "Use offline mode?";
        final String DIALOG_DESCRIPTION = "It seems that calibration took too long, do you want to keep calibrating or use offline mode?";
        final String DIALOG_POS_BTN = "Keep calibrating";
        final String DIALOG_NEG_BTN = "Offline mode";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("offline mode dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                //Dismiss, nothing happen
                mActivity.playClick();
            }
            @Override
            public void onNegativeButtonPressed() {
                mActivity.playClick();
                mListener.calibrate(false);
            }
        });
        dialogFragment.show(manager, "offline mode dialog");
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
    public void onResume() {
        super.onResume();
        drawGraph();
        checkStatus();
    }

    private void checkStatus() {
        mCheckStatus = new Runnable() {
            @Override
            public void run(){
                showOfflineModeDialog();
                mCheckStatusHandler.postDelayed(this, WAIT_TIME_MILLISECONDS);
            }
        };
        mCheckStatusHandler.postDelayed(mCheckStatus, WAIT_TIME_MILLISECONDS);
    }

    private void drawGraph() {
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
        mCheckStatusHandler.removeCallbacks(mCheckStatus);
        super.onPause();
    }


    public void isHeartMonitoring (boolean monitoring) {
        mHeartMonitoring = monitoring;
        if (mAirflowMonitoring) {
            if (!isCalled) {
                isCalled = true;
                mListener.calibrate(true);
            }
        }
    }

    public void isAirflowMonitoring (boolean monitoring) {
        mAirflowMonitoring = monitoring;
        if (mHeartMonitoring) {
            if (!isCalled) {
                isCalled = true;
                mListener.calibrate(true);
            }
        }
    }

    public void heartData (int data) {
        if (mPulse != null) {
            mPulse.setText("Pulse: "+Integer.toString(data)+" Bpm");
        }
    }

    public void airflowData (int data) {
        Log.d("DEBUG", "AIRFLOWDATA: " + Integer.toString(data));
        mAirflowData = (int) Math.floor(data / 10);
    }

    public interface OnCalibrationFragmentInteractionListener {
        void calibrate(boolean i);
        void calibrationBackPressed();
    }
}
