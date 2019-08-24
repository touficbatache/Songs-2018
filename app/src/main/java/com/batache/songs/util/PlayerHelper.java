package com.batache.songs.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.LastPlayed;
import com.batache.songs.model.Song;
import com.batache.songs.network.SongDownloader;
import com.batache.songs.util.events.PlayerEvent;
import com.google.android.exoplayer2.C;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.YtFile;

public class PlayerHelper {

  private static PlayerHelper sInstance;

  public static PlayerHelper getInstance() {
    if (sInstance == null) {
      sInstance = new PlayerHelper();
    }
    return sInstance;
  }

  // Stream song
  private boolean isStreaming = false;
  private Song streamSong;

  // Downloaded song
  private String currentPlaylistId;
  private int songIndexInPlaylist = -1;
  private long currentPosition;

  private List<Song> playingQueue = new ArrayList<>();

  public void playSong(Song song) {
    isStreaming = true;
    streamSong = song;
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.SONG_CHANGED));
  }

  public void playSong(String playlistId, int index) {
    isStreaming = false;
    currentPlaylistId = playlistId;
    songIndexInPlaylist = index;
    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.Action.SONG_CHANGED));
  }

  public boolean isStreaming() {
    return isStreaming;
  }

  public Song getStreamSong() {
    return streamSong;
  }

  public boolean initCurrentSong(Context context) {
    getPlaylistId(context);
    getSongIndex(context);
    return currentPlaylistId != null && songIndexInPlaylist != -1;
  }

  public String getPlaylistId(Context context) {
    if (currentPlaylistId == null) {
      LastPlayed mLastPlayed = DatabaseHelper.getInstance(context).getLastPlayed();
      if (mLastPlayed != null) {
        currentPlaylistId = mLastPlayed.getPlaylistId();
      }
    }
    return currentPlaylistId;
  }

  public void setSongIndex(int index) {
    songIndexInPlaylist = index;
  }

  public int getSongIndex(Context context) {
    if (songIndexInPlaylist == -1) {
      LastPlayed mLastPlayed = DatabaseHelper.getInstance(context).getLastPlayed();
      if (mLastPlayed != null) {
        songIndexInPlaylist = mLastPlayed.getSongIndex();
        if(songIndexInPlaylist >= DatabaseHelper.getInstance(context).getSongsForPlaylist(getPlaylistId(context)).size()) {
          songIndexInPlaylist = -1;
        }
      }
    }
    return songIndexInPlaylist;
  }

  public long getPosition(Context context) {
    long currentPositition = DatabaseHelper.getInstance(context).getLastPlayed().getPosition();
    if (currentPositition == -1) {
      currentPositition = C.TIME_UNSET;
    }
    return currentPositition;
  }

  public Song getCurrentSong(Context context) {
    return DatabaseHelper.getInstance(context).getSongsForPlaylist(getPlaylistId(context)).get(getSongIndex(context));
  }

  public Song getSongFromPlaylist(Context context, String playlistId, int songIndexInPlaylist) {
    return DatabaseHelper.getInstance(context).getSongsForPlaylist(playlistId).get(songIndexInPlaylist);
  }

  public void setCurrentPosition(long progress) {
    currentPosition = progress;
  }

  public long getCurrentPosition() {
    return currentPosition;
  }

  public boolean initPlayQueue(Context context, String playlistId) {
    playingQueue = new ArrayList<>();
    List<Song> playlistSongs = DatabaseHelper.getInstance(context).getSongsForPlaylist(playlistId);
    if (!playlistSongs.isEmpty()) {
      for (Song song : playlistSongs) {
        if(song.getPath() != null) {
          updateSongDuration(context, song, Uri.parse(song.getPath().getPath()));
        } else {
          SongDownloader.getInstance().fetchSongURL(context, song, songFile -> updateSongDuration(context, song, Uri.parse(songFile.getUrl())));
        }
      }
      return true;
    }
    return false;
  }

  private void updateSongDuration(Context context, Song song, Uri uri) {
    MediaPlayer mp = MediaPlayer.create(context, uri);
    if (mp != null) {
      song.setDuration(mp.getDuration());
      mp.release();
      playingQueue.add(song);
    } else {
      DatabaseHelper.getInstance(context).deleteSong(song.getId());
    }
  }

  public List<Song> getPlayingQueue() {
    return playingQueue;
  }

  public void setLastPlayed(Context context, String playlistId, int indexInPlaylist, long songPosition) {
    DatabaseHelper.getInstance(context).setLastPlayed(new LastPlayed(playlistId, indexInPlaylist, songPosition));
  }

  public LastPlayed getLastPlayed(Context context) {
    return DatabaseHelper.getInstance(context).getLastPlayed();
  }

  public void deleteSong(Context context, Song song) {
    if (song.getPath().delete()) {
      DatabaseHelper.getInstance(context).deleteSong(song);
    }
  }

  public static void destroy() {
    sInstance = null;
  }

}
