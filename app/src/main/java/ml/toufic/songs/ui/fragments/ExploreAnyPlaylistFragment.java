package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.api.YoutubeApiClient;
import ml.toufic.songs.api.YoutubeApiInterface;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.model.YoutubePlaylistResponse;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.util.SongUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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

    Retrofit retrofit = YoutubeApiClient.getRetrofitClient();
    YoutubeApiInterface apiInterface = retrofit.create(YoutubeApiInterface.class);

    Call<YoutubePlaylistResponse> call = apiInterface.getPlaylist(playlistId, MAX_RESULTS);
    call.enqueue(new Callback<YoutubePlaylistResponse>() {
      @Override
      public void onResponse(@NonNull Call<YoutubePlaylistResponse> call, @NonNull Response<YoutubePlaylistResponse> response) {
        if (response.body() != null) {
          List<YoutubePlaylistResponse.Item> playlistItems = response.body().items;

          if(getActivity() != null && isAdded()) {
            for(YoutubePlaylistResponse.Item playlistItem : playlistItems) {
              YoutubePlaylistResponse.Snippet snippet = playlistItem.snippet;
              YoutubePlaylistResponse.ResourceId resourceId = snippet.resourceId;
              YoutubePlaylistResponse.Thumbnails thumbnails = snippet.thumbnails;

              String id = resourceId.videoId;
              String title = snippet.title;
              String author = snippet.channelTitle;
              String thumbnail = thumbnails.thumbnail_high.url; // TODO: Add thumbnail size in settings

              playlistSongs.add(new Song(id, title, author, thumbnail));
            }

            updateRecyclerViewSongs();
          }
        }
      }

      @Override
      public void onFailure(@NonNull Call<YoutubePlaylistResponse> call, @NonNull Throwable t) {
        Toast.makeText(mContext, "Failed fetching playlist", Toast.LENGTH_SHORT).show();
      }
    });
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

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongTitle = itemView.findViewById(R.id.song_title);
    }
  }
}
