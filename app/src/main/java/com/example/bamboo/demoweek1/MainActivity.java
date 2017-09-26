package com.example.bamboo.demoweek1;

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
import com.example.bamboo.demoweek1.view.ExtendGLSurfaceView;
import com.example.bamboo.demoweek1.view.ExtendRenderer;
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

public class MainActivity extends AppCompatActivity implements BluetoothConnectionService.SensorResult, SensorEventListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ExtendGLSurfaceView mSurfaceView;

    private FrameLayout mFrameLayout;

    private SensorManager mSensorManager;
    private Sensor mGameRotationVectorSensor;

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private BluetoothConnectionService mBluetoothService;

    private boolean mBound = false;

    private ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            mBluetoothService = binder.getInstance();
            mBound = true;
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
        mSurfaceView = (ExtendGLSurfaceView) findViewById(R.id.glsurfaceview);
        mFrameLayout = (FrameLayout) findViewById(R.id.pauseview);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGameRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        delegateCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                Bundle extra = data.getExtras();
                Bitmap bitmap = (Bitmap) extra.get("data");
                ExtendRenderer.setRawData(bitmap);
                break;
        }
    }

    private void delegateCamera() {
        Intent takePicture = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorEvent.values);
                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

                if (15 > Math.abs(mOrientationAngles[2]*180/Math.PI)) {
                    mFrameLayout.setVisibility(View.VISIBLE);
                    if (mBluetoothService != null) {
                        mBluetoothService.pauseService();
                    }
                    mSurfaceView.onPause();
                } else if (15 < Math.abs(mOrientationAngles[2]*180/Math.PI)) {
                    mFrameLayout.setVisibility(View.GONE);
                    if (mBluetoothService != null) {
                        mBluetoothService.unpauseService();
                    }
                    mSurfaceView.onResume();
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BluetoothConnectionService.class);
        mSurfaceView.onResume();
        mSensorManager.registerListener(this, mGameRotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
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
        mSurfaceView.goUp();
    }

    @Override
    public void goDown() {
        mSurfaceView.goDown();
    }

    @Override
    public void addObstacle() {
        mSurfaceView.addObstacle();
    }
}
