package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.ui.activities.MainActivity;

public class PlayingQueueFragment extends BottomSheetDialogFragment {

  private static final String TAG = "PlayingQueueFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private PlayingQueueAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private List<PlayableSong> playingQueueSongs = new ArrayList<>();
  private int currentSongIndex = -1;

  public static PlayingQueueFragment newInstance(List<PlayableSong> playingQueueSongs, int currentSongIndex) {
    PlayingQueueFragment fragment = new PlayingQueueFragment();
    Bundle args = new Bundle();
    args.putString("playingQueueSongs", new Gson().toJson(playingQueueSongs));
    args.putInt("currentSongIndex", currentSongIndex);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    mContext = getContext();
    playingQueueSongs = new Gson().fromJson(getArguments().getString("playingQueueSongs"), new TypeToken<List<PlayableSong>>(){}.getType());
    currentSongIndex = getArguments().getInt("currentSongIndex");
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.layout_recyclerview, container, false);

    mAdapter = new PlayingQueueAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    for(PlayableSong playableSong : playingQueueSongs) {
      if(playableSong.getIndexInQueue() == currentSongIndex) {
        mAdapter.addCurrentSongModel(playableSong);
      } else {
        mAdapter.addPlayingQueueSongModel(playableSong);
      }
    }
  }

  class PlayingQueueAdapter extends EpoxyAdapter {
    void addPlayingQueueSongModel(PlayableSong playableSong) {
      addModel(new PlayingQueueSongModel(playableSong, false));
    }

    void addCurrentSongModel(PlayableSong playableSong) {
      addModel(new PlayingQueueSongModel(playableSong, true));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class PlayingQueueSongModel extends EpoxyModelWithHolder<PlayingQueueSongHolder> {

    private PlayableSong song;
    private String songId;
    private File songPath;
    private String songTitle;
    private String songArtist;
    private String songCoverArt;
    private boolean isCurrentSong;

    private View.OnClickListener onClickListener;

    PlayingQueueSongModel(final PlayableSong playableSong, boolean isCurrentSong) {
      song = playableSong;
      songId = song.getId();
      songPath = song.getPath();
      songTitle = song.getTitle();
      songArtist = song.getArtist();
      songCoverArt = song.getCoverArt();
      this.isCurrentSong = isCurrentSong;
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mActivity.loadSong(playableSong);
          dismiss();
        }
      };
    }

    @Override
    protected PlayingQueueSongHolder createNewHolder() {
      return new PlayingQueueSongHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_song_my_music;
    }

    @Override
    public void bind(@NonNull final PlayingQueueSongHolder holder) {
      super.bind(holder);

      if(isCurrentSong) {
        holder.mSongIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_equalizer_24dp));
      }

      holder.itemView.setOnClickListener(onClickListener);
      holder.mSongTitle.setText(songTitle);
      holder.mSongArtist.setText(songArtist);
    }

    @Override
    public void unbind(@NonNull PlayingQueueSongHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
    }
  }

  private class PlayingQueueSongHolder extends EpoxyHolder {

    View itemView;
    ImageView mSongIcon; //for multiple selection #22
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
