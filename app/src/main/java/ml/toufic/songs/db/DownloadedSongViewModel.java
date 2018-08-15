package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

public class DownloadedSongViewModel extends AndroidViewModel {

  private DownloadedSongsRepositoryRepository mRepository;
  private LiveData<List<PlayableSong>> mAllSongs;

  public DownloadedSongViewModel(Application application) {
    super(application);
    mRepository = new DownloadedSongsRepositoryRepository(application);
    mAllSongs = mRepository.getAllSongs();
  }

  public LiveData<List<PlayableSong>> getAllSongs() { return mAllSongs; }

  public void insert(PlayableSong word) { mRepository.insert(word); }

}
