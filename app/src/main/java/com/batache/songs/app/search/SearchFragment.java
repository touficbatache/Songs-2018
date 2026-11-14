package com.batache.songs.app.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.ui.controller.SongsController;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.PrefUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends ListFragment<SongsController> {

  private SwitchMaterial musicOnlySwitch;
  private EpoxyRecyclerView recyclerView;
  private SongsController controller;
  private boolean youtubeFailed, dndnhaFailed, xiamiFailed = false;
  private String searchQuery;
  private List<Song> searchResults = new ArrayList<>();

  @Override
  public int getLayout() {
    return R.layout.fragment_search;
  }

  @Override
  public void onCreateView() {
    if (getArguments() != null) {
      searchQuery = SearchFragmentArgs.fromBundle(getArguments()).getQuery();
    }
  }

  @Override
  public SongsController getController() {
    return new SongsController();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (CodeUtils.isEmpty(searchQuery)) {
      return;
    }

    musicOnlySwitch = rootView.findViewById(R.id.music_only_switch);
    musicOnlySwitch.setChecked(PrefUtils.getInstance().getBoolean(PrefUtils.SEARCH_SHOW_MUSIC_ONLY_PREF, PrefUtils.SEARCH_SHOW_MUSIC_ONLY_DEFAULT_VALUE));
    musicOnlySwitch.setOnCheckedChangeListener((compoundButton, b) -> {
      PrefUtils.getInstance().putBoolean(PrefUtils.SEARCH_SHOW_MUSIC_ONLY_PREF, b);
      search();
    });

    controller = new SongsController();
    recyclerView = rootView.findViewById(R.id.recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setController(controller);

    search();
  }

  private void search() {
    musicOnlySwitch.setEnabled(false);

    controller.clear();

    searchResults = new ArrayList<>();

    searchYoutube(searchQuery, () -> searchDndnha(searchQuery, () -> searchXiami(searchQuery, () -> {
      musicOnlySwitch.setEnabled(true);

      if (youtubeFailed && dndnhaFailed && xiamiFailed) {
        Snackbar.make(rootView.findViewById(R.id.recyclerview), "No songs found", Snackbar.LENGTH_SHORT).show();
        return;
      }

      controller.addExploreSearchResultsSection(searchResults);
    })));
  }

  private void searchYoutube(String searchQuery, SearchListener searchListener) {
    SongsAPI.INSTANCE.searchYoutube(searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        youtubeFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(Throwable t) {
        youtubeFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private void searchDndnha(String searchQuery, SearchListener searchListener) {
    SongsAPI.INSTANCE.searchDndnha(searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        dndnhaFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(@Nullable Throwable t) {
        dndnhaFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private void searchXiami(String searchQuery, SearchListener searchListener) {
    SongsAPI.INSTANCE.searchXiami(searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        xiamiFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(Throwable t) {
        xiamiFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private interface SearchListener {
    void onComplete();
  }

  @Override
  public View getTopSpaceableView() {
    return rootView;
  }
}
