package com.batache.songs.app.player;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.batache.songs.R;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.PlaylistSongJoin;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.api.SongDownloader;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.events.PlayerEvent;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class PlayerManager implements LifecycleOwner {

  private static PlayerManager sInstance;

  public static void initialize(Context context) {
    if (sInstance == null) {
      sInstance = new PlayerManager(context);
    }
  }

  public static PlayerManager getInstance() {
    if (sInstance == null) {
      throw new RuntimeException("Player Manager not initialized");
    }
    return sInstance;
  }

  private Context context;

  private LifecycleRegistry lifecycleRegistry;

  private DatabaseHelper databaseHelper;

  private SimpleExoPlayer exoPlayer;

  private List<Song> playqueue = new ArrayList<>();

  private String playlistId;
  private int index;

  private boolean isStreaming = false;
  private Song streamSong;

  private Handler progressHandler = new Handler();
  private Runnable progressHandlerRunnable;

  private PlayerManager(Context ctxt) {
    // we should always use the application context to avoid memory leaks
    this.context = ctxt.getApplicationContext();

    lifecycleRegistry = new LifecycleRegistry(this);
    lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
    lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);

    databaseHelper = DatabaseHelper.getInstance(context);
    if (databaseHelper.getPlaylist(PLAYLIST_LIKES_ID) == null) {
      databaseHelper.insertPlaylist(new Playlist(PLAYLIST_LIKES_ID, context.getString(R.string.favorites), new Date()));
    }
    if (databaseHelper.getPlaylist(PLAYLIST_DOWNLOADS_ID) == null) {
      databaseHelper.insertPlaylist(new Playlist(PLAYLIST_DOWNLOADS_ID, context.getString(R.string.downloads), new Date()));
    }

    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE)
        .build();

    exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());

    LastPlayed lastPlayed = getLastPlayed();
    if (lastPlayed != null) {
      loadSong(lastPlayed.getPlaylistId(), lastPlayed.getSongIndex(), lastPlayed.getPosition());
    }

    exoPlayer.addListener(new Player.EventListener() {
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
          case Player.STATE_READY:
            startProgressTracker();
            break;

          case Player.STATE_ENDED:
            stop();
            break;
        }
      }

      @Override
      public void onRepeatModeChanged(int repeatMode) {
        PlayerEvent.postRepeatChanged();
      }

      @Override
      public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        if (shuffleModeEnabled) {
          List<Song> shuffledPlayqueue = new ArrayList<>(getPlayqueue());
          Collections.shuffle(shuffledPlayqueue);
          setPlayQueue(shuffledPlayqueue);
        } else {
          setPlayQueue(getPlayqueue());
        }
        //TODO: seek to current song index
        //TODO: eventbus update recyclerview song covers
      }

      @Override
      public void onPlayerError(ExoPlaybackException error) {

      }

      @Override
      public void onPositionDiscontinuity(int reason) {
        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
          index = exoPlayer.getCurrentPeriodIndex();
          PlayerEvent.postSongChanged();
        }
      }

      @Override
      public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

      }

      @Override
      public void onSeekProcessed() {
        PlayerEvent.postSongChanged();
      }
    });
    exoPlayer.setAudioAttributes(audioAttributes, true);
  }

  private void startProgressTracker() {
    progressHandlerRunnable = new Runnable() {
      @Override
      public void run() {
        if (exoPlayer != null) {
          PlayerEvent.postProgressChanged();
          progressHandler.postDelayed(this, 500);

          if (!isStreaming()) {
            setLastPlayed(playlistId, index, getCurrentPosition());
          }
        }
      }
    };
    progressHandler.post(progressHandlerRunnable);
  }

  private void setPlayQueue(List<Song> songs) {
    PlayerEvent.postPlayQueueChanged();
    pause();

    streamSong = null;
    playqueue = songs;

    MediaSource[] playlist = new MediaSource[songs.size()];
    for (int i = 0; i < songs.size(); i++) {
      Song song = songs.get(i);

      if(!song.isLocallyStored() && song.getSongSource() == Song.SongSource.youtube) {
        int finalI = i;
        SongDownloader.getInstance().fetchSongURL(context, song, songFile -> {
          playlist[finalI] = generateMediaSource(Uri.parse(songFile.getUrl()), false);
        });
        return;
      }

      playlist[i] = generateMediaSource(song.getUri(), song.isLocallyStored());
    }

    ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist);
    exoPlayer.prepare(concatenatingMediaSource);

    PlayerEvent.postPlayQueueReady();
  }

  private void setPlayQueueAndPlayWhenReady(Song song) {
    PlayerEvent.postPlayQueueChanged();
    pause();

    streamSong = song;
    playqueue = Arrays.asList(song);

//    if(song.getUri() == null) {
//      ApiManager.request(MusicApi.INSTANCE.getMusicInfo(mPlayingMusic), new RequestCallBack<Music>() {
//        @Override
//        public void success(Music result) {
//          LogUtil.e(TAG, "-----" + result.toString());
//          mPlayingMusic = result;
//          saveHistory();
//          playErrorTimes = 0;
//          mPlayer.setDataSource(mPlayingMusic.getUri());
//        }
//
//        @Override
//        public void error(String msg) {
//          LogUtil.e(TAG, "播放异常-----" + msg);
//          checkPlayErrorTimes();
//        }
//      });
//    return;
//    }

    if (song.getSongSource() == Song.SongSource.youtube) {
      SongDownloader.getInstance().fetchSongURL(context, song, songFile -> {
        exoPlayer.prepare(generateMediaSource(Uri.parse(songFile.getUrl()), false));
        PlayerEvent.postPlayQueueReady();
        play(0);
      });
      return;
    }

    exoPlayer.prepare(generateMediaSource(song.getUri(), false));
    PlayerEvent.postPlayQueueReady();
    play(0);
  }

  private DataSource.Factory getLocallyStoredSongFactory(Uri uri) {
    DataSpec dataSpec = new DataSpec(uri);
    final FileDataSource fileDataSource = new FileDataSource();
    try {
      fileDataSource.open(dataSpec);
    } catch (FileDataSource.FileDataSourceException e) {
      e.printStackTrace();
    }

    return () -> fileDataSource;
  }

  private DefaultHttpDataSourceFactory getStreamSongFactory() {
    return new DefaultHttpDataSourceFactory("songs-app");
  }

  private MediaSource generateMediaSource(Uri uri, boolean isLocallyStored) {
    return new ExtractorMediaSource
        .Factory(isLocallyStored ? getLocallyStoredSongFactory(uri) : getStreamSongFactory())
        .setExtractorsFactory(new DefaultExtractorsFactory())
        .createMediaSource(uri);
  }

  public List<Song> getPlayqueue() {
    return playqueue;
  }

  public void play(int index) {
    safeSeekTo(index, C.TIME_UNSET);
    resume();
  }

  public void resume() {
    exoPlayer.setPlayWhenReady(true);
    PlayerEvent.postPlayPauseChanged();
  }

  public void pause() {
    exoPlayer.setPlayWhenReady(false);
    PlayerEvent.postPlayPauseChanged();
  }

  private void stop() {
    pause();
    seekTo(0);
  }

  public void seekTo(int index, long milliseconds) {
    if (exoPlayer == null) {
      return;
    }
    this.index = index;
    exoPlayer.seekTo(index, milliseconds);
  }

  public void safeSeekTo(int index, long milliseconds) {
    if (exoPlayer == null) {
      return;
    }
    milliseconds = this.index == index ? getCurrentPosition() : milliseconds;
    this.index = index;
    exoPlayer.seekTo(index, milliseconds);
  }

  public void seekTo(long milliseconds) {
    if (exoPlayer == null) {
      return;
    }
    exoPlayer.seekTo(milliseconds);
  }

  public boolean isPlaying() {
    return exoPlayer.getPlayWhenReady();
  }

  public int getCurrentSongIndex() {
    return index;
  }

  public boolean isCurrentSongLocallyStored() {
    if(getCurrentSong() == null) {
      return false;
    }
    return getCurrentSong().isLocallyStored();
  }

  public boolean isStreaming() {
    return isStreaming;
  }

  public void playSong(String playlistId, int index) {
    isStreaming = false;
    this.playlistId = playlistId;
    setPlayQueue(databaseHelper.getSongsForPlaylist(playlistId));
    PlayerEvent.postSongChanged();
    play(index);
  }

  public void loadSong(String playlistId, int index, long position) {
    isStreaming = false;
    this.playlistId = playlistId;
    setPlayQueue(databaseHelper.getSongsForPlaylist(playlistId));
    seekTo(index, position);
  }

  public void resumeSong(String playlistId, int index, long position) {
    isStreaming = false;
    this.playlistId = playlistId;
    setPlayQueue(databaseHelper.getSongsForPlaylist(playlistId));
    PlayerEvent.postSongChanged();
    play(index);
  }

  public void playSong(Song song) {
    if (databaseHelper.hasSong(song.getId())) {
      List<Song> playlist = databaseHelper.getSongsForPlaylist(PLAYLIST_DOWNLOADS_ID);
      for (int i = 0; i < playlist.size(); i++) {
        if (playlist.get(i).getId().equals(song.getId())) {
          playSong(PLAYLIST_DOWNLOADS_ID, i);
        }
      }
      return;
    }

    isStreaming = true;
    this.playlistId = null;
    setPlayQueueAndPlayWhenReady(song);
    PlayerEvent.postSongChanged();
  }

  public Song getCurrentSong() {
    if(isStreaming()) {
      return streamSong;
    }

    List<Song> playlistSongs = databaseHelper.getSongsForPlaylist(playlistId);

    if (CodeUtils.isEmpty(playlistSongs) || playlistSongs.size() <= index) {
      return null;
    }

    return playlistSongs.get(index);
  }

  public String getCurrentSongTitle() {
    if (getCurrentSong() == null) {
      return null;
    }
    return getCurrentSong().getTitle();
  }

  public String getCurrentSongArtist() {
    if (getCurrentSong() == null) {
      return null;
    }
    return getCurrentSong().getArtist();
  }

  public void addToRemoveFromFavorites() {
    if(getCurrentSong() == null) {
      return;
    }

    if (!isCurrentSongInFavorites()) {

      if(!isCurrentSongLocallyStored()) {
        databaseHelper.insertSong(getCurrentSong());
      }

      databaseHelper.insertSongInPlaylist(new PlaylistSongJoin(PLAYLIST_LIKES_ID, getCurrentSong().getId()));
    } else {

      if(!isCurrentSongLocallyStored()) {
        databaseHelper.deleteSong(getCurrentSong());
      }

      databaseHelper.removeSongFromPlaylist(PLAYLIST_LIKES_ID, getCurrentSong().getId());

      if (isStreaming()) {
        return;
      }

      if (getPlayqueue().size() > 1) {
        int newIndex = index < getPlayqueue().size() ? index : index - 1;
        playSong(playlistId, newIndex);
      } else {
        playqueue = new ArrayList<>();
        stop();
      }
    }
    PlayerEvent.postFavoriteChanged();
  }

  public boolean isCurrentSongInFavorites() {
    if(getCurrentSong() == null) {
      return false;
    }
    return isSongInFavorites(getCurrentSong());
  }

  public boolean isSongInFavorites(Song song) {
    return databaseHelper.isSongInPlaylist(PLAYLIST_LIKES_ID, song.getId());
  }

  public boolean downloadCurrentSong() {
    if (!isCurrentSongLocallyStored()) {
      SongDownloader.getInstance().downloadSong(getCurrentSong());
      return true;
    }
    return false;
  }

  public boolean cancelCurrentSongDownload() {
    if (!isCurrentSongLocallyStored()) {
      SongDownloader.getInstance().cancelDownload(getCurrentSong().getId());
      return true;
    }
    return false;
  }

  public void deleteCurrentSong() {
    if(getCurrentSong().getPath() != null && getCurrentSong().getPath().delete()) {
      databaseHelper.deleteSong(getCurrentSong());
      PlayerEvent.postSongDeleted();
      databaseHelper.getSongsListener().observe(this, songs -> {
        databaseHelper.getSongsListener().removeObservers(this);

        if (isStreaming()) {
          return;
        }

        if (getPlayqueue().size() > 1) {
          int newIndex = index < getPlayqueue().size() ? index : index - 1;
          playSong(playlistId, newIndex);
        } else {
          playqueue = new ArrayList<>();
          stop();
        }
      });
    }
  }

  public boolean isCurrentSongDownloaded() {
    if(getCurrentSong() == null) {
      return false;
    }
    return isCurrentSongInPlaylist(PLAYLIST_DOWNLOADS_ID);
  }

  public List<Playlist> getAllPlaylists() {
    return databaseHelper.getAllPlaylists();
  }

  public boolean isCurrentSongInPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return false;
    }
    return databaseHelper.isSongInPlaylist(playlistId, getCurrentSong().getId());
  }

  public boolean isCurrentSongInCustomPlaylist() {
    if (getCurrentSong() == null) {
      return false;
    }
    return databaseHelper.isSongInCustomPlaylist(getCurrentSong().getId());
  }

  public void createPlaylistAndInsertCurrentSong(String playlistTitle) {
    if (getCurrentSong() == null) {
      return;
    }
    String playlistId = String.valueOf(Integer.valueOf(getAllPlaylists().get(getAllPlaylists().size() - 1).getId()) + 1);
    databaseHelper.insertPlaylist(new Playlist(playlistId, playlistTitle, new Date()));
    insertCurrentSongInPlaylist(playlistId);
  }

  public void insertCurrentSongInPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }
    databaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlistId, getCurrentSong().getId()));
    PlayerEvent.postAddToPlaylistChanged();
  }

  public void removeCurrentSongFromPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }
    databaseHelper.removeSongFromPlaylist(playlistId, getCurrentSong().getId());
    PlayerEvent.postAddToPlaylistChanged();
  }

  public void deletePlaylistAndSongs(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }
    for (Song song : databaseHelper.getSongsForPlaylist(playlistId)) {
      databaseHelper.removeSongFromPlaylist(playlistId, song.getId());
      if (song.getId().equals(getCurrentSong().getId())) {
        PlayerEvent.postAddToPlaylistChanged();
      }
    }
    databaseHelper.deletePlaylist(playlistId);
  }

  public void shareCurrentSong() {
    //TODO: Share current song
  }

  public long getCurrentPosition() {
    if (exoPlayer == null) {
      return 0;
    }
    return exoPlayer.getCurrentPosition();
  }

  public long getSongBufferedPosition() {
    if (exoPlayer == null) {
      return 0;
    }
    if(isCurrentSongLocallyStored()) {
      return -1;
    }
    return exoPlayer.getBufferedPosition();
  }

  public long getSongDuration() {
    if (exoPlayer == null) {
      return 0;
    }
    return exoPlayer.getDuration();
  }

  public void toggleRepeat() {
    if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_OFF) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
    } else if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
    } else if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
    }
  }

  public int getRepeatMode() {
    return exoPlayer.getRepeatMode();
  }

  public void playPrev() {
    if (index == 0 || getCurrentPosition() / 1000 >= 10) {
      seekTo(0);
    } else {
      seekTo(index - 1, C.TIME_UNSET);
    }
    resume();
  }

  public void playNext() {
    if (index == getPlayqueue().size() - 1) {
      seekTo(0);
    } else {
      seekTo(index + 1, C.TIME_UNSET);
    }
    resume();
  }

  public void shuffle() {
    exoPlayer.setShuffleModeEnabled(!exoPlayer.getShuffleModeEnabled());
    PlayerEvent.postShuffleChanged();
  }

  public boolean isShuffled() {
    return exoPlayer.getShuffleModeEnabled();
  }

  public void setLastPlayed(String playlistId, int indexInPlaylist, long songPosition) {
    databaseHelper.setLastPlayed(new LastPlayed(playlistId, indexInPlaylist, songPosition));
  }

  public LastPlayed getLastPlayed() {
    return databaseHelper.getLastPlayed();
  }

  public static void destroy() {
    if(sInstance == null) {
      return;
    }
    if (sInstance.exoPlayer != null) {
      sInstance.exoPlayer.release();
    }
    sInstance.lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
    sInstance = null;
  }

  @NonNull
  @Override
  public Lifecycle getLifecycle() {
    return lifecycleRegistry;
  }
}
