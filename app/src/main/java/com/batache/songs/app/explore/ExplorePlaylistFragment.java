package com.batache.songs.app.explore;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.epoxy.EpoxyController;
import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.ui.controller.SongsController;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.SongUtils;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class ExplorePlaylistFragment extends ListFragment<ExploreController> {

  private final static String MAX_RESULTS = "50";

  private String playlistId;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    setEnterTransition(new MaterialFadeThrough());
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_explore;
  }

  @Override
  public void onCreateView() {
    if (getArguments() != null) {
      playlistId = ExplorePlaylistFragmentArgs.fromBundle(getArguments()).getPlaylistId();
    }
  }

  @Override
  public ExploreController getController() {
    return new ExploreController();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (!CodeUtils.isEmpty(playlistId)) {
      getExploreSongs();
    }
  }

  private void getExploreSongs() {
    SongsAPI.getInstance().getSongsFromPlaylist(playlistId, MAX_RESULTS, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        List<SongsSection> songsSections = new ArrayList<>();
        songsSections.add(new SongsSection(
            playlistId,
            null,
            fetchedSongs
        ));
        mController.setData(songsSections);
      }

      @Override
      public void onFailure(Throwable t) {
        Toast.makeText(getContext(), "Failed fetching playlist", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
