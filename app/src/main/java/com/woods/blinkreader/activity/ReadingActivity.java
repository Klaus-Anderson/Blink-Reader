package com.woods.blinkreader.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.woods.blinkreader.R;
import com.woods.blinkreader.fragment.PreferencesFragment;
import com.woods.blinkreader.fragment.ReadingFragment;
import com.woods.blinkreader.viewmodel.ReadingViewModel;

import java.util.prefs.Preferences;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ReadingViewModel readingViewModel;
    private String readingSpeedString = "readingSpeed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        readingViewModel = ViewModelProviders.of(this)
                .get(ReadingViewModel.class);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reading_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
                String pasteData = getPasteData();
                if (pasteData != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction pasteFragmentTransaction = fragmentManager.beginTransaction();
                    Fragment pasteFragment = fragmentManager.findFragmentByTag(pasteData);
                    if (pasteFragment == null) {
                        Bundle pasteArguments = new Bundle();
                        pasteArguments.putString(READING_FRAGMENT_TEXT_KEY, pasteData);
                        pasteFragment = new ReadingFragment();
                        pasteFragment.setArguments(pasteArguments);
                        pasteFragmentTransaction = pasteFragmentTransaction.add(
                                R.id.fragment_container, pasteFragment, pasteData);
                    } else {
                        pasteFragmentTransaction = pasteFragmentTransaction.show(pasteFragment);
                    }
                    pasteFragmentTransaction.commit();
                } else {
                    Toast toast = Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.action_settings:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction preferencesFragmentTransaction = fragmentManager.beginTransaction();
                Fragment preferencesFragment = fragmentManager.findFragmentByTag(PreferencesFragment.TAG);
                if (preferencesFragment == null) {
                    preferencesFragment = new PreferencesFragment();
                    preferencesFragmentTransaction = preferencesFragmentTransaction.add(
                            R.id.fragment_container, preferencesFragment, PreferencesFragment.TAG);
                } else {
                    preferencesFragmentTransaction = preferencesFragmentTransaction.show(preferencesFragment);
                }
                preferencesFragmentTransaction.commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Nullable
    private String getPasteData() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";
        if (!(clipboard.hasPrimaryClip())) {

        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {

            // since the clipboard has data but it is not plain text

        } else {

            //since the clipboard contains plain text.
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            return item.getText().toString();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(fragmentManager.findFragmentByTag(PreferencesFragment.TAG)!=null){
            fragmentTransaction.remove(fragmentManager.findFragmentByTag(PreferencesFragment.TAG)).commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (readingSpeedString.equals(key)) {
            readingViewModel.setWpm(sharedPreferences.getInt(key, 120));
        }
    }
}
