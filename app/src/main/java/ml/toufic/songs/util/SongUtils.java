package ml.toufic.songs.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.model.DownloadedSong;
import ml.toufic.songs.model.PlayableSong;

public class SongUtils {

  public static final String CACHED_SONGS_KEY = "downloadedSongs";
  public static final String CACHED_LAST_SONG_KEY = "lastSong";

  public final static String NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
  public final static String POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
  public final static String API_KEY = "YOUR_YOUTUBE_API_KEY";

  public static void cacheDownloadedSongs(Context context, List<DownloadedSong> songs) {
    DeviceUtils.cacheData(context, CACHED_SONGS_KEY, new Gson().toJson(songs));
  }

  public static ArrayList<DownloadedSong> getCachedDownloadedSongs(Context context) {
    if(DeviceUtils.getCachedData(context, CACHED_SONGS_KEY) != null) {
      return new Gson().fromJson(DeviceUtils.getCachedData(context, CACHED_SONGS_KEY), new TypeToken<List<DownloadedSong>>() {}.getType());
    } else {
      return new ArrayList<DownloadedSong>();
    }
  }

  public static void cacheLastSong(Context context, PlayableSong song) {
    DeviceUtils.cacheData(context, CACHED_LAST_SONG_KEY, new Gson().toJson(song));
  }

  public static PlayableSong getCachedLastSong(Context context) {
    if(DeviceUtils.getCachedData(context, CACHED_LAST_SONG_KEY) != null) {
      return new Gson().fromJson(DeviceUtils.getCachedData(context, CACHED_LAST_SONG_KEY), new TypeToken<PlayableSong>() {}.getType());
    }
    return null;
  }

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

  public static boolean isSongInMyMusic(Context context, String songId) {
    List<DownloadedSong> downloadedSongs = SongUtils.getCachedDownloadedSongs(context);
    boolean isInMyMusic = false;
    for(DownloadedSong downloadedSong : downloadedSongs) {
      if(downloadedSong.getPath().toString().contains(songId)) {
        isInMyMusic = true;
      }
    }
    return isInMyMusic;
  }

}
