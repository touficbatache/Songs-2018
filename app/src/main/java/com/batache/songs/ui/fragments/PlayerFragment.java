package com.batache.songs.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Playlist;
import com.batache.songs.model.PlaylistSongJoin;
import com.batache.songs.model.Song;
import com.batache.songs.network.SongDownloader;
import com.batache.songs.ui.activities.LyricsActivity;
import com.batache.songs.ui.activities.MainActivity;
import com.batache.songs.ui.base.BaseFragment;
import com.batache.songs.ui.controller.PlayerCoversController;
import com.batache.songs.ui.model.PlayerCoverModel;
import com.batache.songs.ui.model.PlayerCoverModel_;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.batache.songs.util.PlayerHelper;
import com.batache.songs.util.TextUtils;
import com.batache.songs.util.events.PlayerEvent;
import com.batache.songs.views.CloudDownloadBorderToFillImageView;
import com.batache.songs.views.IconToggleView;
import com.batache.songs.views.MorphingImageView;
import com.batache.songs.views.RepeatToggleView;
import com.downloader.Error;
import com.downloader.Progress;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class PlayerFragment extends BaseFragment implements SlidingUpPanelLayout.PanelSlideListener {

  private static String TAG = "PlayerFragment : ";

  private MainActivity mActivity;
  private DatabaseHelper mDatabaseHelper;

  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;

  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
  }

  private PlayerHelper playerHelper;

  private List<Song> mPlayingQueue;
  private String mCurrentPlaylistId;
  private int mCurrentSongIndex;
  private long mCurrentPosition;
  private boolean isPlayerPlaying = false;

  private Song mPlayerStreamSong;

  private ImageButton mPlayerArrowDown;
  private ImageButton mPlayerSeePlayingQueue;
  private LinearLayout mPlayerNextUp;
  private TextView mPlayerNextUpSongTitle;

  private PlayerCoversController mPlayerController;
  private EpoxyRecyclerView mPlayerRecyclerView;
  private RecyclerView.OnScrollListener mPlayerRecyclerViewListener;
  private TextView mPlayerSongTitle;
  private TextView mPlayerSongArtist;
  private IconToggleView mPlayerFavoriteBtn;
  private MaterialProgressBar mPlayerDownloadProgress;
  private CloudDownloadBorderToFillImageView mPlayerCloudDownloadBtn;
  private IconToggleView mPlayerAddToPlaylistBtn;
  private ImageView mPlayerShareBtn;
  private SeekBar mPlayerSongProgress;
  private TextView mPlayerSongProgressCurrent;
  private TextView mPlayerSongProgressTotal;
  private RepeatToggleView mPlayerRepeatBtn;
  private ImageButton mPlayerPrevBtn;
  private MorphingImageView mPlayerPlayToPauseBtn;
  private ImageButton mPlayerNextBtn;
  private ImageButton mPlayerShuffleBtn;

  private LinearLayout mMiniPlayer;
  private MaterialProgressBar mMiniPlayerSongProgress;
  private ImageView mMiniPlayerArrowUp;
  private TextView mMiniPlayerSongTitle;
  private MorphingImageView mMiniPlayerPlayToPauseBtn;

  private SimpleExoPlayer mPlayerExoPlayer;
  private Handler mPlayerSongProgressHandler;
  private Runnable mPlayerSongProgressHandlerRunnable;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    mDatabaseHelper = DatabaseHelper.getInstance(getContext());

    CodeUtils.registerToEventBus(this);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_player;
  }

  @Override
  public void onCreateView() {
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    /* Initialize player & mini player */
    playerHelper = PlayerHelper.getInstance();
    initializePlayer();
    if (playerHelper.initCurrentSong(getContext())) {
      updateCurrentSong(true);
      prepareForPlaying(mCurrentPlaylistId);
    }

    mDatabaseHelper.getPlaylistSongInsertionListener().observe(this, playlistSongJoins -> {
      updatePlaylistIcon();
    });
  }

  private void initializePlayer() {
    mPlayerArrowDown = mRootView.findViewById(R.id.player_arrow_down);
    mPlayerSeePlayingQueue = mRootView.findViewById(R.id.player_see_playing_queue);
    mPlayerNextUp = mRootView.findViewById(R.id.player_next_up);
    mPlayerNextUpSongTitle = mRootView.findViewById(R.id.player_next_up_song_title);
    PlayerCoverModel_.PlayerCoverCallback playerCoverCallback = new PlayerCoverModel.PlayerCoverCallback() {
      @Override
      public void onLyricsBtnClick() {
        startActivity(new Intent(mActivity, LyricsActivity.class));
      }

      @Override
      public void onSeekPrevious(LinearLayout view) {
        int seekTo = Math.max(0, (int) mPlayerExoPlayer.getCurrentPosition() - 10000);
        mPlayerExoPlayer.seekTo(seekTo);
        mPlayerSongProgress.setProgress(seekTo);
        mMiniPlayerSongProgress.setProgress(seekTo);
        forceRippleAnimation(view);
      }

      @Override
      public void onSeekNext(LinearLayout view) {
        int seekTo = Math.min((int) mPlayerExoPlayer.getCurrentPosition() + 10000, (int) mPlayerExoPlayer.getDuration());
        mPlayerExoPlayer.seekTo(seekTo);
        mPlayerSongProgress.setProgress(seekTo);
        mMiniPlayerSongProgress.setProgress(seekTo);
        forceRippleAnimation(view);
      }
    };
    mPlayerController = new PlayerCoversController(getContext(), playerCoverCallback);
    mPlayerRecyclerView = mRootView.findViewById(R.id.player_cover_arts_recyclerview);
    mPlayerSongTitle = mRootView.findViewById(R.id.player_song_title);
    mPlayerSongArtist = mRootView.findViewById(R.id.player_song_artist);
    mPlayerFavoriteBtn = mRootView.findViewById(R.id.player_action_favorite);
    mPlayerDownloadProgress = mRootView.findViewById(R.id.player_download_progress);
    mPlayerCloudDownloadBtn = mRootView.findViewById(R.id.player_action_cloud_download);
    mPlayerAddToPlaylistBtn = mRootView.findViewById(R.id.player_action_add_to_playlist);
    mPlayerShareBtn = mRootView.findViewById(R.id.player_action_share);
    mPlayerSongProgress = mRootView.findViewById(R.id.player_song_progress);
    mPlayerSongProgressCurrent = mRootView.findViewById(R.id.player_progress_current);
    mPlayerSongProgressTotal = mRootView.findViewById(R.id.player_progress_total);
    mPlayerRepeatBtn = mRootView.findViewById(R.id.player_repeat_btn);
    mPlayerPrevBtn = mRootView.findViewById(R.id.player_prev_btn);
    mPlayerPlayToPauseBtn = mRootView.findViewById(R.id.player_play_to_pause_btn);
    mPlayerNextBtn = mRootView.findViewById(R.id.player_next_btn);
    mPlayerShuffleBtn = mRootView.findViewById(R.id.player_shuffle_btn);

    mMiniPlayer = mRootView.findViewById(R.id.mini_player_layout);
    mMiniPlayerSongProgress = mRootView.findViewById(R.id.mini_player_song_progress);
    mMiniPlayerArrowUp = mRootView.findViewById(R.id.mini_player_arrow_up);
    mMiniPlayerSongTitle = mRootView.findViewById(R.id.mini_player_song_title);
    mMiniPlayerPlayToPauseBtn = mRootView.findViewById(R.id.mini_player_play_to_pause_btn);

    mActivity.slidingPanelLayout.addPanelSlideListener(this);
    mPlayerSongProgressHandler = new Handler();

    if (mDatabaseHelper.getLastPlayed() == null || PlayerHelper.getInstance().getPlayingQueue() != null || PlayerHelper.getInstance().getPlayingQueue().isEmpty()) {
      mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }
    if (mActivity.slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      mMiniPlayer.setVisibility(View.GONE);
    }

    mPlayerRecyclerView.setController(mPlayerController);
    mPlayerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(mPlayerRecyclerView);
  }

  @Override
  public void onApplyAllWindowInsets() {
    mRootView.findViewById(R.id.player_layout).setPadding(0, DimensionUtils.TOP_INSET, 0, DimensionUtils.BOTTOM_INSET);
  }

  private void updateCurrentSong(boolean resumeFromLastTime) {
    mCurrentPlaylistId = playerHelper.getPlaylistId(getContext());
    mCurrentSongIndex = playerHelper.getSongIndex(getContext());
    if (resumeFromLastTime) {
      mCurrentPosition = playerHelper.getPosition(getContext());
    } else {
      mCurrentPosition = C.TIME_UNSET;
    }
  }

  private void prepareForPlaying(String playlistId) {
    if (playerHelper.initPlayQueue(getContext(), playlistId)) {
      mPlayingQueue = playerHelper.getPlayingQueue();
      initExoPlayer(mPlayingQueue);
      mPlayerExoPlayer.seekTo(mCurrentSongIndex, mCurrentPosition);
      updateCoversInRecyclerView();
    }
  }

  private void initExoPlayer(List<Song> playingQueue) {
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE)
        .build();

    mPlayerExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector());

    MediaSource[] playlist = new MediaSource[playingQueue.size()];
    for (int i = 0; i < playingQueue.size(); i++) {
      Uri uri = Uri.fromFile(playingQueue.get(i).getPath());
      playlist[i] = new ExtractorMediaSource.Factory(getSongFactory(uri)).setExtractorsFactory(new DefaultExtractorsFactory()).createMediaSource(getSongFactory(uri).createDataSource().getUri());
    }
    ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist);
    mPlayerExoPlayer.prepare(concatenatingMediaSource);

    mPlayerExoPlayer.addListener(new Player.EventListener() {
      @Override
      public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

      }

      @Override
      public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

      }

      @Override
      public void onLoadingChanged(boolean isLoading) {

        }

      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
          case Player.STATE_ENDED:
            mPlayerExoPlayer.seekTo(0);
            pauseSong();
            break;

          case Player.STATE_READY:
            updatePlayer(getCurrentSongIndex());
            setProgress();

            final boolean repeating = mPlayerExoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE || mPlayerExoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL;
            setColorEnabled(mPlayerRepeatBtn, repeating);

            mPlayerRepeatBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                if (mPlayerExoPlayer.getRepeatMode() == Player.REPEAT_MODE_OFF) {
                  mPlayerExoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
                } else if (mPlayerExoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL) {
                  mPlayerExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                } else if (mPlayerExoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE) {
                  mPlayerExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
                }

                mPlayerRepeatBtn.showState(mPlayerExoPlayer.getRepeatMode());
              }
            });
            break;
        }
      }

      @Override
      public void onRepeatModeChanged(int repeatMode) {
        boolean repeating = repeatMode == Player.REPEAT_MODE_ONE || repeatMode == Player.REPEAT_MODE_ALL;
        setColorEnabled(mPlayerRepeatBtn, repeating);
      }

      @Override
      public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        if (shuffleModeEnabled) {
          Collections.shuffle(mPlayingQueue);
        } else {
          playerHelper.initPlayQueue(getContext(), getCurrentPlaylistId());
        }

        MediaSource[] playlist = new MediaSource[mPlayingQueue.size()];
        for (int i = 0; i < mPlayingQueue.size(); i++) {
          Uri uri = Uri.fromFile(mPlayingQueue.get(i).getPath());
          playlist[i] = new ExtractorMediaSource.Factory(getSongFactory(uri)).setExtractorsFactory(new DefaultExtractorsFactory()).createMediaSource(getSongFactory(uri).createDataSource().getUri());
        }
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist);
        mPlayerExoPlayer.prepare(concatenatingMediaSource);

        mPlayerExoPlayer.seekTo(getCurrentSongIndex(), C.TIME_UNSET);

        updateCoversInRecyclerView();
      }

      @Override
      public void onPlayerError(ExoPlaybackException error) {

      }

      @Override
      public void onPositionDiscontinuity(int reason) {
        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
          isVisualUpdate = true;
          playerHelper.setSongIndex(mPlayerExoPlayer.getCurrentPeriodIndex());
          mPlayerRecyclerView.smoothScrollToPosition(getCurrentSongIndex());
          updatePlayer(getCurrentSongIndex());
        }
      }

      @Override
      public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

      }

      @Override
      public void onSeekProcessed() {

      }
    });
    mPlayerExoPlayer.setAudioAttributes(audioAttributes, true);

    mPlayerSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          mPlayerExoPlayer.seekTo(progress * 1000);
        }
        playerHelper.setLastPlayed(getContext(), getCurrentPlaylistId(), getCurrentSongIndex(), mPlayerExoPlayer.getCurrentPosition());
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

        }
      });
    });
  }

  private void updatePlayer(final int songIndexInExoPlayer) {
    final Song currentSong = mPlayingQueue.get(songIndexInExoPlayer);

    mMiniPlayerArrowUp.setOnClickListener(view -> mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED));

    mPlayerArrowDown.setOnClickListener(view -> mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));

    mPlayerSeePlayingQueue.setEnabled(true);
    setColorEnabled(mPlayerSeePlayingQueue, true);
    mPlayerSeePlayingQueue.setOnClickListener(view -> {
      PlayingQueueFragment playingQueueFragment = new PlayingQueueFragment();
      playingQueueFragment.show(mActivity.getSupportFragmentManager(),
          "playing_queue_fragment");
    });

    mPlayerSongTitle.setText(currentSong.getTitle());
    mMiniPlayerSongTitle.setText(currentSong.getTitle());
    mPlayerSongArtist.setText(currentSong.getArtist());
    mPlayerCloudDownloadBtn.showBorder();

    if (!mDatabaseHelper.isSongInPlaylist(PLAYLIST_LIKES_ID, currentSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(view -> {
      if (!mDatabaseHelper.isSongInPlaylist(PLAYLIST_LIKES_ID, currentSong.getId())) {
        mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(PLAYLIST_LIKES_ID, currentSong.getId()));
        mPlayerFavoriteBtn.showSecondState();
      } else {
        mDatabaseHelper.removeSongFromPlaylist(PLAYLIST_LIKES_ID, currentSong.getId());
        mPlayerFavoriteBtn.showFirstState();
      }
    });

    if (mDatabaseHelper.isSongInPlaylist(PLAYLIST_DOWNLOADS_ID, currentSong.getId())) {
      mPlayerCloudDownloadBtn.showFill();
    }
    mPlayerCloudDownloadBtn.setOnDownloadClickedListener(new CloudDownloadBorderToFillImageView.OnDownloadClickListener() {
      @Override
      public void onDownloadStart() {

      }

      @Override
      public void onDownloadCancel() {

      }

      @Override
      public void onDownloadDelete() {
        MaterialDialog.Builder songDeletionDialog = new MaterialDialog.Builder(getContext());
        songDeletionDialog.title(R.string.delete_song);
        songDeletionDialog.content(R.string.delete_song_warning);
        songDeletionDialog.cancelable(true);
        songDeletionDialog.positiveText(R.string.delete);
        songDeletionDialog.onPositive((dialog, which) -> {
          playerHelper.deleteSong(getContext(), currentSong);
          if (mPlayingQueue.size() > 1) {
            if (mCurrentSongIndex < mPlayingQueue.size()) {
              playerHelper.playSong(mCurrentPlaylistId, mCurrentSongIndex);
            } else {
              playerHelper.playSong(mCurrentPlaylistId, mCurrentSongIndex - 1);
            }
          } else {
            mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
          }
        });
        songDeletionDialog.negativeText(R.string.cancel);
        songDeletionDialog.show();
      }
    });

    if (!mDatabaseHelper.isSongInCustomPlaylist(currentSong.getId())) {
      mPlayerAddToPlaylistBtn.showFirstState();
    } else {
      mPlayerAddToPlaylistBtn.showSecondState();
    }
    mPlayerAddToPlaylistBtn.setOnClickListener(view -> {
      final List<Playlist> playlists = mDatabaseHelper.getAllPlaylists().subList(2, mDatabaseHelper.getAllPlaylists().size());
      String[] playlistsTitles = new String[playlists.size() + 1];
      final List<Integer> checkedPlaylistsList = new ArrayList<>();

      playlistsTitles[0] = "New playlist";
      for (int i = 0, j = 1; i < playlists.size(); i++, j++) {
        playlistsTitles[j] = playlists.get(i).getTitle();
        if (mDatabaseHelper.isSongInPlaylist(playlists.get(i).getId(), currentSong.getId())) {
          checkedPlaylistsList.add(j);
        }
      }

      Integer[] checkedPlaylists = new Integer[checkedPlaylistsList.size()];
      for (int index = 0; index < checkedPlaylists.length; index++) {
        checkedPlaylists[index] = checkedPlaylistsList.get(index);
      }

      MaterialDialog.Builder addToPlaylistDialog = new MaterialDialog.Builder(getContext());
      addToPlaylistDialog.title(R.string.select_playlists);
      addToPlaylistDialog.items(playlistsTitles);
      addToPlaylistDialog.alwaysCallMultiChoiceCallback();
      addToPlaylistDialog.itemsCallbackMultiChoice(checkedPlaylists, (dialog, checked, text) -> true);
      addToPlaylistDialog.cancelable(true);
      addToPlaylistDialog.positiveText(R.string.save);
      addToPlaylistDialog.onPositive((dialog, which) -> {
        Integer[] checkedIndexes = dialog.getSelectedIndices();
        for (Playlist playlist : playlists) {
          mDatabaseHelper.removeSongFromPlaylist(playlist.getId(), currentSong.getId());
        }
        if (checkedIndexes != null && checkedIndexes.length > 0) {
          for (Integer index : checkedIndexes) {
            if (index == 0) {
              MaterialDialog.Builder newPlaylistDialog = new MaterialDialog.Builder(getContext());
              newPlaylistDialog.title(R.string.new_playlist);
              newPlaylistDialog.inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
              newPlaylistDialog.alwaysCallInputCallback();
              newPlaylistDialog.input(getString(R.string.playlist_name), null, false, new MaterialDialog.InputCallback() {
                @Override
                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                  if (input.toString().trim().equals("")) {
                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                  }
                }
              });
              newPlaylistDialog.cancelable(true);
              newPlaylistDialog.positiveText(R.string.create);
              newPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  List<Playlist> playlists1 = mDatabaseHelper.getAllPlaylists();
                  dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                  String playlistId = String.valueOf(Integer.valueOf(playlists1.get(playlists1.size() - 1).getId()) + 1);
                  mDatabaseHelper.insertPlaylist(new Playlist(playlistId, dialog.getInputEditText().getText().toString().trim(), new Date()));
                  mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlistId, currentSong.getId()));
                  mPlayerAddToPlaylistBtn.showSecondState();
                }
              });
              newPlaylistDialog.negativeText(R.string.cancel);
              newPlaylistDialog.show();
            } else {
              int playlistIndex = index - 1;
              mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlists.get(playlistIndex).getId(), currentSong.getId()));
              mPlayerAddToPlaylistBtn.showSecondState();
            }
          }
        } else {
          mPlayerAddToPlaylistBtn.showFirstState();
        }
      });
      addToPlaylistDialog.negativeText(R.string.cancel);
      addToPlaylistDialog.show();
    });

    setPlayerLoading(false);

    mPlayerPrevBtn.setOnClickListener(view -> {
      if (mCurrentSongIndex == 0 || mCurrentPosition / 1000 >= 10) {
        mPlayerExoPlayer.seekTo(0);
      } else {
        mPlayerRecyclerView.smoothScrollToPosition(mCurrentSongIndex - 1);
      }
    });

    mPlayerPlayToPauseBtn.setOnClickListener(view -> onPlayPauseClick());

    mMiniPlayerPlayToPauseBtn.setOnClickListener(view -> onPlayPauseClick());

    mPlayerNextBtn.setOnClickListener(view -> {
      if (mCurrentSongIndex == mPlayingQueue.size() - 1) {
        mPlayerExoPlayer.seekTo(0);
      } else {
        int nextSongIndex = mCurrentSongIndex + 1;
        mPlayerRecyclerView.smoothScrollToPosition(nextSongIndex);
      }
    });

    mPlayerShuffleBtn.setOnClickListener(view -> {
      boolean shuffleEnabled = !mPlayerExoPlayer.getShuffleModeEnabled();
      mPlayerExoPlayer.setShuffleModeEnabled(shuffleEnabled);
      if (shuffleEnabled) {
        mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_67));
      } else {
        mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
      }
    });

    if (mActivity.slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private void updatePlaylistIcon() {
    if (!CodeUtils.isEmpty(mPlayingQueue) && !mDatabaseHelper.isSongInCustomPlaylist(mPlayingQueue.get(mCurrentSongIndex).getId())) {
      mPlayerAddToPlaylistBtn.showFirstState();
    } else {
      mPlayerAddToPlaylistBtn.showSecondState();
    }
  }

  private void setPlayerLoading(boolean isPlayerLoading) {
    mPlayerFavoriteBtn.setEnabled(!isPlayerLoading);
    mPlayerCloudDownloadBtn.setEnabled(!isPlayerLoading);
    mPlayerAddToPlaylistBtn.setEnabled(!isPlayerLoading);
    mPlayerShareBtn.setEnabled(!isPlayerLoading);
    setColorEnabled(mPlayerShareBtn, !isPlayerLoading);
    mPlayerSongProgress.setEnabled(!isPlayerLoading);
    mMiniPlayerSongProgress.setProgress(0);
    mPlayerSongProgressCurrent.setText("00:00");
    mPlayerRepeatBtn.setEnabled(!isPlayerLoading);
    mPlayerPrevBtn.setEnabled(!isPlayerLoading);
    setColorEnabled(mPlayerPrevBtn, !isPlayerLoading);
    mPlayerPlayToPauseBtn.setEnabled(!isPlayerLoading);
    mMiniPlayerPlayToPauseBtn.setEnabled(!isPlayerLoading);
    mPlayerNextBtn.setEnabled(!isPlayerLoading);
    setColorEnabled(mPlayerNextBtn, !isPlayerLoading);
    mPlayerShuffleBtn.setEnabled(!isPlayerLoading);
  }

  private void onPlayPauseClick() {
    if (mPlayerPlayToPauseBtn.isEnabled()) {
      if (!isPlayerPlaying) {
        playSong();
      } else {
        pauseSong();
      }
    }
  }

  private void updateCoversInRecyclerView() {
    mPlayerController.reset();
    mPlayerController.addCoverArts(mPlayingQueue);

    mPlayerRecyclerView.clearOnScrollListeners();
    mPlayerRecyclerViewListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int songIndex = ((LinearLayoutManager) mPlayerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
          playerHelper.playSong(mCurrentPlaylistId, songIndex);
        }
      }
    };
    mPlayerRecyclerView.addOnScrollListener(mPlayerRecyclerViewListener);
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

  private DataSource.Factory getSongFactory(Uri uri) {
    DataSpec dataSpec = new DataSpec(uri);
    final FileDataSource fileDataSource = new FileDataSource();
    try {
      fileDataSource.open(dataSpec);
    } catch (FileDataSource.FileDataSourceException e) {
      e.printStackTrace();
    }

    return new DataSource.Factory() {
      @Override
      public DataSource createDataSource() {
        return fileDataSource;
      }
    };
  }

  private void setProgress() {
    mPlayerRecyclerView.scrollToPosition(mCurrentSongIndex);

    mPlayerSongProgress.setMax((int) mPlayerExoPlayer.getDuration() / 1000);
    mPlayerSongProgressCurrent.setText(TextUtils.formatMilliseconds(getContext(), (int) mPlayerExoPlayer.getCurrentPosition()));
    mPlayerSongProgressTotal.setText(TextUtils.formatMilliseconds(getContext(), (int) mPlayerExoPlayer.getDuration()));

    //Make sure you update Seekbar on UI thread
    mPlayerSongProgressHandlerRunnable = new Runnable() {
      @Override
      public void run() {
        if (mPlayerExoPlayer != null) {
          PlayerHelper.getInstance().setCurrentPosition(mPlayerExoPlayer.getCurrentPosition());
          EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.PROGRESS_CHANGED));

          mPlayerSongProgress.setMax((int) mPlayerExoPlayer.getDuration() / 1000);
          mMiniPlayerSongProgress.setMax((int) mPlayerExoPlayer.getDuration() / 1000);
          int mCurrentPosition = (int) mPlayerExoPlayer.getCurrentPosition() / 1000;
          mPlayerSongProgress.setProgress(mCurrentPosition);
          if (playerHelper.isStreaming()) {
            int bufferedPosition = (int) mPlayerExoPlayer.getBufferedPosition() / 1000;
            mPlayerSongProgress.setSecondaryProgress(bufferedPosition);
          } else if (mMiniPlayerSongProgress.getSecondaryProgress() != -1) {
            mPlayerSongProgress.setSecondaryProgress(0);
          }
          mMiniPlayerSongProgress.setProgress(mCurrentPosition);
          mPlayerSongProgressCurrent.setText(TextUtils.formatMilliseconds(getContext(), (int) mPlayerExoPlayer.getCurrentPosition()));
          mPlayerSongProgressTotal.setText(TextUtils.formatMilliseconds(getContext(), (int) mPlayerExoPlayer.getDuration()));

          mPlayerSongProgressHandler.postDelayed(this, 1000);
        }
      }
    };
    mPlayerSongProgressHandler.post(mPlayerSongProgressHandlerRunnable);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onPlayerEvent(PlayerEvent playerEvent) {
    if (playerEvent.action == PlayerEvent.Action.SONG_CHANGED) {
      if (!playerHelper.isStreaming()) {
        loadCurrentSong();
      } else {
        loadStreamSong();
      }
    }
  }

  private void loadCurrentSong() {
    int coverIndex = ((LinearLayoutManager) mPlayerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    if (playerHelper.getStreamSong() == null &&
        playerHelper.getPlaylistId(getContext()).equals(mCurrentPlaylistId) &&
        playerHelper.getCurrentSong(getContext()).getId().equals(playerHelper.getSongFromPlaylist(getContext(), mCurrentPlaylistId, mCurrentSongIndex).getId())
        || (coverIndex != -1 &&
        playerHelper.getSongIndex(getContext()) != coverIndex &&
        mCurrentSongIndex != coverIndex)) {
      return;
    }

    updateCurrentSong(false);
    prepareForPlaying(mCurrentPlaylistId);
    playSong();
  }

  private void loadStreamSong() {
    Song streamSong = playerHelper.getStreamSong();

    if (streamSong == null) {
      return;
    }

    if (mDatabaseHelper.hasSong(streamSong.getId())) {
      List<Song> playlist = mDatabaseHelper.getSongsForPlaylist(PLAYLIST_DOWNLOADS_ID);
      for (int i = 0; i < playlist.size(); i++) {
        if (playlist.get(i).getId().equals(streamSong.getId())) {
          playerHelper.playSong(PLAYLIST_DOWNLOADS_ID, i);
        }
      }
      return;
    }

    if (mPlayerExoPlayer != null) {
      mPlayerExoPlayer.release();
    }
    if (mPlayerSongProgressHandler != null) {
      mPlayerSongProgressHandler.removeCallbacks(mPlayerSongProgressHandlerRunnable);
    }

    mPlayerStreamSong = playerHelper.getStreamSong();
    updateStreamSong();
    updateStreamSongRecyclerView();
    if (mPlayerStreamSong.getSongSource() == Song.SongSource.youtube) {
      SongDownloader.getInstance().fetchSongURL(getContext(), mPlayerStreamSong, songFile -> {
        initExoPlayer(songFile.getUrl());
        playSong();
      });
    } else if (mPlayerStreamSong.getSongSource() == Song.SongSource.xiami) {
      initExoPlayer(mPlayerStreamSong.getXiamiSongUrl());
      playSong();
    }
  }

  private void updateStreamSong() {
    mMiniPlayerArrowUp.setOnClickListener(view -> mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED));

    mPlayerSeePlayingQueue.setEnabled(false);
    setColorEnabled(mPlayerSeePlayingQueue, false);

    mPlayerArrowDown.setOnClickListener(view -> mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));

    mPlayerNextUp.animate().alpha(0.0f);

    mPlayerSongTitle.setText(mPlayerStreamSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerStreamSong.getTitle());
    mPlayerSongArtist.setText(mPlayerStreamSong.getArtist());

    if (!mDatabaseHelper.isSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerStreamSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(view -> {
      if (!mDatabaseHelper.isSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerStreamSong.getId())) {
        mDatabaseHelper.insertSong(mPlayerStreamSong);
        mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(PLAYLIST_LIKES_ID, mPlayerStreamSong.getId()));
        mPlayerFavoriteBtn.showSecondState();
      } else {
        mDatabaseHelper.removeSongFromPlaylist(PLAYLIST_LIKES_ID, mPlayerStreamSong.getId());
        mPlayerFavoriteBtn.showFirstState();
      }
    });

    mPlayerCloudDownloadBtn.showBorder();
    final int[] downloadIdThis = {-1};
    mPlayerCloudDownloadBtn.setOnDownloadClickedListener(new CloudDownloadBorderToFillImageView.OnDownloadClickListener() {
      @Override
      public void onDownloadStart() {
        SongDownloader.getInstance().downloadSong(getContext(), mPlayerStreamSong, new SongDownloader.SongDownloaderCallback() {
          @Override
          public void onStartOrResume(Song song, int downloadId) {
            if (isSongCurrentlyStreaming(song)) {
              onSongDownloadStartOrResume(song.getTitle());
              downloadIdThis[0] = downloadId;
            }
          }

          @Override
          public void onProgress(Song song, Progress progress) {
            if (isSongCurrentlyStreaming(song)) {
              if (mPlayerDownloadProgress.getMax() != progress.totalBytes) {
                mPlayerDownloadProgress.setMax((int) progress.totalBytes);
              }
              mPlayerDownloadProgress.setProgress((int) progress.currentBytes);
              Log.d(TAG, "onProgress: tgblog: " + mPlayerStreamSong.getTitle() + " is downloading and is at: " + progress.currentBytes + "/" + progress.totalBytes);
            }
          }

          @Override
          public void onDownloadComplete(Song downloadedSong) {
            if (isSongCurrentlyStreaming(downloadedSong)) {
              onSongDownloadCompletion(downloadedSong);
            }
          }

          @Override
          public void onFailure(Song song, Error error) {
            Toast.makeText(getContext(), R.string.download_failed_error, Toast.LENGTH_LONG).show();
          }
        });
      }

      @Override
      public void onDownloadCancel() {
        if (downloadIdThis[0] != -1) {
          SongDownloader.getInstance().cancelDownload(downloadIdThis[0]);
          mPlayerCloudDownloadBtn.showBorder();
          mPlayerDownloadProgress.setProgress(0);
          Toast.makeText(getContext(), R.string.download_cancelled, Toast.LENGTH_LONG).show();
        }
      }

      @Override
      public void onDownloadDelete() {
        if (mDatabaseHelper.getSong(mPlayerStreamSong.getId()) != null) {
          MaterialDialog.Builder songDeletionDialog = new MaterialDialog.Builder(getContext());
          songDeletionDialog.title(R.string.delete_song);
          songDeletionDialog.content(R.string.delete_song_warning);
          songDeletionDialog.cancelable(true);
          songDeletionDialog.positiveText(R.string.delete);
          songDeletionDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
              mDatabaseHelper.deleteSong(mPlayerStreamSong.getId());
              mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
              mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
          });
          songDeletionDialog.negativeText(R.string.cancel);
          songDeletionDialog.show();
        }
      }
    });

    mPlayerAddToPlaylistBtn.setOnClickListener(view -> mPlayerAddToPlaylistBtn.switchState());

    setPlayerLoading(true);

    mPlayerPlayToPauseBtn.setOnClickListener(view -> onPlayPauseClick());

    mMiniPlayerPlayToPauseBtn.setOnClickListener(view -> onPlayPauseClick());

    if (mActivity.slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mActivity.slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private boolean isSongCurrentlyStreaming(Song song) {
    return playerHelper.isStreaming() && playerHelper.getStreamSong().getId().equals(song.getId());
  }

  private void updateStreamSongRecyclerView() {
    mPlayerController.reset();
    mPlayerRecyclerView.removeOnScrollListener(mPlayerRecyclerViewListener);
    mPlayerController.addCoverArt(mPlayerStreamSong.getCoverArt());
  }

  private void initExoPlayer(String songUrl) {
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE)
        .build();

    mPlayerExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector());
    Uri uri = Uri.parse(songUrl);
    MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri);
    mPlayerExoPlayer.prepare(mediaSource);

    mPlayerExoPlayer.addListener(new Player.EventListener() {
      @Override
      public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

      }

      @Override
      public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

      }

      @Override
      public void onLoadingChanged(boolean isLoading) {

      }

      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
          case Player.STATE_ENDED:
            mPlayerExoPlayer.seekTo(0);
            pauseSong();
            break;

          case Player.STATE_READY:
            setProgress();
            setPlayerLoading(false);

            mPlayerPrevBtn.setEnabled(false);
            setColorEnabled(mPlayerPrevBtn, false);
            mPlayerNextBtn.setEnabled(false);
            setColorEnabled(mPlayerNextBtn, false);
            mPlayerShuffleBtn.setEnabled(false);
            mPlayerRepeatBtn.setEnabled(false);
        }

        if (!playWhenReady) {
          pauseSong();
        }
      }

      @Override
      public void onRepeatModeChanged(int repeatMode) {
        boolean repeating = repeatMode == Player.REPEAT_MODE_ONE || repeatMode == Player.REPEAT_MODE_ALL;
        setColorEnabled(mPlayerRepeatBtn, repeating);
      }

      @Override
      public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

      }

      @Override
      public void onPlayerError(ExoPlaybackException error) {

      }

      @Override
      public void onPositionDiscontinuity(int reason) {
        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
          mPlayerRecyclerView.smoothScrollToPosition(mCurrentSongIndex);
          updatePlayer(mCurrentSongIndex);
        }
      }

      @Override
      public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

      }

      @Override
      public void onSeekProcessed() {

      }
    });
    mPlayerExoPlayer.setAudioAttributes(audioAttributes, true);

    mPlayerSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          mPlayerExoPlayer.seekTo(progress * 1000);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  private void playSong() {
    isPlayerPlaying = true;
    mPlayerExoPlayer.setPlayWhenReady(true);
    mPlayerPlayToPauseBtn.animateToSecondState(); //pause icon: playing
    mMiniPlayerPlayToPauseBtn.animateToSecondState(); //pause icon: playing
  }

  private void pauseSong() {
    isPlayerPlaying = false;
    mPlayerExoPlayer.setPlayWhenReady(false);
    mPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
    mMiniPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
  }

  private void onSongDownloadStartOrResume(String songTitle) {
    mPlayerCloudDownloadBtn.showBlinkingAnimation();
    Toast.makeText(getContext(), String.format(getString(R.string.download_started_for_TITLE), songTitle), Toast.LENGTH_SHORT).show();
  }

  private void onSongDownloadCompletion(Song song) {
    if (song.getId().equals(mPlayerStreamSong.getId())) {
      mPlayerCloudDownloadBtn.showFill();
    }
    mPlayerDownloadProgress.setProgress(0);
    Toast.makeText(getContext(), String.format(getString(R.string.download_finished_for_TITLE), song.getTitle()), Toast.LENGTH_SHORT).show();
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

  private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
    if (mMiniPlayer == null) {
      return;
    }
    float alpha = 1 - progress;
    mMiniPlayer.setAlpha(alpha);
    mActivity.setBottomNavigationY(alpha);
    mMiniPlayer.setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
    setMiniPlayerAlphaProgress(slideOffset);
  }

  @Override
  public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
      mRootView.findViewById(R.id.player_layout).setClickable(true);
    } else {
      onApplyAllWindowInsets();
      mRootView.findViewById(R.id.player_layout).setClickable(false);
    }
  }

  private void destroy() {
    if (mPlayerExoPlayer != null) {
      mPlayerExoPlayer.release();
    }
    if (mPlayerSongProgressHandler != null) {
      mPlayerSongProgressHandler.removeCallbacksAndMessages(null);
    }
    CodeUtils.unregisterFromEventBus(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroy();
  }
}
