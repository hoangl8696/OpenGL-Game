package com.example.bamboo.demoweek1.view;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;

public class MenuScreenFragment extends android.app.Fragment implements View.OnClickListener{

    public static final int PLAY_BUTTON_CLICKED = 0;
    public static final int ABOUT_BUTTON_CLICKED = 1;
    public static final int GUIDE_BUTTON_CLICKED = 2;

    private OnMenuFragmentInteractionListener mListener;
    private Button mPlay, mAbout, mGuide;
    private TextView mGameName;

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
        mGameName = (TextView) v.findViewById(R.id.gamename);
        mPlay = (Button) v.findViewById(R.id.play);
        mAbout = (Button) v.findViewById(R.id.about);
        mGuide = (Button) v.findViewById(R.id.tutorial);
        mPlay.setOnClickListener(this);
        mAbout.setOnClickListener(this);
        mGuide.setOnClickListener(this);
        mGameName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
        mPlay.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
        mAbout.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
        mGuide.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                mListener.onFragmentInteraction(PLAY_BUTTON_CLICKED);
                break;
            case R.id.about:
                mListener.onFragmentInteraction(ABOUT_BUTTON_CLICKED);
                break;
            case R.id.tutorial:
                mListener.onFragmentInteraction(GUIDE_BUTTON_CLICKED);
                break;
        }
    }

    public interface OnMenuFragmentInteractionListener {
        void onFragmentInteraction(int button);
    }
}
