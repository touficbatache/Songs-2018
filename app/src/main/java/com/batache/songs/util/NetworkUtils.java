package com.batache.songs.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public static boolean isNetworkAvailable(NetworkInfo activeNetworkInfo) {
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

}