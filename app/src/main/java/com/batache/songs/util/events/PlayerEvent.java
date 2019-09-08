package com.batache.songs.util.events;

import org.greenrobot.eventbus.EventBus;

public class PlayerEvent {

  public Action action;

  public PlayerEvent(Action action) {
    this.action = action;
  }

  public enum Action {
    PLAY_QUEUE_CHANGED, PLAY_QUEUE_READY, SONG_CHANGED, PROGRESS_CHANGED, FAVORITE_CHANGED, ADD_TO_PLAYLIST_CHANGED, REPEAT_CHANGED, PLAY_PAUSE_CHANGED, SHUFFLE_CHANGED, SONG_DELETED
  }

  public static void postPlayQueueChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.PLAY_QUEUE_CHANGED));
  }

  public static void postPlayQueueReady() {
    EventBus.getDefault().post(new PlayerEvent(Action.PLAY_QUEUE_READY));
  }

  public static void postSongChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.SONG_CHANGED));
  }

  public static void postFavoriteChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.FAVORITE_CHANGED));
  }

  public static void postAddToPlaylistChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.ADD_TO_PLAYLIST_CHANGED));
  }

  public static void postProgressChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.PROGRESS_CHANGED));
  }

  public static void postRepeatChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.REPEAT_CHANGED));
  }

  public static void postPlayPauseChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.PLAY_PAUSE_CHANGED));
  }

  public static void postShuffleChanged() {
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.SHUFFLE_CHANGED));
  }

  public static void postSongDeleted() {
    EventBus.getDefault().post(new PlayerEvent(Action.SONG_DELETED));
  }

}
