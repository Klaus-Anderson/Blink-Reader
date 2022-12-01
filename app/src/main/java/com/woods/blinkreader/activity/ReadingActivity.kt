package com.woods.blinkreader.activity

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.ActivityReadingBinding
import com.woods.blinkreader.fragment.BlinkFragment
import com.woods.blinkreader.fragment.BookFragment
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel


class ReadingActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    lateinit var blinkReaderViewModel: BlinkReaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blinkReaderViewModel = ViewModelProvider(
            this
        )[BlinkReaderViewModel.implClass]

        setTitle(R.string.app_name)

        // get and display preferences
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            registerOnSharedPreferenceChangeListener(this@ReadingActivity)
            if (getInt(getString(R.string.reading_speed_preference_key), 0) != 0) {
                blinkReaderViewModel.setWpm(getInt(getString(R.string.reading_speed_preference_key), 0))
            }
            if (getBoolean(getString(R.string.dark_theme_preference_key), false)) {
                setTheme(R.style.DarkTheme)
            }

            blinkReaderViewModel.setFont(
                getString(
                    getString(R.string.reading_font_preference_key), null
                ) ?: getString(R.string.raleway_font_value)
            )
        }

        blinkReaderViewModel.setAccentColor(
            String.format(
                "#%06X", 0xFFFFFF and TypedValue().getAttribute(theme, R.attr.colorAccent).data
            )
        )

        blinkReaderViewModel.blinkVisibilityLiveData.observe(this) { visibility ->
            when (visibility) {
                View.VISIBLE -> supportFragmentManager.fragments.firstOrNull {
                    it is BlinkFragment
                } ?: supportFragmentManager.beginTransaction().add(R.id.reading_fragment_layout, BlinkFragment())
                    .commit()
                View.GONE -> supportFragmentManager.fragments.forEach {
                    if (it is BlinkFragment) {
                        supportFragmentManager.beginTransaction().remove(it).commit()
                    }
                }
            }
        }

        blinkReaderViewModel.bookVisibilityLiveData.observe(this) { visibility ->
            replaceFragment(visibility, BookFragment())
        }

        val activityReadingBinding: ActivityReadingBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_reading)
        activityReadingBinding.readingViewModel = blinkReaderViewModel

        blinkReaderViewModel.loadingProgressBarVisibilityLiveData.observe(this) {
            activityReadingBinding.loadingProgressBarLayout?.visibility = it
        }
    }

    private fun replaceFragment(visibility: Int, fragment: Fragment) {
        when (visibility) {
            View.VISIBLE -> supportFragmentManager.fragments.firstOrNull {
                it::class == fragment::class
            } ?: supportFragmentManager.beginTransaction().add(R.id.reading_fragment_layout, fragment)
                .commit()
            View.GONE -> supportFragmentManager.fragments.forEach {
                if (it::class == fragment::class) {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_reading_activity, menu)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        menu.findItem(R.id.action_reader_switch)?.let { menuItem ->
            sharedPreferences.getString(getString(R.string.reading_mode_preference_key), null)?.let {
                if (it == getString(R.string.reading_mode_book_preference_value)) {
                    menuItem.setIcon(R.drawable.ic_content_blink_24dp)
                    blinkReaderViewModel.switchReadingView(getString(R.string.reading_mode_book_preference_value))
                } else {
                    menuItem.setIcon(R.drawable.ic_content_book_24dp)
                    blinkReaderViewModel.switchReadingView(getString(R.string.reading_mode_blink_preference_value))
                }
            } ?: sharedPreferences.edit().putString(
                getString(R.string.reading_mode_preference_key), getString(R.string.reading_mode_blink_preference_value)
            ).apply()
        }

        return true
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.action_paste == item.itemId) {
            blinkReaderViewModel.postClipboardData(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
        } else if (R.id.action_reader_switch == item.itemId) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if (sharedPreferences.getString(
                    getString(R.string.reading_mode_preference_key), null
                ) == getString(R.string.reading_mode_blink_preference_value)
            ) {
                sharedPreferences.edit().putString(
                    getString(R.string.reading_mode_preference_key),
                    getString(R.string.reading_mode_book_preference_value)
                ).apply()
            } else {
                sharedPreferences.edit().putString(
                    getString(R.string.reading_mode_preference_key),
                    getString(R.string.reading_mode_blink_preference_value)
                ).apply()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == getString(R.string.reading_speed_preference_key)) {
            if (sharedPreferences.getInt(key, 120) < 15) {
                sharedPreferences.edit().putInt(key, 15).apply()
            } else {
                blinkReaderViewModel.setWpm(sharedPreferences.getInt(key, 120))
            }
        } else if (key == getString(R.string.dark_theme_preference_key)) {
            setTheme(
                if (sharedPreferences.getBoolean(key, false)) {
                    R.style.DarkTheme
                } else {
                    R.style.LightTheme
                }
            )
            recreate()
        } else if (key == getString(R.string.reading_mode_preference_key)) {
            invalidateOptionsMenu()
        } else if (key == getString(R.string.reading_font_preference_key)) {
            sharedPreferences.getString(key, null)?.let {
                blinkReaderViewModel.setFont(it)
            }
        }
    }

}

private fun TypedValue.getAttribute(theme: Resources.Theme, resId: Int): TypedValue {
    theme.resolveAttribute(R.attr.colorAccent, this, true)
    return this
}
