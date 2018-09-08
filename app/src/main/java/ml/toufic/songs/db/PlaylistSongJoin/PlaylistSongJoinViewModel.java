package ml.toufic.songs.db.PlaylistSongJoin;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.PlaylistSongJoin;
import ml.toufic.songs.model.Song;

public class PlaylistSongJoinViewModel extends AndroidViewModel {

  private PlaylistSongJoinRepository mRepository;

  public PlaylistSongJoinViewModel(Application application) {
    super(application);
    mRepository = new PlaylistSongJoinRepository(application);
  }

  public void insert(PlaylistSongJoin playlistSongJoin) {
    mRepository.insert(playlistSongJoin);
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mRepository.getPlaylistsForSong(songId);
  }

  public List<PlayableSong> getSongsForPlaylist(String playlistId) {
    return mRepository.getSongsForPlaylist(playlistId);
  }

  public List<Song> getRemoteSongsForPlaylist(String playlistId) {
    return mRepository.getRemoteSongsForPlaylist(playlistId);
  }

  public LiveData<List<PlaylistSongJoin>> getAllPlaylistSongJoinLive() {
    return mRepository.getAllPlaylistSongJoinLive();
  }

  public boolean isSongInPlaylist(String playlistId, String songId) {
    return mRepository.isSongInPlaylist(playlistId, songId);
  }

  public boolean isRemoteSongInPlaylist(String playlistId, String songId) {
    return mRepository.isRemoteSongInPlaylist(playlistId, songId);
  }

  public void remove(String playlistId, String songId) {
    mRepository.remove(playlistId, songId);
  }
}
