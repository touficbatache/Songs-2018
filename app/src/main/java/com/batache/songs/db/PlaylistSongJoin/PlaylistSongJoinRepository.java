package com.batache.songs.db.PlaylistSongJoin;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.batache.songs.db.SongsDatabase;
import com.batache.songs.model.Playlist;
import com.batache.songs.model.PlaylistSongJoin;
import com.batache.songs.model.Song;

import java.util.List;

public class PlaylistSongJoinRepository {

  private PlaylistSongJoinDao mPlaylistDao;
  private LiveData<List<PlaylistSongJoin>> mAllPlaylistSongJoin;

  public PlaylistSongJoinRepository(Context context) {
    SongsDatabase db = SongsDatabase.getDatabase(context);
    mPlaylistDao = db.playlistSongJoinDao();
    mAllPlaylistSongJoin = mPlaylistDao.getAllPlaylistSongJoinLive();
  }

  public void insert(PlaylistSongJoin playlistSongJoin) {
    new insertAsyncTask(mPlaylistDao).execute(playlistSongJoin);
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mPlaylistDao.getPlaylistsForSong(songId);
  }

  public List<Song> getSongsForPlaylist(String playlistId) {
    return mPlaylistDao.getSongsForPlaylist(playlistId);
  }

  public LiveData<List<PlaylistSongJoin>> getAllPlaylistSongJoinLive() {
    return mAllPlaylistSongJoin;
  }

  public boolean isSongInPlaylist(String playlistId, String songId) {
    for (Song song : getSongsForPlaylist(playlistId)) {
      if (song.getId().equals(songId)) {
        return true;
      }
    }
    return false;
  }

  public void remove(String playlistId, String songId) {
    mPlaylistDao.remove(playlistId, songId);
  }

  private static class insertAsyncTask extends AsyncTask<PlaylistSongJoin, Void, Void> {

    private PlaylistSongJoinDao mAsyncTaskDao;

    insertAsyncTask(PlaylistSongJoinDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlaylistSongJoin... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }
}
