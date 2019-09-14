package com.batache.songs.app.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.batache.songs.R;

public class DownloadsSettingsFragment extends PreferenceFragmentCompat {
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.settings_downloads);
    setHasOptionsMenu(true);
  }
}
