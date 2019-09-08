package com.batache.songs.app.player;

import android.view.View;
import android.widget.TextView;

import com.batache.songs.R;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.ui.view.MorphingImageView;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MiniPlayerFragment extends BasePlayerFragment {

  private TextView titleTv;

  private MaterialProgressBar progressBar;
  private MorphingImageView playPauseBtn;

  private MiniPlayerComponentListener componentListener;

  @Override
  public int getLayout() {
    return R.layout.fragment_mini_player;
  }

  @Override
  public void initViews() {
    titleTv = mRootView.findViewById(R.id.mini_player_song_title);

    progressBar = mRootView.findViewById(R.id.mini_player_song_progress);
    playPauseBtn = mRootView.findViewById(R.id.mini_player_play_to_pause_btn);
  }

  @Override
  public void initComponentListener() {
    componentListener = new MiniPlayerComponentListener();
  }

  public class MiniPlayerComponentListener extends BaseComponentListener {
    @Override
    public void onClick(View view) {
      if (playPauseBtn == view) {
        if (view.isEnabled()) {
          if (!playerManager.isPlaying()) {
            playerManager.resume();
          } else {
            playerManager.pause();
          }
        }
      }
    }
  }

  @Override
  public void attachViewsToComponentListener() {
    playPauseBtn.setOnClickListener(componentListener);
  }

  @Override
  public void onApplyAllWindowInsets() {

  }

  public void updateVisibility(float alpha) {
    mRootView.setAlpha(alpha);
    mRootView.setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onPlayQueueChanged() {
    setPlayerLoading(true);
  }

  @Override
  public void onPlayQueueReady() {
    setPlayerLoading(false);
  }

  private void setPlayerLoading(boolean loading) {
    playPauseBtn.setEnabled(!loading);
    if (loading) {
      progressBar.setProgress(0);
      progressBar.setSecondaryProgress(0);
    }
  }

  @Override
  public void onSongChanged() {
    titleTv.setText(playerManager.getCurrentSongTitle());
  }

  @Override
  public void onProgressChanged() {
    progressBar.setMax((int) playerManager.getSongDuration() / 1000);
    progressBar.setProgress((int) playerManager.getCurrentPosition() / 1000);
    progressBar.setSecondaryProgress((int) playerManager.getSongBufferedPosition() / 1000);
  }

  @Override
  public void onFavoriteChanged() {

  }

  @Override
  public void onAddToPlaylistChanged() {

  }

  @Override
  public void onDownloadStateChanged() {

  }

  @Override
  public void onDownloadStarted(Song song) {

  }

  @Override
  public void onDownloadProgressChanged(Song song, int progress) {

  }

  @Override
  public void onDownloadCancelled(Song song) {

  }

  @Override
  public void onDownloadComplete(Song song) {

  }

  @Override
  public void onSongDeleted() {

  }

  @Override
  public void onRepeatChanged() {

  }

  @Override
  public void onPlayPauseChanged() {
    if (!playerManager.isPlaying()) {
      playPauseBtn.animateToFirstState(); //play icon: paused
    } else {
      playPauseBtn.animateToSecondState(); //pause icon: playing
    }
  }

  @Override
  public void onShuffleChanged() {

  }

  @Override
  public void update() {
    onSongChanged();
    onPlayPauseChanged();
  }
}
