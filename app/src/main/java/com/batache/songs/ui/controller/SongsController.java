package com.batache.songs.ui.controller;

import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.app.base.BaseController;
import com.batache.songs.model.adapter.AutocompleteQueryModel_;
import com.batache.songs.model.adapter.DownloadingSongModel_;
import com.batache.songs.model.adapter.ImportingPlaylistModel_;
import com.batache.songs.model.adapter.PlaylistModel_;
import com.batache.songs.model.adapter.SongModel_;
import com.batache.songs.model.adapter.SubheaderModel_;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.SongUtils;
import com.batache.songs.util.events.ImportEvent;
import com.tonyodev.fetch2.Download;

import java.util.ArrayList;
import java.util.List;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class SongsController extends BaseController {

  public void addSection(List<Song> songs) {
    for (Song song : songs) {
      data.add(new SongModel_()
          .id(song.getId())
          .song(song)
      );
    }

    requestModelBuild();
  }

  public void addExploreSearchResultsSection(List<Song> songs) {
    data.add(new SubheaderModel_()
        .id("subheader_top_result")
        .title("Top result"));

    data.add(new SongModel_()
        .id(songs.get(0).getId())
        .song(songs.get(0))
        .showYoutubeIcon(true)
    );

    data.add(new SubheaderModel_()
        .id("subheader_songs")
        .title("Songs"));

    for (Song song : songs) {
      data.add(new SongModel_()
          .id(song.getId())
          .song(song)
          .showYoutubeIcon(true)
      );
    }

    requestModelBuild();
  }

  public void addPlaylistsSection(String subheaderTitle, List<Playlist> playlists, List<SongUtils.ExportedPlaylist> importingPlaylists, ImportEvent importEvent) {
    data.add(new SubheaderModel_()
        .id("subheader_playlists")
        .title(subheaderTitle));

    List<EpoxyModelWithHolder> userCreatedPlaylists = new ArrayList<>();

    for (Playlist playlist : playlists) {
      if (playlist.getId().equals(PLAYLIST_LIKES_ID)) {
        data.add(new PlaylistModel_()
            .id(playlist.getId())
            .icon(R.drawable.ic_favorite_outlined_black_24dp)
            .playlist(playlist)
        );
        continue;
      }

      if (playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
        data.add(new PlaylistModel_()
            .id(playlist.getId())
            .icon(R.drawable.ic_cloud_download_outlined_black_24dp)
            .playlist(playlist)
        );
        continue;
      }

      userCreatedPlaylists.add(new PlaylistModel_()
          .id(playlist.getId())
          .playlist(playlist)
      );
    }

    for (int i = 0; i < importingPlaylists.size(); i++) {
      SongUtils.ExportedPlaylist playlist = importingPlaylists.get(i);

      data.add(new ImportingPlaylistModel_()
          .id("importing_playlist_" + playlist.getTitle())
          .playlistTitle(playlist.getTitle())
          .importEvent(i == 0 ? importEvent : null)
      );
    }

    data.addAll(userCreatedPlaylists);

    requestModelBuild();
  }

  public void addPlaylistSection(String playlistId, List<Song> songs) {
    for (int i = 0; i < songs.size(); i++) {
      data.add(new SongModel_()
          .id(songs.get(i).getId())
          .song(songs.get(i))
          .playlistId(playlistId)
          .index(i)
      );
    }

    requestModelBuild();
  }

  public void addDownloadingSection(List<Download> downloads) {
    for (int i = 0; i < downloads.size(); i++) {
      data.add(new DownloadingSongModel_()
          .id(downloads.get(i).getId())
          .download(downloads.get(i))
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
            .id(song.getId())
            .song(songs.get(i))
            .playlistId(playlistId)
            .index(i)
        );
      }
    }

    requestModelBuild();
  }
}