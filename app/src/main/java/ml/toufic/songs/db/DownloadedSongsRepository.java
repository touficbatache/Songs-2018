package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

public class DownloadedSongsRepository {

  private DownloadedSongsDao mDownloadedSongsDao;
  private List<PlayableSong> mLastPlayedSong;
  private LiveData<List<PlayableSong>> mAllDownloadedSongs;

  DownloadedSongsRepository(Application application) {
    DownloadedSongsDatabase db = DownloadedSongsDatabase.getDatabase(application);
    mDownloadedSongsDao = db.downloadedSongsDao();
    mLastPlayedSong = mDownloadedSongsDao.getLastPlayedSong();
    mAllDownloadedSongs = mDownloadedSongsDao.getAllSongs();
  }

  public void insert(PlayableSong playableSong) {
    new insertAsyncTask(mDownloadedSongsDao).execute(playableSong);
  }

  public void setLastPlayedSong(String songId) {
    new resetAllLastPlayedSongsAsyncTask(mDownloadedSongsDao);
    new setLastPlayedAsyncTask(mDownloadedSongsDao).execute(songId);
  }

  PlayableSong getLastPlayedSong() {
    return mDownloadedSongsDao.getLastPlayedSong().get(0);
  }

  public void update(PlayableSong playableSong) {
    new updateAsyncTask(mDownloadedSongsDao).execute(playableSong);
  }

  LiveData<List<PlayableSong>> getAllSongs() {
    return mAllDownloadedSongs;
  }

  public void delete(String songId) {
    new deleteAsyncTask(mDownloadedSongsDao).execute(songId);
  }

  public void delete(PlayableSong playableSong) {
    new deleteSongAsyncTask(mDownloadedSongsDao).execute(playableSong);
  }

  public void deleteAll() {
    new deleteAllSongsAsyncTask(mDownloadedSongsDao).execute();
  }

  private static class insertAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    insertAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class resetAllLastPlayedSongsAsyncTask extends AsyncTask<Void, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    resetAllLastPlayedSongsAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      mAsyncTaskDao.resetAllLastPlayedSongs();
      return null;
    }
  }

  private static class setLastPlayedAsyncTask extends AsyncTask<String, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    setLastPlayedAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      mAsyncTaskDao.setLastPlayedSong(params[0]);
      return null;
    }
  }

  private static class updateAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    updateAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.update(params[0]);
      return null;
    }
  }

  private static class deleteAsyncTask extends AsyncTask<String, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    deleteAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteSongAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    deleteSongAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

  private static class deleteAllSongsAsyncTask extends AsyncTask<Void, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    deleteAllSongsAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }


    @Override
    protected Void doInBackground(Void... voids) {
      mAsyncTaskDao.deleteAll();
      return null;
    }
  }

}
