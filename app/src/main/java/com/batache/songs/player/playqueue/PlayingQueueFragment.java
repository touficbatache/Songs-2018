package com.batache.songs.player.playqueue;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class PlayingQueueFragment extends BottomSheetDialogFragment {

  private static final String TAG = "PlayingQueueFragment : ";
  private View rootView;
  private Context context;

  private List<Song> playingQueue;
  private LastPlayed lastPlayed;

  private PlayingQueueController controller;
  private LinearLayoutManager linearLayoutManager;
  private EpoxyRecyclerView recyclerView;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    context = getContext();

    playingQueue = PlayerManager.getInstance().getPlayqueue();
    lastPlayed = PlayerManager.getInstance().getLastPlayed();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.fragment_playing_queue, container, false);

    controller = new PlayingQueueController();
    recyclerView = rootView.findViewById(R.id.recyclerview);

    linearLayoutManager = new LinearLayoutManager(context);
    recyclerView.setLayoutManager(linearLayoutManager);

    recyclerView.setController(controller);

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    String currentSongId = playingQueue.get(lastPlayed.getSongIndex()).getId();
    controller.addSongs(playingQueue, currentSongId, view1 -> dismiss());

    for (int i = 0; i < playingQueue.size(); i++) {
      Song song = playingQueue.get(i);
      if (song.getId().equals(currentSongId)) {
        linearLayoutManager.scrollToPositionWithOffset(i, recyclerView.getHeight() / 3);
        break;
      }
    }
  }
}
