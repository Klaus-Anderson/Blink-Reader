package com.woods.blinkreader.activity

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.woods.blinkreader.R
import com.woods.blinkreader.fragment.PreferencesFragment
import com.woods.blinkreader.viewmodel.ReadingViewModel

class ReadingActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private var readingViewModel: ReadingViewModel? = null
    private val readingSpeedString = "readingSpeed"

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading)
        readingViewModel = ViewModelProviders.of(this)
                .get(ReadingViewModel::class.java)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        setTitle(R.string.app_name)
        readingViewModel!!.postClipboardData((getSystemService(CLIPBOARD_SERVICE) as ClipboardManager), Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT))
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
            readingViewModel!!.postClipboardData((getSystemService(CLIPBOARD_SERVICE) as ClipboardManager), Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT))
            //                break;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (fragmentManager.findFragmentByTag(PreferencesFragment.TAG) != null) {
            fragmentTransaction.remove(fragmentManager.findFragmentByTag(PreferencesFragment.TAG)!!).commit()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (readingSpeedString == key) {
            readingViewModel!!.setWpm(sharedPreferences.getInt(key, 120))
        }
    }
}