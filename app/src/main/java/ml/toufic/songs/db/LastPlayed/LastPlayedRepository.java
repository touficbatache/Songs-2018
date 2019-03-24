package ml.toufic.songs.db.LastPlayed;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;
import ml.toufic.songs.db.Playlist.PlaylistDao;
import ml.toufic.songs.db.SongsDatabase;
import ml.toufic.songs.model.LastPlayed;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.ui.activities.MainActivity;

public class LastPlayedRepository {

  private LastPlayedDao mLastPlayedDao;

  LastPlayedRepository(Application application) {
    SongsDatabase db = SongsDatabase.getDatabase(application);
    mLastPlayedDao = db.lastPlayedDao();
  }

  public void setLastPlayed(LastPlayed lastPlayed) {
    new insertAsyncTask(mLastPlayedDao).execute(lastPlayed);
  }

  public LastPlayed getLastPlayed() {
    return mLastPlayedDao.getLastPlayed();
  }

  private static class insertAsyncTask extends AsyncTask<LastPlayed, Void, Void> {

    private LastPlayedDao mAsyncTaskDao;

    insertAsyncTask(LastPlayedDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final LastPlayed... params) {
      mAsyncTaskDao.setLastPlayed(params[0]);
      return null;
    }
  }

}
