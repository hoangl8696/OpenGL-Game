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

public class AboutFragment extends android.app.Fragment {
    private OnAboutFragmentInteractionListener mListener;
    private ImageButton btn;
    private SoundInterface mSound;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        btn = (ImageButton) v.findViewById(R.id.back_buton);
        btn.setOnClickListener(new ExtendOnClickListener(mSound){
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.aboutBackPressed();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAboutFragmentInteractionListener) {
            mListener = (OnAboutFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof SoundInterface) {
            mSound = (SoundInterface) context;
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

    public interface OnAboutFragmentInteractionListener {
        void aboutBackPressed();
    }
}
