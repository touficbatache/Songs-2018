package com.batache.songs.db.Song;

import android.content.Context;
import android.os.AsyncTask;

import com.batache.songs.db.SongsDatabase;
import com.batache.songs.model.Song;
import com.batache.songs.ui.activities.MainActivity;

import java.util.List;

public class SongRepository {

  private SongDao mSongDao;

  public SongRepository(Context context) {
    SongsDatabase db = SongsDatabase.getDatabase(context);
    mSongDao = db.songDao();
  }

  public void insert(Song song) {
    new insertAsyncTask(mSongDao).execute(song);
  }

  public void update(Song song) {
    new updateAsyncTask(mSongDao).execute(song);
  }

  public Song getSong(String songId) {
    return mSongDao.getSong(songId);
  }

  public boolean hasSong(String songId) {
    return mSongDao.getSong(songId) != null;
  }

  public List<Song> getAllSongs() {
    return mSongDao.getAllSongs();
  }

  public void delete(Song song) {
    new deleteAsyncTask(mSongDao).execute(song);
  }

  public void delete(String songId) {
    new deleteByIdAsyncTask(mSongDao).execute(songId);
  }

  public void deleteAll() {
    new deleteAllSongsAsyncTask(mSongDao).execute();
  }

  private static class insertAsyncTask extends AsyncTask<Song, Void, Void> {

    private SongDao mAsyncTaskDao;

    insertAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Song... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class updateAsyncTask extends AsyncTask<Song, Void, Void> {

    private SongDao mAsyncTaskDao;

    updateAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Song... params) {
      mAsyncTaskDao.update(params[0]);
      return null;
    }

  }

  private static class deleteAsyncTask extends AsyncTask<Song, Void, Void> {

    private SongDao mAsyncTaskDao;

    deleteAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Song... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteByIdAsyncTask extends AsyncTask<String, Void, Void> {

    private SongDao mAsyncTaskDao;

    deleteByIdAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteAllSongsAsyncTask extends AsyncTask<Void, Void, Void> {

    private SongDao mAsyncTaskDao;

    deleteAllSongsAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }


    @Override
    protected Void doInBackground(Void... voids) {
      mAsyncTaskDao.deleteAll();
      return null;
    }
  }

}
