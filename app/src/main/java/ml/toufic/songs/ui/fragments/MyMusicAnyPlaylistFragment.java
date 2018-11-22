package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class MyMusicAnyPlaylistFragment extends Fragment {

  private final static String MAX_RESULTS = "50";

  private static final String TAG = "MyMusicAnyPlayl : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;
  private Menu mMenu;

  private AnyPlaylistAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private MaterialSearchView searchView;

  private String playlistTitle = "Playlist";
  private String playlistId;
  private List<PlayableSong> playlistSongs = new ArrayList<>();

  public static MyMusicAnyPlaylistFragment newInstance(String playlistTitle, String playlistId) {
    MyMusicAnyPlaylistFragment fragment = new MyMusicAnyPlaylistFragment();
    Bundle bundle = new Bundle();
    bundle.putString("playlist-title", playlistTitle);
    bundle.putString("playlist-id", playlistId);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    setHasOptionsMenu(true);

    mContext = getContext();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.layout_recyclerview, null);

    Bundle args = getArguments();
    if (args != null) {
      playlistTitle = args.getString("playlist-title");
      playlistId = args.getString("playlist-id");
    }

    mActivity.getSupportActionBar().setTitle(playlistTitle);
    mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_grey_67_24dp);

    mAdapter = new AnyPlaylistAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getArguments() != null) {
      loadSongsInPlaylist();
      initSearch();
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_my_music_playlist, menu);

    if (playlistId.equals(PLAYLIST_LIKES_ID) || playlistId.equals(PLAYLIST_DOWNLOADS_ID)) {
      menu.findItem(R.id.action_delete).setVisible(false);
    }

    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);
    searchView.setVoiceSearch(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_delete:
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to delete this playlist? This action cannot be undone!")
          .setCancelable(true)
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              for (PlayableSong playableSong : mActivity.getSongsForPlaylist(playlistId)) {
                mActivity.removeSongFromPlaylist(playlistId, playableSong.getId());
              }
              mActivity.deletePlaylist(playlistId);
              mActivity.getSupportFragmentManager().popBackStack();
            }
          })
          .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void loadSongsInPlaylist() {
    if (mActivity != null) {
      playlistSongs = mActivity.getSongsForPlaylist(playlistId);

      for (Iterator<PlayableSong> playableSongsIterator = playlistSongs.iterator(); playableSongsIterator.hasNext(); ) {
        PlayableSong playableSong = playableSongsIterator.next();
        if (!playableSong.getPath().exists()) {
          playableSongsIterator.remove(); //TODO: Don't remove the song, just tell the user that the song can't be played and ask him if he wants to re-download it.
          mActivity.deleteSong(playableSong);
          updateRecyclerViewSongs();
        }
      }

      File downloadDirectory = DeviceUtils.getInternalFilesSongsDir(mContext);
      if (!downloadDirectory.exists()) {
        try {
          downloadDirectory.mkdirs();
        } catch (RuntimeException e) {
          Log.e(TAG, "downloadDirectory couldn't be created");
        }
      }
      if (downloadDirectory.exists()) {
        File[] downloadedSongsInDirectory = downloadDirectory.listFiles();
        for (File songInDirectory : downloadedSongsInDirectory) {
          final String id = SongUtils.getSongId(songInDirectory.getName());
          if (!mActivity.hasSong(id) && songInDirectory.lastModified() < new Date().getTime() - 300000) {
            new JSONObjectDataFetcher("https://www.googleapis.com/youtube/v3/videos?part=id, snippet&id=" + id + "&key=" + SongUtils.API_KEY, new JSONObjectDataFetcher.JSONDataFetcherCallback() {
              @Override
              public void onSuccess(JSONObject result) {
                try {
                  JSONArray playlistItems = result.getJSONArray("items");
                  for (int i = 0; i < playlistItems.length(); i++) {
                    JSONObject snippet = playlistItems.getJSONObject(i).getJSONObject("snippet");
                    JSONObject thumbnailObj = snippet.getJSONObject("thumbnails");


                    String title = snippet.getString("title");
                    String author = snippet.getString("channelTitle");
                    String thumbnail = thumbnailObj.getJSONObject("high").getString("url");

                    Song song = new Song(id, title, author, thumbnail);
                    PlayableSong playableSong = new PlayableSong(song, DeviceUtils.getInternalFilesSongsDirFilePath(mContext, SongUtils.getSongFileName(id)));

                    mActivity.insertSong(playableSong);
                    mActivity.insertSongInPlaylist(playlistId, playableSong.getId(), false);
                    updateRecyclerViewSongs();
                  }
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }

              @Override
              public void onError() {

              }
            }).execute();
          }
        }
      }

      updateRecyclerViewSongs();
    }
  }

  private void initSearch() {
    searchView = mActivity.findViewById(R.id.search_view);
    searchView.setHint(getResources().getString(R.string.hint_search_playlist));
    searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText.trim())) {
          mAdapter.clear();
          for (int i = 0; i < playlistSongs.size(); i++) {
            PlayableSong playableSong = playlistSongs.get(i);
            if (playableSong.getTitle().toLowerCase().contains(newText.trim().toLowerCase()) || playableSong.getArtist().toLowerCase().contains(newText.trim().toLowerCase())) {
              mAdapter.addSongModel(playableSong, i);
            }
          }
        } else {
          updateRecyclerViewSongs();
        }
        return true;
      }
    });
  }

  private void updateRecyclerViewSongs() {
    mAdapter.clear();

//    for (PlayableSong playableSong : playlistSongs) {
//      mAdapter.addSongModel(playableSong);
//    }
    for (int i = 0; i < playlistSongs.size(); i++) {
      PlayableSong playableSong = playlistSongs.get(i);
      mAdapter.addSongModel(playableSong, i);
    }
  }

  class AnyPlaylistAdapter extends EpoxyAdapter {
    void addSongModel(PlayableSong playableSong, int index) {
      addModel(new SongModel(playableSong, index));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class SongModel extends EpoxyModelWithHolder<SongHolder> {

    private PlayableSong song;
    private String songTitle;
    private String songArtist;

    private View.OnClickListener onClickListener;

    SongModel(final PlayableSong playableSong, final int index) {
      song = playableSong;
      songTitle = song.getTitle();
      songArtist = song.getArtist();
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mActivity.loadSong(index, playlistId);
        }
      };
    }

    @Override
    protected SongHolder createNewHolder() {
      return new SongHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_song_my_music;
    }

    @Override
    public void bind(@NonNull SongHolder holder) {
      super.bind(holder);

      holder.itemView.setOnClickListener(onClickListener);
      holder.mSongTitle.setText(songTitle);
      holder.mSongArtist.setText(songArtist);
    }

    @Override
    public void unbind(@NonNull SongHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
    }
  }

  protected class SongHolder extends EpoxyHolder {

    View itemView;
    ImageView mSongIcon; //for multiple selection #23
    TextView mSongTitle;
    TextView mSongArtist;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongIcon = itemView.findViewById(R.id.song_icon);
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongArtist = itemView.findViewById(R.id.song_artist);
    }
  }
}
