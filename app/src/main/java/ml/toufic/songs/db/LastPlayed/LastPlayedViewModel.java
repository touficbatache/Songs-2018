package ml.toufic.songs.db.LastPlayed;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import ml.toufic.songs.model.LastPlayed;

public class LastPlayedViewModel extends AndroidViewModel {

  private LastPlayedRepository mRepository;

  public LastPlayedViewModel(Application application) {
    super(application);
    mRepository = new LastPlayedRepository(application);
  }

  public void setLastPlayed(LastPlayed lastPlayed) {
    mRepository.setLastPlayed(lastPlayed);
  }

  public LastPlayed getLastPlayed() {
    return mRepository.getLastPlayed();
  }

}
