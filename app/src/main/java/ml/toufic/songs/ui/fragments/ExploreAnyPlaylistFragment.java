package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.util.SongUtils;

public class ExploreAnyPlaylistFragment extends Fragment {

  private final static String MAX_RESULTS = "50";

  private static final String TAG = "ExploreAnyPlaylistFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private Song songToDownload;

  private AnyPlaylistAdapter mAdapter;
  private RecyclerView mRecyclerView;

  private String playlistTitle = "Playlist";
  private String playlistId;
  private List<Song> playlistSongs = new ArrayList<>();

  public static ExploreAnyPlaylistFragment newInstance(String playlistName, String playlistId) {
    ExploreAnyPlaylistFragment fragment = new ExploreAnyPlaylistFragment();
    Bundle bundle = new Bundle();
    bundle.putString("playlist-name", playlistName);
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
      playlistTitle = args.getString("playlist-name");
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
      getExploreSongs();
    }
  }

  private void getExploreSongs() {

    String newThisWeekURL = "https://www.googleapis.com/youtube/v3/playlistItems?part=id, snippet&playlistId=" + playlistId + "&key=" + SongUtils.API_KEY + "&maxResults=" + MAX_RESULTS;

    new JSONObjectDataFetcher(newThisWeekURL, new JSONObjectDataFetcher.JSONDataFetcherCallback() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          JSONArray playlistItems = result.getJSONArray("items");
          for (int i = 0; i < playlistItems.length(); i++) {
            JSONObject snippet = playlistItems.getJSONObject(i).getJSONObject("snippet");
            JSONObject resourceId = snippet.getJSONObject("resourceId");
            JSONObject thumbnailObj = null;
            if(snippet.has("thumbnails")){
              thumbnailObj = snippet.getJSONObject("thumbnails");
            }

            String id = resourceId.getString("videoId");
            String title = snippet.getString("title");
            String author = snippet.getString("channelTitle");
            if(thumbnailObj != null) {
              String thumbnail = thumbnailObj.getJSONObject("high").getString("url"); // TODO: Add thumbnail size in settings

              playlistSongs.add(new Song(id, title, author, thumbnail));
            }
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

    for (Song song : playlistSongs) {
      mAdapter.addSongModel(song);
    }
  }

  class AnyPlaylistAdapter extends EpoxyAdapter {
    void addSongModel(Song song) {
      addModel(new SongModel(song));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class SongModel extends EpoxyModelWithHolder<SongHolder> {

    private SongHolder holder;

    private Song mSong;
    private String songId;
    private String songLink;
    private String songTitle;
    private String songArtist;
    private String songCoverArt;
    private View.OnClickListener playListener;
    private View.OnClickListener downloadListener;

    SongModel(Song song) {
      mSong = song;
      songId = song.getId();
      songLink = song.getLink();
      songTitle = song.getTitle();
      songArtist = song.getArtist();
      songCoverArt = song.getCoverArt();
      playListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          songToDownload = mSong;
          mActivity.loadSong(mSong);
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
    public void bind(@NonNull SongHolder holder) {
      super.bind(holder);

      this.holder = holder;
      holder.itemView.setOnClickListener(playListener);
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
    TextView mSongTitle;
    TextView mSongArtist;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongArtist = itemView.findViewById(R.id.song_artist);
    }
  }
}
