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
import androidx.lifecycle.*
import com.woods.blinkreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

interface BlinkReaderViewModel {
    val blinkTextLiveData: LiveData<String>
    val readingProgressLiveData: LiveData<Int?>
    val scrollToPercentageLiveData: LiveData<Double>
    val maxProgressLiveData: LiveData<Int?>
    val playPauseButtonResIdLiveData: LiveData<Int?>
    val buttonVisibilityLiveData: LiveData<Int>
    val blinkVisibilityLiveData: LiveData<Int>
    val bookVisibilityLiveData: LiveData<Int>
    val bookTextLiveData: LiveData<Spanned>
    val loadingProgressBarVisibilityLiveData: LiveData<Int>
    val readingFontLiveData: LiveData<Typeface>

    fun setWpm(wpm: Int)
    fun setAccentColor(accentColorString: String)
    fun onProgressChanged(): OnProgressChanged
    fun onPlayPauseButtonClickListener(): View.OnClickListener
    fun switchReadingView(readingPreferenceValue: String)
    fun setFont(string: String)
    fun postClipboardData(clipboard: ClipboardManager)

    class BlinkReaderViewModelFactory(private val application: Application) :
        ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BlinkReaderViewModelImpl(application) as T
        }
    }

    companion object {
        val implClass = BlinkReaderViewModelImpl::class.java
    }

    fun onSkipButtonClick(skipAmount: Int)
}

class BlinkReaderViewModelImpl(application: Application) : BlinkReaderViewModel, AndroidViewModel(application) {
    override val blinkTextLiveData = MutableLiveData<String>()
    override val readingProgressLiveData = MutableLiveData<Int?>()
    override val scrollToPercentageLiveData = MutableLiveData<Double>()
    override val maxProgressLiveData = MutableLiveData<Int?>()
    override val playPauseButtonResIdLiveData = MutableLiveData<Int?>()
    override val buttonVisibilityLiveData = MutableLiveData<Int>()
    override val blinkVisibilityLiveData = MutableLiveData<Int>()
    override val bookVisibilityLiveData = MutableLiveData<Int>()
    override val bookTextLiveData = MutableLiveData<Spanned>()
    override val loadingProgressBarVisibilityLiveData = MutableLiveData<Int>()
    override val readingFontLiveData = MutableLiveData<Typeface>()

    // text that is currently being read
    private val wordList = mutableListOf<String>()
    private val compositeDisposable = CompositeDisposable()

    private var wordsPerMinuteDouble = 0.0
    private var accentColorString = "#000000"

    init {
        resetViews()
    }

    private fun resetViews() {
        blinkVisibilityLiveData.value = View.VISIBLE
        bookVisibilityLiveData.value = View.GONE
        (blinkTextLiveData).value = getApplication<Application>().baseContext.getString(R.string.copy_text_instructions)
        bookTextLiveData.value =
            getApplication<Application>().baseContext.getString(R.string.copy_text_instructions).toSpanned()
        (buttonVisibilityLiveData).value = View.GONE
        (loadingProgressBarVisibilityLiveData).value = View.GONE
    }

    override fun setWpm(wpm: Int) {
        wordsPerMinuteDouble = wpm.toDouble()
        if (isPlaying()) {
            getTimerObservable()?.let {
                compositeDisposable.clear()
                compositeDisposable.add(it.subscribe { onSkipButtonClick(1) })
            }
        }
    }

    override fun setAccentColor(accentColorString: String) {
        this.accentColorString = accentColorString
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    override fun onProgressChanged(): OnProgressChanged {
        return OnProgressChanged { _: SeekBar?, progress: Int, _: Boolean -> postValueToWordReadingProgress(progress) }
    }

    override fun onPlayPauseButtonClickListener(): View.OnClickListener {
        return View.OnClickListener { togglePlay() }
    }

    private fun togglePlay() {
        getTimerObservable()?.let { timerObservable ->
            if (!isPlaying()) {
                (playPauseButtonResIdLiveData).value = R.drawable.ic_pause_24dp
                readingProgressLiveData.value?.let { readingProgress ->
                    if (readingProgress == wordList.size - 1) {
                        (readingProgressLiveData).value = 0
                        (scrollToPercentageLiveData).value = 0.0
                    }
                }
                compositeDisposable.add(timerObservable.subscribe { onSkipButtonClick(1) })
            } else {
                (playPauseButtonResIdLiveData).value = R.drawable.ic_play_arrow_24dp
                compositeDisposable.clear()
            }
        } ?: run {
            (playPauseButtonResIdLiveData).value = R.drawable.ic_play_arrow_24dp
            compositeDisposable.clear()
        }
    }

    private fun getTimerObservable(): Observable<Boolean>? {
        return wordsPerMinuteDouble.let {
            Observable.interval(
                (60 / it * 1000).toLong(), TimeUnit.MILLISECONDS
            ).timeInterval().observeOn(AndroidSchedulers.mainThread()).flatMap {
                    if (isPlaying()) {
                        Observable.just(isPlaying())
                    } else {
                        Observable.empty()
                    }
                }
        }
    }

    override fun onSkipButtonClick(skipAmount: Int) {
        (readingProgressLiveData).value?.let { readingProgress ->
            if (readingProgress + skipAmount >= 0) {
                wordList.let {
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
        return (playPauseButtonResIdLiveData).value != null && playPauseButtonResIdLiveData.value == R.drawable.ic_pause_24dp
    }

    private fun postValueToWordReadingProgress(readingProgress: Int) {
        (maxProgressLiveData).value?.let { maxProgress ->
            if (readingProgress <= maxProgress) {
                (readingProgressLiveData).value = readingProgress

                wordList.let {
                    (blinkTextLiveData).value = it[readingProgress]
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
                val scrollToPercentage = when {
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
                    // if readingProgressPercent is maximumThreshold, return 1
                    else -> (readingProgressPercentage - minimumThreshold) / (maximumThreshold - minimumThreshold) - (minimumThreshold / 2)
                }.let {
                    if (it.isNaN()) 0.0 else it
                }
                (scrollToPercentageLiveData).value = scrollToPercentage

                if (maxProgress < 500) {
                    var bookText = ""
                    readingProgressLiveData.value?.let { readingProgress ->
                        wordList.let {
                            it.forEachIndexed { index, element ->
                                bookText += if (readingProgress == index) {
                                    "<span style='background:$accentColorString'>$element</span> "
                                } else "$element "
                            }
                        }
                    }

                    (bookTextLiveData).value = Html.fromHtml(bookText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            }
        }
    }

    override fun postClipboardData(clipboard: ClipboardManager) {
        (loadingProgressBarVisibilityLiveData).value = View.VISIBLE
        clipboard.primaryClip?.getItemAt(0)?.let { clipDataItem ->
            if (clipDataItem.text.toString().isNotEmpty() && clipDataItem.text.toString().isNotBlank()) {
                viewModelScope.launch(Dispatchers.Main) {
                    val deferredWordList = viewModelScope.async(Dispatchers.Default) {
                        val wordList = clipDataItem.text.toString().split(" ", "\n", "\r", "\\s").toMutableList()
                        wordList.removeIf { it.trim().isEmpty() }
                        wordList
                    }

                    wordList.clear()
                    wordList.addAll(deferredWordList.await())

                    (maxProgressLiveData).value = (deferredWordList.await() as ArrayList<String>).size - 1

                    if (deferredWordList.await().size > 500) {
                        Toast.makeText(getApplication(), R.string.max_words_error, Toast.LENGTH_LONG).show()
                        var bookText = ""
                        val deferred = viewModelScope.async(Dispatchers.Default) {
                            wordList.let { wordList ->
                                wordList.forEach {
                                    bookText += "$it "
                                }
                            }
                            bookText
                        }
                        (bookTextLiveData).value = deferred.await().toSpanned()
                        loadingProgressBarVisibilityLiveData.value = View.GONE
                        (buttonVisibilityLiveData).value = View.VISIBLE
                        postValueToWordReadingProgress(0)
                    } else {
                        loadingProgressBarVisibilityLiveData.value = View.GONE
                        (buttonVisibilityLiveData).value = View.VISIBLE
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

    override fun switchReadingView(readingPreferenceValue: String) {
        if (readingPreferenceValue == getApplication<Application>().baseContext.getString(R.string.reading_mode_book_preference_value)) {
            (blinkVisibilityLiveData).value = View.GONE
            (bookVisibilityLiveData).value = View.VISIBLE
        } else {
            (blinkVisibilityLiveData).value = View.VISIBLE
            (bookVisibilityLiveData).value = View.GONE
        }
    }

    override fun setFont(string: String) {
        when (string) {
            getApplication<Application>().baseContext.getString(R.string.roboto_mono_font_value) -> {
                (readingFontLiveData).value =
                    getApplication<Application>().baseContext.resources.getFont(R.font.roboto_mono)
            }
            getApplication<Application>().baseContext.getString(R.string.bitter_font_value) -> {
                (readingFontLiveData).value = getApplication<Application>().baseContext.resources.getFont(R.font.bitter)
            }
            else -> {
                (readingFontLiveData).value =
                    getApplication<Application>().baseContext.resources.getFont(R.font.raleway)
            }
        }

    }
}