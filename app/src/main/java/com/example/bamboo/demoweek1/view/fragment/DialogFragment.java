package com.example.bamboo.demoweek1.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bamboo.demoweek1.R;

public class DialogFragment extends android.app.DialogFragment {
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String BUTTON_POSITIVE = "postive button";
    private static final String BUTTON_NEGATIVE = "negative button";

    private String mTitle;
    private String mDescription;
    private String mPostiveBtn;
    private String mNegativeBtn;

    private TextView mTitleText, mDescriptionText;
    private Button mPosBtn, mNegBtn;

    private OnDialogFragmentInteractionListener mListener;

    public DialogFragment() {
        // Required empty public constructor
    }

    public static DialogFragment newInstance(String title, String description, String postiveBtn, String negativeBtn) {
        DialogFragment fragment = new DialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(DESCRIPTION, description);
        args.putString(BUTTON_NEGATIVE, negativeBtn);
        args.putString(BUTTON_POSITIVE, postiveBtn);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
            mDescription = getArguments().getString(DESCRIPTION);
            mPostiveBtn = getArguments().getString(BUTTON_POSITIVE);
            mNegativeBtn = getArguments().getString(BUTTON_NEGATIVE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleText = (TextView) view.findViewById(R.id.dialog_title);
        mDescriptionText = (TextView) view.findViewById(R.id.dialog_description);
        mNegBtn = (Button) view.findViewById(R.id.negative_btn);
        mPosBtn = (Button) view.findViewById(R.id.positive_btn);
        mPosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    dismiss();
                    mListener.onPositiveButtonPressed();
                }
            }
        });
        mNegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    dismiss();
                    mListener.onNegativeButtonPressed();
                }
            }
        });
        mTitleText.setText(mTitle);
        mDescriptionText.setText(mDescription);
        mNegBtn.setText(mNegativeBtn);
        mPosBtn.setText(mPostiveBtn);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDialogFragmentInteractionListener) {
            mListener = (OnDialogFragmentInteractionListener) context;
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

    public interface OnDialogFragmentInteractionListener {
        void onPositiveButtonPressed();
        void onNegativeButtonPressed();
    }
}
