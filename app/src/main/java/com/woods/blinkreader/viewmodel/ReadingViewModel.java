package com.woods.blinkreader.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class ReadingViewModel extends ViewModel {
    private final MutableLiveData<String> textToDisplayLiveData = new MutableLiveData<>();

    public LiveData<String> getTextToDisplayLiveData() {
        return textToDisplayLiveData;
    }

    public void postText(String toReadString) {
        textToDisplayLiveData.postValue(toReadString);
    }
}
