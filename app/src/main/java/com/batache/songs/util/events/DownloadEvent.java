package com.batache.songs.util.events;

import com.batache.songs.model.pojo.Song;

import org.greenrobot.eventbus.EventBus;

public class DownloadEvent {

  public Song song;
  public int progress = 0;
  public Action action;

  public DownloadEvent(Song song, Action action) {
    this.song = song;
    this.action = action;
  }

  public DownloadEvent(Song song, int progress, Action action) {
    this.song = song;
    this.progress = progress;
    this.action = action;
  }

  public enum Action {
    DOWNLOAD_STARTED, DOWNLOAD_PROGRESS_CHANGED, DOWNLOAD_CANCELLED, DOWNLOAD_COMPLETE
  }

  public static void postDownloadStarted(Song song) {
    EventBus.getDefault().post(new DownloadEvent(song, DownloadEvent.Action.DOWNLOAD_STARTED));
  }

  public static void postDownloadProgressChanged(Song song, int progress) {
    EventBus.getDefault().post(new DownloadEvent(song, progress, DownloadEvent.Action.DOWNLOAD_PROGRESS_CHANGED));
  }

  public static void postDownloadCancelled(Song song) {
    EventBus.getDefault().post(new DownloadEvent(song, DownloadEvent.Action.DOWNLOAD_CANCELLED));
  }

  public static void postDownloadComplete(Song song) {
    EventBus.getDefault().post(new DownloadEvent(song, Action.DOWNLOAD_COMPLETE));
  }

}
