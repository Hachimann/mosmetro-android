package pw.thedrhax.mosmetro.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import pw.thedrhax.mosmetro.R;
import pw.thedrhax.util.Util;

public class ThemesActivity extends AppCompatActivity  {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setupSharedPreferences();
//        if (sharedPreferences.getBoolean("pref_dark_theme", false)) {
//            setTheme(getSavedTheme());
//        }
//        setContentView(R.layout.settings_activity);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.settings, new SettingsFragment())
//                .commit();
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals("pref_dark_theme") || key.equals("pref_AMOLED_theme")) {
//            setTheme(sharedPreferences.getBoolean("pref_dark_theme",true));
//        }
//    }
//
//    public static class SettingsFragment extends PreferenceFragmentCompat {
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.pref_themes_activity, rootKey);
//        }
//    }
//
//    private void setupSharedPreferences() {
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
//    }
//
//    private void setTheme(boolean set_theme) {
//        if (set_theme) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//        this.recreate();
//    }
//
//    private int getSavedTheme() {
//        if (sharedPreferences.getBoolean("pref_AMOLED_theme", false)) {
//            return R.style.AppBaseTheme_AMOLED;
//        } else {
//            return R.style.AppBaseTheme;
//        }
//    }
}