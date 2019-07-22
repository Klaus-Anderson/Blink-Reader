package com.woods.blinkreader.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.woods.blinkreader.R;
import com.woods.blinkreader.fragment.ReadingFragment;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.woods.blinkreader.utils.BundleStrings.READING_FRAGMENT_TEXT_KEY;

public class ReadingActivity extends AppCompatActivity {

    private static final int CONTENT_VIEW_ID = 10101010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

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
        switch (item.getItemId()){
            case R.id.action_paste:
                String pasteData = getPasteData();
                if(pasteData != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction pasteFragmentTransaction = fragmentManager.beginTransaction();
                    Fragment pasteFragment = fragmentManager.findFragmentByTag(pasteData);
                    if(pasteFragment == null) {
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

}
