package com.batache.songs.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.batache.songs.R;
import com.batache.songs.SongsApplication;
import com.batache.songs.api.SongDownloader;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.PlaylistSongJoin;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.events.DownloadEvent;
import com.batache.songs.util.events.PlayerEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class PlayerManager implements LifecycleOwner {

  private static PlayerManager sInstance;

  public static void initialize() {
    if (sInstance == null) {
      sInstance = new PlayerManager(SongsApplication.get());
    }
  }

  public static PlayerManager getInstance() {
    if (sInstance == null) {
      throw new RuntimeException("Player Manager not initialized");
    }
    return sInstance;
  }

  private SongsApplication context;

  private LifecycleRegistry lifecycleRegistry;

  private DatabaseHelper databaseHelper;

  private SimpleExoPlayer exoPlayer;
  private List<Song> playqueue = new ArrayList<>();
  private String playlistId;
  private int playlistSize = -1;
  private boolean showingPlayerNotification;

  private boolean isStreaming = false;
  private Song streamSong;

  private Handler mainHandler = new Handler(Looper.getMainLooper());
  private Runnable notificationUpdaterRunnable = new Runnable() {
    @Override
    public void run() {
      PlayerService.startUpdate(context);
    }
  };
  private Runnable updateProgressRunnable = this::logTime;

  private PlayerManager(SongsApplication songsApplication) {
    // we should always use the application context to avoid memory leaks
    this.context = songsApplication;

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

    exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());

    LastPlayed lastPlayed = getLastPlayed();
    if (lastPlayed != null) {
      isStreaming = false;
      updatePlaylistData(lastPlayed.getPlaylistId());
//      this.playlistId = lastPlayed.getPlaylistId();
//      playlistSize = databaseHelper.getSongsForPlaylist(playlistId).size();
      setPlayQueue(databaseHelper.getSongsForPlaylist(getPlaylistId()));
      seekTo(lastPlayed.getSongIndex(), lastPlayed.getPosition());
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

      @SuppressLint("StaticFieldLeak")
      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        PlayerEvent.postPlayPauseChanged();

        if (playWhenReady && playbackState == Player.STATE_READY) {
          // media actually playing
          startProgressTracker();
        } else {
          // media not playing
          stopProgressTracker();
        }

        switch (playbackState) {
          case Player.STATE_IDLE:
            if (!isStreaming() && getCurrentSong().getSongSource() == Song.SongSource.youtube && getCurrentSong().getUri() == null) {
              new YouTubeExtractor(context) {
                @Override
                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                  if (ytFiles != null) {
                    for (int i = 0, itag; i < ytFiles.size(); i++) {
                      itag = ytFiles.keyAt(i);
                      final YtFile ytFile = ytFiles.get(itag);

                      // We only want audio: height == -1 :)
                      if (ytFile.getFormat().getHeight() == -1) {
                        Song resolvedSong = getCurrentSong();
                        resolvedSong.setUri(Uri.parse(ytFile.getUrl()));

                        List<Song> newPlayQueue = getPlayqueue();
                        newPlayQueue.set(getCurrentSongIndex(), resolvedSong);
                        int songIndex = getCurrentSongIndex();
                        setPlayQueue(newPlayQueue);
                        PlayerEvent.postPlayQueueReady();

                        play(songIndex);
                      }
                    }
                  }
                }
              }.extract(getPlayqueue().get(getCurrentSongIndex()).getLink(), true, false);
            } else {
              PlayerEvent.postPlayQueueReady();
            }
            break;

          case Player.STATE_READY:
//            startProgressTracker();
            break;

          case Player.STATE_ENDED:
            stop();
//            TODO: This is used when preparing exoplayer for each song
//            if (index + 1 < getPlayqueue().size()) {
//              playNext();
//            } else {
//              if (getRepeatMode() == Player.REPEAT_MODE_OFF) {
//                stop();
//              } else if (getRepeatMode() == Player.REPEAT_MODE_ALL) {
//                play(0);
//              }
//            }
            break;

          default:
            if (getCurrentSong().getUri() != null) {
              PlayerEvent.postPlayQueueReady();
            }
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

    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .build();
    exoPlayer.setAudioAttributes(audioAttributes, true);

//    startProgressTracker();
  }

  private void logTime() {
    stopProgressTracker();

    if (exoPlayer == null) {
      return;
    }

    PlayerEvent.postProgressChanged();

    if (!isStreaming()) {
      setLastPlayed(getPlaylistId(), getCurrentSongIndex(), getCurrentPosition());
    }

    mainHandler.postDelayed(updateProgressRunnable, 500);
  }

  private void updatePlaylistData(String id) {
    playlistId = id;
    playlistSize = id == null ? -1 : databaseHelper.getSongsForPlaylist(getPlaylistId()).size();
  }

  private String getPlaylistId() {
    return playlistId;
  }

//  private static void startService(Context context, Intent intent) {
//    if (DeviceUtils.isOreo()) {
//      context.startForegroundService(intent);
//    } else {
//      context.startService(intent);
//    }
//  }

  private void startProgressTracker() {
    mainHandler.post(updateProgressRunnable);
  }

  private void stopProgressTracker() {
    mainHandler.removeCallbacks(updateProgressRunnable);
  }

  private void setPlayQueue(List<Song> songs) {
    PlayerEvent.postPlayQueueChanged();
    pause();

    streamSong = null;
    playqueue = songs;

    MediaSource[] playlist = new MediaSource[songs.size()];
    for (int i = 0; i < songs.size(); i++) {
      Song song = songs.get(i);

//      TODO: No, instead just put a wrong uri that will later be used to generate url and play
//      if (!song.isLocallyStored() && song.getSongSource() == Song.SongSource.youtube) {
//        int finalI = i;
//        SongDownloader.getInstance().fetchSongURL(context, song, songFile -> {
//          playlist[finalI] = generateMediaSource(Uri.parse(songFile.getUrl()), false);
//        });
//        return;
//      }

      playlist[i] = generateMediaSource(song, song.isLocallyStored());
    }

    ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist);
    exoPlayer.prepare(concatenatingMediaSource);
  }

  private void setPlayQueueAndPlayWhenReady(Song song) {
    PlayerEvent.postPlayQueueChanged();
    pause();

    streamSong = song;
    playqueue = Arrays.asList(song);

    if (song.getSongSource() == Song.SongSource.youtube || song.getSongSource() == Song.SongSource.dndnha) {
      SongDownloader.getInstance().fetchSongURL(context, song, new SongDownloader.SongURLFetcherCallback() {
        @Override
        public void onSuccess(String playableUrl) {
          streamSong.setUri(Uri.parse(playableUrl));
          exoPlayer.prepare(generateMediaSource(streamSong, false));
          PlayerEvent.postPlayQueueReady();
          play(0);
        }

        @Override
        public void onError() {
          DownloadEvent.postDownloadCancelled(song);
        }
      });
      return;
    }
//    else if(song.getUri() == null) {
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
//      return;
//    }

    exoPlayer.prepare(generateMediaSource(song, false));
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
    return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "songs-app"));
  }

  private MediaSource generateMediaSource(Song song, boolean isLocallyStored) {
    return new ProgressiveMediaSource
        .Factory(isLocallyStored ? getLocallyStoredSongFactory(song.getUri()) :
//        song.getSongSource() == Song.SongSource.youtube ? getYoutubeSongFactory() :
        getStreamSongFactory(), new DefaultExtractorsFactory())
        .createMediaSource(
//            !isLocallyStored && song.getSongSource() == Song.SongSource.youtube ? Uri.parse("app://" + song.getLink()) :
            song.getUri()
        );
  }

  public List<Song> getPlayqueue() {
    return playqueue;
  }

  private void resetPlayqueueAndStop() {
    playqueue = new ArrayList<>();
    stop();
  }

  public void play(int index) {
    safeSeekTo(index, C.TIME_UNSET);
    resume();
  }

  public void safePlay(int index) {
    if(index == getCurrentSongIndex()) {
      return;
    }
    play(index);
  }

  public void resume() {
    exoPlayer.setPlayWhenReady(true);
    PlayerEvent.postPlayPauseChanged();

    showingPlayerNotification = true;
    updatePlayerNotification();
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
//     TODO: This is used when preparing exoplayer for each song
//    exoPlayer.prepare(generateMediaSource(getPlayqueue().get(index), true));
//    PlayerEvent.postSongChanged();
    exoPlayer.seekTo(index, milliseconds);
  }

  public void safeSeekTo(int index, long milliseconds) {
    milliseconds = getCurrentSongIndex() == index ? getCurrentPosition() : milliseconds;
    seekTo(index, milliseconds);
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
//    return index;
    return exoPlayer.getCurrentWindowIndex();
  }

  public boolean isCurrentSongLocallyStored() {
    if (getCurrentSong() == null) {
      return false;
    }
    return getCurrentSong().isLocallyStored();
  }

  public boolean isStreaming() {
    return isStreaming;
  }

  public Song getCurrentSong() {
    if (isStreaming()) {
      return streamSong;
    }

    List<Song> playlistSongs = databaseHelper.getSongsForPlaylist(getPlaylistId());

    if (CodeUtils.isEmpty(playlistSongs) || playlistSongs.size() <= getCurrentSongIndex()) {
      return null;
    }

    return playlistSongs.get(getCurrentSongIndex());
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
    if (getCurrentSong() == null) {
      return;
    }

    if (!isCurrentSongInFavorites()) {

      if (!isCurrentSongLocallyStored()) {
        Song song = getCurrentSong();
        if (song.getSongSource() == Song.SongSource.youtube) {
          song.setUri(null);
        }
        insertSong(song);
      }

      insertCurrentSongInPlaylist(PLAYLIST_LIKES_ID);
    } else {

      if (!isCurrentSongLocallyStored()) {
        databaseHelper.deleteSong(getCurrentSong());
      }

      removeCurrentSongFromPlaylist(PLAYLIST_LIKES_ID);

      if (!isStreaming()) {
        if (getPlayqueue().size() > 1) {
          int newIndex = getCurrentSongIndex() < getPlayqueue().size() ? getCurrentSongIndex() : getCurrentSongIndex() - 1;
          playSong(getPlaylistId(), newIndex);
        } else {
          resetPlayqueueAndStop();
        }
      }
    }
    mainHandler.postDelayed(PlayerEvent::postFavoriteChanged, 100);
  }

  public void insertSong(Song song) {
    databaseHelper.insertSong(song);
  }

  public void addToPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }

    if (!isCurrentSongInPlaylist(playlistId)) {

      if (!isCurrentSongLocallyStored()) {
        Song song = getCurrentSong();
        if (song.getSongSource() == Song.SongSource.youtube) {
          song.setUri(null);
        }
        insertSong(song);
      }

      insertCurrentSongInPlaylist(playlistId);
    }
  }

  public boolean isCurrentSongInFavorites() {
    if (getCurrentSong() == null) {
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
    if (getCurrentSong().getPath() != null && getCurrentSong().getPath().delete()) {
      databaseHelper.deleteSong(getCurrentSong());

      PlayerEvent.postSongDeleted();

      databaseHelper.getSongsListener().observe(this, songs -> {
        databaseHelper.getSongsListener().removeObservers(this);

        if (isStreaming()) {
          return;
        }

        updatePlaylistData(getPlaylistId());

        if (getPlayqueue().size() > 1) {
          int newIndex = getCurrentSongIndex() < getPlayqueue().size() ? getCurrentSongIndex() : getCurrentSongIndex() - 1;
          playSong(getPlaylistId(), newIndex);
        } else {
          resetPlayqueueAndStop();
        }
      });
    }
  }

  public boolean isCurrentSongDownloaded() {
    if (getCurrentSong() == null) {
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

  public String createPlaylist(String playlistTitle) {
    int lastPlaylistId = Integer.parseInt(getAllPlaylists().get(getAllPlaylists().size() - 1).getId());
    String playlistId = String.valueOf(lastPlaylistId + 1);
    databaseHelper.insertPlaylist(new Playlist(playlistId, playlistTitle, new Date()));
    return playlistId;
  }

  public void createPlaylistAndInsertCurrentSong(String playlistTitle) {
    if (getCurrentSong() == null) {
      return;
    }
    String createdPlaylistId = createPlaylist(playlistTitle);
    insertCurrentSongInPlaylist(createdPlaylistId);
  }

  public void insertCurrentSongInPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }
    insertSongInPlaylist(playlistId, getCurrentSong().getId());

    mainHandler.postDelayed(PlayerEvent::postAddToPlaylistChanged, 100);
  }

  public void insertSongInPlaylist(String playlistId, String songId) {
    databaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlistId, songId));
  }

  public void removeCurrentSongFromPlaylist(String playlistId) {
    if (getCurrentSong() == null) {
      return;
    }
    databaseHelper.removeSongFromPlaylist(playlistId, getCurrentSong().getId());
    mainHandler.postDelayed(PlayerEvent::postAddToPlaylistChanged, 100);
  }

  public void deletePlaylistAndSongs(String playlistId) {
    if (getCurrentSong() != null) {
      String currentSongId = getCurrentSong().getId();

      for (Song song : databaseHelper.getSongsForPlaylist(playlistId)) {
        databaseHelper.removeSongFromPlaylist(playlistId, song.getId());
        if (song.getId().equals(currentSongId)) {
          mainHandler.postDelayed(PlayerEvent::postAddToPlaylistChanged, 100);
        }
      }
    }
    databaseHelper.deletePlaylist(playlistId);
    resetPlayqueueAndStop();
  }

  public void shareCurrentSong(Activity activity) {
    Intent share = new Intent(Intent.ACTION_SEND);
//    share.setType("audio/*");
//    share.putExtra(Intent.EXTRA_STREAM, getCurrentSong().getUri());
    share.setType("text/plain");
    share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share song");

    String linkToFormat = "https://www.youtube.com/watch?v=%s";
    switch (getCurrentSong().getSongSource()) {
      case youtube:
        linkToFormat = "https://www.youtube.com/watch?v=%s";
        break;
      case dndnha:
        linkToFormat = "https://www.dndnha.com/song_listen_%s_1.html";
        break;
      case xiami:
        linkToFormat = "https://www.xiami.com/song/%s";
        break;
    }
    String songLink = String.format(linkToFormat, getCurrentSong().getId());

    share.putExtra(android.content.Intent.EXTRA_TEXT, songLink);
    activity.startActivity(Intent.createChooser(share, "Share song"));
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
    if (isCurrentSongLocallyStored()) {
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
    if (getRepeatMode() == Player.REPEAT_MODE_OFF) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
    } else if (getRepeatMode() == Player.REPEAT_MODE_ALL) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
    } else if (getRepeatMode() == Player.REPEAT_MODE_ONE) {
      exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
    }
  }

  public int getRepeatMode() {
    return exoPlayer.getRepeatMode();
  }

  public void playPrev() {
    if (getCurrentSongIndex() == 0 || getCurrentPosition() / 1000 >= 10) {
      seekTo(0);
    } else {
      seekTo(getCurrentSongIndex() - 1, C.TIME_UNSET);
    }
    resume();
  }

  public void playNext() {
    if (getCurrentSongIndex() == getPlayqueue().size() - 1) {
      seekTo(0);
    } else {
      seekTo(getCurrentSongIndex() + 1, C.TIME_UNSET);
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

  public void playSong(String playlistId, int index) {
    isStreaming = false;
    if (!playlistId.equals(getPlaylistId()) || databaseHelper.getSongsForPlaylist(playlistId).size() == playlistSize) {
      updatePlaylistData(playlistId);
//      this.playlistId = playlistId;
//      playlistSize = databaseHelper.getSongsForPlaylist(playlistId).size();
      setPlayQueue(databaseHelper.getSongsForPlaylist(playlistId));
    }
    play(index);
  }

  public void playSong(Song song) {
    if (databaseHelper.hasSong(song.getId())) {
      List<Song> playlist = databaseHelper.getSongsForPlaylist(PLAYLIST_DOWNLOADS_ID);
      for (int i = 0; i < playlist.size(); i++) {
        if (playlist.get(i).getId().equals(song.getId())) {
          playSong(PLAYLIST_DOWNLOADS_ID, i);
          return;
        }
      }
    }

    isStreaming = true;
    updatePlaylistData(null);
//    playlistId = null;
//    playlistSize = -1;
    setPlayQueueAndPlayWhenReady(song);
    PlayerEvent.postSongChanged();
  }

  ExoPlayer getPlayer() {
    return exoPlayer;
  }

  public void updatePlayerNotification() {
    if (showingPlayerNotification) {
      mainHandler.removeCallbacks(notificationUpdaterRunnable);
      mainHandler.postDelayed(notificationUpdaterRunnable, 100);
    }
  }

  public static void destroy() {
    if (sInstance == null) {
      return;
    }
    if (sInstance.exoPlayer != null) {
      sInstance.exoPlayer.release();
      sInstance.exoPlayer = null;
    }
    sInstance.showingPlayerNotification = false;
    PlayerService.stop(sInstance.context);
    sInstance.lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
    sInstance = null;
  }

  @NonNull
  @Override
  public Lifecycle getLifecycle() {
    return lifecycleRegistry;
  }
}
