package ml.toufic.songs.db.Song;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import ml.toufic.songs.db.SongsDatabase;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.ui.activities.MainActivity;

public class SongRepository {

  private SongDao mSongDao;

  SongRepository(Application application) {
    SongsDatabase db = SongsDatabase.getDatabase(application);
    mSongDao = db.songDao();
  }

  public void insert(PlayableSong playableSong) {
    new insertAsyncTask(mSongDao).execute(playableSong);
  }

  public void insert(Song song) {
    new insertSongAsyncTask(mSongDao).execute(song);
  }

  public void update(PlayableSong playableSong) {
    new updateAsyncTask(mSongDao).execute(playableSong);
  }

  public PlayableSong getSong(String songId) {
    return mSongDao.getSong(songId);
  }

  public boolean hasSong(String songId) {
    return mSongDao.getSong(songId) != null;
  }

  List<PlayableSong> getAllSongs() {
    return mSongDao.getAllSongs();
  }

  public void updateSongWithResetAll(PlayableSong playableSong, MainActivity.OnLastClickedUpdateCompletion completionCallback) {
    new resetAllSongsExceptAsyncTask(mSongDao).execute(playableSong.getId());
    new updateAsyncTask(mSongDao, completionCallback).execute(playableSong);
  }

  PlayableSong getLastPlayedSong() {
    return mSongDao.getLastPlayedSong();
  }

  public void delete(PlayableSong playableSong) {
    new deleteAsyncTask(mSongDao).execute(playableSong);
  }

  public void delete(String songId) {
    new deleteByIdAsyncTask(mSongDao).execute(songId);
  }

  public void deleteAll() {
    new deleteAllSongsAsyncTask(mSongDao).execute();
  }

  private static class insertAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private SongDao mAsyncTaskDao;

    insertAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class insertSongAsyncTask extends AsyncTask<Song, Void, Void> {

    private SongDao mAsyncTaskDao;

    insertSongAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final Song... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }

  private static class updateAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private SongDao mAsyncTaskDao;
    private MainActivity.OnLastClickedUpdateCompletion completionCallback;

    updateAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    updateAsyncTask(SongDao dao, MainActivity.OnLastClickedUpdateCompletion completionCallback) {
      mAsyncTaskDao = dao;
      this.completionCallback = completionCallback;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
      mAsyncTaskDao.update(params[0]);
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if(completionCallback != null) {
        completionCallback.onCompletion();
      }
    }
  }

  private static class resetAllSongsExceptAsyncTask extends AsyncTask<String, Void, Void> {

    private SongDao mAsyncTaskDao;

    resetAllSongsExceptAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(String... params) {
      mAsyncTaskDao.resetAllSongsExcept(params[0]);
      return null;
    }
  }

  private static class deleteAsyncTask extends AsyncTask<PlayableSong, Void, Void> {

    private SongDao mAsyncTaskDao;

    deleteAsyncTask(SongDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlayableSong... params) {
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
