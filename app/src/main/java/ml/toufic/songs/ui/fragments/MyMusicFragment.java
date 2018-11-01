package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.ui.activities.MainActivity;
import ml.toufic.songs.R;

import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static ml.toufic.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class MyMusicFragment extends Fragment {

  private static final String TAG = "MyMusicFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private MyMusicAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRefreshLayout;
  private FloatingActionButton mNewPlaylistBtn;
  private List<Playlist> playlists = new ArrayList<>();

  private MyMusicAnyPlaylistFragment myMusicAnyPlaylistFragment = new MyMusicAnyPlaylistFragment();

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

    mActivity.getSupportActionBar().setTitle(R.string.my_music);
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
        loadMyMusicPlaylists();
      }
    });

    mNewPlaylistBtn = mRootView.findViewById(R.id.new_playlist_btn);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        MaterialDialog.Builder newPlaylistDialog = new MaterialDialog.Builder(mContext);
        newPlaylistDialog.title(R.string.new_playlist);
        newPlaylistDialog.inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        newPlaylistDialog.alwaysCallInputCallback();
        newPlaylistDialog.input(getString(R.string.playlist_name), null, false, new MaterialDialog.InputCallback() {
          @Override
          public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            if(Objects.equals(input.toString().trim(), "")) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            }
          }
        });
        newPlaylistDialog.cancelable(true);
        newPlaylistDialog.positiveText(R.string.create);
        newPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            List<Playlist> playlists = mActivity.getAllPlaylists();
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
            mActivity.insertPlaylist(new Playlist(String.valueOf(Integer.valueOf(playlists.get(playlists.size() - 1).getId()) + 1), dialog.getInputEditText().getText().toString().trim(), new Date()));
            Toast.makeText(mContext, R.string.playlist_created_successfully, Toast.LENGTH_SHORT).show();
          }
        });
        newPlaylistDialog.negativeText(R.string.cancel);
        newPlaylistDialog.show();

      }
    });

    loadMyMusicPlaylists();
  }

  public void loadMyMusicPlaylists() {
    if(mActivity != null) {
      playlists = mActivity.getAllPlaylists();

      mAdapter.clear();

      mAdapter.addSubheader(getString(R.string.playlists));

      for(Playlist playlist : playlists) {
        if(playlist.getId().equals(PLAYLIST_LIKES_ID)) {
          mAdapter.addPlaylistModel(playlist, R.drawable.ic_favorite_24dp);
        } else if(playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
          mAdapter.addPlaylistModel(playlist, R.drawable.ic_cloud_download_24dp);
        } else {
          mAdapter.addPlaylistModel(playlist);
        }
      }

      mSwipeRefreshLayout.setRefreshing(false);
    }
  }

  public void updateSongsInPlaylist() {
    myMusicAnyPlaylistFragment.loadSongsInPlaylist();
  }

  class MyMusicAdapter extends EpoxyAdapter {
    void addSubheader(String title) {
      addModel(new SubheaderModel(title));
    }

    void addPlaylistModel(Playlist playlist) {
      addModel(new PlaylistModel(playlist));
    }

    void addPlaylistModel(Playlist playlist, int playlistIcon) {
      addModel(new PlaylistModel(playlist, playlistIcon));
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
      return R.layout.item_subheader_my_music;
    }

    @Override
    public void bind(@NonNull SubheaderHolder holder) {
      super.bind(holder);

      holder.subheader.setText(title);
    }
  }

  private class PlaylistModel extends EpoxyModelWithHolder<PlaylistHolder> {

    private Playlist playlist;
    private String playlistId;
    private String playlistTitle;
    private Date playlistCreationDate;
    private int playlistSongsCount;

    private View.OnClickListener onClickListener;

    private int playlistIcon = -1;

    PlaylistModel(final Playlist playlist) {
      this.playlist = playlist;
      playlistId = playlist.getId();
      playlistTitle = playlist.getTitle();
      playlistCreationDate = playlist.getCreationDate();
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          myMusicAnyPlaylistFragment = MyMusicAnyPlaylistFragment.newInstance(playlistTitle, playlistId);
          mActivity.pushFragment(myMusicAnyPlaylistFragment);
        }
      };
      this.playlistIcon = -1;
      playlistSongsCount = mActivity.getSongsForPlaylist(playlistId).size();
    }

    PlaylistModel(final Playlist playlist, int playlistIcon) {
      this.playlist = playlist;
      playlistId = playlist.getId();
      playlistTitle = playlist.getTitle();
      playlistCreationDate = playlist.getCreationDate();
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          myMusicAnyPlaylistFragment = MyMusicAnyPlaylistFragment.newInstance(playlistTitle, playlistId);
          mActivity.pushFragment(myMusicAnyPlaylistFragment);
        }
      };
      this.playlistIcon = playlistIcon;
      playlistSongsCount = mActivity.getSongsForPlaylist(playlistId).size();
    }

    @Override
    protected PlaylistHolder createNewHolder() {
      return new PlaylistHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_playlist_my_music;
    }

    @Override
    public void bind(@NonNull final PlaylistHolder holder) {
      super.bind(holder);

      holder.itemView.setOnClickListener(onClickListener);
      holder.mPlaylistIcon.setImageResource(R.drawable.ic_playlist_play_24dp);
      if(playlistIcon != -1) {
        holder.mPlaylistIcon.setImageResource(playlistIcon);
      }
      holder.mPlaylistTitle.setText(playlistTitle);
      holder.mPlaylistSongsCount.setText(String.format(getString(R.string.NUMBER_songs), playlistSongsCount));

    }

    @Override
    public void unbind(@NonNull PlaylistHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
    }
  }

  protected class SubheaderHolder extends EpoxyHolder {

    TextView subheader;

    @Override
    protected void bindView(View itemView) {
      this.subheader = (TextView) itemView;
    }
  }

  private class PlaylistHolder extends EpoxyHolder {

    View itemView;
    ImageView mPlaylistIcon;
    TextView mPlaylistTitle;
    TextView mPlaylistSongsCount;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mPlaylistIcon = itemView.findViewById(R.id.playlist_icon);
      mPlaylistTitle = itemView.findViewById(R.id.playlist_title);
      mPlaylistSongsCount = itemView.findViewById(R.id.playlist_songs_count);
    }
  }

}
