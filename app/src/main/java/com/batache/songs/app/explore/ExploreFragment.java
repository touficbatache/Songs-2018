package com.batache.songs.app.explore;

import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.epoxy.EpoxyModelKotlinExtensionsKt;
import com.batache.songs.R;
import com.batache.songs.app.MainActivity;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.NetworkUtils;
import com.batache.songs.util.SongUtils;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends ListFragment<ExploreController> {

  private ExploreViewModel viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

//    setEnterTransition(new MaterialFadeThrough());
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_explore;
  }

  @Override
  public void onCreateView() {

  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
    viewModel.getExploreLiveData().observe(getViewLifecycleOwner(), exploreLiveData -> updateRecyclerViewSongs(exploreLiveData.isNewThisWeekCallCompleted(), exploreLiveData.isPopularCallCompleted(), exploreLiveData.getNewThisWeekSongs(), exploreLiveData.getPopularSongs()));

    showViews();
  }

  private void showViews() {
    if (!NetworkUtils.isNetworkAvailable(requireContext())) {
      mController.showNoConnection(true, view -> ((MainActivity) requireActivity()).goToMyMusic());
      return;
    }

    mController.showNoConnection(false, null);
    viewModel.fetchPlaylists();
  }

  private void updateRecyclerViewSongs(boolean isNewThisWeekCompleted, boolean isPopularCompleted, List<Song> newThisWeekSongs, List<Song> popularSongs) {
    if(!isNewThisWeekCompleted || !isPopularCompleted) {
      return;
    }

    List<SongsSection> songsSections = new ArrayList<>();

    songsSections.add(new SongsSection(
        SongUtils.NEW_THIS_WEEK_PLAYLIST_ID,
        getString(R.string.new_this_week),
        newThisWeekSongs
    ));

    songsSections.add(new SongsSection(
        SongUtils.POPULAR_PLAYLIST_ID,
        getString(R.string.popular),
        popularSongs
    ));

    mController.setData(songsSections);
  }

  @Override
  public ExploreController getController() {
    return new ExploreController();
  }

  @Override
  public void onNetworkChanged(NetworkInfo networkInfo) {
    super.onNetworkChanged(networkInfo);
    showViews();
  }
}
