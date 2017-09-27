package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.bamboo.demoweek1.R;

public class GuideFragment extends android.app.Fragment {
    private ImageButton btn;

    private OnGuideFragmentInteractionListener mListener;

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
        btn = (ImageButton) v.findViewById(R.id.back_buton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnGuideFragmentInteractionListener {
        void guideBackPressed();
    }
}
