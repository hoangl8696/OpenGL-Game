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

import com.example.bamboo.demoweek1.service.BluetoothConnectionService;
import com.example.bamboo.demoweek1.service.SoundService;
import com.example.bamboo.demoweek1.view.fragment.AboutFragment;
import com.example.bamboo.demoweek1.view.fragment.CalibrationFragment;
import com.example.bamboo.demoweek1.view.fragment.DialogFragment;
import com.example.bamboo.demoweek1.view.fragment.GuideFragment;
import com.example.bamboo.demoweek1.view.fragment.InstructionFragment;
import com.example.bamboo.demoweek1.view.fragment.MenuScreenFragment;
import com.example.bamboo.demoweek1.view.fragment.PlayFragment;

public class MainActivity extends AppCompatActivity implements InstructionFragment.OnInstructionFragmentInteractionListener, SoundInterface, AboutFragment.OnAboutFragmentInteractionListener, GuideFragment.OnGuideFragmentInteractionListener, CalibrationFragment.OnCalibrationFragmentInteractionListener, PlayFragment.OnPlayFragmentInteractionListener, BluetoothConnectionService.SensorResult, MenuScreenFragment.OnMenuFragmentInteractionListener {

    public static boolean OFFLINE_FLAG = false;
    private static boolean JUMP_FLAG = false;

    private long mLastClickTime;

    private Intent intent;

    public static final String NEW_AUDIO = "com.example.bamboo.demoweek1.NewAudio";

    private BluetoothConnectionService mBluetoothService;
    private SoundService mSoundService;

    private PlayFragment playFragment;
    private CalibrationFragment calibrationFragment;
    private GuideFragment guideFragment;
    private AboutFragment aboutFragment;
    private InstructionFragment instructionFragment;

    private int up;

    private IntentFilter mFilter;

    private BatteryMonitoringBroadcastReceiver mReceiver;

    private boolean mBound = false;
    private boolean mSoundBound = false;
    private boolean mIsBRRegistered = false;
    private boolean mIsCharging = false;

    private boolean mIsPlaying = false;

    private ServiceConnection mConnection;

    private ServiceConnection mSoundConnection;


    //Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        menuScreenTransaction();
        setUpBR();

        mConnection = new ServiceConnection() {
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

        mSoundConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                SoundService.LocalBinder binder = (SoundService.LocalBinder) iBinder;
                mSoundService = binder.getService();
                mSoundBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mSoundBound = false;
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mIsPlaying) {
            showExitDialog();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                showExitDialog();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBR();
        pauseService();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBR();
        resumeService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("Service state", mBound);
        outState.putBoolean("Sound Service state", mSoundBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBound = savedInstanceState.getBoolean("Service state");
        mSoundBound = savedInstanceState.getBoolean("Sound Service state");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            mBluetoothService = null;
            stopService(intent);
        }
        if (mSoundBound) {
            unbindService(mSoundConnection);
            mSoundBound = false;
            mSoundService.stopSelf();
            mSoundService = null;
        }
    }

    //Activity helper methods
    public void playAudio (String path) {
        if (!mSoundBound) {
            Intent playIntent = new Intent (this, SoundService.class);
            playIntent.putExtra("media", path);
            startService(playIntent);
            bindService(playIntent, mSoundConnection, Context.BIND_AUTO_CREATE);
        } else {
            Intent newSoundIntent = new Intent (NEW_AUDIO);
            newSoundIntent.putExtra("media", path);
            sendBroadcast(newSoundIntent);
        }
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

    //Method for updating the battery status
    public void receiver (int status, float percentage) {
        if (status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB) {
            mIsCharging = true;
        } else {
            mIsCharging = false;
        }
    }

    private void startService() {
        if (!mBound) {
            intent = new Intent(this, BluetoothConnectionService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    private void showDieDialog() {
        final String DIALOG_TITLE = "You Die!";
        final String DIALOG_DESCRIPTION = "Do you want to retry or exit the game?";
        final String DIALOG_POS_BTN = "Retry";
        final String DIALOG_NEG_BTN = "Exit";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("die dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                playScreenTransaction(-1);
            }
            @Override
            public void onNegativeButtonPressed() {
                finish();
            }
        }, MainActivity.this);
        dialogFragment.show(manager, "die dialog");
    }

    private void showOfflineDialog() {
        final String DIALOG_TITLE = "Offline mode!";
        final String DIALOG_DESCRIPTION = "Do you want to use offline mode? You don't need to be plugged in";
        final String DIALOG_POS_BTN = "Yes";
        final String DIALOG_NEG_BTN = "No";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("offline dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                showCameraDialog();
                OFFLINE_FLAG = true;
            }
            @Override
            public void onNegativeButtonPressed() {
                OFFLINE_FLAG = false;
                if (mIsCharging) {
                    startService();
                    calibrationScreenTransaction();
                } else {
                    showPlayDialog();
                }
            }
        }, MainActivity.this);
        dialogFragment.show(manager, "offline dialog");
    }

    private void showCameraDialog() {
        final String DIALOG_TITLE = "Game is ready!";
        final String DIALOG_DESCRIPTION = "Do you also want to take a picture, this will be used to customize your cube";
        final String DIALOG_POS_BTN = "Picture";
        final String DIALOG_NEG_BTN = "Default";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("camera dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                playScreenTransaction(0);
            }
            @Override
            public void onNegativeButtonPressed() {
                playScreenTransaction(-1);
            }
        }, MainActivity.this);
        dialogFragment.show(manager, "camera dialog");
    }

    private void showPlayDialog() {
        final String DIALOG_TITLE = "Not plugged in!";
        final String DIALOG_DESCRIPTION = "This game is battery intensive, plug your phone to a power source";
        final String DIALOG_POS_BTN = "Retry";
        final String DIALOG_NEG_BTN = "Cancel";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("plug in dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                playButtonPressed();
            }
            @Override
            public void onNegativeButtonPressed() {
                //Dismiss, nothing happen
            }
        }, MainActivity.this);
        dialogFragment.show(manager, "plug in dialog");
    }

    private void showExitDialog() {
        final String DIALOG_TITLE = "Exiting";
        final String DIALOG_DESCRIPTION = "Quit the game?";
        final String DIALOG_POS_BTN = "Yes";
        final String DIALOG_NEG_BTN = "No";
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("exit dialog");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
        DialogFragment dialogFragment = DialogFragment.newInstance(DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_POS_BTN, DIALOG_NEG_BTN);
        dialogFragment.setListener(new DialogFragment.OnDialogFragmentInteractionListener() {
            @Override
            public void onPositiveButtonPressed() {
                finish();
            }
            @Override
            public void onNegativeButtonPressed() {
                //Dismiss, nothing happen
            }
        }, MainActivity.this);
        dialogFragment.show(manager, "exit dialog");
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

    private void instructionScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        instructionFragment = InstructionFragment.newInstance();
        ft.replace(R.id.container, instructionFragment, "Instruciton Transaction");
        ft.addToBackStack("Instruction Transaction");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void playScreenTransaction(int isCamera) {
        mIsPlaying = true;
        if (isCamera == 0) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            playFragment = PlayFragment.newInstance(true);
            ft.replace(R.id.container, playFragment);
            ft.commit();
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            playFragment = PlayFragment.newInstance(false);
            ft.replace(R.id.container, playFragment);
            ft.commit();
        }
    }

    private void guideScreenTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        guideFragment = GuideFragment.newInstance();
        ft.replace(R.id.container, guideFragment, "Guide Transaction");
        ft.addToBackStack("Guide Transaction");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    //Methods from interfaces

    //Post data directly to playfragment for controlling character and calibration fragment for checking
    @Override
    public void airflowStreaming(int data) {
        if (calibrationFragment != null) {
            calibrationFragment.airflowData(data);
        }
        if (playFragment != null) {
            playFragment.airflowData(data);
        }
    }

    @Override
    public void pulseStreaming(final int data) {
        if (calibrationFragment != null) {
            calibrationFragment.isHeartMonitoring(true);
            calibrationFragment.heartData(data);
        }
        if (playFragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playFragment.pulseData(data);
                }
            });
        }
    }

    //Check method for calibration fragment's airflow data, register 10 deep breaths before the calibration become valid
    //Control method for character in play fragment
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
            if (!JUMP_FLAG) {
                playAudio("jump");
                JUMP_FLAG = true;
            }
        }
    }

    //Control method for character in play fragment
    @Override
    public void goDown() {
        if (playFragment != null) {
            if (JUMP_FLAG)
            playFragment.goDown();
            JUMP_FLAG = false;
        }
    }

    //Control method for adding obstacle
    @Override
    public void addObstacle() {
        if (playFragment != null) {
            playFragment.addObstacle();
        }
    }

    //Pause service when phone is lying horizontally
    @Override
    public void pauseService() {
        if (mBluetoothService != null) {
            mBluetoothService.pauseService();
        }
    }

    //Resume service when phone in standing position
    @Override
    public void resumeService() {
        if (mBluetoothService != null) {
            mBluetoothService.unpauseService();
        }
    }

    //Method call when you die
    @Override
    public void youDie() {
        showDieDialog();
    }

    //Restart service when the app get start
    @Override
    public void restartService() {
        startService();
    }

    //Callback when calibration is finished
    @Override
    public void calibrate(boolean i) {
        if (i) {
            showCameraDialog();
        } else {
            showOfflineDialog();
        }
    }

    //Method for setting live data stream's RHR
    @Override
    public void setRestingPulse(final int data) {
        if (playFragment!=null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playFragment.setRestingPulse(data);
                }
            });
        }
    }

    @Override
    public void instructionBackPressed() {
        menuScreenTransaction();
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
        showOfflineDialog();
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
    public void instructionButtonPressed() {
        instructionScreenTransaction();
    }

    @Override
    public void playJump() {
        playAudio("jump");
    }

    @Override
    public void playLand() {
        playAudio("landing");
    }

    @Override
    public void playClick() {
        playAudio("buttonpress");
    }

    @Override
    public void playCollide() {
        long lastClickTime = mLastClickTime;
        long now = System.currentTimeMillis();
        mLastClickTime = now;
        if (now - lastClickTime < 100) {
            // Too fast: ignore
        } else {
            // Register the click
            playAudio("collide");
        }
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
