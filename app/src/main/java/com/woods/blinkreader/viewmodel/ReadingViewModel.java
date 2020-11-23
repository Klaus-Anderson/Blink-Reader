package com.woods.blinkreader.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ClipboardManager;
import android.databinding.adapters.SeekBarBindingAdapter;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.woods.blinkreader.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ReadingViewModel extends AndroidViewModel {

    private final MutableLiveData<String> textToDisplayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> readingProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> playPauseButtonResIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> buttonVisibilityLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> wpmLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<String> wordList;

    public ReadingViewModel(@NonNull Application application) {
        super(application);
        wpmLiveData.postValue((double) 120);
    }

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

    public LiveData<Integer> getPlayPauseButtonResIdLiveData() {
        return playPauseButtonResIdLiveData;
    }

    public MutableLiveData<Integer> getButtonVisibilityLiveData() {
        return buttonVisibilityLiveData;
    }

    public void setWpm(int wpm) {
        wpmLiveData.setValue((double) wpm);
        Observable observable = getTimerObservable();
        if (isPlaying() && observable != null) {
            // @todo move to disposable to trigger speed change at the correct moment
            compositeDisposable.clear();
            compositeDisposable.add(observable.subscribe(wishIWasAZeroArgumentBoolean -> {
                onSkipButtonClick(1);
            }));
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
        Observable observable = getTimerObservable();
        if (!isPlaying() && observable != null) {
            playPauseButtonResIdLiveData.setValue(R.drawable.ic_pause_black_24dp);
            if (readingProgressLiveData.getValue() != null &&
                    readingProgressLiveData.getValue().equals(wordList.size() - 1)) {
                readingProgressLiveData.setValue(0);
            }
            compositeDisposable.add(observable.subscribe(wishIWasAZeroArgumentBoolean -> {
                onSkipButtonClick(1);
            }));
        } else {
            playPauseButtonResIdLiveData.postValue(R.drawable.ic_play_arrow_black_24dp);
            compositeDisposable.clear();
        }
    }

    private Observable getTimerObservable() {
        if (wpmLiveData.getValue() != null) {
            return Observable.interval(
                    (long) (((60 / wpmLiveData.getValue())) * 1000), TimeUnit.MILLISECONDS)
                    .timeInterval()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(longTimed -> {
                        if (isPlaying()) {
                            return Observable.just(isPlaying());
                        }
                        return Observer::onComplete;
                    })
//
                    ;
        } else {
            return null;
        }
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
        return playPauseButtonResIdLiveData.getValue() != null &&
                playPauseButtonResIdLiveData.getValue().equals(R.drawable.ic_pause_black_24dp);
    }


    private void postValueToWordReadingProgress(int progress) {
        if (maxProgressLiveData.getValue() != null && progress <= maxProgressLiveData.getValue()) {
            readingProgressLiveData.postValue(progress);
            textToDisplayLiveData.postValue(wordList.get(progress));
        }
    }

    public void postClipboardData(@NonNull ClipboardManager clipboard, @NonNull Toast toast) {
        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null &&
                clipboard.getPrimaryClipDescription() != null &&
                clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            wordList = new ArrayList<>(Arrays.asList(clipboard.getPrimaryClip().getItemAt(0).getText().toString().split(" ")));
            postValueToWordReadingProgress(0);
            maxProgressLiveData.postValue(wordList.size() - 1);
            buttonVisibilityLiveData.postValue(View.VISIBLE);
        } else {
            buttonVisibilityLiveData.postValue(View.GONE);
            textToDisplayLiveData.postValue(getApplication().getBaseContext().getString(R.string.copy_text_instructions));
            toast.show();
        }
    }
}
