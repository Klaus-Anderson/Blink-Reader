package com.woods.blinkreader.activity;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.woods.blinkreader.R;
import com.woods.blinkreader.fragment.PreferencesFragment;
import com.woods.blinkreader.viewmodel.ReadingViewModel;

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

        readingViewModel.postClipboardData((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reading_activity, menu);
        return true;
    }

    @SuppressLint("ShowToast")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
        if (R.id.action_paste == item.getItemId()) {
            readingViewModel.postClipboardData((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), Toast.makeText(this, R.string.paste_error, Toast.LENGTH_SHORT));
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(PreferencesFragment.TAG) != null) {
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
