package ml.toufic.songs.db.Lyrics;

import android.app.Application;
import android.os.AsyncTask;

import ml.toufic.songs.db.SongsDatabase;
import ml.toufic.songs.model.Lyrics;

public class LyricsRepository {

  private LyricsDao mLyricsDao;

  LyricsRepository(Application application) {
    SongsDatabase db = SongsDatabase.getDatabase(application);
    mLyricsDao = db.lyricsDao();
  }

  public void insert(Lyrics lyrics) {
    new insertAsyncTask(mLyricsDao).execute(lyrics);
  }

  public Lyrics getLyrics(String songId) {
    return mLyricsDao.getLyrics(songId);
  }

  public void delete(String songId) {
    new deleteAsyncTask(mLyricsDao).execute(songId);
  }

  private static class insertAsyncTask extends AsyncTask<Lyrics, Void, Void> {

    private LyricsDao mAsyncTaskDao;

    insertAsyncTask(LyricsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Lyrics... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class deleteAsyncTask extends AsyncTask<String, Void, Void> {

    private LyricsDao mAsyncTaskDao;

    deleteAsyncTask(LyricsDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      mAsyncTaskDao.delete(params[0]);
      return null;
    }
  }

}
