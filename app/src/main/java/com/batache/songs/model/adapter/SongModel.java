package com.batache.songs.model.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.app.player.PlayerManager;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.CodeUtils;

@EpoxyModelClass(layout = R.layout.item_song)
public abstract class SongModel extends EpoxyModelWithHolder<SongModel.SongHolder> {

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

    holder.songTitle.setText(song.getTitle());

    if (showArtist && !CodeUtils.isEmpty(song.getArtist())) {
      holder.songArtist.setVisibility(View.VISIBLE);
      holder.songArtist.setText(song.getArtist());
    } else {
      holder.songArtist.setVisibility(View.GONE);
    }

    if (CodeUtils.isEmpty(playlistId)) {
      holder.itemView.setOnClickListener(view -> PlayerManager.getInstance().playSong(song));
    } else {
      holder.itemView.setOnClickListener(view -> PlayerManager.getInstance().playSong(playlistId, index));
    }
  }

  @Override
  public void unbind(@NonNull SongHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class SongHolder extends EpoxyHolder {

    View itemView;
    ImageView icon;
    TextView songTitle, songArtist;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      icon = itemView.findViewById(R.id.song_icon);
      songTitle = itemView.findViewById(R.id.song_title);
      songArtist = itemView.findViewById(R.id.song_artist);
    }
  }

}
