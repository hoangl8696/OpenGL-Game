package com.example.bamboo.demoweek1;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.example.bamboo.demoweek1.service.BluetoothConnectionService;
import com.example.bamboo.demoweek1.view.AboutFragment;
import com.example.bamboo.demoweek1.view.CalibrationFragment;
import com.example.bamboo.demoweek1.view.ExtendGLSurfaceView;
import com.example.bamboo.demoweek1.view.ExtendRenderer;
import com.example.bamboo.demoweek1.view.GuideFragment;
import com.example.bamboo.demoweek1.view.MenuScreenFragment;
import com.example.bamboo.demoweek1.view.PlayFragment;
import com.libelium.mysignalsconnectkit.BluetoothManagerHelper;
import com.libelium.mysignalsconnectkit.BluetoothManagerService;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerCharacteristicsCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerHelperCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerQueueCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerServicesCallback;
import com.libelium.mysignalsconnectkit.pojo.LBSensorObject;
import com.libelium.mysignalsconnectkit.utils.BitManager;
import com.libelium.mysignalsconnectkit.utils.LBValueConverter;
import com.libelium.mysignalsconnectkit.utils.StringConstants;
import com.libelium.mysignalsconnectkit.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        setContentView(R.layout.activity_main);
        menuScreenTransaction();
    }

    private void menuScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MenuScreenFragment screenFragment = MenuScreenFragment.newInstance();
        ft.replace(R.id.container, screenFragment);
        ft.commit();
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
    public void onFragmentInteraction(int button) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (button) {
            case MenuScreenFragment.PLAY_BUTTON_CLICKED:
                calibrationFragment = CalibrationFragment.newInstance();
                ft.replace(R.id.container, calibrationFragment);
                ft.commit();
                break;
            case MenuScreenFragment.ABOUT_BUTTON_CLICKED:
                aboutFragment = AboutFragment.newInstance();
                ft.replace(R.id.container, aboutFragment);
                ft.commit();
                break;
            case MenuScreenFragment.GUIDE_BUTTON_CLICKED:
                guideFragment = GuideFragment.newInstance();
                ft.replace(R.id.container, guideFragment);
                ft.commit();
                break;
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

    }

    @Override
    public void guideBackPressed() {

    }
}
