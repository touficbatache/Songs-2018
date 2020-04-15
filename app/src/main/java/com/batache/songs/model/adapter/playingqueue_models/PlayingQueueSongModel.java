package com.batache.songs.model.adapter.playingqueue_models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.util.TextUtils;

@EpoxyModelClass(layout = R.layout.item_song_queue)
public abstract class PlayingQueueSongModel extends EpoxyModelWithHolder<PlayingQueueSongModel.PlayingQueueSongHolder> {

  @EpoxyAttribute
  int songIndex = -1;

  @EpoxyAttribute
  String songTitle;

  @EpoxyAttribute
  View.OnClickListener onClickListener;

  @Override
  public void bind(@NonNull final PlayingQueueSongHolder holder) {
    super.bind(holder);

    holder.mSongIndex.setText(TextUtils.formatIntoTwoNumbers(songIndex + 1));
    holder.mSongTitle.setText(songTitle);

    holder.itemView.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(@NonNull PlayingQueueSongHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class PlayingQueueSongHolder extends EpoxyHolder {

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