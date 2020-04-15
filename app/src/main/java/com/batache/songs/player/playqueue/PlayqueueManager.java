package com.batache.songs.player.playqueue;

import com.batache.songs.SongsApplication;

public class PlayqueueManager {

  private static PlayqueueManager instance;

  private SongsApplication context;

  private PlayqueueManager(SongsApplication songsApplication) {
    context = songsApplication;
  }

  public static PlayqueueManager getInstance() {
    if (instance == null) {
      instance = new PlayqueueManager(SongsApplication.get());
    }
    return instance;
  }

}
