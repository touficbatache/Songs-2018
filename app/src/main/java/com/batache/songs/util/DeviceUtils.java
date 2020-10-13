package com.batache.songs.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DeviceUtils {

  public final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

  public static boolean isQ() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
  }

  public static boolean isPie() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
  }

  public static boolean isOreo() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
  }

  public static boolean isNextNougat() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
  }

  public static boolean isNougat() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
  }

  public static boolean isMarshmallow() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  public static boolean isNextLollipop() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
  }

  public static boolean isLollipop() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  private static File getExternalFilesDir(Context context) {
    return context.getExternalFilesDir(null);
  }

  private static File getExternalFilesSongsDir(Context context) {
    return new File(getExternalFilesDir(context), "songs");
  }

  private static File getExternalFilesSongsDirFilePath(Context context, String filename) {
    return new File(getExternalFilesSongsDir(context), filename);
  }

  private static File getInternalFilesDir(Context context) {
    return context.getFilesDir();
  }

  private static File getInternalFilesSongsDir(Context context) {
    return new File(getInternalFilesDir(context), "songs");
  }

  private static File getInternalFilesSongsDirFilePath(Context context, String filename) {
    return new File(getInternalFilesSongsDir(context), filename);
  }

  public static File getDownloadSongsDir(Context context) {
    String songDownloadDir = PrefUtils.getInstance().getString(PrefUtils.SETTINGS_DOWNLOAD_LOCATION_PREF, PrefUtils.SETTINGS_DOWNLOAD_LOCATION_DEFAULT_VALUE);
    if ("in_app".equals(songDownloadDir)) {
      return getInternalFilesSongsDir(context);
    }
    return getExternalFilesSongsDir(context);
  }

  public static File getDownloadSongsDirFilePath(Context context, String fileName) {
    String songDownloadDir = PrefUtils.getInstance().getString(PrefUtils.SETTINGS_DOWNLOAD_LOCATION_PREF, PrefUtils.SETTINGS_DOWNLOAD_LOCATION_DEFAULT_VALUE);
    if ("in_app".equals(songDownloadDir)) {
      return getInternalFilesSongsDirFilePath(context, fileName);
    }
    return getExternalFilesSongsDirFilePath(context, fileName);
  }

  public static File getInternalCacheDir(Context context) {
    return context.getCacheDir();
  }

  public static File getInternalCacheDirFilePath(Context context, String filename) {
    return new File(getInternalCacheDir(context), filename);
  }

  public static boolean areFilesSame(String file_url, File theFile) throws IOException {
    long lastModified = theFile.lastModified();
    boolean are_the_same = false;
    try {
      URL url = new URL(file_url);
      URLConnection connection = url.openConnection();
      connection.connect();
      if (lastModified != 0) {

        are_the_same = (connection.getLastModified() < lastModified);

      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return are_the_same;
  }

  public static void hideKeyboard(Activity activity) {
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    //Find the currently focused view, so we can grab the correct window token from it.
    View view = activity.getCurrentFocus();
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
      view = new View(activity);
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  public static void showKeyboardOn(Context context, View view) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
  }

  public static void hideKeyboardFrom(Context context, View view) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}
