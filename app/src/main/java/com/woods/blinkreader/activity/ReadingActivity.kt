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
import com.woods.blinkreader.viewmodel.ReadingViewModel

class ReadingActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private val readingViewModel: ReadingViewModel by viewModels()

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        setTitle(R.string.app_name)
        if (prefs.getInt(getString(R.string.reading_speed_preference_key), 0) != 0) {
            readingViewModel.setWpm(prefs.getInt(getString(R.string.reading_speed_preference_key), 0))
        }
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
            readingViewModel.postClipboardData((getSystemService(CLIPBOARD_SERVICE) as ClipboardManager),
                    Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT))
            //                break;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (getString(R.string.reading_speed_preference_key) == key) {
            readingViewModel.setWpm(sharedPreferences.getInt(key, 120))
        }
    }
}