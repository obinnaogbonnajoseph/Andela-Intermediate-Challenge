package com.example.android.myvolleyapp;



import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            // Set a listener to listen for changes in repo settings
            Preference repoPref = findPreference(getString(R.string.settings_repos_key));
            repoPref.setOnPreferenceChangeListener(this);

            // Set a listener to listen for changes in followers settings
            Preference followersPref = findPreference(getString(R.string.settings_followers_key));
            followersPref.setOnPreferenceChangeListener(this);

            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            PreferenceScreen prefScreen = getPreferenceScreen();
            int count = prefScreen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference p = prefScreen.getPreference(i);
                if (!(p instanceof CheckBoxPreference)) {
                    String value = sharedPreferences.getString(p.getKey(), "");
                    setPreferenceSummary(p, value);
                }
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            getPreferenceManager().getSharedPreferences().
                    registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onStop() {
            super.onStop();
            getPreferenceManager().getSharedPreferences().
                    unregisterOnSharedPreferenceChangeListener(this);
        }

        public void setPreferenceSummary(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            // Set summary
            if (null != preference) {
                if (!(preference instanceof CheckBoxPreference)) {
                    setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Toast error = Toast.makeText(getContext(),"Please enter a number", Toast.LENGTH_SHORT);

            String repoKey = getString(R.string.settings_repos_key);
            String followersKey = getString(R.string.settings_followers_key);
            if(preference.getKey().equals(repoKey) || preference.getKey().equals(followersKey))
            {
                String choiceValue = ((String) (newValue)).trim();
                if(choiceValue == null) choiceValue = "0";
                try {
                    int size = Integer.parseInt(choiceValue);
                    if (size < 0)
                    {
                        error.show();
                        return false;
                    }
                } catch (NumberFormatException nfe)
                {
                    error.show();
                    return false;
                }
            }
            return true;
        }
    }
}

