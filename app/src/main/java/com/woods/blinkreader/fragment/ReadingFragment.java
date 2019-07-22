package com.woods.blinkreader.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.woods.blinkreader.R;
import com.woods.blinkreader.databinding.FragmentReadingBinding;

import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingFragment extends Fragment {

    private String toReadString;
    private MutableLiveData<String> displayStringLiveData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturnView = inflater.inflate(R.layout.fragment_reading, container, false);
        Bundle arguments = getArguments();
        if(arguments != null) {
            toReadString = arguments.getString(READING_FRAGMENT_TEXT_KEY);
            FragmentReadingBinding fragmentReadingBinding = DataBindingUtil.getBinding(toReturnView);
            if(fragmentReadingBinding != null) {
                fragmentReadingBinding.setLifecycleOwner(this);
                fragmentReadingBinding.setTextToBeRead(displayStringLiveData);
            }

            // Inflate view and obtain an instance of the binding class.
//         binding = DataBindingUtil.getBinding(toReturnView);

            // Specify the current activity as the lifecycle owner.
//        binding.setLifecycleOwner(this);
        }
        return toReturnView;
    }
}
