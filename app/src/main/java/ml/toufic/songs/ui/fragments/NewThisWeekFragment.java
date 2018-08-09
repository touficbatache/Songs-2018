package ml.toufic.songs.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ml.toufic.songs.R;
import ml.toufic.songs.model.DownloadedSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.network.SongDownloader;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

public class NewThisWeekFragment extends Fragment {

  private final static String MAX_RESULTS = "50";

  private static final String TAG = "NewThisWeekFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private Song songToDownload;
  private String toastText = "Oops, it looks like this song isn't currently available. Try again later";

  private ExploreAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private MaterialSearchView searchView;

  private List<Song> newThisWeekSongs = new ArrayList<Song>();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    setHasOptionsMenu(true);

    mContext = getContext();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.layout_recyclerview, null);

    mActivity.getSupportActionBar().setTitle(R.string.new_this_week);
    mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mAdapter = new ExploreAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getExploreSongs();
  }

  private void getExploreSongs() {

    String newThisWeekURL = "https://www.googleapis.com/youtube/v3/playlistItems?part=id, snippet&playlistId=" + SongUtils.NEW_THIS_WEEK_PLAYLIST_ID + "&key=" + SongUtils.API_KEY + "&maxResults=" + MAX_RESULTS;

    new JSONObjectDataFetcher(newThisWeekURL, new JSONObjectDataFetcher.JSONDataFetcherCallback() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          JSONArray playlistItems = result.getJSONArray("items");
          for (int i = 0; i < playlistItems.length(); i++) {
            JSONObject snippet = playlistItems.getJSONObject(i).getJSONObject("snippet");
            JSONObject resourceId = snippet.getJSONObject("resourceId");
            JSONObject thumbnailObj = snippet.getJSONObject("thumbnails");

            String id = resourceId.getString("videoId");
            String title = snippet.getString("title");
            String author = snippet.getString("channelTitle");
            String thumbnail = thumbnailObj.getJSONObject("high").getString("url"); // TODO: Add thumbnail size in settings

            newThisWeekSongs.add(new Song(id, title, author, thumbnail));
          }

          updateRecyclerViewSongs();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError() {

      }
    }).execute();
  }

  private void updateRecyclerViewSongs() {
    mAdapter.clear();

    for (Song song : newThisWeekSongs) {
      mAdapter.addSongModel(song);
    }
  }

  @SuppressLint("StaticFieldLeak")
  private void downloadSong(final Song song) {
    new YouTubeExtractor(mContext) {
      @Override
      public void onExtractionComplete(SparseArray<YtFile> ytFiles, final VideoMeta vMeta) {
        if (ytFiles != null) {
          for (int i = 0, itag; i < ytFiles.size(); i++) {
            itag = ytFiles.keyAt(i);
            final YtFile ytFile = ytFiles.get(itag);

            // We only want audio: height == -1 :)
            if (ytFile.getFormat().getHeight() == -1) {
              boolean isSongInDownloadsDir = SongUtils.isSongInDownloadsDir(song.getId());
              boolean isSongInMyMusic = SongUtils.isSongInMyMusic(mContext, song.getId());

              if (isSongInDownloadsDir && isSongInMyMusic) {
                toastText = "Song already exists!";
              } else {
                toastText = "Download started";
                SongDownloader.startDownload(mContext, song, ytFile, new SongDownloader.SongDownloaderCallback() {
                  @Override
                  public void onSuccess(DownloadedSong downloadedSong) {
                    if (downloadedSong.getPath().exists()) {
                      List<DownloadedSong> downloadedSongs = SongUtils.getCachedDownloadedSongs(mContext);
                      downloadedSongs.add(downloadedSong);
                      DeviceUtils.cacheData(mContext, SongUtils.CACHED_SONGS_KEY, new Gson().toJson(downloadedSongs));
                    }
                  }
                });
              }
            }
          }
        }
        Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
      }
    }.extract(song.getLink(), true, false);
  }

  public class ExploreAdapter extends EpoxyAdapter {
    void addSongModel(Song song) {
      addModel(new SongModel(song));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class SongModel extends EpoxyModelWithHolder<SongHolder> {

    private Song mSong;
    private String songId;
    private String songLink;
    private String songTitle;
    private String songArtist;
    private String songCoverArt;
    private View.OnClickListener onClickListener;

    SongModel(final Song song) {
      mSong = song;
      songId = song.getId();
      songLink = song.getLink();
      songTitle = song.getTitle();
      songArtist = song.getArtist();
      songCoverArt = song.getCoverArt();
      onClickListener = new View.OnClickListener() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void onClick(View view) {
          if (ContextCompat.checkSelfPermission(mActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
          } else {
            songToDownload = song;
            downloadSong(song);
          }
        }
      };
    }

    @Override
    protected SongHolder createNewHolder() {
      return new SongHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_song_explore;
    }

    @Override
    public void bind(SongHolder holder) {
      super.bind(holder);

      holder.itemView.setOnClickListener(onClickListener);
      holder.mSongTitle.setText(songTitle);
      holder.mSongArtist.setText(songArtist);
    }

    @Override
    public void unbind(SongHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
    }
  }

  public class SongHolder extends EpoxyHolder {

    View itemView;
    TextView mSongTitle;
    TextView mSongArtist;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongArtist = itemView.findViewById(R.id.song_artist);
    }
  }

  public class SeeMoreHolder extends EpoxyHolder {

    View itemView;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == DeviceUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        downloadSong(songToDownload);
      } else {
        Toast.makeText(mContext, "Please accept permission to download songs!", Toast.LENGTH_LONG).show();
      }
    }
  }
}
