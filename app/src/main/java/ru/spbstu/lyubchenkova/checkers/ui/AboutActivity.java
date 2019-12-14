package ru.spbstu.lyubchenkova.checkers.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import ru.spbstu.lyubchenkova.checkers.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AboutActivity extends AppCompatActivity {
    SharedPreferences mSharedPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(BaseActivity.MAIN_CONTENT_FADEIN_DURATION);
        }

        overridePendingTransition(0, 0);
    }

    private void setTheme() {
        mSharedPreferences = getDefaultSharedPreferences(this);
        int theme = mSharedPreferences.getInt("theme", 1);
        if (theme == 1) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }
        Log.v("theme", "theme is " + theme);
    }
}

