package com.batache.songs.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.api.SongDownloader;
import com.batache.songs.app.MainActivity;
import com.batache.songs.app.lyrics.LyricsActivity;
import com.batache.songs.app.playingqueue.PlayingQueueFragment;
import com.batache.songs.model.adapter.PlayerCoverModel;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.ui.view.CloudDownloadBorderToFillImageView;
import com.batache.songs.ui.view.IconToggleView;
import com.batache.songs.ui.view.MorphingImageView;
import com.batache.songs.ui.view.RepeatToggleView;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.batache.songs.util.ImageLoader;
import com.batache.songs.util.TextUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tonyodev.fetch2.Download;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class PlayerFragment extends BasePlayerFragment {

  private MainActivity activity;

  private boolean isPlayerLoading = false;

  private ImageButton arrowDownBtn;
  private ImageButton playingQueueBtn;

  private EpoxyRecyclerView recyclerView;
  private RecyclerView.OnScrollListener recyclerViewScrollListener;
  private PlayerCoversController controller;

  private TextView titleTv;
  private TextView artistTv;

  private IconToggleView favoriteBtn;
  private MaterialProgressBar downloadProgress;
  private CloudDownloadBorderToFillImageView cloudDownloadBtn;
  private IconToggleView addToPlaylistBtn;
  private ImageView shareBtn;

  private SeekBar progressBar;
  private TextView progressCurrentTv;
  private TextView progressTotalTv;

  private RepeatToggleView repeatBtn;
  private ImageButton prevBtn;
  private MorphingImageView playPauseBtn;
  private ImageButton nextBtn;
  private ImageButton shuffleBtn;

  private PlayerComponentListener componentListener;

  @Override
  public int getLayout() {
    return R.layout.fragment_player;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activity = (MainActivity) getActivity();
  }

  @Override
  public void initViews() {
    arrowDownBtn = rootView.findViewById(R.id.player_arrow_down);
    playingQueueBtn = rootView.findViewById(R.id.player_see_playing_queue);

    recyclerView = rootView.findViewById(R.id.player_cover_arts_recyclerview);
    recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int songIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
          playerManager.play(songIndex);
        }
      }
    };

    titleTv = rootView.findViewById(R.id.player_song_title);
    artistTv = rootView.findViewById(R.id.player_song_artist);

    favoriteBtn = rootView.findViewById(R.id.player_action_favorite);
    downloadProgress = rootView.findViewById(R.id.player_download_progress);
    downloadProgress.setMax(100);
    cloudDownloadBtn = rootView.findViewById(R.id.player_action_cloud_download);
    addToPlaylistBtn = rootView.findViewById(R.id.player_action_add_to_playlist);
    shareBtn = rootView.findViewById(R.id.player_action_share);

    progressBar = rootView.findViewById(R.id.player_song_progress);
    progressCurrentTv = rootView.findViewById(R.id.player_progress_current);
    progressTotalTv = rootView.findViewById(R.id.player_progress_total);

    repeatBtn = rootView.findViewById(R.id.player_repeat_btn);
    prevBtn = rootView.findViewById(R.id.player_prev_btn);
    playPauseBtn = rootView.findViewById(R.id.player_play_to_pause_btn);
    nextBtn = rootView.findViewById(R.id.player_next_btn);
    shuffleBtn = rootView.findViewById(R.id.player_shuffle_btn);
  }

  @Override
  public void initComponentListener() {
    componentListener = new PlayerComponentListener();
  }

  private String dialogPlaylistName;

  public class PlayerComponentListener extends BaseComponentListener implements PlayerCoverModel.PlayerCoverCallback, CloudDownloadBorderToFillImageView.OnDownloadClickListener, SeekBar.OnSeekBarChangeListener {
    @Override
    public void onClick(View view) {
      if (arrowDownBtn == view) {
        activity.collapsePlayer();
      } else if (playingQueueBtn == view) {
        new PlayingQueueFragment().show(activity.getSupportFragmentManager(), "playing_queue_fragment");
      } else if (favoriteBtn == view) {
        playerManager.addToRemoveFromFavorites();
      } else if (addToPlaylistBtn == view) {
        final List<Playlist> customPlaylists = playerManager.getAllPlaylists().subList(2, playerManager.getAllPlaylists().size());

        List<String> playlistTitles = new ArrayList<>();
        List<Boolean> checkedPlaylists = new ArrayList<>();

        playlistTitles.add("New playlist");
        checkedPlaylists.add(false);

        for (Playlist playlist : customPlaylists) {
          playlistTitles.add(playlist.getTitle());
          checkedPlaylists.add(playerManager.isCurrentSongInPlaylist(playlist.getId()));
        }

        new MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.select_playlists)
            .setMultiChoiceItems(playlistTitles.toArray(new CharSequence[playlistTitles.size()]), toPrimitiveArray(checkedPlaylists), (dialogInterface, which, checked) -> checkedPlaylists.set(which, checked))
            .setCancelable(true)
            .setPositiveButton(R.string.save, (dialogInterface, which) -> {
              for (Playlist playlist : customPlaylists) {
                playerManager.removeCurrentSongFromPlaylist(playlist.getId());
              }
              if (checkedPlaylists.size() > 0) {
                for (int i = 0; i < checkedPlaylists.size(); i++) {
                  if (checkedPlaylists.get(i)) {
                    if (i == 0) {
                      AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                          .setTitle(R.string.create_new_playlist)
                          .setView(R.layout.layout_outlined_text_field)
                          .setCancelable(false)
                          .setPositiveButton(R.string.create, (dialogInterface1, which1) -> playerManager.createPlaylistAndInsertCurrentSong(dialogPlaylistName))
                          .setNegativeButton(R.string.cancel, null)
                          .show();

                      TextInputLayout playlistNameHint = dialog.findViewById(R.id.input_layout);
                      playlistNameHint.setHint(getResources().getString(R.string.title));
                      TextInputEditText playlistNameEt = dialog.findViewById(R.id.edit_text);
                      playlistNameEt.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                          dialogPlaylistName = charSequence.toString().trim();
                          if (dialogPlaylistName.equals("")) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                          }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                      });
                    } else {
                      int playlistIndex = i - 1;
                      playerManager.addToPlaylist(customPlaylists.get(playlistIndex).getId());
                    }
                  }
                }
              }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
      } else if (shareBtn == view) {
        playerManager.shareCurrentSong(activity);
      } else if (repeatBtn == view) {
        playerManager.toggleRepeat();
      } else if (prevBtn == view) {
        playerManager.playPrev();
      } else if (playPauseBtn == view) {
        if (view.isEnabled()) {
          if (!playerManager.isPlaying()) {
            playerManager.resume();
          } else {
            playerManager.pause();
          }
        }
      } else if (nextBtn == view) {
        playerManager.playNext();
      } else if (shuffleBtn == view) {
        playerManager.shuffle();
      }
    }

    @Override
    public void onLyricsBtnClick() {
      if (playerManager.isCurrentSongLocallyStored()) {
        startActivity(new Intent(context, LyricsActivity.class));
      } else {
        new MaterialAlertDialogBuilder(activity)
            .setTitle("Lyrics not available")
            .setMessage("Lyrics are not available for songs that you haven't downloaded. Download this song to see its lyrics.")
            .setCancelable(true)
            .setPositiveButton("Ok", null)
            .show();
      }
    }

    @Override
    public void onSeekPrevious(LinearLayout view) {
      int seekTo = Math.max(0, (int) playerManager.getCurrentPosition() - 10000);
      playerManager.seekTo(seekTo);
      forceRippleAnimation(view);
    }

    @Override
    public void onSeekNext(LinearLayout view) {
      int seekTo = Math.min((int) playerManager.getCurrentPosition() + 10000, (int) playerManager.getSongDuration());
      playerManager.seekTo(seekTo);
      forceRippleAnimation(view);
    }

    @Override
    public void onDownloadStart() {
      if (playerManager.downloadCurrentSong()) {
        showDownloadLoading(true, true);
      }
    }

    @Override
    public void onDownloadCancel() {
      if (playerManager.cancelCurrentSongDownload()) {
        cloudDownloadBtn.showBorder();
        downloadProgress.setProgress(0);
      }
    }

    @Override
    public void onDownloadDelete() {
      new MaterialAlertDialogBuilder(activity)
          .setTitle(R.string.delete_song)
          .setMessage(R.string.delete_song_warning)
          .setCancelable(true)
          .setPositiveButton(R.string.delete, (dialogInterface, i) -> playerManager.deleteCurrentSong())
          .setNegativeButton(R.string.cancel, null)
          .show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        playerManager.seekTo(progress * 1000);
      }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
  }

  private boolean[] toPrimitiveArray(final List<Boolean> booleanList) {
    final boolean[] primitives = new boolean[booleanList.size()];
    int index = 0;
    for (Boolean object : booleanList) {
      primitives[index++] = object;
    }
    return primitives;
  }

  private void forceRippleAnimation(View view) {
    Drawable foreground = view.getForeground();

    if (Build.VERSION.SDK_INT >= 21 && foreground instanceof RippleDrawable) {
      final RippleDrawable rippleDrawable = (RippleDrawable) foreground;

      rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

      Handler handler = new Handler();

      handler.postDelayed(() -> rippleDrawable.setState(new int[]{}), 200);
    }
  }

  @Override
  public void attachViewsToComponentListener() {
    arrowDownBtn.setOnClickListener(componentListener);
    playingQueueBtn.setOnClickListener(componentListener);

    controller = new PlayerCoversController(context, componentListener);
    recyclerView.setController(controller);
    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);

    favoriteBtn.setOnClickListener(componentListener);
    cloudDownloadBtn.setOnDownloadClickedListener(componentListener);
    addToPlaylistBtn.setOnClickListener(componentListener);
    shareBtn.setOnClickListener(componentListener);

    progressBar.setOnSeekBarChangeListener(componentListener);

    repeatBtn.setOnClickListener(componentListener);
    prevBtn.setOnClickListener(componentListener);
    playPauseBtn.setOnClickListener(componentListener);
    nextBtn.setOnClickListener(componentListener);
    shuffleBtn.setOnClickListener(componentListener);
  }

  public void addListenerToRecyclerView() {
    recyclerView.addOnScrollListener(recyclerViewScrollListener);
  }

  public void clearListenerFromRecyclerView() {
    recyclerView.clearOnScrollListeners();
  }

  @Override
  public void onApplyAllWindowInsets() {
    rootView.setPadding(0, DimensionUtils.TOP_INSET, 0, DimensionUtils.BOTTOM_INSET);
  }

  public void setClickable(boolean clickable) {
    rootView.setClickable(clickable);
  }

  @Override
  public void onPlayQueueChanged() {
    setPlayerLoading(true);
  }

  @Override
  public void onPlayQueueReady() {
    if (CodeUtils.isEmpty(playerManager.getPlayqueue())) {
      activity.hidePlayer();
      return;
    } else if (activity.getPlayerState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      activity.collapsePlayer();
    }
    controller.reset();
    controller.addCoverArts(playerManager.getPlayqueue());
    setPlayerLoading(false);
  }

  private void setPlayerLoading(boolean loading) {
    isPlayerLoading = loading;

    playingQueueBtn.setEnabled(!loading);
    setColorEnabled(playingQueueBtn, !loading);
    favoriteBtn.setEnabled(!loading);
    cloudDownloadBtn.setEnabled(!loading);
    addToPlaylistBtn.setEnabled(!loading);
    shareBtn.setEnabled(!loading);
    setColorEnabled(shareBtn, !loading);
    progressBar.setEnabled(!loading);
    repeatBtn.setEnabled(!loading);
    prevBtn.setEnabled(!loading);
    setColorEnabled(prevBtn, !loading);
    playPauseBtn.setEnabled(!loading);
    nextBtn.setEnabled(!loading);
    setColorEnabled(nextBtn, !loading);
    shuffleBtn.setEnabled(!loading);
    if (loading) {
      progressBar.setProgress(0);
      progressBar.setSecondaryProgress(0);
      progressCurrentTv.setText("");
      progressTotalTv.setText("");
    }
  }

  @Override
  public void onSongChanged() {
    if(playerManager.getCurrentSong() == null) {
      return;
    }

    recyclerView.smoothScrollToPosition(playerManager.getCurrentSongIndex());

    ImageLoader.getBitmap(context, playerManager.getCurrentSong().getId(), playerManager.getCurrentSong().getCoverArt(), new ImageLoader.FetchBitmapCallback() {
      @Override
      public void onCompletion(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        int dominantColorRGB = (palette.getLightVibrantSwatch() != null ? palette.getLightVibrantSwatch() :
            (palette.getVibrantSwatch() != null ? palette.getVibrantSwatch() : (palette.getDarkVibrantSwatch() != null ? palette.getDarkVibrantSwatch() : palette.getDominantSwatch()))).getRgb();
        int dominantColor = dominantColorRGB != -1 ? dominantColorRGB : Color.WHITE;
        updatePlayerBackgroundColor(dominantColor);
      }
    });


    titleTv.setText(playerManager.getCurrentSongTitle());
    titleTv.setSelected(true);
    artistTv.setText(playerManager.getCurrentSongArtist());

    onFavoriteChanged();
    onAddToPlaylistChanged();
    onDownloadStateChanged();

    if (playerManager.isStreaming()) {
      disableDownloadedSongButtons();
    } else {
      if (!isPlayerLoading) {
        enableDownloadedSongButtons();
      }
    }
  }

  private void updatePlayerBackgroundColor(int newColor) {
//    int oldColor = rootView.getBackground() instanceof ColorDrawable ? ((ColorDrawable) rootView.getBackground()).getColor() : getResources().getColor(R.color.grey_e0);
//    ValueAnimator valueAnimator = ObjectAnimator.ofInt(rootView, "backgroundColor", oldColor, newColor);
//    valueAnimator.setDuration(200);
//    valueAnimator.setEvaluator(new ArgbEvaluator());
//    valueAnimator.start();
  }

//  private void getClosestMaterialColor() {
//    String[] readableMaterialColors = {
//        "#EF5350", "#E53935", "#D32F2F", "#C62828", "#B71C1C", "#FF5252", "#FF1744", "#D50000",
//        "#EC407A", "#D81B60", "#C2185B", "#AD1457", "#880E4F", "#FF4081", "#F50057", "#C51162",
//        "#BA68C8", "#AB47BC", "#9C27B0", "#8E24AA", "#7B1FA2", "#6A1B9A", "#4A148C", "#E040FB", "#D500F9"
//    };
//  }

  private void disableDownloadedSongButtons() {
    playingQueueBtn.setEnabled(false);
    setColorEnabled(playingQueueBtn, false);
    repeatBtn.setEnabled(false);
    prevBtn.setEnabled(false);
    setColorEnabled(prevBtn, false);
    nextBtn.setEnabled(false);
    setColorEnabled(nextBtn, false);
    shuffleBtn.setEnabled(false);
  }

  private void enableDownloadedSongButtons() {
    playingQueueBtn.setEnabled(true);
    setColorEnabled(playingQueueBtn, true);
    repeatBtn.setEnabled(true);
    prevBtn.setEnabled(true);
    setColorEnabled(prevBtn, true);
    nextBtn.setEnabled(true);
    setColorEnabled(nextBtn, true);
    shuffleBtn.setEnabled(true);
  }

  @Override
  public void onProgressChanged() {
    progressBar.setMax((int) playerManager.getSongDuration() / 1000);
    progressBar.setProgress((int) playerManager.getCurrentPosition() / 1000);
    int bufferedPosition = playerManager.getSongBufferedPosition() != -1 ? (int) playerManager.getSongBufferedPosition() : (int) playerManager.getSongDuration();
    progressBar.setSecondaryProgress(bufferedPosition / 1000);

    progressCurrentTv.setText(TextUtils.formatMilliseconds(context, (int) playerManager.getCurrentPosition()));
    progressTotalTv.setText(TextUtils.formatMilliseconds(context, (int) playerManager.getSongDuration()));
  }

  @Override
  public void onFavoriteChanged() {
    if (!playerManager.isCurrentSongInFavorites()) {
      favoriteBtn.showFirstState();
    } else {
      favoriteBtn.showSecondState();
    }
    if (playerManager.isStreaming() && CodeUtils.isEmpty(playerManager.getPlayqueue())) {
      activity.hidePlayer();
    }
  }

  @Override
  public void onAddToPlaylistChanged() {
    if (!playerManager.isCurrentSongInCustomPlaylist()) {
      addToPlaylistBtn.showFirstState();
    } else {
      addToPlaylistBtn.showSecondState();
    }
  }

  @Override
  public void onDownloadStateChanged() {
    if (!playerManager.isCurrentSongDownloaded()) {
      cloudDownloadBtn.showBorder();
    } else {
      cloudDownloadBtn.showFill();
    }
    if (!playerManager.isCurrentSongLocallyStored() && playerManager.getCurrentSong() != null) {
      SongDownloader.getInstance().getQueuedSongs(result -> {
        for (Download download : result) {
          SongDownloader.getInstance().getDownloadIdBySongId(playerManager.getCurrentSong().getId(), downloadId -> {
            if (downloadId == -1) {
              return;
            }
            if (downloadId == download.getId()) {
              cloudDownloadBtn.showBlinkingAnimation();
              showDownloadLoading(false, false);
            }
          });
        }
      });

      SongDownloader.getInstance().getDownloadingSongs(result -> {
        for (Download download : result) {
          SongDownloader.getInstance().getDownloadIdBySongId(playerManager.getCurrentSong().getId(), downloadId -> {
            if (downloadId == -1) {
              return;
            }
            if (downloadId == download.getId()) {
              cloudDownloadBtn.showBorder();
              showDownloadLoading(true, true);
            }
          });
        }
      });
    }
    downloadProgress.setProgress(0);
  }

  @Override
  public void onDownloadStarted(Song song) {
    if (playerManager.getCurrentSong().getId().equals(song.getId())) {
      cloudDownloadBtn.showBlinkingAnimation();
      showDownloadLoading(false, true);
    }
  }

  @Override
  public void onDownloadProgressChanged(Song song, int progress) {
    if (playerManager.getCurrentSong().getId().equals(song.getId())) {
      cloudDownloadBtn.showBlinkingAnimation();
      showDownloadLoading(false, false);
      downloadProgress.setProgress(progress);
    }
  }

  @Override
  public void onDownloadCancelled(Song song) {
    if (playerManager.getCurrentSong().getId().equals(song.getId())) {
      cloudDownloadBtn.showBorder();
      showDownloadLoading(false, true);
    }
  }

  @Override
  public void onDownloadComplete(Song song) {
    if (playerManager.getCurrentSong().getId().equals(song.getId())) {
      cloudDownloadBtn.showFill();
      showDownloadLoading(false, true);
    }
  }

  private void showDownloadLoading(boolean loading, boolean resetProgress) {
    cloudDownloadBtn.setEnabled(!loading);
    downloadProgress.setIndeterminate(loading);
    if (resetProgress) {
      downloadProgress.setProgress(0);
    }
  }

  @Override
  public void onSongDeleted() {
    if (playerManager.isStreaming() || CodeUtils.isEmpty(playerManager.getPlayqueue())) {
      activity.hidePlayer();
    }
  }

  @Override
  public void onRepeatChanged() {
    repeatBtn.showState(playerManager.getRepeatMode());
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
    if (playerManager.isShuffled()) {
      shuffleBtn.setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      shuffleBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }

  @Override
  public void update() {
    onPlayQueueReady();
    onSongChanged();
    onPlayPauseChanged();
  }

  private void setColorEnabled(ImageView imageView, boolean enabled) {
    if (enabled) {
      imageView.setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      imageView.setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }

  private void setColorEnabled(ImageButton imageButtonView, boolean enabled) {
    if (enabled) {
      imageButtonView.setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      imageButtonView.setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }
}
