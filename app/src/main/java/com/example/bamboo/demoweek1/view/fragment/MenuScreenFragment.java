package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendButton;
import com.example.bamboo.demoweek1.view.extended.ExtendOnClickListener;

//Olli
public class MenuScreenFragment extends android.app.Fragment {

    private SoundInterface mActivity;
    private OnMenuFragmentInteractionListener mListener;
    private ExtendButton mPlay, mAbout, mGuide, mTutorial;

    public MenuScreenFragment() {
        // Required empty public constructor
    }

    public static MenuScreenFragment newInstance() {
        MenuScreenFragment fragment = new MenuScreenFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu_screen, container, false);
        // Add buttons to play, about, tutorial and instruction fragments and set listeners to all of them that extend SoundInterface
        mPlay = (ExtendButton) v.findViewById(R.id.play);
        mAbout = (ExtendButton) v.findViewById(R.id.about);
        mGuide = (ExtendButton) v.findViewById(R.id.tutorial);
        mTutorial = (ExtendButton) v.findViewById(R.id.instruction);
        mTutorial.setOnClickListener(new ExtendOnClickListener(mActivity) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.instructionButtonPressed();
            }
        });
        mPlay.setOnClickListener(new ExtendOnClickListener(mActivity) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.playButtonPressed();
            }
        });
        mAbout.setOnClickListener(new ExtendOnClickListener(mActivity) {
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.aboutButtonPressed();
            }
        });
        mGuide.setOnClickListener(new ExtendOnClickListener(mActivity){
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.guideButtonPressed();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMenuFragmentInteractionListener) {
            mListener = (OnMenuFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof SoundInterface) {
            mActivity = (SoundInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    public interface OnMenuFragmentInteractionListener {
        void playButtonPressed();
        void guideButtonPressed();
        void aboutButtonPressed();
        void instructionButtonPressed();
    }
}
