package com.woods.blinkreader.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.woods.blinkreader.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
    }

    companion object {
        @JvmField
        val TAG: String = PreferencesFragment::class.java.name
    }
}