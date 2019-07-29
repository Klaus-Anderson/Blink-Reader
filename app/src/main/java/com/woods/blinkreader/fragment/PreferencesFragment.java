package com.woods.blinkreader.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.woods.blinkreader.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    public static final String TAG = PreferencesFragment.class.getName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);
    }



}
