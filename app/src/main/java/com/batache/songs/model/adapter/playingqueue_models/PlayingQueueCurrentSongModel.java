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

@EpoxyModelClass(layout = R.layout.item_current_song_queue)
public abstract class PlayingQueueCurrentSongModel extends EpoxyModelWithHolder<PlayingQueueCurrentSongModel.PlayingQueueSongHolder> {

  @EpoxyAttribute
  int songIndex = -1;

  @EpoxyAttribute
  String songTitle;

  @Override
  public void bind(@NonNull final PlayingQueueSongHolder holder) {
    super.bind(holder);

    holder.mSongIndex.setText(TextUtils.formatIntoTwoNumbers(songIndex + 1));
    holder.mSongTitle.setText(songTitle);

    holder.mSongTitle.setSelected(true);
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