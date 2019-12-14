package ru.spbstu.lyubchenkova.checkers.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import ru.spbstu.lyubchenkova.checkers.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class HelpActivity extends BaseActivity {
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_help);

        overridePendingTransition(0, 0);
    }

    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

    private void setTheme() {
        mSharedPreferences = getDefaultSharedPreferences(this);
        int theme = mSharedPreferences.getInt("theme", 1);
        if (theme == 1) {
            setTheme(R.style.AppTheme_NoActionBar);
        } else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
    }
}