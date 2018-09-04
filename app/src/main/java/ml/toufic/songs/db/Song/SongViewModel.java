package ml.toufic.songs.db.Song;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import ml.toufic.songs.db.Song.SongRepository;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.ui.activities.MainActivity;

public class SongViewModel extends AndroidViewModel {

  private SongRepository mRepository;
  private LiveData<List<PlayableSong>> mAllSongs;

  public SongViewModel(Application application) {
    super(application);
    mRepository = new SongRepository(application);
    mAllSongs = mRepository.getAllSongsLive();
  }

  public void insert(PlayableSong playableSong) {
    mRepository.insert(playableSong);
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

  public LiveData<List<PlayableSong>> getAllSongsLive() { return mAllSongs; }

  public void updateSongWithResetAll(PlayableSong playableSong) {
    updateSongWithResetAll(playableSong, new MainActivity.OnLastPlayedUpdateCompletionCallback() {
      @Override
      public void onCompletion() {}
    });
  }

  public void updateSongWithResetAll(PlayableSong playableSong, MainActivity.OnLastPlayedUpdateCompletionCallback completionCallback) {
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
