package ml.toufic.songs.db.Playlist;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import ml.toufic.songs.db.SongsDatabase;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.ui.activities.MainActivity;

public class PlaylistRepository {

  private PlaylistDao mPlaylistDao;
  private LiveData<List<Playlist>> mAllPlaylists;

  PlaylistRepository(Application application) {
    SongsDatabase db = SongsDatabase.getDatabase(application);
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

  List<Playlist> getAllPlaylists() {
    return mPlaylistDao.getAllPlaylists();
  }

  LiveData<List<Playlist>> getAllPlaylistsLive() {
    return mAllPlaylists;
  }

  public void updatePlaylistWithResetAll(Playlist playlist, MainActivity.OnLastClickedUpdateCompletion completionCallback) {
    new resetAllPlaylistsExceptAsyncTask(mPlaylistDao).execute(playlist.getId());
    new updateAsyncTask(mPlaylistDao, completionCallback).execute(playlist);
  }

  Playlist getLastOpenedPlaylist() {
    return mPlaylistDao.getLastOpenedPlaylist();
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
    private MainActivity.OnLastClickedUpdateCompletion completionCallback;

    updateAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    updateAsyncTask(PlaylistDao dao, MainActivity.OnLastClickedUpdateCompletion completionCallback) {
      mAsyncTaskDao = dao;
      this.completionCallback = completionCallback;
    }

    @Override
    protected Void doInBackground(final Playlist... params) {
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

  private static class resetAllPlaylistsExceptAsyncTask extends AsyncTask<String, Void, Void> {

    private PlaylistDao mAsyncTaskDao;

    resetAllPlaylistsExceptAsyncTask(PlaylistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(String... params) {
      mAsyncTaskDao.resetAllPlaylistsExcept(params[0]);
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
