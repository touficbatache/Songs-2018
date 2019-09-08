package com.batache.songs.app.player;

import android.content.Context;

import com.batache.songs.app.base.BaseController;
import com.batache.songs.model.adapter.PlayerCoverModel;
import com.batache.songs.model.adapter.PlayerCoverModel_;
import com.batache.songs.model.pojo.Song;

import java.util.List;

public class PlayerCoversController extends BaseController {

  private Context context;
  private PlayerCoverModel.PlayerCoverCallback playerCoverCallback;

  public PlayerCoversController(Context context, PlayerCoverModel.PlayerCoverCallback callback) {
    this.context = context;
    playerCoverCallback = callback;
  }

  public void addCoverArts(List<Song> songs) {
    for (Song song : songs) {
      data.add(
          new PlayerCoverModel_()
              .context(context)
              .songId(song.getId())
              .coverArtUrl(song.getCoverArt())
              .callback(playerCoverCallback)
      );
    }

    requestModelBuild();
  }

  public void addCoverArt(String coverArtUrl) {
    data.add(
        new PlayerCoverModel_()
            .context(context)
            .coverArtUrl(coverArtUrl)
            .callback(playerCoverCallback)
    );

    requestModelBuild();
  }

}