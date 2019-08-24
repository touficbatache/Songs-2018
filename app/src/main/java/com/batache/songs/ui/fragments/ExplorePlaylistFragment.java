package com.batache.songs.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import com.batache.songs.R;
import com.batache.songs.model.Song;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.ui.base.ListFragment;
import com.batache.songs.util.CodeUtils;

public class ExplorePlaylistFragment extends ListFragment {

  private final static String MAX_RESULTS = "50";

  private static final String TAG = "ExplorePlaylistFragment : ";

  private String playlistId;

  @Override
  public int getLayout() {
    return R.layout.fragment_explore_playlist;
  }

  @Override
  public void onCreateView() {
    if (getArguments() != null) {
      playlistId = ExplorePlaylistFragmentArgs.fromBundle(getArguments()).getPlaylistId();
    }
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
        mController.reset();
        mController.addSection(fetchedSongs);
      }

      @Override
      public void onFailure(Throwable t) {
        Toast.makeText(getContext(), "Failed fetching playlist", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
