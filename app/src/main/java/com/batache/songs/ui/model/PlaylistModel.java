package com.batache.songs.ui.model;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Playlist;
import com.batache.songs.ui.fragments.MyMusicFragmentDirections;
import com.batache.songs.util.CodeUtils;

@EpoxyModelClass(layout = R.layout.item_playlist)
public abstract class PlaylistModel extends EpoxyModelWithHolder<PlaylistModel.PlaylistHolder> {

  @EpoxyAttribute
  Context context;

  @EpoxyAttribute
  @DrawableRes
  int icon = -1;

  @EpoxyAttribute
  Playlist playlist;

  @Override
  public void bind(@NonNull PlaylistHolder holder) {
    super.bind(holder);

    holder.itemView.setOnClickListener(view -> {
      MyMusicFragmentDirections.PlaylistAction playlistAction = MyMusicFragmentDirections.playlistAction(playlist.getTitle(), playlist.getId());
      Navigation.findNavController(view).navigate(playlistAction);
    });

    if (!CodeUtils.isEmpty(icon)) {
      holder.icon.setImageResource(icon);
    } else {
      holder.icon.setImageResource(R.drawable.ic_playlist_play_24dp);
    }

    holder.title.setText(playlist.getTitle());

    int songsCount = DatabaseHelper.getInstance(context).getSongsForPlaylist(playlist.getId()).size();
    holder.songsCount.setText(String.format(context.getString(R.string.NUMBER_songs), songsCount));
  }

  @Override
  public void unbind(@NonNull PlaylistHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class PlaylistHolder extends EpoxyHolder {

    View itemView;
    ImageView icon;
    TextView title, songsCount;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      icon = itemView.findViewById(R.id.playlist_icon);
      title = itemView.findViewById(R.id.playlist_title);
      songsCount = itemView.findViewById(R.id.playlist_songs_count);
    }
  }

}
