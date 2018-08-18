package ml.toufic.songs.util;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.model.PlayableSong;

public class SongUtils {

  public static final int DOWNLOAD_COMPLETE_SONG_ACTIVITY_REQUEST_CODE = 468;
  public static final String EXTRA_REPLY = "com.example.android.wordlistsql.REPLY";

  private static final String CACHED_SONGS_KEY = "downloadedSongs";
  private static final String CACHED_LAST_SONG_KEY = "lastSong";

  public final static String NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
  public final static String POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
  public final static String API_KEY = "YOUR_YOUTUBE_API_KEY";

  public static File getSongPath(String filename) {
    return new File(DeviceUtils.getDownloadStorageDirectory(), filename);
  }

  public static String getSongFileName(String songId) {
    return songId + ".m4a";
  }

  public static String getSongId(String fileName) {
    return fileName.split(".m4a")[0];
  }

  public static boolean isSongInDownloadsDir(String songId) {
    boolean isDownloaded = false;
    File[] downloadedSongsInDirectory = DeviceUtils.getDownloadStorageDirectory().listFiles();
    for(File songInDirectory : downloadedSongsInDirectory) {
      if(songInDirectory.getName().contains(songId)) {
        isDownloaded = true;
      }
    }
    return isDownloaded;
  }

}
