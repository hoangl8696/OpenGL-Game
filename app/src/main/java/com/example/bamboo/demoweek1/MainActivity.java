package com.example.bamboo.demoweek1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.bamboo.demoweek1.service.BluetoothConnectionService;
import com.example.bamboo.demoweek1.view.fragment.AboutFragment;
import com.example.bamboo.demoweek1.view.fragment.CalibrationFragment;
import com.example.bamboo.demoweek1.view.fragment.DialogFragment;
import com.example.bamboo.demoweek1.view.fragment.GuideFragment;
import com.example.bamboo.demoweek1.view.fragment.MenuScreenFragment;
import com.example.bamboo.demoweek1.view.fragment.PlayFragment;


public class MainActivity extends AppCompatActivity implements DialogFragment.OnDialogFragmentInteractionListener, AboutFragment.OnAboutFragmentInteractionListener, GuideFragment.OnGuideFragmentInteractionListener, CalibrationFragment.OnCalibrationFragmentInteractionListener, PlayFragment.OnPlayFragmentInteractionListener, BluetoothConnectionService.SensorResult, MenuScreenFragment.OnMenuFragmentInteractionListener {
    private static final String DIALOG_TITLE = "Not pluged in";
    private static final String DIALOG_DESCRIPTION = "This game is battery intensive, please plug your phone to a power source";
    private static final String DIALOG_POS_BTN = "Retry";
    private static final String DIALOG_NEG_BTN = "Cancel";

    private BluetoothConnectionService mBluetoothService;
    private PlayFragment playFragment;
    private CalibrationFragment calibrationFragment;
    private GuideFragment guideFragment;
    private AboutFragment aboutFragment;
    private int up;
    private IntentFilter mFilter;
    private BatteryMonitoringBroadcastReceiver mReceiver;

    private boolean mBound = false;
    private boolean mIsBRRegistered = false;
    private boolean mIsCharging = false;

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
        setUpBR();
    }

    private void setUpBR () {
        mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mReceiver = new BatteryMonitoringBroadcastReceiver();
    }

    private void registerBR() {
        if (!mIsBRRegistered) {
            registerReceiver(mReceiver,mFilter);
            mIsBRRegistered = true;
        }
    }

    private void unregisterBR() {
        if (mIsBRRegistered) {
            unregisterReceiver(mReceiver);
            mIsBRRegistered = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void menuScreenTransaction() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            MenuScreenFragment screenFragment = MenuScreenFragment.newInstance();
            ft.replace(R.id.container, screenFragment);
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void calibrationScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        calibrationFragment = CalibrationFragment.newInstance();
        ft.replace(R.id.container, calibrationFragment, "Calibration Transaction");
        ft.addToBackStack("Calibration Transaction");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void aboutScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        aboutFragment = AboutFragment.newInstance();
        ft.replace(R.id.container, aboutFragment, "About Transaction");
        ft.addToBackStack("About Transaction");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void guideScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        guideFragment = GuideFragment.newInstance();
        ft.replace(R.id.container, guideFragment, "Guide Transaction");
        ft.addToBackStack("Guide Transaction");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void receiver (int status, float percentage) {
        if (status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB) {
            mIsCharging = true;
        } else {
            mIsCharging = false;
        }
    }

    @Override
    public void airflowStreaming(int data) {
        if (calibrationFragment != null) {
            calibrationFragment.airflowData(data);
        }
    }

    @Override
    public void pulseStreaming(int data) {
        if (calibrationFragment != null) {
            calibrationFragment.isHeartMonitoring(true);
            calibrationFragment.heartData(data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBR();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBR();
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
    public void calibrationBackPressed() {
        menuScreenTransaction();
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
        if (mIsCharging) {
            calibrationScreenTransaction();
        } else {
            FragmentManager manager = getFragmentManager();
            Fragment fragment = manager.findFragmentByTag("dialog");
            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commit();
            }
            DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
            dialogFragment.show(manager, "dialog");
        }
    }

    @Override
    public void guideButtonPressed() {
        guideScreenTransaction();
    }

    @Override
    public void aboutButtonPressed() {
        aboutScreenTransaction();
    }

    @Override
    public void onPositiveButtonPressed() {
        playButtonPressed();
    }

    @Override
    public void onNegativeButtonPressed() {
        //Dismiss the dialog, nothing happen
    }

    private class BatteryMonitoringBroadcastReceiver extends BroadcastReceiver {
        private float batteryPct;

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level / (float)scale * 100;
            receiver(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1), batteryPct);
        }
    }
}
