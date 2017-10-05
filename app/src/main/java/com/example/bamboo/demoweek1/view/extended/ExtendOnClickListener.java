package com.example.bamboo.demoweek1.view.extended;

import android.view.View;

import com.example.bamboo.demoweek1.SoundInterface;

public class ExtendOnClickListener implements View.OnClickListener {
    private SoundInterface mSound;

    public ExtendOnClickListener (SoundInterface sound) {
        mSound = sound;
    }

    @Override
    public void onClick(View view) {
        mSound.playClick();
    }
}
