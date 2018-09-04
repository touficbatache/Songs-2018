package ml.toufic.songs.util;

public class SongUtils {

  public final static String PLAYLIST_LIKES_ID = "1";
  public final static String PLAYLIST_DOWNLOADS_ID = "2";

  public final static String NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
  public final static String POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
  public final static String API_KEY = "YOUR_YOUTUBE_API_KEY";

  public static String getSongFileName(String songId) {
    return songId + ".m4a";
  }

  public static String getSongId(String fileName) {
    return fileName.split(".m4a")[0];
  }

}
