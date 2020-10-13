package com.batache.songs.model.adapter;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;
import com.batache.songs.util.CodeUtils;
import com.facebook.drawee.view.SimpleDraweeView;

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

  @EpoxyAttribute
  boolean showYoutubeIcon = false;

  @Override
  public void bind(@NonNull SongHolder holder) {
    super.bind(holder);

    holder.cover.setImageURI(Uri.parse(song.getCoverArt()));

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

    holder.songSourceYoutube.setVisibility(View.GONE);
    holder.songKaraokeAvailable.setVisibility(View.GONE);

    if(showYoutubeIcon && song.getSongSource() == Song.SongSource.youtube) {
      holder.songSourceYoutube.setVisibility(View.VISIBLE);
    }

    if(song.getLyricsUrl() != null && !song.getLyricsUrl().equals("")) {
      holder.songKaraokeAvailable.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void unbind(@NonNull SongHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class SongHolder extends EpoxyHolder {

    View itemView;
    SimpleDraweeView cover;
    TextView songTitle, songArtist;
    ImageView songSourceYoutube, songKaraokeAvailable;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      cover = itemView.findViewById(R.id.iv_cover);
      songTitle = itemView.findViewById(R.id.song_title);
      songArtist = itemView.findViewById(R.id.song_artist);
      songSourceYoutube = itemView.findViewById(R.id.song_source_youtube);
      songKaraokeAvailable = itemView.findViewById(R.id.song_karaoke_available);
    }
  }

}
