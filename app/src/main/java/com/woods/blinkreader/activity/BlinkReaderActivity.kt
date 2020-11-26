package com.woods.blinkreader.activity

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.woods.blinkreader.R
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel


class BlinkReaderActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private val blinkReaderViewModel: BlinkReaderViewModel by viewModels()

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.app_name)

        // get and display preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (sharedPreferences.getInt(getString(R.string.reading_speed_preference_key), 0) != 0) {
            blinkReaderViewModel.setWpm(sharedPreferences.getInt(getString(R.string.reading_speed_preference_key), 0))
        }
        if (sharedPreferences.getBoolean(getString(R.string.dark_theme_preference_key), false)) {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_blink_reader)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_reading_activity, menu)
        return true
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        switch (item.getItemId()) {
        if (R.id.action_paste == item.itemId) {
            blinkReaderViewModel.postClipboardData((getSystemService(CLIPBOARD_SERVICE) as ClipboardManager),
                    Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT))
            //                break;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (getString(R.string.reading_speed_preference_key) == key) {
            if (sharedPreferences.getInt(key, 120) < 15) {
                sharedPreferences.edit().putInt(key, 15).apply()
            } else {
                blinkReaderViewModel.setWpm(sharedPreferences.getInt(key, 120))
            }
        } else if (getString(R.string.dark_theme_preference_key) == key) {
            setTheme(if (sharedPreferences.getBoolean(key, false)) {
                R.style.DarkTheme
            } else {
                R.style.LightTheme
            })
            recreate()
        }
    }

}