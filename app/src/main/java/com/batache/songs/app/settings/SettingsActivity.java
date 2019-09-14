package com.batache.songs.app.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.batache.songs.R;
import com.batache.songs.app.base.BaseActivity;
import com.batache.songs.util.DimensionUtils;
import com.google.android.material.appbar.AppBarLayout;

public class SettingsActivity extends BaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  private AppBarLayout appBar;

  @Override
  public Context getContext() {
    return SettingsActivity.this;
  }

  @Override
  public int getLayout() {
    return R.layout.activity_settings;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    appBar = findViewById(R.id.app_bar);
    setSupportActionBar(findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onApplyAllWindowInsets() {
    appBar.setPadding(0, DimensionUtils.TOP_INSET, 0, 0);
  }

  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    // Instantiate the new Fragment
    Bundle args = pref.getExtras();
    Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
    fragment.setArguments(args);
    fragment.setTargetFragment(caller, 0);
    // Replace the existing Fragment with the new Fragment
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.settings_fragment, fragment)
        .addToBackStack(null)
        .commit();
    return true;
  }
}
