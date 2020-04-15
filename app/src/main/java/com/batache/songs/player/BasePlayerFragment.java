package com.batache.songs.player;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.batache.songs.model.pojo.Song;
import com.batache.songs.app.base.BaseFragment;
import com.batache.songs.util.events.DownloadEvent;
import com.batache.songs.util.events.PlayerEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BasePlayerFragment extends BaseFragment {

  Context context;
  PlayerManager playerManager;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // we should always use the application context to avoid memory leaks
    context = getContext().getApplicationContext();
    playerManager = PlayerManager.getInstance();
  }

  @Override
  public void onCreateView() {
    initViews();
    initComponentListener();
    attachViewsToComponentListener();
  }

  abstract class BaseComponentListener implements View.OnClickListener {
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onPlayerEvent(PlayerEvent playerEvent) {
    switch (playerEvent.action) {
      case PLAY_QUEUE_CHANGED:
        onPlayQueueChanged();
        break;
      case PLAY_QUEUE_READY:
        onPlayQueueReady();
        break;
      case SONG_CHANGED:
        onSongChanged();
        break;
      case FAVORITE_CHANGED:
        onFavoriteChanged();
        break;
      case ADD_TO_PLAYLIST_CHANGED:
        onAddToPlaylistChanged();
        break;
      case PROGRESS_CHANGED:
        onProgressChanged();
        break;
      case REPEAT_CHANGED:
        onRepeatChanged();
        break;
      case PLAY_PAUSE_CHANGED:
        onPlayPauseChanged();
        break;
      case SHUFFLE_CHANGED:
        onShuffleChanged();
        break;
      case SONG_DELETED:
        onSongDeleted();
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onDownloadEvent(DownloadEvent downloadEvent) {
    switch (downloadEvent.action) {
      case DOWNLOAD_STARTED:
        onDownloadStarted(downloadEvent.song);
        break;
      case DOWNLOAD_PROGRESS_CHANGED:
        onDownloadProgressChanged(downloadEvent.song, downloadEvent.progress);
        break;
      case DOWNLOAD_CANCELLED:
        onDownloadCancelled(downloadEvent.song);
        break;
      case DOWNLOAD_COMPLETE:
        onDownloadComplete(downloadEvent.song);
        break;
    }
  }

  public abstract void initViews();

  public abstract void initComponentListener();

  public abstract void attachViewsToComponentListener();

  public abstract void onPlayQueueChanged();

  public abstract void onPlayQueueReady();

  public abstract void onSongChanged();

  public abstract void onProgressChanged();

  public abstract void onFavoriteChanged();

  public abstract void onAddToPlaylistChanged();

  public abstract void onDownloadStateChanged();

  public abstract void onDownloadStarted(Song song);

  public abstract void onDownloadProgressChanged(Song song, int progress);

  public abstract void onDownloadCancelled(Song song);

  public abstract void onDownloadComplete(Song song);

  public abstract void onSongDeleted();

  public abstract void onRepeatChanged();

  public abstract void onPlayPauseChanged();

  public abstract void onShuffleChanged();

  public abstract void update();

}
