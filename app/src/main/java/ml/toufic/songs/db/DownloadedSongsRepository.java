package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.ui.activities.MainActivity;

public class DownloadedSongsRepository {

  private DownloadedSongsDao mDownloadedSongsDao;
  private PlayableSong mLastPlayedSong;
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

  public PlayableSong getSong(String songId) {
    return mDownloadedSongsDao.getSong(songId);
  }

  public void setLastPlayedSong(PlayableSong playableSong, MainActivity.OnLastPlayedUpdateCompletionCallback completionCallback) {
    new resetLastPlayedAsyncTask(mDownloadedSongsDao).execute(playableSong.getId());
    new setLastPlayedAsyncTask(mDownloadedSongsDao, completionCallback).execute(playableSong);
  }

  PlayableSong getLastPlayedSong() {
    return mDownloadedSongsDao.getLastPlayedSong();
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

  private static class resetLastPlayedAsyncTask extends AsyncTask<String, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;

    resetLastPlayedAsyncTask(DownloadedSongsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(String... params) {
      mAsyncTaskDao.resetAllLastPlayedSongs(params[0]);
      return null;
    }
  }

  private static class setLastPlayedAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private DownloadedSongsDao mAsyncTaskDao;
    private MainActivity.OnLastPlayedUpdateCompletionCallback completionCallback;

    setLastPlayedAsyncTask(DownloadedSongsDao dao, MainActivity.OnLastPlayedUpdateCompletionCallback completionCallback) {
      mAsyncTaskDao = dao;
      this.completionCallback = completionCallback;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.setLastPlayedSong(params[0]);
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      completionCallback.onCompletion();
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
