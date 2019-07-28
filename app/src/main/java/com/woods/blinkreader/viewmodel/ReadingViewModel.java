package com.woods.blinkreader.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.adapters.SeekBarBindingAdapter;
import android.support.annotation.NonNull;
import android.view.View;

import com.woods.blinkreader.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ReadingViewModel extends ViewModel {

    private final MutableLiveData<String> textToDisplayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> readingProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> playPuaseButtonResIdLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<String> wordList;

    @NonNull
    public LiveData<String> getTextToDisplayLiveData() {
        return textToDisplayLiveData;
    }

    public LiveData<Integer> getMaxProgressLiveData() {
        return maxProgressLiveData;
    }

    public LiveData<Integer> getReadingProgressLiveData() {
        return readingProgressLiveData;
    }

    public LiveData<Integer> getPlayPuaseButtonResIdLiveData() {
        return playPuaseButtonResIdLiveData;
    }

    public void postText(@NonNull String toReadString) {
        wordList = new ArrayList<>(Arrays.asList(toReadString.split(" ")));
        if (!wordList.isEmpty()) {
            postValueToWordReadingProgress(0);

            maxProgressLiveData.postValue(wordList.size() - 1);
            playPuaseButtonResIdLiveData.postValue(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

    @NonNull
    public SeekBarBindingAdapter.OnProgressChanged onProgressChanged() {
        return (seekBar, progress, fromUser) -> postValueToWordReadingProgress(progress);
    }

    public View.OnClickListener onPlayPauseButtonClickListener() {
        return v -> togglePlay();

    }

    private void togglePlay() {
        if (playPuaseButtonResIdLiveData.getValue() != null)
            if (!isPlaying()) {
                playPuaseButtonResIdLiveData.setValue(R.drawable.ic_pause_black_24dp);
                if (readingProgressLiveData.getValue() != null &&
                        readingProgressLiveData.getValue().equals(wordList.size()-1)) {
                    readingProgressLiveData.setValue(0);
                }
                compositeDisposable.add(getTimerDisposable());
            } else {
                playPuaseButtonResIdLiveData.postValue(R.drawable.ic_play_arrow_black_24dp);
                compositeDisposable.clear();
            }
    }

    private Disposable getTimerDisposable() {
        return Observable.interval(500L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(longTimed -> {
                    if (isPlaying()) {
                        //todo: find an observable that can emit 0 arguments
                        return Observable.just(isPlaying());
                    }
                    return Observer::onComplete;

                })
                .subscribe(wishIWasAZeroArgumentBoolean -> {
                    onSkipButtonClick(1);
                });
    }

    public void onSkipButtonClick(int skipAmount) {
        if (readingProgressLiveData.getValue() != null) {
            int currentIndex = readingProgressLiveData.getValue();
            if (currentIndex + skipAmount >= 0) {
                if (currentIndex + skipAmount <= wordList.size() - 1) {
                    postValueToWordReadingProgress(currentIndex + skipAmount);
                } else {
                    if (isPlaying()) {
                        togglePlay();
                    }
                    postValueToWordReadingProgress(wordList.size() - 1);
                }
            } else {
                postValueToWordReadingProgress(0);
            }
        }
    }

    private boolean isPlaying() {
        return playPuaseButtonResIdLiveData.getValue() != null &&
                playPuaseButtonResIdLiveData.getValue().equals(R.drawable.ic_pause_black_24dp);
    }


    private void postValueToWordReadingProgress(int progress) {
        if (maxProgressLiveData.getValue() != null && progress <= maxProgressLiveData.getValue()) {
            readingProgressLiveData.postValue(progress);
            textToDisplayLiveData.postValue(wordList.get(progress));
        }
    }
}
