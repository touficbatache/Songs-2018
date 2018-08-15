package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

public class DownloadedSongsRepositoryRepository {

  private DownloadedSongsDao mDownloadedSongsDao;
  private LiveData<List<PlayableSong>> mAllDownloadedSongs;

  DownloadedSongsRepositoryRepository(Application application) {
    DownloadedSongsDatabase db = DownloadedSongsDatabase.getDatabase(application);
    mDownloadedSongsDao = db.downloadedSongsDao();
    mAllDownloadedSongs = mDownloadedSongsDao.getAllSongs();
  }

  LiveData<List<PlayableSong>> getAllSongs() {
    return mAllDownloadedSongs;
  }

  public void insert(PlayableSong playableSong) {
    new insertAsyncTask(mDownloadedSongsDao).execute(playableSong);
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

}
