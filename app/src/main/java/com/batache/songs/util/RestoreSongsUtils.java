package com.batache.songs.util;

import java.util.ArrayList;
import java.util.List;

import com.batache.songs.model.Song;

public class RestoreSongsUtils {

  private static RestoreSongsUtils sInstance;

  private int TOTAL_PLAYLIST_SONGS;
  private int PROGRESS;
  private List<Song> songs = new ArrayList<>();

  public static RestoreSongsUtils initialize(int totalPlaylistSongs) {
    sInstance = new RestoreSongsUtils(totalPlaylistSongs);
    return sInstance;
  }

  private RestoreSongsUtils(int totalPlaylistSongs) {
    TOTAL_PLAYLIST_SONGS = totalPlaylistSongs;
  }

  public void addSuccess(Song song) {
    PROGRESS += 1/TOTAL_PLAYLIST_SONGS;
    songs.add(song);
  }

  public int getProgress() {
    return PROGRESS;
  }

  public List<Song> getSongs() {
    return songs;
  }

}
