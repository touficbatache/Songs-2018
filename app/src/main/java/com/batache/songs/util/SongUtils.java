package com.batache.songs.util;

import android.net.Uri;

import com.batache.songs.db.Deserializers;
import com.batache.songs.model.pojo.Song;
import com.cyl.musicapi.playlist.ArtistsItem;
import com.cyl.musicapi.playlist.MusicInfo;
import com.google.gson.GsonBuilder;
import com.tonyodev.fetch2.Download;

public class SongUtils {

  public static final String SONG_EXTRA = "SONG_EXTRA";

  public static final String PLAYLIST_LIKES_ID = "1";
  public static final String PLAYLIST_DOWNLOADS_ID = "2";

  public static final String NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
  public static final String POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
  public static final String API_KEY = "YOUR_YOUTUBE_API_KEY";
  public static final String API_KEY_2 = "YOUR_YOUTUBE_API_KEY_2";
  public static final String AS_API_KEY = "YOUR_LASTFM_API_KEY"; //AudioScrobbler API Key

  public static final String SEARCH_KEY_QQ = "QQ";
  public static final String SEARCH_KEY_XIAMI = "XIAMI";
  public static final String SEARCH_KEY_NETEASE = "NETEASE";

  public static final String SONG_KEY_QQ = "qq";
  public static final String SONG_KEY_XIAMI = "xiami";
  public static final String SONG_KEY_BAIDU = "baidu";
  public static final String SONG_KEY_NETEASE = "netease";

  private static int PIC_SIZE_SMALL = 0;
  private static int PIC_SIZE_NORMAL = 1;
  private static int PIC_SIZE_BIG = 2;

  public static String toString(Song song) {
    return new GsonBuilder()
        .registerTypeAdapter(Uri.class, new Deserializers.UriInOut())
        .create()
        .toJson(song);
  }

  public static Song getSong(Download download) {
    return new GsonBuilder()
        .registerTypeAdapter(Uri.class, new Deserializers.UriInOut())
        .create()
        .fromJson(download.getExtras().getMap().get(SONG_EXTRA), Song.class);
  }

  public static String getSongFileName(String songId, Song.SongSource songSource) {
    return songId + "-" + songSource.name() + ".m4a";
  }

  public static String getSongId(String fileName) {
    return fileName.split(".m4a")[0];
  }

  public static Song getSong(MusicInfo musicInfo) {
    Song song = new Song();
    if (musicInfo.getSongId() != null) {
      song.setId(musicInfo.getSongId());
    } else if (musicInfo.getId() != null) {
      song.setId(musicInfo.getId());
    }
    song.setTitle(musicInfo.getName());
//    song.type = musicInfo.vendor
//    song.album = musicInfo.album.name
//    song.albumId = musicInfo.album.id
//    song.isCp = musicInfo.cp
//    song.isDl = musicInfo.dl
//    musicInfo.quality?.let {
//      song.sq = it.sq
//      song.hq = it.hq
//      song.high = it.high
//    }
    if (musicInfo.getArtists() != null) {
      StringBuilder artistIds = new StringBuilder();
      StringBuilder artistNames = new StringBuilder();
      for (ArtistsItem artist : musicInfo.getArtists()) {
        artistIds.append(", ").append(artist.getId());
        artistNames.append(", ").append(artist.getName());
      }
      song.setArtist(artistNames.toString());
//      song.artistId = artistIds.toString()
    }
    song.setCoverArt(getAlbumPic(musicInfo.getAlbum().getCover(), musicInfo.getVendor(), PIC_SIZE_NORMAL));
//    song.coverBig = getAlbumPic(musicInfo.album.cover, musicInfo.vendor, PIC_SIZE_NORMAL);
//    song.coverSmall = getAlbumPic(musicInfo.album.cover, musicInfo.vendor, PIC_SIZE_SMALL);

    // TODO: set song uri
    return song;
  }

  private static String getAlbumPic(String url, String type, int size) {
    String tmp;

    String returnValue = "";
    switch (type) {
      case SONG_KEY_QQ:
        if (size == PIC_SIZE_SMALL) {
          returnValue = url.replace("150x150", "90x90");
        } else if (size == PIC_SIZE_BIG) {
          returnValue = url.replace("150x150", "300x300");
        }
        break;
      case SONG_KEY_XIAMI:
        tmp = url.split("@")[0];
        if (size == PIC_SIZE_SMALL) {
          returnValue = tmp + "@1e_1c_100Q_90w_90h";
        } else if (size == PIC_SIZE_NORMAL) {
          returnValue = tmp + "@1e_1c_100Q_150w_150h";
        } else {
          returnValue = tmp + "@1e_1c_100Q_450w_450h";
        }
        break;
      case SONG_KEY_NETEASE:
        tmp = url.split("\\?")[0];
        if (size == PIC_SIZE_SMALL) {
          returnValue = tmp + "?param=90y90";
        } else if (size == PIC_SIZE_NORMAL) {
          returnValue = tmp + "?param=150y150";
        } else {
          returnValue = tmp + "?param=450y450";
        }
        break;
      case SONG_KEY_BAIDU:
        tmp = url.split("@")[0];
        if (size == PIC_SIZE_SMALL) {
          returnValue = tmp + "@s_1,w_90,h_90";
        } else if (size == PIC_SIZE_NORMAL) {
          returnValue = tmp + "@s_1,w_150,h_150";
        } else {
          returnValue = tmp + "@s_1,w_450,h_450";
        }
        break;
    }
    return returnValue;
  }

}
