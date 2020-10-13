package com.batache.songs.util;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.batache.songs.SongsApplication;

public class PrefUtils {

  private static PrefUtils sInstance;

  public static void initialize() {
    if (sInstance == null) {
      sInstance = new PrefUtils(SongsApplication.get());
    }
  }

  public static PrefUtils getInstance() {
    if (sInstance == null) {
      throw new RuntimeException("PrefUtils not initialized");
    }
    return sInstance;
  }

  private SharedPreferences prefs;

  public static final String SEARCH_SHOW_MUSIC_ONLY_PREF = "search_show_music_only";
  public static final boolean SEARCH_SHOW_MUSIC_ONLY_DEFAULT_VALUE = true;

  public static final String SETTINGS_DOWNLOAD_OVER_WIFI_ONLY_PREF = "download_over_wifi_only";
  public static final boolean SETTINGS_DOWNLOAD_OVER_WIFI_ONLY_DEFAULT_VALUE = true;

  public static final String SETTINGS_DOWNLOAD_LOCATION_PREF = "download_location";
  public static final String SETTINGS_DOWNLOAD_LOCATION_DEFAULT_VALUE = "in_app";

  private PrefUtils(SongsApplication songsApplication) {
    // we should always use the application context to avoid memory leaks
    prefs = PreferenceManager.getDefaultSharedPreferences(songsApplication);
  }

  public void putBoolean(String key, boolean bool) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(key, bool);
    editor.apply();
  }

  public boolean getBoolean(String key, boolean defValue) {
    return prefs.getBoolean(key, defValue);
  }

  public String getString(String key, String defValue) {
    return prefs.getString(key, defValue);
  }

  public static void destroy() {
    if (sInstance == null) {
      return;
    }
    sInstance = null;
  }

}
