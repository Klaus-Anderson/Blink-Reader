package com.woods.blinkreader.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.ClipDescription
import android.content.ClipboardManager
import androidx.databinding.adapters.SeekBarBindingAdapter.OnProgressChanged
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.woods.blinkreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

class ReadingViewModel(application: Application) : AndroidViewModel(application) {
    private val textToDisplayLiveData = MutableLiveData<String>()
    private val readingProgressLiveData = MutableLiveData<Int?>()
    private val maxProgressLiveData = MutableLiveData<Int?>()
    private val playPauseButtonResIdLiveData = MutableLiveData<Int?>()
    val buttonVisibilityLiveData = MutableLiveData<Int>()
    private val wpmLiveData = MutableLiveData<Double?>()
    private val compositeDisposable = CompositeDisposable()
    private var wordList: List<String>? = null
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
        val observable = getTimerObservable()
        if (isPlaying() && observable != null) {
            // @todo move to disposable to trigger speed change at the correct moment
            compositeDisposable.clear()
            compositeDisposable.add(observable.subscribe(Consumer { onSkipButtonClick(1) }))
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
        val observable = getTimerObservable()
        if (!isPlaying() && observable != null) {
            playPauseButtonResIdLiveData.value = R.drawable.ic_pause_black_24dp
            if (readingProgressLiveData.value != null && readingProgressLiveData.value == wordList!!.size - 1) {
                readingProgressLiveData.value = 0
            }
            compositeDisposable.add(observable.subscribe(Consumer { onSkipButtonClick(1) }))
        } else {
            playPauseButtonResIdLiveData.postValue(R.drawable.ic_play_arrow_black_24dp)
            compositeDisposable.clear()
        }
    }

    private fun getTimerObservable(): Observable<Boolean>? {
        return if (wpmLiveData.value != null) {
            Observable.interval(
                    (60 / wpmLiveData.value!! * 1000).toLong(), TimeUnit.MILLISECONDS)
                    .timeInterval()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        if (isPlaying()) {
                            Observable.just(isPlaying())
                        } else {
                            Observable.empty()
                        }
                    }
        } else {
            null
        }
    }

    fun onSkipButtonClick(skipAmount: Int) {
        if (readingProgressLiveData.value != null) {
            val currentIndex = readingProgressLiveData.value!!
            if (currentIndex + skipAmount >= 0) {
                if (currentIndex + skipAmount <= wordList!!.size - 1) {
                    postValueToWordReadingProgress(currentIndex + skipAmount)
                } else {
                    if (isPlaying()) {
                        togglePlay()
                    }
                    postValueToWordReadingProgress(wordList!!.size - 1)
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
        if (maxProgressLiveData.value != null && progress <= maxProgressLiveData.value!!) {
            readingProgressLiveData.postValue(progress)
            textToDisplayLiveData.postValue(wordList!![progress])
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun postClipboardData(clipboard: ClipboardManager, toast: Toast) {
        if (clipboard.hasPrimaryClip() && clipboard.primaryClip != null && clipboard.primaryClipDescription != null &&
                clipboard.primaryClipDescription != null && clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            wordList = ArrayList(mutableListOf(*clipboard.primaryClip.getItemAt(0).text.toString().split(" ").toTypedArray()))
            postValueToWordReadingProgress(0)
            maxProgressLiveData.postValue((wordList as ArrayList<String>).size - 1)
            buttonVisibilityLiveData.postValue(View.VISIBLE)
        } else {
            buttonVisibilityLiveData.postValue(View.GONE)
            textToDisplayLiveData.postValue(getApplication<Application>().baseContext.getString(R.string.copy_text_instructions))
            toast.show()
        }
    }

    init {
        wpmLiveData.postValue(120.toDouble())
    }
}