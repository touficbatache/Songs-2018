package com.batache.songs.util.events;

public class PlayerEvent {

  public Action action;

  public PlayerEvent(Action action) {
    this.action = action;
  }

  public enum Action {
    SONG_CHANGED, PROGRESS_CHANGED
  }

}
