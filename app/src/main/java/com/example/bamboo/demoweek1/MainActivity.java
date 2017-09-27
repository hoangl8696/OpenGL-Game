package com.example.bamboo.demoweek1;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.bamboo.demoweek1.service.BluetoothConnectionService;
import com.example.bamboo.demoweek1.view.fragment.AboutFragment;
import com.example.bamboo.demoweek1.view.fragment.CalibrationFragment;
import com.example.bamboo.demoweek1.view.fragment.GuideFragment;
import com.example.bamboo.demoweek1.view.fragment.MenuScreenFragment;
import com.example.bamboo.demoweek1.view.fragment.PlayFragment;


public class MainActivity extends AppCompatActivity implements AboutFragment.OnAboutFragmentInteractionListener, GuideFragment.OnGuideFragmentInteractionListener, CalibrationFragment.OnCalibrationFragmentInteractionListener, PlayFragment.OnPlayFragmentInteractionListener, BluetoothConnectionService.SensorResult, MenuScreenFragment.OnMenuFragmentInteractionListener {
    private BluetoothConnectionService mBluetoothService;
    private PlayFragment playFragment;
    private CalibrationFragment calibrationFragment;
    private GuideFragment guideFragment;
    private AboutFragment aboutFragment;
    private int up;

    private boolean mBound = false;

    private ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            mBluetoothService = binder.getInstance();
            mBound = true;
            up = 0;
            mBluetoothService.setClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        menuScreenTransaction();
    }

    private void menuScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MenuScreenFragment screenFragment = MenuScreenFragment.newInstance();
        ft.replace(R.id.container, screenFragment);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void calibrationScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        calibrationFragment = CalibrationFragment.newInstance();
        ft.replace(R.id.container, calibrationFragment);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void aboutScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        aboutFragment = AboutFragment.newInstance();
        ft.replace(R.id.container, aboutFragment);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void guideScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        guideFragment = GuideFragment.newInstance();
        ft.replace(R.id.container, guideFragment);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BluetoothConnectionService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void goUp() {
        up++;
        if (up == 10) {
            if (calibrationFragment != null) {
                calibrationFragment.isAirflowMonitoring(true);
            }
        }
        if (playFragment != null) {
            playFragment.goUp();
        }
    }

    @Override
    public void goDown() {
        if (playFragment != null) {
            playFragment.goDown();
        }
    }

    @Override
    public void addObstacle() {
        if (calibrationFragment != null) {
            calibrationFragment.isHeartMonitoring(true);
        }
        if (playFragment != null) {
            playFragment.addObstacle();
        }
    }

    @Override
    public void pauseService() {
        if (mBluetoothService != null) {
            mBluetoothService.pauseService();
        }
    }

    @Override
    public void resumeService() {
        if (mBluetoothService != null) {
            mBluetoothService.unpauseService();
        }
    }

    @Override
    public void calibrate() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        playFragment = PlayFragment.newInstance();
        ft.replace(R.id.container, playFragment);
        ft.commit();
    }

    @Override
    public void aboutBackPressed() {
        menuScreenTransaction();
    }

    @Override
    public void guideBackPressed() {
        menuScreenTransaction();
    }

    @Override
    public void playButtonPressed() {
        calibrationScreenTransaction();
    }

    @Override
    public void guideButtonPressed() {
        guideScreenTransaction();
    }

    @Override
    public void aboutButtonPressed() {
        aboutScreenTransaction();
    }
}
