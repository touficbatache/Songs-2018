package ml.toufic.songs.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import at.huber.youtubeExtractor.YtFile;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ml.toufic.songs.R;
import ml.toufic.songs.db.PlaylistSongJoin.PlaylistSongJoinViewModel;
import ml.toufic.songs.db.Playlist.PlaylistViewModel;
import ml.toufic.songs.db.Song.SongViewModel;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.PlayerCoverModel;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.PlaylistSongJoin;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.SongDownloader;
import ml.toufic.songs.ui.fragments.ExploreFragment;
import ml.toufic.songs.ui.fragments.MyMusicFragment;
import ml.toufic.songs.ui.fragments.PlayingQueueFragment;
import ml.toufic.songs.util.TextUtils;
import ml.toufic.songs.views.CloudDownloadBorderToFillImageView;
import ml.toufic.songs.views.IconToggleView;
import ml.toufic.songs.views.MorphingImageView;

import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class MainActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {

  private static final String TAG = "MainActivity: ";

  private Context mContext;

  private BottomNavigationView mBottomNavigationView;

  private SongViewModel mSongViewModel;
  private PlaylistViewModel mPlaylistViewModel;
  private PlaylistSongJoinViewModel mPlaylistSongJoinViewModel;

  private ExploreFragment mExploreFragment;
  private MyMusicFragment mMyMusicFragment;

  private List<PlayableSong> mOriginalPlayingQueue = new ArrayList<>();
  private Playlist mPlayerPlaylist;
  private PlayableSong mPlayerSong;
  private List<PlayableSong> mShuffledPlayingQueue = new ArrayList<>();
  private boolean isPlayingQueueShuffled = false;

  private boolean isRemoteSongPlaying = false;
  private boolean isRemoteSongLooping = false;
  private Song mPlayerRemoteSong;

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
  private ImageButton mPlayerRepeatBtn;
  private ImageButton mPlayerPrevBtn;
  private MorphingImageView mPlayerPlayToPauseBtn;
  private ImageButton mPlayerNextBtn;
  private ImageButton mPlayerShuffleBtn;

  private LinearLayout mMiniPlayer;
  private MaterialProgressBar mMiniPlayerSongProgress;
  private ImageView mMiniPlayerArrowUp;
  private TextView mMiniPlayerSongTitle;
  private MorphingImageView mMiniPlayerPlayToPauseBtn;

  private SlidingUpPanelLayout mSlidingPanelLayout;
  private MediaPlayer mPlayerMediaPlayer;
  private Handler mPlayerSongProgressHandler;
  private Runnable mPlayerSongProgressHandlerRunnable;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
    = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

      Fragment fragment = mExploreFragment;

      switch (item.getItemId()) {
        case R.id.navigation_explore:
          fragment = mExploreFragment;
          break;

        case R.id.navigation_my_music:
          fragment = mMyMusicFragment;
          break;
      }

      return loadFragment(fragment);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
      .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
      .setResizeAndRotateEnabledForNetwork(true)
      .setDownsampleEnabled(true)
      .build();
    Fresco.initialize(this, imagePipelineConfig);

    PRDownloaderConfig prDownloaderConfig = PRDownloaderConfig.newBuilder()
      .build();
    PRDownloader.initialize(getApplicationContext(), prDownloaderConfig);

    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mContext = MainActivity.this;

    mExploreFragment = new ExploreFragment();
    mMyMusicFragment = new MyMusicFragment();

    mBottomNavigationView = findViewById(R.id.navigation);
    mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.fragment_container, new ExploreFragment())
      .commitAllowingStateLoss();

    mSongViewModel = ViewModelProviders.of(this).get(SongViewModel.class);

    mPlaylistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
    mPlaylistViewModel.getAllPlaylistsLive().observe(this, new Observer<List<Playlist>>() {
      @Override
      public void onChanged(@Nullable final List<Playlist> playlists) {
        mMyMusicFragment.loadMyMusicPlaylists();
      }
    });

    mPlaylistSongJoinViewModel = ViewModelProviders.of(this).get(PlaylistSongJoinViewModel.class);
    mPlaylistSongJoinViewModel.getAllPlaylistSongJoinLive().observe(this, new Observer<List<PlaylistSongJoin>>() {
      @Override
      public void onChanged(@Nullable final List<PlaylistSongJoin> playlistSongJoins) {
        // Update the cached copy of the songs in the adapter.
        mMyMusicFragment.loadMyMusicPlaylists();
        mMyMusicFragment.updateSongsInPlaylist();
        updateVisuals();
      }
    });

    insertPlaylist(new Playlist(PLAYLIST_LIKES_ID, getString(R.string.favorites), new Date()));
    insertPlaylist(new Playlist(PLAYLIST_DOWNLOADS_ID, getString(R.string.downloads), new Date()));

    /* Initialize player & mini player */

    initializePlayer();
    initializeRecyclerView();
    if(isLastOpenedPlaylistGood() && isLastPlayedSongGood()) {
      prepareForPlaying(getLastOpenedPlaylist().getId());
    }

//    ((LinearLayout)findViewById(R.id.circlee)).addView(new GraphicsView(this));
  }

  private boolean loadFragment(Fragment fragment) {
    if (fragment != null) {
      getSupportFragmentManager().popBackStack();
      getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commitAllowingStateLoss();
      return true;
    }
    return false;
  }

  public boolean pushFragment(Fragment fragment) {
    if (fragment != null) {
      getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commitAllowingStateLoss();
      return true;
    }
    return false;
  }

  private void initializePlayer() {
    mPlayerArrowDown = findViewById(R.id.player_arrow_down);
    mPlayerSeePlayingQueue = findViewById(R.id.player_see_playing_queue);
    mPlayerNextUp = findViewById(R.id.player_next_up);
    mPlayerNextUpSongTitle = findViewById(R.id.player_next_up_song_title);
    mPlayerAdapter = new PlayerCoversAdapter();
    mPlayerRecyclerView = findViewById(R.id.player_cover_arts_recyclerview);
    mPlayerSongTitle = findViewById(R.id.player_song_title);
    mPlayerSongArtist = findViewById(R.id.player_song_artist);
    mPlayerFavoriteBtn = findViewById(R.id.player_action_favorite);
    mPlayerCloudDownloadBtn = findViewById(R.id.player_action_cloud_download);
    mPlayerAddToPlaylistBtn = findViewById(R.id.player_action_add_to_playlist);
    mPlayerShareBtn = findViewById(R.id.player_action_share);
    mPlayerSongProgress = findViewById(R.id.player_song_progress);
    mPlayerSongProgressCurrent = findViewById(R.id.player_progress_current);
    mPlayerSongProgressTotal = findViewById(R.id.player_progress_total);
    mPlayerRepeatBtn = findViewById(R.id.player_repeat_btn);
    mPlayerPrevBtn = findViewById(R.id.player_prev_btn);
    mPlayerPlayToPauseBtn = findViewById(R.id.player_play_to_pause_btn);
    mPlayerNextBtn = findViewById(R.id.player_next_btn);
    mPlayerShuffleBtn = findViewById(R.id.player_shuffle_btn);

    mMiniPlayer = findViewById(R.id.mini_player_layout);
    mMiniPlayerSongProgress = findViewById(R.id.mini_player_song_progress);
    mMiniPlayerArrowUp = findViewById(R.id.mini_player_arrow_up);
    mMiniPlayerSongTitle = findViewById(R.id.mini_player_song_title);
    mMiniPlayerPlayToPauseBtn = findViewById(R.id.mini_player_play_to_pause_btn);

    mSlidingPanelLayout = findViewById(R.id.sliding_layout);
    mSlidingPanelLayout.addPanelSlideListener(this);
    mPlayerSongProgressHandler = new Handler();

    if(mSongViewModel.getLastPlayedSong() == null || mOriginalPlayingQueue.isEmpty()) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }
  }

  private void initializeRecyclerView() {
    mPlayerRecyclerView.setAdapter(mPlayerAdapter);
    mPlayerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(mPlayerRecyclerView);
  }

  private boolean isLastOpenedPlaylistGood() {
    mPlayerPlaylist = getLastOpenedPlaylist();

    boolean isPlaylistOK = false;
    if(mPlayerPlaylist != null) {
      isPlaylistOK = true;
    }
    return isPlaylistOK;
  }

  private boolean isLastPlayedSongGood() {
    mPlayerSong = mSongViewModel.getLastPlayedSong();

    boolean isSongOK = false;
    if(mPlayerSong != null && mPlayerSong.getPath().exists()) {
      isSongOK = true;
    }
    return isSongOK;
  }

  private void prepareForPlaying(String playlistId) {
    updatePlayingQueue(playlistId);
    updateLastPlayedSong(playlistId);
    updateCoversInRecyclerView(playlistId);
    updateMediaPlayer();
  }

  private void updateVisuals() {
    updateLastPlayedSong(getLastOpenedPlaylist().getId());
    updateCoversInRecyclerView(getLastOpenedPlaylist().getId());
  }

  private void updatePlayingQueue(String playlistId) {
    mOriginalPlayingQueue = new ArrayList<>();
    List<PlayableSong> playlistSongs = getSongsForPlaylist(playlistId);
    for(int i=0; i<playlistSongs.size(); i++) {
      PlayableSong playableSong = playlistSongs.get(i);
      playableSong.setIndexInQueue(i);
      MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(playableSong.getPath().getPath()));
      playableSong.setDurationInMs(mp.getDuration());
      mp.release();
      mOriginalPlayingQueue.add(playableSong);
    }
  }

  private void updateLastPlayedSong(final String playlistId) {
    mMiniPlayerArrowUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
      }
    });

    mPlayerArrowDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      }
    });

    mPlayerSeePlayingQueue.setEnabled(true);
    setColorEnabled(mPlayerSeePlayingQueue, true);
    mPlayerSeePlayingQueue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        PlayingQueueFragment playingQueueFragment = PlayingQueueFragment.newInstance(getCurrentPlayingQueue(), mPlayerSong.getIndexInQueue(), playlistId);
        playingQueueFragment.show(getSupportFragmentManager(),
          "playing_queue_fragment");
      }
    });

    mPlayerSongTitle.setText(mPlayerSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerSong.getTitle());
    mPlayerSongArtist.setText(mPlayerSong.getArtist());
    mPlayerCloudDownloadBtn.showBorder();

    if(!isSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!isSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerSong.getId())) {
          insertSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerSong.getId(), false);
          mPlayerFavoriteBtn.showSecondState();
        } else {
          removeSongFromPlaylist(PLAYLIST_LIKES_ID, mPlayerSong.getId());
          mPlayerFavoriteBtn.showFirstState();
        }
      }
    });

    if(isSongInPlaylist(PLAYLIST_DOWNLOADS_ID, mPlayerSong.getId())) {
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
            deleteSong(mPlayerSong);
            if(getAllSongsAsync().size() >= 1) {
              if(mPlayerSong.getIndexInQueue() - 1 >= 0) {
                loadSong(getCurrentPlayingQueue().get(mPlayerSong.getIndexInQueue() - 1), playlistId);
              } else {
                loadSong(getCurrentPlayingQueue().get(mPlayerSong.getIndexInQueue() + 1), playlistId);
              }
            } else {
              mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
          }
        });
        songDeletionDialog.negativeText(R.string.cancel);
        songDeletionDialog.show();
      }
    });

    if(!isSongInCustomPlaylist(mPlayerSong.getId())) {
      mPlayerAddToPlaylistBtn.showFirstState();
    } else {
      mPlayerAddToPlaylistBtn.showSecondState();
    }
    mPlayerAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        final List<Playlist> playlists = getAllPlaylists().subList(2, getAllPlaylists().size());
        String[] playlistsTitles = new String[playlists.size()];

        final List<Integer> checkedPlaylistsList = new ArrayList<>();

        for(int i=0; i<playlists.size(); i++) {
          playlistsTitles[i] = playlists.get(i).getTitle();
          if(isSongInPlaylist(playlists.get(i).getId(), mPlayerSong.getId())) {
            checkedPlaylistsList.add(i);
          }
        }

        Integer[] checkedPlaylists = new Integer[checkedPlaylistsList.size()];
        for(int index=0; index<checkedPlaylists.length; index++) {
          checkedPlaylists[index] = checkedPlaylistsList.get(index);
        }

        MaterialDialog.Builder addToPlaylistDialog = new MaterialDialog.Builder(mContext);
        addToPlaylistDialog.title(R.string.select_playlists);
        addToPlaylistDialog.items(playlistsTitles);
        addToPlaylistDialog.alwaysCallMultiChoiceCallback();
        addToPlaylistDialog.itemsCallbackMultiChoice(checkedPlaylists, new MaterialDialog.ListCallbackMultiChoice() {
          @Override
          public boolean onSelection(MaterialDialog dialog, Integer[] checked, CharSequence[] text) {
            return true;
          }
        });
        addToPlaylistDialog.cancelable(true);
        addToPlaylistDialog.positiveText(R.string.add);
        addToPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            Integer[] playlistsIdsToAddTo = dialog.getSelectedIndices();
            for(Playlist playlist : playlists) {
              removeSongFromPlaylist(playlist.getId(), mPlayerSong.getId());
            }
            if (playlistsIdsToAddTo != null && playlistsIdsToAddTo.length > 0) {
              for (Integer index : playlistsIdsToAddTo) {
                insertSongInPlaylist(String.valueOf(playlists.get(index).getId()), mPlayerSong.getId(), false);
              }
              mPlayerAddToPlaylistBtn.showSecondState();
            } else {
              mPlayerAddToPlaylistBtn.showFirstState();
            }
          }
        });
        addToPlaylistDialog.negativeText(R.string.cancel);
        if(playlists.size() > 0) {
          addToPlaylistDialog.show();
        } else {
          Toast.makeText(mContext, R.string.no_custom_playlists_error, Toast.LENGTH_LONG).show();
        }
      }
    });

    setPlayerLoading(false);

    if (getCurrentPlayingQueue() != null) {
      if (mPlayerSong.getIndexInQueue() == 0) {
        mPlayerPrevBtn.setEnabled(false);
        setColorEnabled(mPlayerPrevBtn, false);
      } else {
        mPlayerPrevBtn.setEnabled(true);
        setColorEnabled(mPlayerPrevBtn, true);
      }

      if (mPlayerSong.getIndexInQueue() == getCurrentPlayingQueue().size() - 1) {
        mPlayerNextBtn.setEnabled(false);
        setColorEnabled(mPlayerNextBtn, false);
      } else {
        mPlayerNextBtn.setEnabled(true);
        setColorEnabled(mPlayerNextBtn, true);
//        mPlayerNextUpSongTitle.setText(getCurrentPlayingQueue().get(mPlayerSong.getIndexInQueue()+1).getTitle());
      }

      setColorEnabled(mPlayerRepeatBtn, mPlayerSong.isLooping());
    }

    mPlayerRepeatBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!mPlayerSong.isLooping()) {
          mPlayerSong.setLooping(true);
          mPlayerRepeatBtn.setColorFilter(getResources().getColor(R.color.grey_67));
        } else {
          mPlayerSong.setLooping(false);
          mPlayerRepeatBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
        }
      }
    });

    mPlayerPrevBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(mPlayerMediaPlayer.getCurrentPosition() / 1000 >= 10) {
          mPlayerMediaPlayer.seekTo(0);
          mPlayerSongProgress.setProgress(0);
        } else {
          int previousSongIndex = mPlayerSong.getIndexInQueue() - 1;
          mPlayerRecyclerView.smoothScrollToPosition(previousSongIndex);
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
        int nextSongIndex = mPlayerSong.getIndexInQueue() + 1;
        mPlayerRecyclerView.smoothScrollToPosition(nextSongIndex);
      }
    });

    mPlayerShuffleBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!isPlayingQueueShuffled) {
          List<PlayableSong> shuffledPlayingQueue = mOriginalPlayingQueue;
          Collections.shuffle(shuffledPlayingQueue);
          for(PlayableSong currentSongInShuffled : shuffledPlayingQueue) {
            if(currentSongInShuffled.getId().equals(mPlayerSong.getId())) {
              Collections.swap(shuffledPlayingQueue, mPlayerSong.getIndexInQueue(), shuffledPlayingQueue.indexOf(currentSongInShuffled));
            }
          }
          updateShuffledPlayingQueue(shuffledPlayingQueue);
          isPlayingQueueShuffled = true;
          mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_67));
        } else {
          for(PlayableSong currentSongInOriginal : mOriginalPlayingQueue) {
            if(currentSongInOriginal.getId().equals(mPlayerSong.getId())) {
              mPlayerSong.setIndexInQueue(mOriginalPlayingQueue.indexOf(currentSongInOriginal));
            }
          }
          isPlayingQueueShuffled = false;
          mPlayerShuffleBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
          updateVisuals();
        }

      }
    });

    if(mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private void updateCoversInRecyclerView(final String playlistId) {
    mPlayerAdapter.clear();
    for (PlayableSong playableSong : getCurrentPlayingQueue()) {
      mPlayerAdapter.addCoverArt(playableSong);
    }

    mPlayerRecyclerView.clearOnScrollListeners();
    mPlayerRecyclerViewListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int songIndex = ((LinearLayoutManager) mPlayerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
          if(mPlayerSong.getIndexInQueue() != songIndex) {
            loadSong(getCurrentPlayingQueue().get(songIndex), playlistId);
          }
        }
      }
    };
    mPlayerRecyclerView.addOnScrollListener(mPlayerRecyclerViewListener);

    mPlayerRecyclerView.scrollToPosition(mPlayerSong.getIndexInQueue());
  }

  class PlayerCoversAdapter extends EpoxyAdapter {
    void addCoverArt(PlayableSong playableSong) {
      addModel(new PlayerCoverModel(mContext, playableSong));
    }

    void addCoverArt(String coverArtUrl) {
      addModel(new PlayerCoverModel(mContext, coverArtUrl));
    }

    void clear() {
      removeAllModels();
    }
  }

  private void updateMediaPlayer() {
    mPlayerMediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(mPlayerSong.getPath()));
    mPlayerMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mPlayerMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {
        mPlayerSongProgressTotal.setText(TextUtils.formatMilliseconds(mContext, mPlayerMediaPlayer.getDuration()));
        mPlayerSongProgress.setMax(mediaPlayer.getDuration());
        mMiniPlayerSongProgress.setMax(mediaPlayer.getDuration());

        mPlayerMediaPlayer.seekTo(mPlayerSong.getLastPosition());
        mPlayerSongProgress.setProgress(mPlayerSong.getLastPosition());
        mMiniPlayerSongProgress.setProgress(mPlayerSong.getLastPosition());
      }
    });

    mPlayerMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        if (!mPlayerSong.isLooping()) {
          if (mPlayerSong.getIndexInQueue() == getCurrentPlayingQueue().size() - 1) {
            mPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
            mMiniPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
            mPlayerMediaPlayer.seekTo(0);
          } else {
            int nextSongIndex = mPlayerSong.getIndexInQueue() + 1;
            mPlayerRecyclerView.smoothScrollToPosition(nextSongIndex);
          }
        } else {
          mPlayerMediaPlayer.seekTo(0);
          mPlayerMediaPlayer.start();
        }
      }
    });

    mPlayerSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
        if (input) {
          mPlayerMediaPlayer.seekTo(progress);
        }
        if(progress / 1000 < (mPlayerMediaPlayer.getDuration() / 1000 - 7) && mPlayerNextUp.getAlpha() == 1.0f) {
          mPlayerNextUp.animate().alpha(0.0f);
        } else if (progress / 1000 >= (mPlayerMediaPlayer.getDuration() / 1000 - 7) && mPlayerSong.getIndexInQueue() != getCurrentPlayingQueue().size() - 1) {
          mPlayerNextUp.animate().alpha(1.0f);
        }
        mPlayerSongProgressCurrent.setText(TextUtils.formatMilliseconds(mContext, progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  private void updateShuffledPlayingQueue(List<PlayableSong> playableSongsList) {
    mShuffledPlayingQueue = new ArrayList<>();
    for(int i=0; i<playableSongsList.size(); i++) {
      PlayableSong playableSong = playableSongsList.get(i);
      playableSong.setIndexInQueue(i);
      mShuffledPlayingQueue.add(playableSong);
    }
  }

  private List<PlayableSong> getCurrentPlayingQueue() {
    List<PlayableSong> mCurrentPlayingQueue;
    if(!isPlayingQueueShuffled) {
      mCurrentPlayingQueue = mOriginalPlayingQueue;
    } else {
      mCurrentPlayingQueue = mShuffledPlayingQueue;
    }
    return mCurrentPlayingQueue;
  }

  public void loadSong(PlayableSong playableSong, final String playlistId) {
    isRemoteSongPlaying = false;
    updateSongWithResetAll(playableSong, new OnLastClickedUpdateCompletion() {
      @Override
      public void onCompletion() {
        destroy();
        if(isLastPlayedSongGood()) {
          setLastOpenedPlaylist(getPlaylist(playlistId));
          prepareForPlaying(playlistId);
          playSong();
        }
      }
    });
  }

  public interface OnLastClickedUpdateCompletion {
    void onCompletion();
  }

  private void updateRemoteSong(final Song remoteSong) {
    mPlayerRemoteSong = remoteSong;

    mMiniPlayerArrowUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
      }
    });

    mPlayerSeePlayingQueue.setEnabled(false);
    setColorEnabled(mPlayerSeePlayingQueue, false);

    mPlayerArrowDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      }
    });

    mPlayerNextUp.animate().alpha(0.0f);

    mPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mPlayerSongArtist.setText(mPlayerRemoteSong.getArtist());

    if(!isRemoteSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId())) {
      mPlayerFavoriteBtn.showFirstState();
    } else {
      mPlayerFavoriteBtn.showSecondState();
    }
    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!isRemoteSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId())) {
//          insertRemoteSong(mPlayerRemoteSong);
//          insertSongInPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId(), true);
          mPlayerFavoriteBtn.showSecondState();
        } else {
          removeSongFromPlaylist(PLAYLIST_LIKES_ID, mPlayerRemoteSong.getId());
          mPlayerFavoriteBtn.showFirstState();
        }
      }
    });

    mPlayerCloudDownloadBtn.showBorder();
    mPlayerCloudDownloadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(!mPlayerCloudDownloadBtn.isBlinking()) {
          SongDownloader.downloadSong(mContext, mPlayerRemoteSong, new SongDownloader.SongDownloaderCallback() {
            @Override
            public void onStartOrResume(String songTitle) {
              onSongDownloadStartOrResume(songTitle);
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
        }
      }
    });

    mPlayerAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerAddToPlaylistBtn.switchState();
      }
    });

    mPlayerRepeatBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!isRemoteSongLooping) {
          mPlayerRepeatBtn.setColorFilter(getResources().getColor(R.color.grey_67));
          isRemoteSongLooping = true;
        } else {
          mPlayerRepeatBtn.setColorFilter(getResources().getColor(R.color.grey_c0));
          isRemoteSongLooping = false;
        }
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

    if(mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private void updateRemoteSongRecyclerView() {
    mPlayerAdapter.clear();
    mPlayerRecyclerView.removeOnScrollListener(mPlayerRecyclerViewListener);
    mPlayerAdapter.addCoverArt(mPlayerRemoteSong.getCoverArt());
  }

  private void updateMediaPlayer(String songUrl) {
    mPlayerMediaPlayer = new MediaPlayer();
    mPlayerMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
      mPlayerMediaPlayer.setDataSource(songUrl);
      mPlayerMediaPlayer.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
    mPlayerMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {
        setPlayerLoading(false);
        mPlayerPrevBtn.setEnabled(false);
        setColorEnabled(mPlayerPrevBtn, false);
        mPlayerNextBtn.setEnabled(false);
        setColorEnabled(mPlayerNextBtn, false);
        mPlayerShuffleBtn.setEnabled(false);

        DecimalFormat progressFormat = new DecimalFormat("00");
        String minutes = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 / 60);
        String seconds = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 % 60);
        mPlayerSongProgressTotal.setText(String.format(getResources().getString(R.string.player_progress_text), minutes, seconds));
        mPlayerSongProgress.setMax(mediaPlayer.getDuration());
      }
    });

    mPlayerMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        if (!isRemoteSongLooping) {
          mPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
          mMiniPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
          mPlayerMediaPlayer.seekTo(0);
        } else {
          mPlayerMediaPlayer.seekTo(0);
          mPlayerMediaPlayer.start();
        }
      }
    });

    mPlayerSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
        if (input) {
          mPlayerMediaPlayer.seekTo(progress-1);
        }
        DecimalFormat formatter = new DecimalFormat("00");
        String minutes = formatter.format(progress / 1000 / 60);
        String seconds = formatter.format(progress / 1000 % 60);
        mPlayerSongProgressCurrent.setText(String.format(getResources().getString(R.string.player_progress_text), minutes, seconds));
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
    if(hasSong(remoteSong.getId())) {
      loadSong(getSong(remoteSong.getId()), PLAYLIST_DOWNLOADS_ID);
      return;
    }
    destroy();
    isRemoteSongPlaying = true;
    isRemoteSongLooping = false;

    updateRemoteSong(remoteSong);
    updateRemoteSongRecyclerView();
    SongDownloader.fetchSongURL(mContext, remoteSong, new SongDownloader.SongURLFetcherCallback() {
      @Override
      public void onSuccess(YtFile songFile) {
        updateMediaPlayer(songFile.getUrl());
        playSong();
      }
    });
  }

  private void playSong() {
    mPlayerMediaPlayer.start();
    mPlayerPlayToPauseBtn.animateToSecondState(); //pause icon: playing
    mMiniPlayerPlayToPauseBtn.animateToSecondState(); //pause icon: playing

    startProgressTracking();
  }

  private void pauseSong() {
    mPlayerMediaPlayer.pause();
    mPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
    mMiniPlayerPlayToPauseBtn.animateToFirstState(); //play icon: paused
  }

  private void startProgressTracking() {
    mPlayerSongProgress.setProgress(mPlayerMediaPlayer.getCurrentPosition());
    mMiniPlayerSongProgress.setProgress(mPlayerMediaPlayer.getCurrentPosition());
    if (mPlayerMediaPlayer.isPlaying()) {
      mPlayerSongProgressHandlerRunnable = new Runnable() {
        @Override
        public void run() {
          startProgressTracking();
        }
      };
      mPlayerSongProgressHandler.postDelayed(mPlayerSongProgressHandlerRunnable, 1000);
    }

    if(!isRemoteSongPlaying) {
      mPlayerSong.setLastPosition(mPlayerMediaPlayer.getCurrentPosition());
      updateSongWithResetAll(mPlayerSong);
    }
  }

  private void onPlayPauseClick() {
    if(mPlayerPlayToPauseBtn.isEnabled()) {
      if (mPlayerMediaPlayer.isPlaying()) {
        pauseSong();
      } else {
        playSong();
      }
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

  public void insertPlaylist(Playlist playlist) {
    if(!hasPlaylist(playlist.getId())) {
      mPlaylistViewModel.insert(playlist);
    }
  }

  public Playlist getPlaylist(String playlistId) {
    return mPlaylistViewModel.getPlaylist(playlistId);
  }

  public List<Playlist> getAllPlaylists() {
    return mPlaylistViewModel.getAllPlaylists();
  }

  private void setLastOpenedPlaylist(Playlist playlist) {
    mPlaylistViewModel.updatePlaylistWithResetAll(playlist);
  }

  public Playlist getLastOpenedPlaylist() {
    return mPlaylistViewModel.getLastOpenedPlaylist();
  }

  public boolean hasPlaylist(String playlistId) {
    return mPlaylistViewModel.hasPlaylist(playlistId);
  }

  public void deletePlaylist(Playlist playlist) {
    mPlaylistViewModel.delete(playlist);
  }

  public void deletePlaylist(String playlistId) {
    mPlaylistViewModel.delete(playlistId);
  }

  public void insertSong(PlayableSong playableSong) {
    mSongViewModel.insert(playableSong);
  }

  public void insertRemoteSong(Song song) {
    mSongViewModel.insert(song);
  }

  public void updateSong(PlayableSong playableSong) {
    mSongViewModel.update(playableSong);
  }

  public PlayableSong getSong(String songId) {
    return mSongViewModel.getSong(songId);
  }

  private void updateSongWithResetAll(PlayableSong playableSong) {
    mSongViewModel.updateSongWithResetAll(playableSong);
  }

  private void updateSongWithResetAll(PlayableSong playableSong, OnLastClickedUpdateCompletion completionCallback) {
    mSongViewModel.updateSongWithResetAll(playableSong, completionCallback);
  }

  public boolean hasSong(String songId) {
    return mSongViewModel.hasSong(songId);
  }

  public List<PlayableSong> getAllSongsAsync() {
    return mSongViewModel.getAllSongs();
  }

  public void deleteSong(PlayableSong playableSong) {
    if(playableSong.getPath().delete()) {
      mSongViewModel.delete(playableSong);
    }
  }

  public void insertSongInPlaylist(String playlistId, String songId, boolean isRemoteSong) {
    if(!isSongInPlaylist(playlistId, songId)) {
      mPlaylistSongJoinViewModel.insert(new PlaylistSongJoin(playlistId, songId, isRemoteSong));
    }
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mPlaylistSongJoinViewModel.getPlaylistsForSong(songId);
  }

  public List<PlayableSong> getSongsForPlaylist(String playlistId) {
    return mPlaylistSongJoinViewModel.getSongsForPlaylist(playlistId);
  }

  public boolean isSongInPlaylist(String playlistId, String songId) {
    return mPlaylistSongJoinViewModel.isSongInPlaylist(playlistId, songId);
  }

  public boolean isRemoteSongInPlaylist(String playlistId, String songId) {
    return mPlaylistSongJoinViewModel.isRemoteSongInPlaylist(playlistId, songId);
  }

  public boolean isSongInCustomPlaylist(String songId) {
    boolean isInCustomPlaylist = false;
    for(Playlist playlist : getPlaylistsForSong(songId)) {
      if(!playlist.getId().equals(PLAYLIST_LIKES_ID) && !playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
        isInCustomPlaylist = true;
      }
    }
    return isInCustomPlaylist;
  }

  public void removeSongFromPlaylist(String playlistId, String songId) {
    mPlaylistSongJoinViewModel.remove(playlistId, songId);
  }

  public void onSongDownloadStartOrResume(String songTitle) {
    mPlayerCloudDownloadBtn.showBlinkingAnimation();
    Toast.makeText(mContext, String.format(getString(R.string.download_started_for_TITLE), songTitle), Toast.LENGTH_SHORT).show();
  }

  public void onSongDownloadCompletion(PlayableSong playableSong) {
    if(playableSong.getPath().exists()) {
      playableSong.setIndexInQueue(getSongsForPlaylist(PLAYLIST_DOWNLOADS_ID).size());
      insertSong(playableSong);
      insertSongInPlaylist(PLAYLIST_DOWNLOADS_ID, playableSong.getId(), false);
      if (playableSong.getId().equals(mPlayerRemoteSong.getId())) {
        mPlayerCloudDownloadBtn.showFill();
      }
      Toast.makeText(mContext, playableSong.getTitle() + " has been downloaded! Go to \"My Music\" to play it", Toast.LENGTH_SHORT).show();
    }
  }

  private void setColorEnabled(ImageView imageView, boolean enabled) {
    if(enabled) {
      imageView.setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      imageView.setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }

  private void setColorEnabled(ImageButton imageButtonView, boolean enabled) {
    if(enabled) {
      imageButtonView.setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      imageButtonView.setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }

  private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
    if (mMiniPlayer == null || mBottomNavigationView == null) return;
    float alpha = 1 - progress;
    mMiniPlayer.setAlpha(alpha);
    mBottomNavigationView.setTranslationY(mBottomNavigationView.getHeight() * (alpha * -1 + 1));

    mMiniPlayer.setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
    setMiniPlayerAlphaProgress(slideOffset);
  }

  @Override
  public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
    if(newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
      findViewById(R.id.player_layout).setClickable(true);
    } else {
      findViewById(R.id.player_layout).setClickable(false);
    }
  }

  private void destroy() {
    if(mPlayerMediaPlayer != null) {
      mPlayerMediaPlayer.release();
    }
    if(mPlayerSongProgressHandler != null) {
      mPlayerSongProgressHandler.removeCallbacks(mPlayerSongProgressHandlerRunnable);
    }
  }

  @Override
  public void onBackPressed() {
    if(mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      return;
    }

    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      getSupportFragmentManager().popBackStack();
      return;
    }

    super.onBackPressed();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    destroy();
  }

//  private static class GraphicsView extends View {
//    private static final String animatedText =
//      "NEXT UP - Make It Look Easy";
//
//    private Animation rotateAnim;
//
//    public GraphicsView(Context context) {
//      super(context);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//      super.onDraw(canvas);
//
//      // creates the animation the first time
//      if (rotateAnim == null) {
//        createAnim(canvas);
//      }
//
//      Path circle = new Path();
//
//      int centerX = canvas.getWidth() / 2;
//      int centerY = canvas.getWidth() / 2;
//      int r = Math.min(centerX, centerY);
//
//      circle.addCircle(centerX, centerY, r, Path.Direction.CW);
//      Paint paint = new Paint();
//      paint.setColor(getResources().getColor(R.color.grey_67));
//      paint.setTextSize(50);
//      paint.setAntiAlias(true);
//
//      canvas.drawTextOnPath(animatedText, circle, 0, 50, paint);
//    }
//
//    private void createAnim(Canvas canvas) {
//      rotateAnim = new RotateAnimation(360, 0, canvas.getWidth() / 2, canvas.getWidth() / 2);
//      rotateAnim.setDuration(30000L);
//      rotateAnim.setRepeatMode(Animation.INFINITE);
//
//      startAnimation(rotateAnim);
//    }
//  }
}
