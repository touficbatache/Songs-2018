package com.batache.songs.util;

import android.net.Uri;

import com.batache.songs.SongsApplication;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;
import com.batache.songs.util.events.ImportEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PlaylistImporter {
  private static PlaylistImporter sInstance;

  public static void initialize() {
    if (sInstance == null) {
      sInstance = new PlaylistImporter(SongsApplication.get());
    }
  }

  public static PlaylistImporter getInstance() {
    if (sInstance == null) {
      throw new RuntimeException("Playlist importer not initialized");
    }
    return sInstance;
  }

  private SongsApplication context;

  private PlayerManager playerManager;

  private List<SongUtils.ExportedPlaylist> importingPlaylists = new ArrayList<>();

  private PlaylistImporter(SongsApplication songsApplication) {
    // we should always use the application context to avoid memory leaks
    context = songsApplication;

    playerManager = PlayerManager.getInstance();
  }

  public void importPlaylist(Uri data) {
    StringBuilder text = new StringBuilder();

    try {
      InputStream inputStream = context.getContentResolver().openInputStream(data);
      BufferedReader br = null;
      if (inputStream != null) {
        br = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = br.readLine()) != null) {
          text.append(line);
          text.append("\n");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    SongUtils.ExportedPlaylist exportedPlaylist = SongUtils.getExportedPlaylist(text.toString());
    importingPlaylists.add(exportedPlaylist);

    ImportEvent.postImportQueued();

    if (importingPlaylists.size() > 1) {
      return;
    }

    startImport();
  }

  private void startImport() {
    SongUtils.ExportedPlaylist importingPlaylist = importingPlaylists.get(0);

    ImportEvent.postImportProgressChanged(0);

    List<Song> songs = new ArrayList<>();

//    for (SongUtils.ExportedPlaylist.ExportedSong exportedSong : importingPlaylists.get(0).songs) {
//
//      playerManager.insertSong();
//      songs.add();
//    }

//    String playlistId = playerManager.createPlaylist(importingPlaylist.title);
//
//    for (Song song : songs) {
//      playerManager.insertSongInPlaylist(playlistId, song.getId());
//    }

    importingPlaylists.remove(0);
    ImportEvent.postImportComplete();

    if (importingPlaylists.size() > 0) {
      startImport();
    }
  }

  public List<SongUtils.ExportedPlaylist> getImportingPlaylists() {
    return importingPlaylists;
  }

  public interface OnImportListener {
    void onSuccess();
  }

  public static void destroy() {
    if (sInstance == null) {
      return;
    }
    sInstance = null;
  }
}
