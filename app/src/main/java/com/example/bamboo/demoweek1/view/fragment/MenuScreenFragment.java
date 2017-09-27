package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;

public class MenuScreenFragment extends android.app.Fragment {

    private OnMenuFragmentInteractionListener mListener;
    private Button mPlay, mAbout, mGuide;

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
        mPlay = (Button) v.findViewById(R.id.play);
        mAbout = (Button) v.findViewById(R.id.about);
        mGuide = (Button) v.findViewById(R.id.tutorial);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.playButtonPressed();
            }
        });
        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.aboutButtonPressed();
            }
        });
        mGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMenuFragmentInteractionListener {
        void playButtonPressed();
        void guideButtonPressed();
        void aboutButtonPressed();
    }
}
