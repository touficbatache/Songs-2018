package ml.toufic.songs.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.R;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

public class MyMusicFragment extends Fragment {

  private static final String TAG = "MyMusicFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;
  private ActionMode mActionMode;

  private MyMusicAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRefreshLayout;
  private MaterialSearchView searchView;
  private List<PlayableSong> playableSongs = new ArrayList<>();

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
    mRootView = inflater.inflate(R.layout.fragment_my_music, null);

    mActivity.getSupportActionBar().setTitle(R.string.title_my_music);
    mActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
    mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    mAdapter = new MyMusicAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    mSwipeRefreshLayout = mRootView.findViewById(R.id.swipe_refresh_layout);
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh() {
        loadMyMusic();
      }
    });

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loadDownloadedSongs();

    initSearch();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_main, menu);

    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);
    searchView.setVoiceSearch(true);
  }

  private void loadDownloadedSongs() {
    playableSongs = SongUtils.getCachedSongs(mContext);

    if (ContextCompat.checkSelfPermission(mActivity,
      Manifest.permission.WRITE_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED) {

      requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
        DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    } else {
      loadMyMusic();
    }
  }

  private void loadMyMusic() {
    for (Iterator<PlayableSong> playableSongsIterator = playableSongs.iterator(); playableSongsIterator.hasNext(); ) {
      PlayableSong playableSong = playableSongsIterator.next();
      if (!playableSong.getPath().exists()) {
        playableSongsIterator.remove(); //TODO: Don't remove the song, just tell the user that the song can't be played and ask him if he wants to re-download it.
      }
    }

    File downloadDirectory = DeviceUtils.getDownloadStorageDirectory();
    if(!downloadDirectory.exists()){
      try {
        downloadDirectory.mkdirs();
      }
      catch (RuntimeException e){
        Log.e(TAG, "downloadDirectory couldn't be created");
      }
    }
    if(downloadDirectory.exists()) {
      File[] downloadedSongsInDirectory = downloadDirectory.listFiles();
      for (File songInDirectory : downloadedSongsInDirectory) {
        final String id = SongUtils.getSongId(songInDirectory.getName());
        if (!SongUtils.isSongInMyMusic(mContext, id) && songInDirectory.lastModified() < new Date().getTime() - 300000) {
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
                  playableSongs.add(new PlayableSong(song, SongUtils.getSongPath(SongUtils.getSongFileName(id))));
                  SongUtils.cacheSongs(mContext, playableSongs);
                  updateRecyclerViewDownloadedSongs();
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

    SongUtils.cacheSongs(mContext, playableSongs);
    updateRecyclerViewDownloadedSongs();
  }

  private void initSearch() {
    searchView = mActivity.findViewById(R.id.search_view);
    searchView.setHint(getResources().getString(R.string.hint_search_downloaded));
    searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if(!TextUtils.isEmpty(newText.trim())) {
          mAdapter.clear();
          for(PlayableSong playableSong : playableSongs) {
            if(playableSong.getTitle().toLowerCase().contains(newText.trim().toLowerCase()) || playableSong.getArtist().toLowerCase().contains(newText.trim().toLowerCase())) {
              mAdapter.addDownloadedSongModel(playableSong);
            }
          }
        } else {
          updateRecyclerViewDownloadedSongs();
        }
        return true;
      }
    });
  }

  public void updateRecyclerViewDownloadedSongs() {
    mAdapter.clear();

    for(int i=0; i<playableSongs.size(); i++) {
      PlayableSong playableSong = playableSongs.get(i);
      playableSong.setIndexInQueue(i);
      mAdapter.addDownloadedSongModel(playableSong);
    }

    mSwipeRefreshLayout.setRefreshing(false);
  }

  class MyMusicAdapter extends EpoxyAdapter {
    void addDownloadedSongModel(PlayableSong playableSong) {
      addModel(new DownloadedSongModel(playableSong));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class DownloadedSongModel extends EpoxyModelWithHolder<DownloadedSongHolder> {

    private PlayableSong song;
    private String songId;
    private File songPath;
    private String songTitle;
    private String songArtist;
    private String songCoverArt;

    private View.OnClickListener onClickListener;
    private View.OnClickListener deleteOnClickListener;

    DownloadedSongModel(final PlayableSong playableSong) {
      song = playableSong;
      songId = song.getId();
      songPath = song.getPath();
      songTitle = song.getTitle();
      songArtist = song.getArtist();
      songCoverArt = song.getCoverArt();
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mActivity.loadSong(playableSong);
        }
      };
      deleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
          builder.setMessage("Are you sure you want to delete this song? This action cannot be reverted!")
            .setCancelable(true)
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            })
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                if(SongUtils.deleteSong(mContext, song)) {
                  updateRecyclerViewDownloadedSongs();
                }
              }
            });
          AlertDialog alert = builder.create();
          alert.show();
        }
      };
    }

    @Override
    protected DownloadedSongHolder createNewHolder() {
      return new DownloadedSongHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_song_my_music;
    }

    @Override
    public void bind(@NonNull final DownloadedSongHolder holder) {
      super.bind(holder);

      holder.itemView.setOnClickListener(onClickListener);
      holder.mSongTitle.setText(songTitle);
      holder.mSongArtist.setText(songArtist);
      holder.mDeleteBtn.setOnClickListener(deleteOnClickListener);
    }

    @Override
    public void unbind(@NonNull DownloadedSongHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
      holder.mDeleteBtn.setOnClickListener(null);
    }
  }

  private class DownloadedSongHolder extends EpoxyHolder {

    View itemView;
    ImageView mSongIcon;
    TextView mSongTitle;
    TextView mSongArtist;
    ImageButton mDeleteBtn;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongIcon = itemView.findViewById(R.id.song_icon);
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongArtist = itemView.findViewById(R.id.song_artist);
      mDeleteBtn = itemView.findViewById(R.id.btn_delete);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        loadMyMusic();
      } else {
        Toast.makeText(mContext, "Please accept permission to see downloaded songs!", Toast.LENGTH_LONG).show();
      }
    }
  }

}
