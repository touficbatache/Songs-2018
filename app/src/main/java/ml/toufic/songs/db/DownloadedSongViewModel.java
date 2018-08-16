package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

public class DownloadedSongViewModel extends AndroidViewModel {

  private DownloadedSongsRepository mRepository;
  private LiveData<List<PlayableSong>> mAllSongs;

  public DownloadedSongViewModel(Application application) {
    super(application);
    mRepository = new DownloadedSongsRepository(application);
    mAllSongs = mRepository.getAllSongs();
  }

  public void insert(PlayableSong word) { mRepository.insert(word); }

  public LiveData<List<PlayableSong>> getAllSongs() { return mAllSongs; }

  public void delete(String songId) {
    mRepository.delete(songId);
  }

  public void delete(PlayableSong playableSong) {
    mRepository.delete(playableSong);
  }

  public void deleteAll() {
    mRepository.deleteAll();
  }

}
