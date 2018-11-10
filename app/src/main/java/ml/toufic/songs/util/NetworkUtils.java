package ml.toufic.songs.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

  private static boolean IS_ONLINE = false;

  public static boolean isOnline(Context context) {
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    IS_ONLINE = networkInfo != null && networkInfo.isConnected();
    return IS_ONLINE;
  }

}