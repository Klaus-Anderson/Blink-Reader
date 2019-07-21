package com.woods.blinkreader.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woods.blinkreader.R;

import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingFragment extends Fragment {

    private String toReadString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturnView = inflater.inflate(R.layout.fragment_reading, container, false);
        Bundle argument = getArguments();
        toReadString = argument.getString(READING_FRAGMENT_TEXT_KEY);
        return toReturnView;
    }
}
