package com.batache.songs.ui.model;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import com.batache.songs.R;
import com.batache.songs.model.Song;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.PlayerHelper;

@EpoxyModelClass(layout = R.layout.item_song)
public abstract class SongModel extends EpoxyModelWithHolder<SongModel.SongHolder> {

  @EpoxyAttribute
  Context context;

  // Stream song
  @EpoxyAttribute
  Song song;

  // Downloaded song
  @EpoxyAttribute
  String playlistId;

  @EpoxyAttribute
  int index;

  @EpoxyAttribute
  boolean showArtist = true;

  @Override
  public void bind(@NonNull SongHolder holder) {
    super.bind(holder);

    if(CodeUtils.isEmpty(playlistId)) {
      holder.itemView.setOnClickListener(view -> PlayerHelper.getInstance().playSong(song));
    } else {
      holder.itemView.setOnClickListener(view -> PlayerHelper.getInstance().playSong(playlistId, index));
    }

    holder.mSongTitle.setText(song.getTitle());

    if (showArtist && !CodeUtils.isEmpty(song.getArtist())) {
      holder.mSongArtist.setVisibility(View.VISIBLE);
      holder.mSongArtist.setText(song.getArtist());
    } else {
      holder.mSongArtist.setVisibility(View.GONE);
    }
  }

  @Override
  public void unbind(@NonNull SongHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class SongHolder extends EpoxyHolder {

    View itemView;
    TextView mSongTitle, mSongArtist;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      mSongTitle = itemView.findViewById(R.id.song_title);
      mSongArtist = itemView.findViewById(R.id.song_artist);
    }
  }

}
