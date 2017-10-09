package com.example.bamboo.demoweek1.view.extended;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

//Olli
public class ExtendTextView extends android.support.v7.widget.AppCompatTextView {
    public ExtendTextView(Context context) {
        super(context);
        setUp();
    }

    public ExtendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public ExtendTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp();
    }

    private void setUp () {
        // Set the custom font to text view
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/abc.ttf"));
    }
}
