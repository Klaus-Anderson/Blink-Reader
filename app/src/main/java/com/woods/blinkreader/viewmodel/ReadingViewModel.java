package com.woods.blinkreader.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.adapters.SeekBarBindingAdapter;
import android.support.annotation.NonNull;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadingViewModel extends ViewModel {

    private final MutableLiveData<String> textToDisplayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> readingProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxProgressLiveData = new MutableLiveData<>();
    private List<String> wordList;

    @NonNull
    public LiveData<String> getTextToDisplayLiveData() {
        return textToDisplayLiveData;
    }

    public MutableLiveData<Integer> getMaxProgressLiveData() {
        return maxProgressLiveData;
    }

    public MutableLiveData<Integer> getReadingProgressLiveData() {
        return readingProgressLiveData;
    }

    public void postText(@NonNull String toReadString) {
        wordList = new ArrayList<>(Arrays.asList(toReadString.split(" ")));
        if (!wordList.isEmpty()) {
            postValueToWordReadingProgress(0);
            maxProgressLiveData.postValue(wordList.size() - 1);
        }
    }

    @NonNull
    public SeekBarBindingAdapter.OnProgressChanged onProgressChanged() {
        return new SeekBarBindingAdapter.OnProgressChanged() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                postValueToWordReadingProgress(progress);
            }
        };
    }

    public void onClick(int skipAmount) {
        if (readingProgressLiveData.getValue() != null) {
            int currentIndex = readingProgressLiveData.getValue();
            if (currentIndex + skipAmount >= 0) {
                if (currentIndex + skipAmount <= wordList.size() - 1) {
                    postValueToWordReadingProgress(currentIndex + skipAmount);
                } else {
                    postValueToWordReadingProgress(wordList.size() - 1);
                }
            } else {
                postValueToWordReadingProgress(0);
            }
        }
    }


    public void postValueToWordReadingProgress(int progress) {
        readingProgressLiveData.postValue(progress);
        textToDisplayLiveData.postValue(wordList.get(progress));
    }
}
