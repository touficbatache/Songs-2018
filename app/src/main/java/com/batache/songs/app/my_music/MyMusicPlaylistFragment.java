package com.batache.songs.app.my_music;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.batache.songs.R;
import com.batache.songs.api.SongDownloader;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.app.MainActivity;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.app.explore.ExplorePlaylistFragmentArgs;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.PlaylistSongJoin;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;
import com.batache.songs.ui.controller.SongsController;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DeviceUtils;
import com.batache.songs.util.RestoreSongsUtils;
import com.batache.songs.util.SongUtils;
import com.batache.songs.util.events.DownloadEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.batache.songs.util.DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class MyMusicPlaylistFragment extends ListFragment<SongsController> {
  private static final String TAG = "MyMusicPlaylistFragm : ";

  private DatabaseHelper mDatabaseHelper;

  private String playlistTitle;
  private String playlistId;
  private List<Song> playlistSongs = new ArrayList<>();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setHasOptionsMenu(true);

    mDatabaseHelper = DatabaseHelper.getInstance(getContext());
    mDatabaseHelper.getPlaylistSongInsertionListener().observe(this, playlistSongJoins -> {
      loadSongsInPlaylist();
    });

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
  }

  @Override
  public void onResume() {
    super.onResume();
    CodeUtils.registerToEventBus(this);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_my_music_playlist;
  }

  @Override
  public void onCreateView() {
    if (getArguments() != null) {
      playlistId = ExplorePlaylistFragmentArgs.fromBundle(getArguments()).getPlaylistId();
      playlistTitle = ExplorePlaylistFragmentArgs.fromBundle(getArguments()).getTitle();
    }
  }

  @Override
  public SongsController getController() {
    return new SongsController();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (!CodeUtils.isEmpty(playlistId)) {
      loadSongsInPlaylist();
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_my_music_playlist, menu);

    if (playlistId.equals(PLAYLIST_LIKES_ID) || playlistId.equals(PLAYLIST_DOWNLOADS_ID)) {
      menu.findItem(R.id.action_delete).setVisible(false);
    }

    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) searchItem.getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText.trim())) {
          mController.reset();
          mController.addSearchSection(playlistId, mDatabaseHelper.getSongsForPlaylist(playlistId), newText.trim());
        } else {
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_export:
        new MaterialAlertDialogBuilder(requireActivity())
            .setMessage(getString(R.string.export_message))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.export), (dialog, id) -> {
              exportPlaylist();
            })
            .setNegativeButton(getString(android.R.string.cancel), (dialog, id) -> dialog.cancel())
            .show();
        return true;
      case R.id.action_delete:
        new MaterialAlertDialogBuilder(requireActivity())
            .setMessage(getString(R.string.delete_song_warning))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
              PlayerManager.getInstance().deletePlaylistAndSongs(playlistId);
              ((MainActivity) getActivity()).onSupportNavigateUp();
            })
            .setNegativeButton(getString(android.R.string.no), (dialog, id) -> dialog.cancel())
            .show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void exportPlaylist() {
    AsyncTask.execute(() -> {
      File phoneStorage = Environment.getExternalStorageDirectory();
      File dir = new File(phoneStorage.getAbsolutePath() + "/");

      File exportFile = new File(dir, String.format("%s.songsplaylist", playlistTitle));
      try {
        if (exportFile.createNewFile()) {
          FileWriter writer = new FileWriter(exportFile, true);
          writer.write(SongUtils.generateExportedPlaylist(playlistTitle, playlistSongs));
          writer.flush();
          writer.close();
          requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.export_success), Toast.LENGTH_LONG).show());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        exportPlaylist();
      }
    }
  }

  private void loadSongsInPlaylist() {
    playlistSongs = mDatabaseHelper.getSongsForPlaylist(playlistId);

    for (Iterator<Song> songsIterator = playlistSongs.iterator(); songsIterator.hasNext(); ) {
      Song song = songsIterator.next();
      if (song.getPath() != null && !song.getPath().exists()) {
        songsIterator.remove(); //TODO: Don't remove the songId, just tell the user that the songId can't be played and ask him if he wants to re-download it.
        mDatabaseHelper.deleteSong(song);
        updateRecyclerViewSongs();
      }
    }

    File downloadDirectory = DeviceUtils.getDownloadSongsDir(getContext());
    if (!downloadDirectory.exists()) {
      try {
        downloadDirectory.mkdirs();
      } catch (RuntimeException e) {
        Log.e(TAG, "downloadDirectory couldn't be created");
      }
    }
    if (downloadDirectory.exists()) {
      File[] downloadedSongsInDirectory = downloadDirectory.listFiles();
      List<String> songIdsToGet = new ArrayList<>();
      List<Song> recovered = new ArrayList<>();
      for (File songInDirectory : downloadedSongsInDirectory) {
        final String id = SongUtils.getSongId(songInDirectory.getName()).split("-")[0];
        if (songInDirectory.toString().contains(Song.SongSource.youtube.name())) {
          if (!mDatabaseHelper.hasSong(id) && songInDirectory.lastModified() < new Date().getTime() - 300000) {
            songIdsToGet.add(id);
          }
        } else if (songInDirectory.toString().contains(Song.SongSource.xiami.name())) {
          if (!mDatabaseHelper.hasSong(id) && songInDirectory.lastModified() < new Date().getTime() - 300000) {
            recovered.add(new Song(id, id, null, null));
          }
        }
      }
      if (!songIdsToGet.isEmpty()) {
        for (String songIdToGet : songIdsToGet) {
          RestoreSongsUtils restoreSongsUtils = RestoreSongsUtils.initialize(songIdsToGet.size());
          SongsAPI.getInstance().getSong(songIdToGet, new SongsAPI.OnSongsFetchedListener() {
            @Override
            public void onSuccess(List<Song> fetchedSongs) {
              restoreSongsUtils.addSuccess(fetchedSongs.get(0));
              if (restoreSongsUtils.getProgress() == 1) {
                List<Song> youtubeRecovered = restoreSongsUtils.getSongs();
                for (Song song : youtubeRecovered) {
                  song.setPath(DeviceUtils.getDownloadSongsDirFilePath(getContext(), SongUtils.getSongFileName(song.getId(), Song.SongSource.youtube)));

                  mDatabaseHelper.insertSong(song);
                  mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlistId, song.getId()));
                }
              }
            }

            @Override
            public void onFailure(@Nullable Throwable t) {

            }
          });
        }
      }
      if (!recovered.isEmpty()) {
        for (Song song : recovered) {
          song.setPath(DeviceUtils.getDownloadSongsDirFilePath(getContext(), SongUtils.getSongFileName(song.getId(), Song.SongSource.xiami)));

          mDatabaseHelper.insertSong(song);
          mDatabaseHelper.insertSongInPlaylist(new PlaylistSongJoin(playlistId, song.getId()));
        }
        updateRecyclerViewSongs();
      }
    }

    updateRecyclerViewSongs();
  }

  private void updateRecyclerViewSongs() {
//    if (PLAYLIST_DOWNLOADS_ID.equals(playlistId)) {
//      SongDownloader.getInstance().getDownloadingAndQueuedSongs(result -> {
//        mController.reset();
//
//        mController.addDownloadingSection(getContext(), result);
//
//        mController.addPlaylistSection(playlistId, playlistSongs);
//      });
//
//      return;
//    }

    mController.reset();

    if (PLAYLIST_DOWNLOADS_ID.equals(playlistId)) {
      SongDownloader.getInstance().getDownloadingAndQueuedSongs(result -> {
        mController.addDownloadingSection(result);
      });
    }

    mController.addPlaylistSection(playlistId, playlistSongs);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onDownloadEvent(DownloadEvent downloadEvent) {
    updateRecyclerViewSongs();
  }

  @Override
  public void onPause() {
    super.onPause();

    CodeUtils.unregisterFromEventBus(this);
  }
}
