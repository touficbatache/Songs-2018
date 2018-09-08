package ml.toufic.songs.db.Song;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.ui.activities.MainActivity;

public class SongViewModel extends AndroidViewModel {

  private SongRepository mRepository;

  public SongViewModel(Application application) {
    super(application);
    mRepository = new SongRepository(application);
  }

  public void insert(PlayableSong playableSong) {
    mRepository.insert(playableSong);
  }

  public void insert(Song song) {
    mRepository.insert(song);
  }

  public void update(PlayableSong playableSong) {
    mRepository.update(playableSong);
  }

  public PlayableSong getSong(String songId) {
    return mRepository.getSong(songId);
  }

  public boolean hasSong(String songId) {
    return mRepository.hasSong(songId);
  }

  public List<PlayableSong> getAllSongs() {
    return mRepository.getAllSongs();
  }

  public void updateSongWithResetAll(PlayableSong playableSong) {
    updateSongWithResetAll(playableSong, new MainActivity.OnLastClickedUpdateCompletion() {
      @Override
      public void onCompletion() {}
    });
  }

  public void updateSongWithResetAll(PlayableSong playableSong, MainActivity.OnLastClickedUpdateCompletion completionCallback) {
    playableSong.setLastPlayed(true);
    mRepository.updateSongWithResetAll(playableSong, completionCallback);
  }

  public PlayableSong getLastPlayedSong() {
    return mRepository.getLastPlayedSong();
  }

  public void delete(PlayableSong playableSong) {
    mRepository.delete(playableSong);
  }

  public void delete(String songId) {
    mRepository.delete(songId);
  }

  public void deleteAll() {
    mRepository.deleteAll();
  }

}
