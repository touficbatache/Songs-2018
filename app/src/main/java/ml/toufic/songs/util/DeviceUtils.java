package ml.toufic.songs.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DeviceUtils {

  public final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

  public static boolean isLollipop() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  public static boolean isNextLollipop() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
  }

  public static boolean isNougat() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
  }

  public static boolean isNextNougat() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
  }

  public static boolean isMarshmallow() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  public static boolean isOreo() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
  }

  public static File getDownloadStorageDirectory() {
    return new File(Environment.getExternalStorageDirectory() + "/Songs/downloads/");
  }

  public static String getDownloadStorageDirectoryPath() {
    return Environment.getExternalStorageDirectory() + "/Songs/downloads/";
  }

  public static String getDownloadStorageDirectorySmallPath() {
    return "/Songs/downloads/";
  }

  public static File getImagesStorageDirectory() {
    return new File(Environment.getExternalStorageDirectory() + "/Songs/images/");
  }

  public static File getMediaCacheStorageDirectory() {
    return new File(Environment.getExternalStorageDirectory() + "/Songs/cache/");
  }

  public static boolean areFilesSame(String file_url, String filename) throws IOException {
    File the_file = new File(getMediaCacheStorageDirectory(), filename);
    long lastModified = the_file.lastModified();
    boolean are_the_same = false;
    try {
      URL url = new URL(file_url);
      URLConnection connection = url.openConnection();
      connection.connect();
      if(lastModified != 0)
      {

        are_the_same = (connection.getLastModified() < lastModified);

      }
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }

    return are_the_same;
  }

  public static void cacheData(Context context, String sharedPreferencesKey, String cache) {
    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor prefsEditor = mPrefs.edit();
    prefsEditor.putString(sharedPreferencesKey, cache);
    prefsEditor.apply();
  }

  public static String getCachedData(Context context, String sharedPreferencesName) {
    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    return mPrefs.getString(sharedPreferencesName, null);
  }

}
