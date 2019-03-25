package ml.toufic.songs.ui.fragments;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.epoxy.EpoxyAdapter;
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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import at.huber.youtubeExtractor.YtFile;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ml.toufic.songs.R;
import ml.toufic.songs.db.Song.SongViewModel;
import ml.toufic.songs.model.LastPlayed;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.PlayerCoverModel;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.SongDownloader;
import ml.toufic.songs.ui.activities.LyricsActivity;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.util.TextUtils;
import ml.toufic.songs.views.CloudDownloadBorderToFillImageView;
import ml.toufic.songs.views.IconToggleView;
import ml.toufic.songs.views.MorphingImageView;
import ml.toufic.songs.views.RepeatToggleView;

import static ml.toufic.songs.ui.activities.LyricsActivity.songIdKey;
import static ml.toufic.songs.ui.activities.LyricsActivity.songTitleKey;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class PlayerFragment extends Fragment implements SlidingUpPanelLayout.PanelSlideListener {

  private static String TAG = "PlayerFragment : ";

  private SongViewModel mSongViewModel;

  private MainActivity mActivity;
  private Context mContext;
  private View mRootView;

  private List<PlayableSong> mPlayingQueue = new ArrayList<>();
  private int mCurrentSongIndex;
  private String mCurrentPlaylistId;
  private boolean isPlayerPlaying = false;

  private Song mPlayerRemoteSong;

  private LastPlayed mLastPlayed;

  private ImageButton mPlayerArrowDown;
  private ImageButton mPlayerSeePlayingQueue;
  private LinearLayout mPlayerNextUp;
  private TextView mPlayerNextUpSongTitle;
  private PlayerCoversAdapter mPlayerAdapter;
  private RecyclerView mPlayerRecyclerView;
  private RecyclerView.OnScrollListener mPlayerRecyclerViewListener;
  private TextView mPlayerSongTitle;
  private TextView mPlayerSongArtist;
  private IconToggleView mPlayerFavoriteBtn;
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

    mSongViewModel = ViewModelProviders.of(this).get(SongViewModel.class);
    mActivity = (MainActivity) getActivity();
    mContext = getContext();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_player, null);

    /* Initialize player & mini player */
    initializePlayer();
    mLastPlayed = mActivity.getLastPlayed();
    if (isLastPlayedOK()) {
      prepareForPlaying(mLastPlayed.getPlaylistId());
    }

    return mRootView;
  }

  private void initializePlayer() {
    mPlayerArrowDown = mRootView.findViewById(R.id.player_arrow_down);
    mPlayerSeePlayingQueue = mRootView.findViewById(R.id.player_see_playing_queue);
    mPlayerNextUp = mRootView.findViewById(R.id.player_next_up);
    mPlayerNextUpSongTitle = mRootView.findViewById(R.id.player_next_up_song_title);
    mPlayerAdapter = new PlayerCoversAdapter();
    mPlayerRecyclerView = mRootView.findViewById(R.id.player_cover_arts_recyclerview);
    mPlayerSongTitle = mRootView.findViewById(R.id.player_song_title);
    mPlayerSongArtist = mRootView.findViewById(R.id.player_song_artist);
    mPlayerFavoriteBtn = mRootView.findViewById(R.id.player_action_favorite);
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

    mActivity.mSlidingPanelLayout.addPanelSlideListener(this);
    mPlayerSongProgressHandler = new Handler();

    if (mSongViewModel.getLastPlayedSong() == null || mPlayingQueue.isEmpty()) {
      mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }
    if (mActivity.mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      mMiniPlayer.setVisibility(View.GONE);
    }

    mPlayerRecyclerView.setAdapter(mPlayerAdapter);
    mPlayerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(mPlayerRecyclerView);
  }

  private boolean isLastPlayedOK() {
    boolean isPlaylistOK = false;
    if (mLastPlayed != null) {
      mCurrentSongIndex = mLastPlayed.getSongIndex();
      mCurrentPlaylistId = mLastPlayed.getPlaylistId();
      isPlaylistOK = true;
    }
    return isPlaylistOK;
  }

  private void prepareForPlaying(String playlistId) {
    initPlayingQueue(playlistId);
    initExoPlayer(mPlayingQueue);
    mPlayerExoPlayer.seekTo(mCurrentSongIndex, C.TIME_UNSET);
    updateCoversInRecyclerView();
  }

  private void initPlayingQueue(String playlistId) {
    mPlayingQueue = new ArrayList<>();
    List<PlayableSong> playlistSongs = mActivity.getSongsForPlaylist(playlistId);
    for (PlayableSong playableSong : playlistSongs) {
      MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(playableSong.getPath().getPath()));
      playableSong.setDurationInMs(mp.getDuration());
      mp.release();
      mPlayingQueue.add(playableSong);
    }
  }

  private void initExoPlayer(List<PlayableSong> playingQueue) {
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
      .setUsage(C.USAGE_MEDIA)
      .setContentType(C.CONTENT_TYPE_MOVIE)
      .build();

    mPlayerExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());

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
            updatePlayer(mCurrentSongIndex);
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
        if(shuffleModeEnabled) {
          Collections.shuffle(mPlayingQueue);
        } else {
          initPlayingQueue(mCurrentPlaylistId);
        }

        MediaSource[] playlist = new MediaSource[playingQueue.size()];
        for (int i = 0; i < playingQueue.size(); i++) {
          Uri uri = Uri.fromFile(playingQueue.get(i).getPath());
          playlist[i] = new ExtractorMediaSource.Factory(getSongFactory(uri)).setExtractorsFactory(new DefaultExtractorsFactory()).createMediaSource(getSongFactory(uri).createDataSource().getUri());
        }
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist);
        mPlayerExoPlayer.prepare(concatenatingMediaSource);

        mPlayerExoPlayer.seekTo(mCurrentSongIndex, C.TIME_UNSET);

        updateCoversInRecyclerView();
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

  private void updatePlayer(final int songIndexInExoPlayer) {
    final PlayableSong currentSong = mPlayingQueue.get(songIndexInExoPlayer);

    mMiniPlayerArrowUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
      }
    });

    mPlayerArrowDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      }
    });

    mPlayerSeePlayingQueue.setEnabled(true);
    setColorEnabled(mPlayerSeePlayingQueue, true);
    mPlayerSeePlayingQueue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        PlayingQueueFragment playingQueueFragment = PlayingQueueFragment.newInstance(mPlayingQueue, currentSong.getId(), mCurrentPlaylistId);
        playingQueueFragment.show(mActivity.getSupportFragmentManager(),
          "playing_queue_fragment");
      }
    });

    mPlayerSongTitle.setText(currentSong.getTitle());
    mMiniPlayerSongTitle.setText(currentSong.getTitle());
    mPlayerSongArtist.setText(currentSong.getArtist());
    mPlayerCloudDownloadBtn.showBorder();

    if (!mActivity.isSongInPlaylist(PLAYLIST_LIKES_ID, currentSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!mActivity.isSongInPlaylist(PLAYLIST_LIKES_ID, currentSong.getId())) {
          mActivity.insertSongInPlaylist(PLAYLIST_LIKES_ID, currentSong.getId(), false);
          mPlayerFavoriteBtn.showSecondState();
        } else {
          mActivity.removeSongFromPlaylist(PLAYLIST_LIKES_ID, currentSong.getId());
          mPlayerFavoriteBtn.showFirstState();
        }
      }
    });

    if (mActivity.isSongInPlaylist(PLAYLIST_DOWNLOADS_ID, currentSong.getId())) {
      mPlayerCloudDownloadBtn.showFill();
    }
    mPlayerCloudDownloadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MaterialDialog.Builder songDeletionDialog = new MaterialDialog.Builder(mContext);
        songDeletionDialog.title(R.string.delete_song);
        songDeletionDialog.content(R.string.delete_song_warning);
        songDeletionDialog.cancelable(true);
        songDeletionDialog.positiveText(R.string.delete);
        songDeletionDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            mActivity.deleteSong(currentSong);
            if (mActivity.getAllSongsAsync().size() >= 1) {
              if (mCurrentSongIndex - 1 >= 0) {
                loadSong(mCurrentSongIndex - 1, mCurrentPlaylistId);
              } else {
                loadSong(mCurrentSongIndex + 1, mCurrentPlaylistId);
              }
            } else {
              mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
          }
        });
        songDeletionDialog.negativeText(R.string.cancel);
        songDeletionDialog.show();
      }
    });

    if (!mActivity.isSongInCustomPlaylist(currentSong.getId())) {
      mPlayerAddToPlaylistBtn.showFirstState();
    } else {
      mPlayerAddToPlaylistBtn.showSecondState();
    }
    mPlayerAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        final List<Playlist> playlists = mActivity.getAllPlaylists().subList(2, mActivity.getAllPlaylists().size());
        String[] playlistsTitles = new String[playlists.size() + 1];
        final List<Integer> checkedPlaylistsList = new ArrayList<>();

        for (int i = 0; i < playlists.size(); i++) {
          playlistsTitles[i] = playlists.get(i).getTitle();
          if (mActivity.isSongInPlaylist(playlists.get(i).getId(), currentSong.getId())) {
            checkedPlaylistsList.add(i);
          }
        }
        playlistsTitles[playlists.size()] = "New playlist";

        Integer[] checkedPlaylists = new Integer[checkedPlaylistsList.size()];
        for (int index = 0; index < checkedPlaylists.length; index++) {
          checkedPlaylists[index] = checkedPlaylistsList.get(index);
        }

        MaterialDialog.Builder addToPlaylistDialog = new MaterialDialog.Builder(mContext);
        addToPlaylistDialog.title(R.string.select_playlists);
        addToPlaylistDialog.items(playlistsTitles);
        addToPlaylistDialog.alwaysCallMultiChoiceCallback();
        addToPlaylistDialog.itemsCallbackMultiChoice(checkedPlaylists, (dialog, checked, text) -> true);
        addToPlaylistDialog.cancelable(true);
        addToPlaylistDialog.positiveText(R.string.save);
        addToPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            Integer[] playlistsIdsToAddTo = dialog.getSelectedIndices();
            for (Playlist playlist : playlists) {
              mActivity.removeSongFromPlaylist(playlist.getId(), currentSong.getId());
            }
            if (playlistsIdsToAddTo != null && playlistsIdsToAddTo.length > 0) {
              for (Integer index : playlistsIdsToAddTo) {
                if (index == playlistsIdsToAddTo.length - 1) {
                  MaterialDialog.Builder newPlaylistDialog = new MaterialDialog.Builder(mContext);
                  newPlaylistDialog.title(R.string.new_playlist);
                  newPlaylistDialog.inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                  newPlaylistDialog.alwaysCallInputCallback();
                  newPlaylistDialog.input(getString(R.string.playlist_name), null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                      if (Objects.equals(input.toString().trim(), "")) {
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                      }
                    }
                  });
                  newPlaylistDialog.cancelable(true);
                  newPlaylistDialog.positiveText(R.string.create);
                  newPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                      List<Playlist> playlists = mActivity.getAllPlaylists();
                      dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                      String playlistId = String.valueOf(Integer.valueOf(playlists.get(playlists.size() - 1).getId()) + 1);
                      mActivity.insertPlaylist(new Playlist(playlistId, dialog.getInputEditText().getText().toString().trim(), new Date()));
                      mActivity.insertSongInPlaylist(playlistId, currentSong.getId(), false);
                    }
                  });
                  newPlaylistDialog.negativeText(R.string.cancel);
                  newPlaylistDialog.show();
                } else {
                  mActivity.insertSongInPlaylist(String.valueOf(playlists.get(index).getId()), currentSong.getId(), false);
                }
              }
              mPlayerAddToPlaylistBtn.showSecondState();
            } else {
              mPlayerAddToPlaylistBtn.showFirstState();
            }
          }
        });
        addToPlaylistDialog.negativeText(R.string.cancel);
        addToPlaylistDialog.show();
      }
    });

    setPlayerLoading(false);

    mPlayerPrevBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCurrentSongIndex == 0) {
          mPlayerExoPlayer.seekTo(0);
        } else {
          if (mCurrentSongIndex / 1000 >= 10) {
            mPlayerExoPlayer.seekTo(0);
          } else {
            int previousSongIndex = mCurrentSongIndex - 1;
            mPlayerRecyclerView.smoothScrollToPosition(previousSongIndex);
          }
        }
      }
    });

    mPlayerPlayToPauseBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onPlayPauseClick();
      }
    });

    mMiniPlayerPlayToPauseBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onPlayPauseClick();
      }
    });

    mPlayerNextBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCurrentSongIndex == mPlayingQueue.size() - 1) {
          mPlayerExoPlayer.seekTo(0);
        } else {
          int nextSongIndex = mCurrentSongIndex + 1;
          mPlayerRecyclerView.smoothScrollToPosition(nextSongIndex);
        }
      }
    });

    mPlayerShuffleBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean shuffleEnabled = !mPlayerExoPlayer.getShuffleModeEnabled();
        mPlayerExoPlayer.setShuffleModeEnabled(shuffleEnabled);
        if (shuffleEnabled) {
          mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_67));
        } else {
          mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
        }
      }
    });

    if (mActivity.mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
    mPlayerAdapter.clear();
    for (PlayableSong playableSong : mPlayingQueue) {
      mPlayerAdapter.addCoverArt(playableSong);
    }

    mPlayerRecyclerView.clearOnScrollListeners();
    mPlayerRecyclerViewListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int songIndex = ((LinearLayoutManager) mPlayerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
          mPlayerExoPlayer.seekTo(songIndex, C.TIME_UNSET);
          mCurrentSongIndex = songIndex;
          mActivity.setLastPlayed(new LastPlayed(songIndex, mCurrentPlaylistId));
          playSong();
        }
      }
    };
    mPlayerRecyclerView.addOnScrollListener(mPlayerRecyclerViewListener);
  }

  class PlayerCoversAdapter extends EpoxyAdapter {
    void addCoverArt(PlayableSong playableSong) {
      addModel(new PlayerCoverModel(mContext, playableSong,
        new PlayerCoverModel.PlayerCoverCallback() {
          @Override
          public void onLyricsBtnClick() {
            PlayableSong currentSong = mPlayingQueue.get(mCurrentSongIndex);
            Intent lyricsActivityIntent = new Intent(mActivity, LyricsActivity.class);
            lyricsActivityIntent.putExtra(songTitleKey, currentSong.getTitle());
            lyricsActivityIntent.putExtra(songIdKey, currentSong.getId());
            startActivity(lyricsActivityIntent);
          }

          @Override
          public void onSeekPrevious() {
            int seekTo = (int) mPlayerExoPlayer.getCurrentPosition() - 10000;
            mPlayerExoPlayer.seekTo(seekTo);
            mPlayerSongProgress.setProgress(seekTo);
            mMiniPlayerSongProgress.setProgress(seekTo);
          }

          @Override
          public void onSeekNext() {
            int seekTo = (int) mPlayerExoPlayer.getCurrentPosition() + 10000;
            mPlayerExoPlayer.seekTo(seekTo);
            mPlayerSongProgress.setProgress(seekTo);
            mMiniPlayerSongProgress.setProgress(seekTo);
          }
        }));
    }

    void addCoverArt(String coverArtUrl) {
      addModel(new PlayerCoverModel(mContext, coverArtUrl,
        new PlayerCoverModel.PlayerCoverCallback() {
          @Override
          public void onLyricsBtnClick() {
            PlayableSong mPlayerSong = mPlayingQueue.get(mCurrentSongIndex);
            Intent lyricsActivityIntent = new Intent(mActivity, LyricsActivity.class);
            lyricsActivityIntent.putExtra(songTitleKey, mPlayerSong.getTitle());
            lyricsActivityIntent.putExtra(songIdKey, mPlayerSong.getId());
            startActivity(lyricsActivityIntent);
          }

          @Override
          public void onSeekPrevious() {
            int seekTo = (int) mPlayerExoPlayer.getCurrentPosition() - 10000;
            mPlayerExoPlayer.seekTo(seekTo);
            mPlayerSongProgress.setProgress(seekTo);
            mMiniPlayerSongProgress.setProgress(seekTo);
          }

          @Override
          public void onSeekNext() {
            int seekTo = (int) mPlayerExoPlayer.getCurrentPosition() + 10000;
            mPlayerExoPlayer.seekTo(seekTo);
            mPlayerSongProgress.setProgress(seekTo);
            mMiniPlayerSongProgress.setProgress(seekTo);
          }
        }));
    }

    void clear() {
      removeAllModels();
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
    mPlayerSongProgressCurrent.setText(TextUtils.formatMilliseconds(mContext, (int) mPlayerExoPlayer.getCurrentPosition()));
    mPlayerSongProgressTotal.setText(TextUtils.formatMilliseconds(mContext, (int) mPlayerExoPlayer.getDuration()));

    //Make sure you update Seekbar on UI thread
    mPlayerSongProgressHandlerRunnable = new Runnable() {
      @Override
      public void run() {
        if (mPlayerExoPlayer != null) {
          mPlayerSongProgress.setMax((int) mPlayerExoPlayer.getDuration() / 1000);
          mMiniPlayerSongProgress.setMax((int) mPlayerExoPlayer.getDuration() / 1000);
          int mCurrentPosition = (int) mPlayerExoPlayer.getCurrentPosition() / 1000;
          mPlayerSongProgress.setProgress(mCurrentPosition);
          mMiniPlayerSongProgress.setProgress(mCurrentPosition);
          mPlayerSongProgressCurrent.setText(TextUtils.formatMilliseconds(mContext, (int) mPlayerExoPlayer.getCurrentPosition()));
          mPlayerSongProgressTotal.setText(TextUtils.formatMilliseconds(mContext, (int) mPlayerExoPlayer.getDuration()));

          mPlayerSongProgressHandler.postDelayed(this, 1000);
        }
      }
    };
    mPlayerSongProgressHandler.post(mPlayerSongProgressHandlerRunnable);
  }

  public void loadSong(final int index, final String playlistId) {
    mActivity.setLastPlayed(new LastPlayed(index, playlistId));
    mCurrentSongIndex = index;
    mCurrentPlaylistId = playlistId;
    prepareForPlaying(playlistId);
    playSong();
  }

  private void updateRemoteSong(final Song remoteSong) {
    mPlayerRemoteSong = remoteSong;

    mMiniPlayerArrowUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
      }
    });

    mPlayerSeePlayingQueue.setEnabled(false);
    setColorEnabled(mPlayerSeePlayingQueue, false);

    mPlayerArrowDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      }
    });

    mPlayerNextUp.animate().alpha(0.0f);

    mPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mPlayerSongArtist.setText(mPlayerRemoteSong.getArtist());

    if (!mActivity.isRemoteSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!mActivity.isRemoteSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId())) {
//          insertRemoteSong(mPlayerRemoteSong);
//          insertSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId(), true);
          mPlayerFavoriteBtn.showSecondState();
        } else {
          mActivity.removeSongFromPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId());
          mPlayerFavoriteBtn.showFirstState();
        }
      }
    });

    mPlayerCloudDownloadBtn.showBorder();
    final int[] downloadIdThis = {-1};
    mPlayerCloudDownloadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mPlayerCloudDownloadBtn.isShowingBorder() && !mPlayerCloudDownloadBtn.isBlinking()) {
          SongDownloader.downloadSong(mContext, mPlayerRemoteSong, new SongDownloader.SongDownloaderCallback() {
            @Override
            public void onStartOrResume(int downloadId, String songTitle) {
              onSongDownloadStartOrResume(songTitle);
              downloadIdThis[0] = downloadId;
            }

            @Override
            public void onProgress(Progress progress) {
            }

            @Override
            public void onDownloadComplete(PlayableSong playableSong) {
              onSongDownloadCompletion(playableSong);
            }

            @Override
            public void onError(Error error) {
              Toast.makeText(mContext, R.string.download_failed_error, Toast.LENGTH_LONG).show();
            }
          });
        } else if (mPlayerCloudDownloadBtn.isBlinking()) {
          if (downloadIdThis[0] != -1) {
            SongDownloader.cancelDownload(downloadIdThis[0]);
            mPlayerCloudDownloadBtn.showBorder();
            Toast.makeText(mContext, R.string.download_cancelled, Toast.LENGTH_LONG).show();
          }
        }
      }
    });

    mPlayerAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerAddToPlaylistBtn.switchState();
      }
    });

    setPlayerLoading(true);

    mPlayerPlayToPauseBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onPlayPauseClick();
      }
    });

    mMiniPlayerPlayToPauseBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onPlayPauseClick();
      }
    });

    if (mActivity.mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mActivity.mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private void updateRemoteSongRecyclerView() {
    mPlayerAdapter.clear();
    mPlayerRecyclerView.removeOnScrollListener(mPlayerRecyclerViewListener);
    mPlayerAdapter.addCoverArt(mPlayerRemoteSong.getCoverArt());
  }

  private void initExoPlayer(String songUrl) {
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
      .setUsage(C.USAGE_MEDIA)
      .setContentType(C.CONTENT_TYPE_MOVIE)
      .build();

    mPlayerExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
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

  public void loadSong(Song remoteSong) {
    if (mActivity.hasSong(remoteSong.getId())) {
      List<PlayableSong> playlist = mActivity.getSongsForPlaylist(PLAYLIST_DOWNLOADS_ID);
      for (int i = 0; i < playlist.size(); i++) {
        if (playlist.get(i).getId().equals(remoteSong.getId())) {
          loadSong(i, PLAYLIST_DOWNLOADS_ID);
        }
      }
      return;
    }
    destroy();

    updateRemoteSong(remoteSong);
    updateRemoteSongRecyclerView();
    SongDownloader.fetchSongURL(mContext, remoteSong, new SongDownloader.SongURLFetcherCallback() {
      @Override
      public void onSuccess(YtFile songFile) {
        initExoPlayer(songFile.getUrl());
        playSong();
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
    Toast.makeText(mContext, String.format(getString(R.string.download_started_for_TITLE), songTitle), Toast.LENGTH_SHORT).show();
  }

  private void onSongDownloadCompletion(PlayableSong playableSong) {
    if (playableSong.getPath().exists()) {
      mActivity.insertSong(playableSong);
      mActivity.insertSongInPlaylist(PLAYLIST_DOWNLOADS_ID, playableSong.getId(), false);
      if (playableSong.getId().equals(mPlayerRemoteSong.getId())) {
        mPlayerCloudDownloadBtn.showFill();
      }
      Toast.makeText(mContext, String.format(getString(R.string.download_finished_for_TITLE), playableSong.getTitle()), Toast.LENGTH_SHORT).show();
    }
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
    if (mMiniPlayer == null || mActivity.mBottomNavigationView == null) return;
    float alpha = 1 - progress;
    mMiniPlayer.setAlpha(alpha);
    mActivity.mBottomNavigationView.setTranslationY(mActivity.mBottomNavigationView.getHeight() * (alpha * -1 + 1));

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
      mRootView.findViewById(R.id.player_layout).setClickable(false);
    }
  }

  private void destroy() {
    if (mPlayerExoPlayer != null) {
      mPlayerExoPlayer.release();
    }
    if (mPlayerSongProgressHandler != null) {
      mPlayerSongProgressHandler.removeCallbacks(mPlayerSongProgressHandlerRunnable);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroy();
  }
}
