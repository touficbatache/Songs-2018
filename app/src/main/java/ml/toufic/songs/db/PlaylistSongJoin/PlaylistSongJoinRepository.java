package ml.toufic.songs.db.PlaylistSongJoin;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import ml.toufic.songs.db.SongsDatabase;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.PlaylistSongJoin;
import ml.toufic.songs.model.Song;

public class PlaylistSongJoinRepository {

  private PlaylistSongJoinDao mPlaylistDao;
  private LiveData<List<PlaylistSongJoin>> mAllPlaylistSongJoin;

  PlaylistSongJoinRepository(Application application) {
    SongsDatabase db = SongsDatabase.getDatabase(application);
    mPlaylistDao = db.playlistSongJoinDao();
    mAllPlaylistSongJoin = mPlaylistDao.getAllPlaylistSongJoinLive();
  }

  public void insert(PlaylistSongJoin playlistSongJoin) {
    new insertAsyncTask(mPlaylistDao).execute(playlistSongJoin);
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mPlaylistDao.getPlaylistsForSong(songId);
  }

  public List<PlayableSong> getSongsForPlaylist(String playlistId) {
    return mPlaylistDao.getSongsForPlaylist(playlistId);
  }

  public List<Song> getRemoteSongsForPlaylist(String playlistId) {
    return mPlaylistDao.getRemoteSongsForPlaylist(playlistId);
  }

  public LiveData<List<PlaylistSongJoin>> getAllPlaylistSongJoinLive() {
    return mAllPlaylistSongJoin;
  }

  boolean isSongInPlaylist(String playlistId, String songId) {
    for(PlayableSong playableSong : getSongsForPlaylist(playlistId)) {
      if(playableSong.getId().equals(songId)) {
        return true;
      }
    }
    return false;
  }

  boolean isRemoteSongInPlaylist(String playlistId, String songId) {
    for(Song song : getRemoteSongsForPlaylist(playlistId)) {
      if(song.getId().equals(songId)) {
        return true;
      }
    }
    return false;
  }

  public void remove(String playlistId, String songId) {
    mPlaylistDao.remove(playlistId, songId);
  }

  private static class insertAsyncTask extends AsyncTask<PlaylistSongJoin, Void, Void> {

    private PlaylistSongJoinDao mAsyncTaskDao;

    insertAsyncTask(PlaylistSongJoinDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final PlaylistSongJoin... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }
}
