package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendOnClickListener;

//Olli
public class GuideFragment extends android.app.Fragment {
    private ImageButton btn;

    private OnGuideFragmentInteractionListener mListener;
    private SoundInterface mSound;

    public GuideFragment() {
        // Required empty public constructor
    }

    public static GuideFragment newInstance() {
        GuideFragment fragment = new GuideFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_guide, container, false);
        // initialize back button and set listener
        btn = (ImageButton) v.findViewById(R.id.back_buton);
        btn.setOnClickListener(new ExtendOnClickListener(mSound){
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.guideBackPressed();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGuideFragmentInteractionListener) {
            mListener = (OnGuideFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        // Add sound interface
        if (context instanceof SoundInterface) {
            mSound = (SoundInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SoundInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mSound = null;
    }

    public interface OnGuideFragmentInteractionListener {
        void guideBackPressed();
    }
}
