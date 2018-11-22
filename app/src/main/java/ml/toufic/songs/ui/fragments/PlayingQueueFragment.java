package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import ml.toufic.songs.util.TextUtils;

public class PlayingQueueFragment extends BottomSheetDialogFragment {

  private static final String TAG = "PlayingQueueFragment : ";
  private MainActivity mActivity;
  private View mRootView;
  private Context mContext;

  private PlayingQueueAdapter mAdapter;
  private RecyclerView mRecyclerView;
  private List<PlayableSong> playingQueueSongs = new ArrayList<>();
  private String currentSongId;
  private String playlistId;

  public static PlayingQueueFragment newInstance(List<PlayableSong> playingQueueSongs, String currentSongId, String playlistId) {
    PlayingQueueFragment fragment = new PlayingQueueFragment();
    Bundle args = new Bundle();
    args.putString("playingQueueSongs", new Gson().toJson(playingQueueSongs));
    args.putString("currentSongId", currentSongId);
    args.putString("playlistId", playlistId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    mContext = getContext();
    Bundle bundle = getArguments();
    playingQueueSongs = new Gson().fromJson(bundle.getString("playingQueueSongs"), new TypeToken<List<PlayableSong>>(){}.getType());
    currentSongId = bundle.getString("currentSongId");
    playlistId = bundle.getString("playlistId");
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
      if(playableSong.getId().equals(currentSongId)) {
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
    private int songIndex = -1;
    private int songDurationInMs = 0;
    private String songTitle;
    private boolean isCurrentSong;

    private View.OnClickListener onClickListener;

    PlayingQueueSongModel(final PlayableSong playableSong, boolean isCurrentSong) {
      song = playableSong;
      songIndex = playingQueueSongs.indexOf(playableSong);
      songDurationInMs = song.getDurationInMs();
      songTitle = song.getTitle();
      this.isCurrentSong = isCurrentSong;
      onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mActivity.loadSong(songIndex, playlistId);
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
      return R.layout.item_song_queue;
    }

    @Override
    public void bind(@NonNull final PlayingQueueSongHolder holder) {
      super.bind(holder);

      holder.mSongIndex.setText(TextUtils.formatIntoTwoNumbers(songIndex + 1));
      holder.mSongTitle.setText(songTitle);
      holder.mSongDuration.setText(TextUtils.formatMilliseconds(mContext, songDurationInMs));

      if(isCurrentSong) {
        int[] attrs = new int[] { android.R.attr.textColorPrimary };
        TypedArray a = mContext.getTheme().obtainStyledAttributes(R.style.AppTheme, attrs);
        int currentSongColor = a.getColor(0, Color.BLACK);
        a.recycle();

        holder.mSongIndex.setTextColor(currentSongColor);
        holder.mSongTitle.setTextColor(currentSongColor);
        holder.mSongTitle.setSelected(true);
        holder.mSongDuration.setTextColor(currentSongColor);
        holder.mSongNowPlaying.setVisibility(View.VISIBLE);
        return;
      }

      holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public void unbind(@NonNull PlayingQueueSongHolder holder) {
      super.unbind(holder);

      holder.itemView.setOnClickListener(null);
    }
  }

  private class PlayingQueueSongHolder extends EpoxyHolder {

    View itemView;
    TextView mSongIndex;
    TextView mSongTitle;
    TextView mSongDuration;
    ImageView mSongNowPlaying;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongIndex = itemView.findViewById(R.id.song_index);
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongDuration = itemView.findViewById(R.id.song_duration);
      mSongNowPlaying = itemView.findViewById(R.id.song_now_playing);
    }
  }
}
