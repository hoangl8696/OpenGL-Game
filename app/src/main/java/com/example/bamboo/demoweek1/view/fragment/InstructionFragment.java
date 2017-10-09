package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.bamboo.demoweek1.R;
import com.example.bamboo.demoweek1.SoundInterface;
import com.example.bamboo.demoweek1.view.extended.ExtendOnClickListener;

//Olli
public class InstructionFragment extends android.app.Fragment {
    private ImageButton btn;

    private SoundInterface mSound;
    private OnInstructionFragmentInteractionListener mListener;

    public InstructionFragment() {
        // Required empty public constructor
    }

    public static InstructionFragment newInstance() {
        InstructionFragment fragment = new InstructionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_instruction, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize back button and set listener and extend with SoundInterface
        btn = (ImageButton) view.findViewById(R.id.back_buton);
        btn.setOnClickListener(new ExtendOnClickListener(mSound){
            @Override
            public void onClick(View view) {
                super.onClick(view);
                mListener.instructionBackPressed();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInstructionFragmentInteractionListener) {
            mListener = (OnInstructionFragmentInteractionListener) context;
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
        mSound = null;
    }

    public interface OnInstructionFragmentInteractionListener {
        void instructionBackPressed();
    }
}
