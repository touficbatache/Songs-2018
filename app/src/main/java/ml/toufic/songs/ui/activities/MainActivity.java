package ml.toufic.songs.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Date;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.db.PlaylistSongJoin.PlaylistSongJoinViewModel;
import ml.toufic.songs.db.Playlist.PlaylistViewModel;
import ml.toufic.songs.db.Song.SongViewModel;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.PlaylistSongJoin;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.ui.fragments.ExploreFragment;
import ml.toufic.songs.ui.fragments.MyMusicFragment;
import ml.toufic.songs.ui.fragments.PlayerFragment;

import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity: ";

  public BottomNavigationView mBottomNavigationView;

  private SongViewModel mSongViewModel;
  private PlaylistViewModel mPlaylistViewModel;
  private PlaylistSongJoinViewModel mPlaylistSongJoinViewModel;

  private ExploreFragment mExploreFragment;
  private MyMusicFragment mMyMusicFragment;
  private PlayerFragment mPlayerFragment;

  public SlidingUpPanelLayout mSlidingPanelLayout;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
    = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

      Fragment fragment = mExploreFragment;

      switch (item.getItemId()) {
        case R.id.navigation_explore:
          fragment = mExploreFragment;
          break;

        case R.id.navigation_my_music:
          fragment = mMyMusicFragment;
          break;
      }

      return loadFragment(fragment);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
      .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
      .setResizeAndRotateEnabledForNetwork(true)
      .setDownsampleEnabled(true)
      .build();
    Fresco.initialize(this, imagePipelineConfig);

    PRDownloaderConfig prDownloaderConfig = PRDownloaderConfig.newBuilder()
      .build();
    PRDownloader.initialize(getApplicationContext(), prDownloaderConfig);

    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mExploreFragment = new ExploreFragment();
    mMyMusicFragment = new MyMusicFragment();

    mBottomNavigationView = findViewById(R.id.navigation);
    mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.fragment_container, new ExploreFragment())
      .commitAllowingStateLoss();

    mPlayerFragment = new PlayerFragment();
    mSlidingPanelLayout = findViewById(R.id.sliding_layout);
    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.player_fragment_container, mPlayerFragment)
      .commitAllowingStateLoss();

    mSongViewModel = ViewModelProviders.of(this).get(SongViewModel.class);

    mPlaylistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
    mPlaylistViewModel.getAllPlaylistsLive().observe(this, new Observer<List<Playlist>>() {
      @Override
      public void onChanged(@Nullable final List<Playlist> playlists) {
        mMyMusicFragment.loadMyMusicPlaylists();
      }
    });

    mPlaylistSongJoinViewModel = ViewModelProviders.of(this).get(PlaylistSongJoinViewModel.class);
    mPlaylistSongJoinViewModel.getAllPlaylistSongJoinLive().observe(this, new Observer<List<PlaylistSongJoin>>() {
      @Override
      public void onChanged(@Nullable final List<PlaylistSongJoin> playlistSongJoins) {
        // Update the cached copy of the songs in the adapter.
        mMyMusicFragment.loadMyMusicPlaylists();
        mMyMusicFragment.updateSongsInPlaylist();
        mPlayerFragment.updateVisuals();
      }
    });

    insertPlaylist(new Playlist(PLAYLIST_LIKES_ID, getString(R.string.favorites), new Date()));
    insertPlaylist(new Playlist(PLAYLIST_DOWNLOADS_ID, getString(R.string.downloads), new Date()));
  }

  private boolean loadFragment(Fragment fragment) {
    if (fragment != null) {
      getSupportFragmentManager().popBackStack();
      getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commitAllowingStateLoss();
      return true;
    }
    return false;
  }

  public boolean pushFragment(Fragment fragment) {
    if (fragment != null) {
      getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commitAllowingStateLoss();
      return true;
    }
    return false;
  }

  public interface OnLastClickedUpdateCompletion {
    void onCompletion();
  }

  public void insertPlaylist(Playlist playlist) {
    if (!hasPlaylist(playlist.getId())) {
      mPlaylistViewModel.insert(playlist);
    }
  }

  public Playlist getPlaylist(String playlistId) {
    return mPlaylistViewModel.getPlaylist(playlistId);
  }

  public List<Playlist> getAllPlaylists() {
    return mPlaylistViewModel.getAllPlaylists();
  }

  public void setLastOpenedPlaylist(Playlist playlist) {
    mPlaylistViewModel.updatePlaylistWithResetAll(playlist);
  }

  public Playlist getLastOpenedPlaylist() {
    return mPlaylistViewModel.getLastOpenedPlaylist();
  }

  public boolean hasPlaylist(String playlistId) {
    return mPlaylistViewModel.hasPlaylist(playlistId);
  }

  public void deletePlaylist(Playlist playlist) {
    mPlaylistViewModel.delete(playlist);
  }

  public void deletePlaylist(String playlistId) {
    mPlaylistViewModel.delete(playlistId);
  }

  public void insertSong(PlayableSong playableSong) {
    mSongViewModel.insert(playableSong);
  }

  public void insertRemoteSong(Song song) {
    mSongViewModel.insert(song);
  }

  public void updateSong(PlayableSong playableSong) {
    mSongViewModel.update(playableSong);
  }

  public PlayableSong getSong(String songId) {
    return mSongViewModel.getSong(songId);
  }

  public void updateSongWithResetAll(PlayableSong playableSong) {
    mSongViewModel.updateSongWithResetAll(playableSong);
  }

  public void updateSongWithResetAll(PlayableSong playableSong, OnLastClickedUpdateCompletion completionCallback) {
    mSongViewModel.updateSongWithResetAll(playableSong, completionCallback);
  }

  public boolean hasSong(String songId) {
    return mSongViewModel.hasSong(songId);
  }

  public List<PlayableSong> getAllSongsAsync() {
    return mSongViewModel.getAllSongs();
  }

  public void deleteSong(PlayableSong playableSong) {
    if (playableSong.getPath().delete()) {
      mSongViewModel.delete(playableSong);
    }
  }

  public void insertSongInPlaylist(String playlistId, String songId, boolean isRemoteSong) {
    if (!isSongInPlaylist(playlistId, songId)) {
      mPlaylistSongJoinViewModel.insert(new PlaylistSongJoin(playlistId, songId, isRemoteSong));
    }
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mPlaylistSongJoinViewModel.getPlaylistsForSong(songId);
  }

  public List<PlayableSong> getSongsForPlaylist(String playlistId) {
    return mPlaylistSongJoinViewModel.getSongsForPlaylist(playlistId);
  }

  public boolean isSongInPlaylist(String playlistId, String songId) {
    return mPlaylistSongJoinViewModel.isSongInPlaylist(playlistId, songId);
  }

  public boolean isRemoteSongInPlaylist(String playlistId, String songId) {
    return mPlaylistSongJoinViewModel.isRemoteSongInPlaylist(playlistId, songId);
  }

  public boolean isSongInCustomPlaylist(String songId) {
    boolean isInCustomPlaylist = false;
    for (Playlist playlist : getPlaylistsForSong(songId)) {
      if (!playlist.getId().equals(PLAYLIST_LIKES_ID) && !playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
        isInCustomPlaylist = true;
      }
    }
    return isInCustomPlaylist;
  }

  public void removeSongFromPlaylist(String playlistId, String songId) {
    mPlaylistSongJoinViewModel.remove(playlistId, songId);
  }

  @Override
  public void onBackPressed() {
    MaterialSearchView searchView = findViewById(R.id.search_view);
    if (searchView.isSearchOpen()) {
      searchView.closeSearch();
      return;
    }

    if (mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      return;
    }

    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      getSupportFragmentManager().popBackStack();
      return;
    }

    super.onBackPressed();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  public void loadSong(PlayableSong playableSong, String playlistId) {
    mPlayerFragment.loadSong(playableSong, playlistId);
  }

  public void loadSong(Song remoteSong) {
    mPlayerFragment.loadSong(remoteSong);
  }
}
