package com.batache.songs.db.LastPlayed;

import android.content.Context;
import android.os.AsyncTask;

import com.batache.songs.db.SongsDatabase;
import com.batache.songs.model.pojo.LastPlayed;

public class LastPlayedRepository {

  private LastPlayedDao mLastPlayedDao;

  public LastPlayedRepository(Context context) {
    SongsDatabase db = SongsDatabase.getDatabase(context);
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
