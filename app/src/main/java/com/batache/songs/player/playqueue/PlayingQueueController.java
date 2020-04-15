package com.batache.songs.player.playqueue;

import android.view.View;

import com.batache.songs.app.base.BaseController;
import com.batache.songs.model.adapter.playingqueue_models.PlayingQueueCurrentSongModel_;
import com.batache.songs.model.adapter.playingqueue_models.PlayingQueueSongModel_;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;

import java.util.ArrayList;
import java.util.List;

public class PlayingQueueController extends BaseController {

  private List<Song> songs = new ArrayList<>();

  public void addSongs(List<Song> songs, String currentSongId, View.OnClickListener dismissListener) {
    this.songs = songs;
    for (int i = 0; i < songs.size(); i++) {
      Song song = songs.get(i);
      if (song.getId().equals(currentSongId)) {
        data.add(
            new PlayingQueueCurrentSongModel_()
                .songIndex(i)
                .songTitle(song.getTitle())
        );
      } else {
        int finalI = i;
        data.add(
            new PlayingQueueSongModel_()
                .songIndex(i)
                .songTitle(song.getTitle())
                .onClickListener(view -> {
                  PlayerManager.getInstance().playSong(PlayerManager.getInstance().getLastPlayed().getPlaylistId(), finalI);
                  dismissListener.onClick(view);
                })
        );
      }
    }
    requestModelBuild();
  }

}
