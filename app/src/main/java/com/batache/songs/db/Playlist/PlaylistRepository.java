package com.batache.songs.db.Playlist;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.batache.songs.db.SongsDatabase;
import com.batache.songs.model.pojo.Playlist;

import java.util.List;

public class PlaylistRepository {

  private PlaylistDao mPlaylistDao;
  private LiveData<List<Playlist>> mAllPlaylists;

  public PlaylistRepository(Context context) {
    SongsDatabase db = SongsDatabase.getDatabase(context);
    mPlaylistDao = db.playlistDao();
    mAllPlaylists = mPlaylistDao.getAllPlaylistsLive();
  }

  public void insert(Playlist playlist) {
    new insertAsyncTask(mPlaylistDao).execute(playlist);
  }

  public void update(Playlist playlist) {
    new updateAsyncTask(mPlaylistDao).execute(playlist);
  }

  public Playlist getPlaylist(String playlistId) {
    return mPlaylistDao.getPlaylist(playlistId);
  }

  public boolean hasPlaylist(String playlistId) {
    return mPlaylistDao.getPlaylist(playlistId) != null;
  }

  public List<Playlist> getAllPlaylists() {
    return mPlaylistDao.getAllPlaylists();
  }

  public List<Playlist> getAllPlaylistsDesc() {
    return mPlaylistDao.getAllPlaylistsDesc();
  }

  public LiveData<List<Playlist>> getAllPlaylistsLive() {
    return mAllPlaylists;
  }

  public void delete(Playlist playlist) {
    new deleteAsyncTask(mPlaylistDao).execute(playlist);
  }

  public void delete(String playlistId) {
    new deleteByIdAsyncTask(mPlaylistDao).execute(playlistId);
  }

  public void deleteAll() {
    new deleteAllPlaylistsAsyncTask(mPlaylistDao).execute();
  }

  private static class insertAsyncTask extends AsyncTask<Playlist, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    insertAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Playlist... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class updateAsyncTask extends AsyncTask<Playlist, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    updateAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Playlist... params) {
      mAsyncTaskDao.update(params[0]);
      return null;
    }
  }

  private static class deleteAsyncTask extends AsyncTask<Playlist, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    deleteAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Playlist... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteByIdAsyncTask extends AsyncTask<String, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    deleteByIdAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteAllPlaylistsAsyncTask extends AsyncTask<Void, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    deleteAllPlaylistsAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }


    @Override
    protected Void doInBackground(Void... voids) {
      mAsyncTaskDao.deleteAll();
      return null;
    }
  }

}
