package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.api.YoutubeApiClient;
import ml.toufic.songs.api.YoutubeApiInterface;
import ml.toufic.songs.model.YoutubePlaylistResponse;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.R;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.util.SongUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ExploreFragment extends Fragment {

  private final static String MAX_RESULTS = "5";

  private static final String TAG = "ExploreFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private ExploreAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private MaterialSearchView searchView;

  private List<Song> newThisWeekSongs = new ArrayList<>();
  private List<Song> popularSongs = new ArrayList<>();
  private boolean hasNewThisWeek = false;
  private boolean hasPopular = false;

  private String mSearchQuery;
  private final Handler handler = new Handler();
  private Runnable searchRunnable = new Runnable() {
    @Override
    public void run() {
      mAdapter.clear();
      search();
    }
  };

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

    mActivity.getSupportActionBar().setTitle(R.string.explore);
    mActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
    mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    mAdapter = new ExploreAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if(!newThisWeekSongs.isEmpty()) {
      hasNewThisWeek = true;
    }
    if (!popularSongs.isEmpty()) {
      hasPopular = true;
    }
    getExploreSongs();
    updateRecyclerViewSongs();

    initSearch();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_main, menu);

    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);
    searchView.setBackIcon(getResources().getDrawable(R.drawable.ic_arrow_back_grey_67_24dp));
    searchView.setVoiceSearch(true);
  }

  private void getExploreSongs() {
    if(newThisWeekSongs.isEmpty()) {
      Retrofit retrofit = YoutubeApiClient.getRetrofitClient();
      YoutubeApiInterface apiInterface = retrofit.create(YoutubeApiInterface.class);

      Call<YoutubePlaylistResponse> call = apiInterface.getPlaylist(SongUtils.NEW_THIS_WEEK_PLAYLIST_ID, MAX_RESULTS);
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

                newThisWeekSongs.add(new Song(id, title, author, thumbnail));
              }

              hasNewThisWeek = true;

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

    if(popularSongs.isEmpty()) {
      Retrofit retrofit = YoutubeApiClient.getRetrofitClient();
      YoutubeApiInterface apiInterface = retrofit.create(YoutubeApiInterface.class);

      Call<YoutubePlaylistResponse> call = apiInterface.getPlaylist(SongUtils.POPULAR_PLAYLIST_ID, MAX_RESULTS);
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

                popularSongs.add(new Song(id, title, author, thumbnail));
              }

              hasPopular = true;

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
  }

  private void initSearch() {
    searchView = mActivity.findViewById(R.id.search_view);
    searchView.setHint(getResources().getString(R.string.hint_search));
    searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText.trim())) {
          handler.removeCallbacks(searchRunnable);
          mSearchQuery = newText.trim();
          handler.postDelayed(searchRunnable, 300);
        } else {
          if(!newThisWeekSongs.isEmpty()) {
            hasNewThisWeek = true;
          }
          if (!popularSongs.isEmpty()) {
            hasPopular = true;
          }

          updateRecyclerViewSongs();
        }
        return true;
      }
    });
  }

  private void search() {
    new JSONObjectDataFetcher("https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + mSearchQuery + "&type=video&key=" + SongUtils.API_KEY, new JSONObjectDataFetcher.JSONDataFetcherCallback() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          JSONArray videoItems = result.getJSONArray("items");
          for (int i = 0; i < videoItems.length(); i++) {
            JSONObject snippet = videoItems.getJSONObject(i).getJSONObject("snippet");

            String id = videoItems.getJSONObject(i).getJSONObject("id").getString("videoId");
            String title = snippet.getString("title");
            String author = snippet.getString("channelTitle");
            String thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

            mAdapter.addSongModel(new Song(id, title, author, thumbnail));
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

  private void updateRecyclerViewSongs() {
    if(getActivity() != null) {
      mAdapter.clear();

      if (hasNewThisWeek) {
        mAdapter.addSubheader(getString(R.string.new_this_week));

        for (Song song : newThisWeekSongs) {
          mAdapter.addSongModel(song);
        }

        mAdapter.addSeeMore(ExploreAnyPlaylistFragment.newInstance(getResources().getString(R.string.new_this_week), SongUtils.NEW_THIS_WEEK_PLAYLIST_ID));
      }

      if (hasPopular) {
        mAdapter.addSubheader(getString(R.string.popular));

        for (Song song : popularSongs) {
          mAdapter.addSongModel(song);
        }

        mAdapter.addSeeMore(ExploreAnyPlaylistFragment.newInstance(getResources().getString(R.string.popular), SongUtils.POPULAR_PLAYLIST_ID));
      }
    }
  }

  class ExploreAdapter extends EpoxyAdapter {
    void addSubheader(String title) {
      addModel(new SubheaderModel(title));
    }

    void addSongModel(Song song) {
      addModel(new SongModel(song));
    }

    void addSeeMore(Fragment fragment) {
      addModel(new SeeMoreModel(fragment));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class SubheaderModel extends EpoxyModelWithHolder<SubheaderHolder> {

    String title;

    SubheaderModel(String title) {
      this.title = title;
    }

    @Override
    protected SubheaderHolder createNewHolder() {
      return new SubheaderHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_subheader;
    }

    @Override
    public void bind(@NonNull SubheaderHolder holder) {
      super.bind(holder);

      holder.subheader.setText(title);
    }
  }

  private class SongModel extends EpoxyModelWithHolder<SongHolder> {

    private SongHolder holder;

    private Song mSong;
    private String songTitle;
    private View.OnClickListener playListener;

    SongModel(Song song) {
      mSong = song;
      songTitle = song.getTitle();
      playListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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

  private class SeeMoreModel extends EpoxyModelWithHolder<SeeMoreHolder> {

    Fragment fragment;
    AppCompatActivity appCompatActivity;
    View.OnClickListener onClickListener;

    SeeMoreModel(final Fragment fragment) {
      this.fragment = fragment;
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mActivity.pushFragment(fragment);
        }
      };
    }

    @Override
    protected SeeMoreHolder createNewHolder() {
      return new SeeMoreHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_see_more;
    }

    @Override
    public void bind(@NonNull SeeMoreHolder holder) {
      super.bind(holder);

      holder.itemView.setOnClickListener(onClickListener);
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

  protected class SubheaderHolder extends EpoxyHolder {

    TextView subheader;

    @Override
    protected void bindView(View itemView) {
      this.subheader = (TextView) itemView;
    }
  }

  protected class SeeMoreHolder extends EpoxyHolder {

    View itemView;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
    }
  }
}
