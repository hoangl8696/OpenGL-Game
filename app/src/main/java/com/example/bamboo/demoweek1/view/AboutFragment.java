package com.example.bamboo.demoweek1.view;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;

public class AboutFragment extends android.app.Fragment {
    private OnAboutFragmentInteractionListener mListener;
    private TextView txt1,txt2,txt3;

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
        txt1 = (TextView) v.findViewById(R.id.gamename2);
        txt2 = (TextView) v.findViewById(R.id.aboutus);
        txt3 = (TextView) v.findViewById(R.id.textView5);
        txt1.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
        txt2.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
        txt3.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/abc.ttf"));
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
