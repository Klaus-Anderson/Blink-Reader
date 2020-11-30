package com.woods.blinkreader.viewmodel

import android.app.Application
import android.content.ClipboardManager
import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import androidx.databinding.adapters.SeekBarBindingAdapter.OnProgressChanged
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.woods.blinkreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class BlinkReaderViewModel(application: Application) : AndroidViewModel(application) {
    val blinkTextLiveData: LiveData<String> = MutableLiveData()
    val readingProgressLiveData: LiveData<Int?> = MutableLiveData()
    val scrollToPercentageLiveData: LiveData<Double> = MutableLiveData()
    val maxProgressLiveData: LiveData<Int?> = MutableLiveData()
    val playPauseButtonResIdLiveData: LiveData<Int?> = MutableLiveData()
    val buttonVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val blinkVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val bookVisibilityLiveData: LiveData<Int> = MutableLiveData()
    val bookTextLiveData: LiveData<Spanned> = MutableLiveData()
    val loadingProgressBarVisibilityLiveData: LiveData<Int> = MutableLiveData()
    private val copiedTextLiveData: LiveData<String> = MutableLiveData()
    val readingFontLiveData: LiveData<Typeface> = MutableLiveData()

    // text that is currently being read
    private val wordListLiveData: LiveData<List<String>> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    private var wordsPerMinuteDouble = 0.0
    private var accentColorString = "#000000"

    init {
        resetViews()
    }

    private fun resetViews() {
        (blinkTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
        (bookTextLiveData as MutableLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions).toSpanned()
        (buttonVisibilityLiveData as MutableLiveData).value = View.GONE
        (loadingProgressBarVisibilityLiveData as MutableLiveData).value = View.GONE
    }

    fun setWpm(wpm: Int) {
        wordsPerMinuteDouble = wpm.toDouble()
        if (isPlaying()) {
            getTimerObservable()?.let {
                compositeDisposable.clear()
                compositeDisposable.add(it.subscribe { onSkipButtonClick(1) })
            }
        }
    }

    fun setAccentColor(accentColorString: String) {
        this.accentColorString = accentColorString
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
                            (scrollToPercentageLiveData as MutableLiveData).value = 0.0
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
        return wordsPerMinuteDouble.let {
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

    private fun postValueToWordReadingProgress(readingProgress: Int) {
        (maxProgressLiveData as MutableLiveData).value?.let { maxProgress ->
            if (readingProgress <= maxProgress) {
                (readingProgressLiveData as MutableLiveData).value = readingProgress

                wordListLiveData.value?.let {
                    (blinkTextLiveData as MutableLiveData).value = it[readingProgress]
                }

                // TODO: workshop the math process scrolling to follow the text on book fragment.
                // This will work for most reading scenarios, but is specifically tuned
                // to the requirements of 1000 words. This MAY not work for greater than 1000 words,
                // or maybe ^.7 is the magic number for what is trying to be achieved here.
                // Furthermore, this may have issues on different screen types.
                // Lastly, if there is a significant imbalance in the lengths of words
                // in the first half and second half of list, this scrolling will not work properly

                val minimumThreshold = 1.0 / maxProgress.toDouble().pow(.7)
                val maximumThreshold = 1.0 - (minimumThreshold)

                val readingProgressPercentage = readingProgress.toFloat().div(maxProgress)
                val scrollToPercentage =
                        when {
                            // only begin auto-scrolling when the user has passed the minimum
                            // threshold so text on the first line is not blocked
                            readingProgressPercentage <= minimumThreshold -> 0.0
                            // stop auto-scrolling and scroll the user to the end when they've
                            // reached the maximum threshold so that the final line is completely
                            // visible once the user has reached it
                            readingProgressPercentage > maximumThreshold -> 1.0
                            // if the user is within the thresholds then scroll the user through
                            // the rest of the of view by using a proportion of reading progress
                            //
                            // for example:
                            // if readingProgressPercentage is minimumThreshold, return 0
                            // if readingProgressPercent is .5, return .5
                            // if readingProgressPercent is is maximumThreshold, return 1
                            else -> (readingProgressPercentage - minimumThreshold) /
                                    (maximumThreshold - minimumThreshold) - (minimumThreshold / 2)
                        }.let {
                            if (it.isNaN()) 0.0 else it
                        }
                (scrollToPercentageLiveData as MutableLiveData).value = scrollToPercentage

                if (maxProgress < 500) {
                    var bookText = ""
                    readingProgressLiveData.value?.let { readingProgress ->
                        wordListLiveData.value?.let {
                            it.forEachIndexed { index, element ->
                                bookText += if (readingProgress == index) {
                                    "<span style='background:$accentColorString'>$element</span> "
                                } else
                                    "$element "
                            }
                        }
                    }

                    (bookTextLiveData as MutableLiveData).value =
                            Html.fromHtml(bookText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            }
        }
    }

    fun postClipboardData(clipboard: ClipboardManager) {
        (loadingProgressBarVisibilityLiveData as MutableLiveData).value = View.VISIBLE
        clipboard.primaryClip?.getItemAt(0)?.let { clipDataItem ->
            if (clipDataItem.text.toString().isNotEmpty() && clipDataItem.text.toString().isNotBlank()) {
                (copiedTextLiveData as MutableLiveData).value = clipDataItem.text.toString()
                viewModelScope.launch(Dispatchers.Main) {
                    val deferredWordList = viewModelScope.async(Dispatchers.Default) {
                        val wordList = clipDataItem.text.toString().split(" ", "\n", "\r", "\\s").toMutableList()
                        wordList.removeIf { it.trim().isEmpty() }
                        wordList
                    }

                    (wordListLiveData as MutableLiveData).value = deferredWordList.await()
                    (maxProgressLiveData as MutableLiveData).value = (deferredWordList.await() as ArrayList<String>).size - 1

                    if (deferredWordList.await().size > 500) {
                        Toast.makeText(getApplication(), R.string.max_words_error, Toast.LENGTH_LONG).show()
                        var bookText = ""
                        val deferred = viewModelScope.async(Dispatchers.Default) {
                            wordListLiveData.value?.let { wordList ->
                                wordList.forEach {
                                    bookText += "$it "
                                }
                            }
                            bookText
                        }
                        (bookTextLiveData as MutableLiveData).value = deferred.await().toSpanned()
                        loadingProgressBarVisibilityLiveData.value = View.GONE
                        (buttonVisibilityLiveData as MutableLiveData).value = View.VISIBLE
                        postValueToWordReadingProgress(0)
                    } else {
                        loadingProgressBarVisibilityLiveData.value = View.GONE
                        (buttonVisibilityLiveData as MutableLiveData).value = View.VISIBLE
                        postValueToWordReadingProgress(0)
                    }
                }
            } else {
                resetViews()
                Toast.makeText(getApplication(), R.string.paste_error, Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            resetViews()
            Toast.makeText(getApplication(), R.string.paste_error, Toast.LENGTH_SHORT).show()
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

    fun setFont(string: String) {
        when (string) {
            getApplication<Application>().baseContext.getString(R.string.roboto_mono_font_value) -> {
                (readingFontLiveData as MutableLiveData).value = getApplication<Application>().baseContext.resources.getFont(R.font.roboto_mono)
            }
            getApplication<Application>().baseContext.getString(R.string.bitter_font_value) -> {
                (readingFontLiveData as MutableLiveData).value = getApplication<Application>().baseContext.resources.getFont(R.font.bitter)
            }
            else -> {
                (readingFontLiveData as MutableLiveData).value = getApplication<Application>().baseContext.resources.getFont(R.font.raleway)
            }
        }

    }
}