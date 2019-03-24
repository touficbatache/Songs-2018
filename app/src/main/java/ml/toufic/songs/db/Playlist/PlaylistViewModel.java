package ml.toufic.songs.db.Playlist;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.ui.activities.MainActivity;

public class PlaylistViewModel extends AndroidViewModel {

  private PlaylistRepository mRepository;
  private LiveData<List<Playlist>> mAllPlaylists;

  public PlaylistViewModel(Application application) {
    super(application);
    mRepository = new PlaylistRepository(application);
    mAllPlaylists = mRepository.getAllPlaylistsLive();
  }

  public void insert(Playlist playlist) {
    mRepository.insert(playlist);
  }

  public void update(Playlist playlist) {
    mRepository.update(playlist);
  }

  public Playlist getPlaylist(String playlistId) {
    return mRepository.getPlaylist(playlistId);
  }

  public boolean hasPlaylist(String playlistId) {
    return mRepository.hasPlaylist(playlistId);
  }

  public List<Playlist> getAllPlaylists() {
    return mRepository.getAllPlaylists();
  }

  public LiveData<List<Playlist>> getAllPlaylistsLive() { return mAllPlaylists; }

  public void delete(Playlist playlist) {
    mRepository.delete(playlist);
  }

  public void delete(String playlistId) {
    mRepository.delete(playlistId);
  }

  public void deleteAll() {
    mRepository.deleteAll();
  }

}
