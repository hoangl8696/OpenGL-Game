package com.example.bamboo.demoweek1.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.bamboo.demoweek1.MainActivity;

public class SoundService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{
    private final IBinder ibinder = new LocalBinder();
    private MediaPlayer mPlayer;
    private String mMediaPath;

    private BroadcastReceiver  newAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayer.reset();
            try {
                //An audio file is passed to the service through putExtra();
                mMediaPath = "android.resource://" + getPackageName() + "/raw/" + intent.getExtras().getString("media");
            } catch (NullPointerException e) {
                stopSelf();
            }
            if (mMediaPath != null && !mMediaPath.isEmpty()) {
                initializePlayer();
            }
        }
    };

    public SoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerBr();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //An audio file is passed to the service through putExtra();
            mMediaPath = "android.resource://" + this.getPackageName() + "/raw/" + intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }
        if (mMediaPath != null && !mMediaPath.isEmpty()) {
            initializePlayer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer!=null) {
            mPlayer.release();
        }
        unregisterReceiver(newAudioReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ibinder;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    private void initializePlayer() {
        mPlayer = new MediaPlayer();
        switch (mMediaPath) {
            case "android.resource://com.example.bamboo.demoweek1/raw/buttonpress":
                mPlayer.setVolume(0.25f,0.25f);
                break;
            case "android.resource://com.example.bamboo.demoweek1/raw/collide":
                mPlayer.setVolume(1.0f,1.0f);
                break;
            case "android.resource://com.example.bamboo.demoweek1/raw/jump":
                mPlayer.setVolume(0.15f,0.15f);
                break;
            case "android.resource://com.example.bamboo.demoweek1/raw/landing":
                mPlayer.setVolume(1.0f,1.0f);
                break;
        }
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.reset();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(this, Uri.parse(mMediaPath));
        } catch (Exception e) {
            stopSelf();
        }
        mPlayer.prepareAsync();
    }

    public class LocalBinder extends Binder {
        public SoundService getService() {
            return SoundService.this;
        }
    }

    private void registerBr() {
        IntentFilter filter = new IntentFilter(MainActivity.NEW_AUDIO);
        registerReceiver(newAudioReceiver, filter);
    }
}
