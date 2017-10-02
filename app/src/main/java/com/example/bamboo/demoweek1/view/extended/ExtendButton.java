package com.example.bamboo.demoweek1.view.extended;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class ExtendButton extends android.support.v7.widget.AppCompatButton {
    public ExtendButton(Context context) {
        super(context);
        setUp();
    }

    public ExtendButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public ExtendButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp();
    }

    private void setUp () {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/abc.ttf"));
    }
}