package com.batache.songs.ui.controller;

import android.content.Context;

import androidx.navigation.Navigation;

import com.batache.songs.R;
import com.batache.songs.app.base.BaseController;
import com.batache.songs.app.explore.ExploreFragmentDirections;
import com.batache.songs.model.adapter.PlaylistModel_;
import com.batache.songs.model.adapter.SongModel_;
import com.batache.songs.model.adapter.SubheaderModel_;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.Song;

import java.util.List;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class SongsController extends BaseController {

  public void addSection(List<Song> songs) {
    for (Song song : songs) {
      data.add(new SongModel_()
          .song(song)
      );
    }

    requestModelBuild();
  }

  public void addTitledSection(Context context, String subheaderTitle, List<Song> songs) {
    data.add(new SubheaderModel_()
        .context(context)
        .title(subheaderTitle));

    for (Song song : songs) {
      data.add(new SongModel_()
          .song(song)
      );
    }

    requestModelBuild();
  }

  public void addSeeMoreSection(Context context, String title, List<Song> songs, String playlistId, boolean showArtist) {
    data.add(new SubheaderModel_()
        .context(context)
        .title(title)
        .onClickListener(view -> {
          ExploreFragmentDirections.PlaylistAction action = ExploreFragmentDirections.playlistAction(title, playlistId);
          Navigation.findNavController(view).navigate(action);
        })
    );

    for (Song song : songs) {
      data.add(new SongModel_()
          .song(song)
          .showArtist(showArtist)
      );
    }

    requestModelBuild();
  }

  public void addPlaylistsSection(Context context, String subheaderTitle, List<Playlist> playlists) {
    data.add(new SubheaderModel_()
        .context(context)
        .title(subheaderTitle));

    for (Playlist playlist : playlists) {
      if (playlist.getId().equals(PLAYLIST_LIKES_ID)) {
        data.add(new PlaylistModel_()
            .context(context)
            .icon(R.drawable.ic_favorite_outlined_black_24dp)
            .playlist(playlist)
        );
      } else if (playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
        data.add(new PlaylistModel_()
            .context(context)
            .icon(R.drawable.ic_cloud_download_outlined_black_24dp)
            .playlist(playlist)
        );
      } else {
        data.add(new PlaylistModel_()
            .context(context)
            .playlist(playlist)
        );
      }
    }

    requestModelBuild();
  }

  public void addPlaylistSection(String playlistId, List<Song> songs) {
    for (int i = 0; i < songs.size(); i++) {
      data.add(new SongModel_()
          .song(songs.get(i))
          .playlistId(playlistId)
          .index(i)
      );
    }

    requestModelBuild();
  }

  public void addSearchSection(String playlistId, List<Song> songs, String query) {
    for (int i = 0; i < songs.size(); i++) {
      Song song = songs.get(i);
      if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
          song.getArtist().toLowerCase().contains(query.toLowerCase())) {
        data.add(new SongModel_()
            .song(songs.get(i))
            .playlistId(playlistId)
            .index(i)
        );
      }
    }

    requestModelBuild();
  }
}