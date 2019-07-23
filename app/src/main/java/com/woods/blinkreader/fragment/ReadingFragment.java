package com.woods.blinkreader.fragment;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woods.blinkreader.R;
import com.woods.blinkreader.databinding.FragmentReadingBinding;
import com.woods.blinkreader.viewmodel.ReadingViewModel;

import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingFragment extends Fragment {

    private String toReadString;
    private ReadingViewModel readingViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Obtain the ViewModel component.

        View toReturnView = inflater.inflate(R.layout.fragment_reading, container, false);
        Bundle arguments = getArguments();
        readingViewModel = ViewModelProviders.of(getActivity())
                .get(ReadingViewModel.class);
        if(arguments != null) {
            toReadString = arguments.getString(READING_FRAGMENT_TEXT_KEY);
            FragmentReadingBinding fragmentReadingBinding = DataBindingUtil.bind(toReturnView);
            if(fragmentReadingBinding != null) {
                fragmentReadingBinding.setLifecycleOwner(this);
                fragmentReadingBinding.setReadingViewModel(readingViewModel);
                readingViewModel.postText(toReadString);
            }

            // Inflate view and obtain an instance of the binding class.
//         binding = DataBindingUtil.getBinding(toReturnView);

            // Specify the current activity as the lifecycle owner.
//        binding.setLifecycleOwner(this);
        }
        return toReturnView;
    }
}
