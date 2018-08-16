package ml.toufic.songs.ui.activities;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import at.huber.youtubeExtractor.YtFile;
import ml.toufic.songs.R;
import ml.toufic.songs.db.Converters;
import ml.toufic.songs.db.DownloadedSongViewModel;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.PlayerCoverModel;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.SongDownloader;
import ml.toufic.songs.ui.fragments.ExploreFragment;
import ml.toufic.songs.ui.fragments.MyMusicFragment;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;
import ml.toufic.songs.views.CloudDownloadBorderToFillImageView;
import ml.toufic.songs.views.FavoriteBorderToFillMorphingImageView;
import ml.toufic.songs.views.PlayToPauseMorphingImageView;
import ml.toufic.songs.views.PlaylistAddToCheckMorphingImageView;

import static ml.toufic.songs.util.SongUtils.DOWNLOAD_COMPLETE_SONG_ACTIVITY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {

  private static final String TAG = "MainActivity: ";

  private Context mContext;

  private BottomNavigationView mBottomNavigationView;

  private DownloadedSongViewModel mDownloadedSongViewModel;

  private ExploreFragment mExploreFragment;
  private MyMusicFragment mMyMusicFragment;

  private List<PlayableSong> mPlayingQueue = new ArrayList<>();
  private PlayableSong mPlayerSong;

  private boolean isRemoteSongPlaying = false;
  private boolean isRemoteSongLooping = false;
  private Song mPlayerRemoteSong;

  private ImageButton mPlayerArrowDown;
  private LinearLayout mPlayerNextUp;
  private TextView mPlayerNextUpSongTitle;
  private PlayerCoversAdapter mPlayerAdapter;
  private RecyclerView mPlayerRecyclerView;
  private RecyclerView.OnScrollListener mPlayerRecyclerViewListener;
  private TextView mPlayerSongTitle;
  private TextView mPlayerSongArtist;
  private FavoriteBorderToFillMorphingImageView mPlayerFavoriteBtn;
  private CloudDownloadBorderToFillImageView mPlayerCloudDownloadBtn;
  private PlaylistAddToCheckMorphingImageView mPlayerPlaylistAddToPlaylistBtn;
  private SeekBar mPlayerSongProgress;
  private TextView mPlayerSongProgressCurrent;
  private TextView mPlayerSongProgressTotal;
  private ImageButton mPlayerRepeatBtn;
  private ImageButton mPlayerPrevBtn;
  private PlayToPauseMorphingImageView mPlayerPlayToPauseBtn;
  private ImageButton mPlayerNextBtn;

  private LinearLayout mMiniPlayer;
  private ImageView mMiniPlayerArrowUp;
  private TextView mMiniPlayerSongTitle;
  private PlayToPauseMorphingImageView mMiniPlayerPlayToPauseBtn;

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
//      .setDatabaseEnabled(true)
//      .setReadTimeout(30_000)
//      .setConnectTimeout(30_000)
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

    mDownloadedSongViewModel = ViewModelProviders.of(this).get(DownloadedSongViewModel.class);

    mDownloadedSongViewModel.getAllSongs().observe(this, new Observer<List<PlayableSong>>() {
      @Override
      public void onChanged(@Nullable final List<PlayableSong> songs) {
        // Update the cached copy of the songs in the adapter.
        for(Song song : songs) {
          Log.d(TAG, "onChanged: tgblog: " + Converters.fromSong(song));
        }
      }
    });

    /* Initialize player & mini player */

    initializePlayer();
    initializeRecyclerView();
    if(getLastPlayedSong()) {
      prepareForPlaying();
    }
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
    mPlayerNextUp = findViewById(R.id.player_next_up);
    mPlayerNextUpSongTitle = findViewById(R.id.player_next_up_song_title);
    mPlayerAdapter = new PlayerCoversAdapter();
    mPlayerRecyclerView = findViewById(R.id.player_cover_arts_recyclerview);
    mPlayerSongTitle = findViewById(R.id.player_song_title);
    mPlayerSongArtist = findViewById(R.id.player_song_artist);
    mPlayerFavoriteBtn = findViewById(R.id.player_action_favorite);
    mPlayerCloudDownloadBtn = findViewById(R.id.player_action_cloud_download);
    mPlayerPlaylistAddToPlaylistBtn = findViewById(R.id.player_action_add_to_playlist);
    mPlayerSongProgress = findViewById(R.id.player_song_progress);
    mPlayerSongProgressCurrent = findViewById(R.id.player_progress_current);
    mPlayerSongProgressTotal = findViewById(R.id.player_progress_total);
    mPlayerRepeatBtn = findViewById(R.id.player_repeat_btn);
    mPlayerPrevBtn = findViewById(R.id.player_prev_btn);
    mPlayerPlayToPauseBtn = findViewById(R.id.player_play_to_pause_btn);
    mPlayerNextBtn = findViewById(R.id.player_next_btn);

    mMiniPlayer = findViewById(R.id.mini_player_layout);
    mMiniPlayerArrowUp = findViewById(R.id.mini_player_arrow_up);
    mMiniPlayerSongTitle = findViewById(R.id.mini_player_song_title);
    mMiniPlayerPlayToPauseBtn = findViewById(R.id.mini_player_play_to_pause_btn);

    mSlidingPanelLayout = findViewById(R.id.sliding_layout);
    mSlidingPanelLayout.addPanelSlideListener(this);
    mPlayerSongProgressHandler = new Handler();

    if(SongUtils.getCachedLastSong(mContext) == null || mPlayingQueue.isEmpty()) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }
  }

  private void initializeRecyclerView() {
    mPlayerRecyclerView.setAdapter(mPlayerAdapter);
    mPlayerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(mPlayerRecyclerView);
  }

  private boolean getLastPlayedSong() {
    mPlayerSong = SongUtils.getCachedLastSong(mContext);

    boolean isSongOK = false;
    if(mPlayerSong != null && mPlayerSong.getPath().exists()) {
      isSongOK = true;
    }
    return isSongOK;
  }

  private void prepareForPlaying() {
    updatePlayingQueue();
    updateLastPlayedSong();
    updateCoversInRecyclerView();
    mPlayerRecyclerView.scrollToPosition(mPlayerSong.getIndexInQueue());
    updateMediaPlayer();
  }

  private void updatePlayingQueue() {
    mPlayingQueue = new ArrayList<>();
    List<PlayableSong> playableSongsList = SongUtils.getCachedSongs(mContext);
    for(int i=0; i<playableSongsList.size(); i++) {
      PlayableSong playableSong = playableSongsList.get(i);
      playableSong.setIndexInQueue(i);
      mPlayingQueue.add(playableSong);
    }
  }

  private void updateLastPlayedSong() {
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

    mPlayerSongTitle.setText(mPlayerSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerSong.getTitle());
    mPlayerSongArtist.setText(mPlayerSong.getArtist());

    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerFavoriteBtn.switchState();

      }
    });

    mPlayerCloudDownloadBtn.showFill();
    mPlayerCloudDownloadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(SongUtils.deleteSong(mContext, mPlayerSong)) {
          if(SongUtils.getCachedSongs(mContext).size() >= 1) {
            if(mPlayerSong.getIndexInQueue() - 1 >= 0) {
              loadSong(mPlayingQueue.get(mPlayerSong.getIndexInQueue() - 1));
            } else {
              loadSong(mPlayingQueue.get(mPlayerSong.getIndexInQueue() + 1));
            }
          } else {
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
          }
          mMyMusicFragment.updateRecyclerViewDownloadedSongs();
        }
      }
    });

    mPlayerPlaylistAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerPlaylistAddToPlaylistBtn.switchState();
      }
    });

    if (mPlayingQueue != null) {
      if (mPlayerSong.getIndexInQueue() == 0) {
        mPlayerPrevBtn.setEnabled(false);
        setColorEnabled(mPlayerPrevBtn, false);
      } else {
        mPlayerPrevBtn.setEnabled(true);
        setColorEnabled(mPlayerPrevBtn, true);
      }

      if (mPlayerSong.getIndexInQueue() == mPlayingQueue.size() - 1) {
        mPlayerNextBtn.setEnabled(false);
        setColorEnabled(mPlayerNextBtn, false);
      } else {
        mPlayerNextBtn.setEnabled(true);
        setColorEnabled(mPlayerNextBtn, true);
        mPlayerNextUpSongTitle.setText(mPlayingQueue.get(mPlayerSong.getIndexInQueue()+1).getTitle());
      }

      setColorEnabled(mPlayerRepeatBtn, mPlayerSong.isLooping());

      mPlayerPlayToPauseBtn.setEnabled(true);
      mMiniPlayerPlayToPauseBtn.setEnabled(true);
      mPlayerSongProgress.setEnabled(true);
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
//        mDownloadedSongViewModel.insert(mPlayerSong);
      }
    });

    mPlayerPrevBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int previousSongIndex = mPlayerSong.getIndexInQueue() - 1;
        mPlayerRecyclerView.smoothScrollToPosition(previousSongIndex);
      }
    });
    mPlayerNextBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int nextSongIndex = mPlayerSong.getIndexInQueue() + 1;
        mPlayerRecyclerView.smoothScrollToPosition(nextSongIndex);
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

    if(mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
  }

  private void updateCoversInRecyclerView() {
    if(mPlayingQueue.size() != mPlayerAdapter.getItemCount()) {
      mPlayerAdapter.clear();
      for (PlayableSong playableSong : mPlayingQueue) {
        mPlayerAdapter.addCoverArt(playableSong.getCoverArt());
      }
    }

    mPlayerRecyclerViewListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int songIndex = ((LinearLayoutManager) mPlayerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
          if(mPlayerSong.getIndexInQueue() != songIndex) {
            loadSong(mPlayingQueue.get(songIndex));
          }
        }
      }
    };
    mPlayerRecyclerView.addOnScrollListener(mPlayerRecyclerViewListener);
  }

  class PlayerCoversAdapter extends EpoxyAdapter {
    void addCoverArt(String coverArtUrl) {
      addModel(new PlayerCoverModel(coverArtUrl));
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
        DecimalFormat progressFormat = new DecimalFormat("00");
        String minutes = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 / 60);
        String seconds = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 % 60);
        mPlayerSongProgressTotal.setText(String.format(getResources().getString(R.string.player_progress_text), minutes, seconds));
        mPlayerSongProgress.setMax(mediaPlayer.getDuration());

        mPlayerMediaPlayer.seekTo(mPlayerSong.getLastPosition());
        mPlayerSongProgress.setProgress(mPlayerSong.getLastPosition());
      }
    });

    mPlayerMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        if (!mPlayerSong.isLooping()) {
          if (mPlayerSong.getIndexInQueue() == mPlayingQueue.size() - 1) {
            mPlayerPlayToPauseBtn.pause();
            mMiniPlayerPlayToPauseBtn.pause();
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
        } else if (progress / 1000 >= (mPlayerMediaPlayer.getDuration() / 1000 - 7) && mPlayerSong.getIndexInQueue() != mPlayingQueue.size() - 1) {
          mPlayerNextUp.animate().alpha(1.0f);
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

  public void loadSong(PlayableSong playableSong) {
    destroy();
    isRemoteSongPlaying = false;

    SongUtils.cacheLastSong(mContext, playableSong);
    getLastPlayedSong();
    prepareForPlaying();
    playSong();
  }

  private void updateRemoteSong(final Song remoteSong) {
    mPlayerRemoteSong = remoteSong;

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

    mPlayerNextUp.animate().alpha(0.0f);

    mPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mMiniPlayerSongTitle.setText(mPlayerRemoteSong.getTitle());
    mPlayerSongArtist.setText(mPlayerRemoteSong.getArtist());

    mPlayerFavoriteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerFavoriteBtn.switchState();

      }
    });

    mPlayerCloudDownloadBtn.clearAnimation();
    mPlayerCloudDownloadBtn.showBorder();
    mPlayerCloudDownloadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(mContext,
          Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED && DeviceUtils.isMarshmallow()) {

          requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
          SongDownloader.downloadSong(mContext, mPlayerRemoteSong, new SongDownloader.SongDownloaderCallback() {
            @Override
            public void onStartOrResume() {
              onSongDownloadStartOrResume();
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
              Toast.makeText(mContext, "Oops, we couldn't download your song. Please try again later", Toast.LENGTH_LONG).show();
            }
          });
        }
      }
    });

    mPlayerPlaylistAddToPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPlayerPlaylistAddToPlaylistBtn.switchState();
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

    mPlayerPrevBtn.setEnabled(false);
    setColorEnabled(mPlayerPrevBtn, false);

    // Temporarily while loading...
    mPlayerPlayToPauseBtn.setEnabled(false);
    mMiniPlayerPlayToPauseBtn.setEnabled(false);
    mPlayerSongProgress.setEnabled(false);

    mPlayerNextBtn.setEnabled(false);
    setColorEnabled(mPlayerNextBtn, false);

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
        DecimalFormat progressFormat = new DecimalFormat("00");
        String minutes = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 / 60);
        String seconds = progressFormat.format(mPlayerMediaPlayer.getDuration() / 1000 % 60);
        mPlayerSongProgressTotal.setText(String.format(getResources().getString(R.string.player_progress_text), minutes, seconds));
        mPlayerSongProgress.setMax(mediaPlayer.getDuration());

        mPlayerPlayToPauseBtn.setEnabled(true);
        mMiniPlayerPlayToPauseBtn.setEnabled(true);
        mPlayerSongProgress.setEnabled(true);
      }
    });

    mPlayerMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        if (!isRemoteSongLooping) {
          mPlayerPlayToPauseBtn.pause();
          mMiniPlayerPlayToPauseBtn.pause();
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
    mPlayerPlayToPauseBtn.play();
    mMiniPlayerPlayToPauseBtn.play();

    startProgressTracking();
  }

  private void pauseSong() {
    mPlayerMediaPlayer.pause();
    mPlayerPlayToPauseBtn.pause();
    mMiniPlayerPlayToPauseBtn.pause();
  }

  private void startProgressTracking() {
    mPlayerSongProgress.setProgress(mPlayerMediaPlayer.getCurrentPosition());
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
      SongUtils.cacheLastSong(mContext, mPlayerSong);

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

  public void onSongDownloadStartOrResume() {
    Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_fade_animation);
    mPlayerCloudDownloadBtn.startAnimation(myFadeInAnimation);
  }

  public void onSongDownloadCompletion(PlayableSong playableSong) {
    if(playableSong.getId().equals(mPlayerSong.getId())) {
      mPlayerCloudDownloadBtn.clearAnimation();
      mPlayerCloudDownloadBtn.showFill();
    }
    Toast.makeText(mContext, playableSong.getTitle() + " has been downloaded! Go to \"My Music\" to play it", Toast.LENGTH_LONG).show();
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        SongDownloader.downloadSong(mContext, mPlayerRemoteSong, new SongDownloader.SongDownloaderCallback() {
          @Override
          public void onStartOrResume() {
            onSongDownloadStartOrResume();
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
            Toast.makeText(mContext, "Oops, we couldn't download your song. Please try again later", Toast.LENGTH_LONG).show();
          }
        });
      } else {
        Toast.makeText(mContext, "Please accept permission to download songs!", Toast.LENGTH_LONG).show();
      }
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
    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      getSupportFragmentManager().popBackStack();
    } else {
      super.onBackPressed();
    }
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
}