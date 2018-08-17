package ml.toufic.songs.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

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

  public void insert(PlayableSong playableSong) {
    mRepository.insert(playableSong);
    Log.d("DownloadedSongViewModel", "tgblog: insert: " + Converters.fromSong(playableSong));
  }

  public void setLastPlayedSong(String songId) {
    mRepository.setLastPlayedSong(songId);
    Log.d("DownloadedSongViewModel", "tgblog: setLastPlayedSong: " + songId);
    for(PlayableSong playableSong : getAllSongs().getValue()) {
      Log.d("DownloadedSongViewModel", "tgblog: allsongs: " + Converters.fromSong(playableSong));
    }
  }

  public PlayableSong getLastPlayedSong() {
    return mRepository.getLastPlayedSong();
  }

  public void update(PlayableSong playableSong) {
    mRepository.update(playableSong);
  }

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
