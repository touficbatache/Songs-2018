package com.batache.songs.app.explore;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.SongUtils;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends ListFragment {

  private final static String MAX_RESULTS = "5";

  private List<Song> newThisWeekSongs = new ArrayList<>();
  private List<Song> popularSongs = new ArrayList<>();
  private boolean hasNewThisWeek = false;
  private boolean hasPopular = false;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_explore;
  }

  @Override
  public void onCreateView() {

  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (!newThisWeekSongs.isEmpty()) {
      hasNewThisWeek = true;
    }
    if (!popularSongs.isEmpty()) {
      hasPopular = true;
    }
    getExploreSongs();
    updateRecyclerViewSongs();
  }

  private void getExploreSongs() {
    if (newThisWeekSongs.isEmpty()) {
      SongsAPI.getInstance().getSongsFromPlaylist(SongUtils.NEW_THIS_WEEK_PLAYLIST_ID, MAX_RESULTS, new SongsAPI.OnSongsFetchedListener() {
        @Override
        public void onSuccess(List<Song> fetchedSongs) {
          newThisWeekSongs = fetchedSongs;
          hasNewThisWeek = true;
          updateRecyclerViewSongs();
        }

        @Override
        public void onFailure(Throwable t) {
          Toast.makeText(getContext(), R.string.failed_fetching_playlist, Toast.LENGTH_SHORT).show();
        }
      });
    }

    if (popularSongs.isEmpty()) {
      SongsAPI.getInstance().getSongsFromPlaylist(SongUtils.POPULAR_PLAYLIST_ID, MAX_RESULTS, new SongsAPI.OnSongsFetchedListener() {
        @Override
        public void onSuccess(List<Song> fetchedSongs) {
          popularSongs = fetchedSongs;
          hasPopular = true;
          updateRecyclerViewSongs();
        }

        @Override
        public void onFailure(Throwable t) {
          Toast.makeText(getContext(), R.string.failed_fetching_playlist, Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  private void updateRecyclerViewSongs() {
    if (getActivity() != null) {
      mController.reset();

      if (hasNewThisWeek) {
        mController.addSeeMoreSection(
            getContext(),
            getString(R.string.new_this_week),
            newThisWeekSongs,
            SongUtils.NEW_THIS_WEEK_PLAYLIST_ID,
            false
        );
      }

      if (hasPopular) {
        mController.addSeeMoreSection(
            getContext(),
            getString(R.string.popular),
            popularSongs,
            SongUtils.POPULAR_PLAYLIST_ID,
            false
        );
      }
    }
  }

}
