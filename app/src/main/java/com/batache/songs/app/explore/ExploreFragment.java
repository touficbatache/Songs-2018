package com.batache.songs.app.explore;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.util.SongUtils;
import com.cyl.musicapi.BaseApiImpl;
import com.cyl.musicapi.playlist.MusicInfo;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import static com.batache.songs.util.SongUtils.SEARCH_KEY_QQ;

public class ExploreFragment extends ListFragment {

  private final static String MAX_RESULTS = "5";

  private static final String TAG = "ExploreFragment : ";

  private List<Song> newThisWeekSongs = new ArrayList<>();
  private List<Song> popularSongs = new ArrayList<>();
  private boolean hasNewThisWeek = false;
  private boolean hasPopular = false;

  private String mSearchQuery;
  private final Handler handler = new Handler();
  private Runnable searchRunnable = this::search;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_explore_playlist;
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

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) searchItem.getActionView();
    searchView.setQueryHint("Search by name, artist or lyrics...");
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText.trim())) {
          handler.removeCallbacks(searchRunnable);
          mSearchQuery = newText.trim();
          handler.postDelayed(searchRunnable, 600);
        } else {
          if (!newThisWeekSongs.isEmpty()) {
            hasNewThisWeek = true;
          }
          if (!popularSongs.isEmpty()) {
            hasPopular = true;
          }

          updateRecyclerViewSongs();
        }
        return true;
      }

      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }
    });
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

  private void search() {
    mController.clear();
    BaseApiImpl.INSTANCE.searchSongSingle(mSearchQuery, SEARCH_KEY_QQ, 5, 0, searchSingleData -> {
      List<Song> songList = new ArrayList<>();
      if (searchSingleData.getStatus()) {
        for (MusicInfo music : searchSingleData.getData().getSongs()) {
          music.setVendor(SEARCH_KEY_QQ.toLowerCase());
          songList.add(SongUtils.getSong(music));
        }
      }
      mController.addTitledSection(getContext(), "QQ", songList);
      return Unit.INSTANCE;
    });
    SongsAPI.getInstance().searchXiami(mSearchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        mController.addTitledSection(getContext(), getString(R.string.recommended), fetchedSongs);
        SongsAPI.getInstance().searchYoutube(mSearchQuery, new SongsAPI.OnSongsFetchedListener() {
          @Override
          public void onSuccess(List<Song> fetchedSongs) {
            mController.addTitledSection(getContext(), getString(R.string.youtube), fetchedSongs);
          }

          @Override
          public void onFailure(Throwable t) {
            Toast.makeText(getContext(), R.string.search_failed, Toast.LENGTH_SHORT).show();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        Toast.makeText(getContext(), R.string.search_failed, Toast.LENGTH_SHORT).show();
      }
    });
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
