package com.woods.blinkreader.viewmodel

import android.app.Application
import android.content.ClipDescription
import android.content.ClipboardManager
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.adapters.SeekBarBindingAdapter.OnProgressChanged
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woods.blinkreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class ReadingViewModel(application: Application) : AndroidViewModel(application) {
    private val textToDisplayLiveData = MutableLiveData<String>()
    private val readingProgressLiveData = MutableLiveData<Int?>()
    private val maxProgressLiveData = MutableLiveData<Int?>()
    private val playPauseButtonResIdLiveData = MutableLiveData<Int?>()
    val buttonVisibilityLiveData = MutableLiveData<Int>()
    private val wpmLiveData = MutableLiveData<Double?>()
    private val compositeDisposable = CompositeDisposable()
    private var wordList: MutableList<String>? = null

    init {
        textToDisplayLiveData.value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
        buttonVisibilityLiveData.value = View.GONE
    }

    fun getTextToDisplayLiveData(): LiveData<String> {
        return textToDisplayLiveData
    }

    fun getMaxProgressLiveData(): LiveData<Int?> {
        return maxProgressLiveData
    }

    fun getReadingProgressLiveData(): LiveData<Int?> {
        return readingProgressLiveData
    }

    fun getPlayPauseButtonResIdLiveData(): LiveData<Int?> {
        return playPauseButtonResIdLiveData
    }

    fun setWpm(wpm: Int) {
        wpmLiveData.value = wpm.toDouble()
        if (isPlaying()) {
            getTimerObservable()?.let {
                // @todo move to disposable to trigger speed change at the correct moment
                compositeDisposable.clear()
                compositeDisposable.add(it.subscribe { onSkipButtonClick(1) })
            }
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun onProgressChanged(): OnProgressChanged {
        return OnProgressChanged { _: SeekBar?, progress: Int, _: Boolean -> postValueToWordReadingProgress(progress) }
    }

    fun onPlayPauseButtonClickListener(): View.OnClickListener {
        return View.OnClickListener { togglePlay() }
    }

    private fun togglePlay() {
        getTimerObservable()?.let { timerObservable ->
            if (!isPlaying()) {
                playPauseButtonResIdLiveData.value = R.drawable.ic_pause_black_24dp
                readingProgressLiveData.value?.let { readingProgress ->
                    wordList?.let {
                        if (readingProgress == it.size - 1) {
                            readingProgressLiveData.value = 0
                        }
                    }
                }
                compositeDisposable.add(timerObservable.subscribe { onSkipButtonClick(1) })
            } else {
                playPauseButtonResIdLiveData.value = R.drawable.ic_play_arrow_black_24dp
                compositeDisposable.clear()
            }
        } ?: run {
            playPauseButtonResIdLiveData.value = R.drawable.ic_play_arrow_black_24dp
            compositeDisposable.clear()
        }
    }

    private fun getTimerObservable(): Observable<Boolean>? {
        return wpmLiveData.value?.let {
            Observable.interval(
                    (60 / it * 1000).toLong(), TimeUnit.MILLISECONDS)
                    .timeInterval()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        if (isPlaying()) {
                            Observable.just(isPlaying())
                        } else {
                            Observable.empty()
                        }
                    }
        }
    }

    fun onSkipButtonClick(skipAmount: Int) {
        readingProgressLiveData.value?.let { readingProgress ->
            if (readingProgress + skipAmount >= 0) {
                wordList?.let {
                    if (readingProgress + skipAmount <= it.size - 1) {
                        postValueToWordReadingProgress(readingProgress + skipAmount)
                    } else {
                        if (isPlaying()) {
                            togglePlay()
                        }
                        postValueToWordReadingProgress(it.size - 1)
                    }
                }
            } else {
                postValueToWordReadingProgress(0)
            }
        }
    }

    private fun isPlaying(): Boolean {
        return playPauseButtonResIdLiveData.value != null && playPauseButtonResIdLiveData.value == R.drawable.ic_pause_black_24dp
    }

    private fun postValueToWordReadingProgress(progress: Int) {
        maxProgressLiveData.value?.let { maxProgress ->
            if (progress <= maxProgress) {
                readingProgressLiveData.value = progress
                wordList?.let {
                    textToDisplayLiveData.value = it[progress]

                }
            }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun postClipboardData(clipboard: ClipboardManager, toast: Toast) {
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            wordList = mutableListOf(*clipboard.primaryClip?.getItemAt(0)?.text.toString().split(" ").toTypedArray())
            wordList?.removeIf { it.trim().isEmpty() }
            maxProgressLiveData.value = (wordList as ArrayList<String>).size - 1
            buttonVisibilityLiveData.value = View.VISIBLE
            postValueToWordReadingProgress(0)
        } else {
            buttonVisibilityLiveData.value = View.GONE
            textToDisplayLiveData.value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
            toast.show()
        }
    }
}