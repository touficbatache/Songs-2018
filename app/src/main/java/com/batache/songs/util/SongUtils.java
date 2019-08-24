package com.batache.songs.util;

import com.batache.songs.model.Song;

public class SongUtils {

  public static final String PLAYLIST_LIKES_ID = "1";
  public static final String PLAYLIST_DOWNLOADS_ID = "2";

  public static final String NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
  public static final String POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
  public static final String API_KEY = "YOUR_YOUTUBE_API_KEY";
  public static final String API_KEY_2 = "YOUR_YOUTUBE_API_KEY_2";
  public static final String AS_API_KEY = "YOUR_LASTFM_API_KEY"; //AudioScrobbler API Key

  public static String getSongFileName(String songId, Song.SongSource songSource) {
    return songId + "-" + songSource.name() + ".m4a";
  }

  public static String getSongId(String fileName) {
    return fileName.split(".m4a")[0];
  }

}
