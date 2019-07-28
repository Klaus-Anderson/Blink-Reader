package com.woods.blinkreader.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woods.blinkreader.R;
import com.woods.blinkreader.databinding.FragmentReadingBinding;
import com.woods.blinkreader.viewmodel.ReadingViewModel;

import java.util.Objects;

import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingFragment extends Fragment {

    private ReadingViewModel readingViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        readingViewModel = ViewModelProviders.of(getActivity())
                .get(ReadingViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturnView = inflater.inflate(R.layout.fragment_reading, container, false);

        Bundle arguments = getArguments();

        FragmentReadingBinding fragmentReadingBinding = DataBindingUtil.bind(toReturnView);

        if (arguments != null && arguments.getString(READING_FRAGMENT_TEXT_KEY) != null &&
                fragmentReadingBinding != null) {
            fragmentReadingBinding.setLifecycleOwner(this);
            fragmentReadingBinding.setReadingViewModel(readingViewModel);
            fragmentReadingBinding.setContext(getActivity());
            readingViewModel.postText(Objects.requireNonNull(arguments.getString(READING_FRAGMENT_TEXT_KEY)));
        }

        return toReturnView;
    }
}
