package com.woods.blinkreader.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woods.blinkreader.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadingFragment extends Fragment {

    public ReadingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading, container, false);
    }
}
