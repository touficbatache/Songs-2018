package com.batache.songs.app.playqueue;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.app.player.PlayerManager;
import com.batache.songs.util.TextUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class PlayingQueueFragment extends BottomSheetDialogFragment {

  private static final String TAG = "PlayingQueueFragment : ";
  private View mRootView;
  private Context mContext;

  private List<Song> playingQueue;
  private LastPlayed lastPlayed;

  private PlayingQueueAdapter mAdapter;
  private RecyclerView mRecyclerView;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mContext = getContext();

    playingQueue = PlayerManager.getInstance().getPlayqueue();
    lastPlayed = PlayerManager.getInstance().getLastPlayed();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_playing_queue, container, false);

    mAdapter = new PlayingQueueAdapter();
    mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    mRecyclerView.setAdapter(mAdapter);

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    for (Song song : playingQueue) {
      String currentSongId = playingQueue.get(lastPlayed.getSongIndex()).getId();
      if (song.getId().equals(currentSongId)) {
        mAdapter.addCurrentSongModel(song);
      } else {
        mAdapter.addPlayingQueueSongModel(song);
      }
    }
  }

  class PlayingQueueAdapter extends EpoxyAdapter {
    void addPlayingQueueSongModel(Song song) {
      addModel(new PlayingQueueSongModel(song, false));
    }

    void addCurrentSongModel(Song song) {
      addModel(new PlayingQueueSongModel(song, true));
    }

    void clear() {
      removeAllModels();
    }
  }

  private class PlayingQueueSongModel extends EpoxyModelWithHolder<PlayingQueueSongHolder> {

    private int songIndex = -1;
    private String songTitle;
    private boolean isCurrentSong;

    private View.OnClickListener onClickListener;

    PlayingQueueSongModel(final Song song, boolean isCurrentSong) {
      songIndex = playingQueue.indexOf(song);
      songTitle = song.getTitle();
      this.isCurrentSong = isCurrentSong;
      onClickListener = view -> {
        PlayerManager.getInstance().playSong(lastPlayed.getPlaylistId(), songIndex);
        dismiss();
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

      if (isCurrentSong) {
        int[] attrs = new int[]{android.R.attr.textColorPrimary};
        TypedArray a = mContext.getTheme().obtainStyledAttributes(R.style.Theme_Songs, attrs);
        int currentSongColor = a.getColor(0, Color.BLACK);
        a.recycle();

        holder.mSongIndex.setTextColor(currentSongColor);
        holder.mSongTitle.setTextColor(currentSongColor);
        holder.mSongTitle.setSelected(true);
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
    ImageView mSongNowPlaying;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      mSongIndex = itemView.findViewById(R.id.song_index);
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongNowPlaying = itemView.findViewById(R.id.song_now_playing);
    }
  }
}
