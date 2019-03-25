package ml.toufic.songs.db.Lyrics;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import ml.toufic.songs.model.Lyrics;

public class LyricsViewModel extends AndroidViewModel {

  private LyricsRepository mRepository;

  public LyricsViewModel(Application application) {
    super(application);
    mRepository = new LyricsRepository(application);
  }

  public void insert(Lyrics lyrics) {
    mRepository.insert(lyrics);
  }

  public Lyrics getLyrics(String songId) {
    return mRepository.getLyrics(songId);
  }

  public void delete(String songId) {
    mRepository.delete(songId);
  }

}
