package com.woods.blinkreader.viewmodel

import android.app.Application
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

class BlinkReaderViewModel(application: Application) : AndroidViewModel(application) {
    val blinkTextLiveData: LiveData<String> = MutableLiveData()
    val readingProgressLiveData: LiveData<Int?> = MutableLiveData()
    val maxProgressLiveData: LiveData<Int?> = MutableLiveData()
    val playPauseButtonResIdLiveData: LiveData<Int?> = MutableLiveData()
    val buttonVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val blinkVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val bookVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val bookTextLiveData: LiveData<String> = MutableLiveData()

    // text that is currently being read
    private val wordListLiveData: LiveData<List<String>> = MutableLiveData()
    private val wpmLiveData: LiveData<Double?> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    init {
        (blinkTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
        (bookTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
        (buttonVisibilityLiveData as MutableLiveData).value = View.GONE
    }

    fun setWpm(wpm: Int) {
        (wpmLiveData as MutableLiveData).value = wpm.toDouble()
        if (isPlaying()) {
            getTimerObservable()?.let {
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
                (playPauseButtonResIdLiveData as MutableLiveData).value = R.drawable.ic_pause_24dp
                readingProgressLiveData.value?.let { readingProgress ->
                    wordListLiveData.value?.let {
                        if (readingProgress == it.size - 1) {
                            (readingProgressLiveData as MutableLiveData).value = 0
                        }
                    }
                }
                compositeDisposable.add(timerObservable.subscribe { onSkipButtonClick(1) })
            } else {
                (playPauseButtonResIdLiveData as MutableLiveData).value = R.drawable.ic_play_arrow_24dp
                compositeDisposable.clear()
            }
        } ?: run {
            (playPauseButtonResIdLiveData as MutableLiveData).value = R.drawable.ic_play_arrow_24dp
            compositeDisposable.clear()
        }
    }

    private fun getTimerObservable(): Observable<Boolean>? {
        return (wpmLiveData as MutableLiveData).value?.let {
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
        (readingProgressLiveData as MutableLiveData).value?.let { readingProgress ->
            if (readingProgress + skipAmount >= 0) {
                wordListLiveData.value?.let {
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
        return (playPauseButtonResIdLiveData as MutableLiveData).value != null &&
                playPauseButtonResIdLiveData.value == R.drawable.ic_pause_24dp
    }

    private fun postValueToWordReadingProgress(progress: Int) {
        (maxProgressLiveData as MutableLiveData).value?.let { maxProgress ->
            if (progress <= maxProgress) {
                (readingProgressLiveData as MutableLiveData).value = progress
                wordListLiveData.value?.let {
                    (blinkTextLiveData as MutableLiveData).value = it[progress]
                }
            }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun postClipboardData(clipboard: ClipboardManager, toast: Toast) {
        clipboard.primaryClip?.getItemAt(0)?.let { clipDataItem ->
            if (clipDataItem.text.toString().isNotEmpty() && clipDataItem.text.toString().isNotBlank()) {
                (bookTextLiveData as MutableLiveData).value = clipDataItem.text.toString()
                val wordList = clipDataItem.text.toString().split(" ").toMutableList()
                wordList.removeIf { it.trim().isEmpty() }
                (wordListLiveData as MutableLiveData).value = wordList
                (maxProgressLiveData as MutableLiveData).value = (wordList as ArrayList<String>).size - 1
                (buttonVisibilityLiveData as MutableLiveData).value = View.VISIBLE
                postValueToWordReadingProgress(0)
            } else {
                (buttonVisibilityLiveData as MutableLiveData).value = View.GONE
                (blinkTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
                toast.show()
            }
        } ?: run {
            (buttonVisibilityLiveData as MutableLiveData).value = View.GONE
            (blinkTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
            toast.show()
        }
    }

    fun switchReadingView(readingPreferenceValue: String) {
        if (readingPreferenceValue == getApplication<Application>().baseContext.getString(R.string.reading_mode_book_preference_value)) {
            (blinkVisibilityLiveData as MutableLiveData).value = View.GONE
            (bookVisibilityLiveData as MutableLiveData).value = View.VISIBLE
        } else {
            (blinkVisibilityLiveData as MutableLiveData).value = View.VISIBLE
            (bookVisibilityLiveData as MutableLiveData).value = View.GONE
        }
    }
}