package com.batache.songs.model.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.util.PlaylistImporter;
import com.batache.songs.util.events.ImportEvent;

import static com.batache.songs.util.events.ImportEvent.Action.IMPORT_PROGRESS_CHANGED;

@EpoxyModelClass(layout = R.layout.item_song_downloading)
public abstract class ImportingPlaylistModel extends EpoxyModelWithHolder<ImportingPlaylistModel.ImportingPlaylistHolder> {

  @EpoxyAttribute
  String playlistTitle;

  @EpoxyAttribute
  ImportEvent importEvent;

  @Override
  public void bind(@NonNull ImportingPlaylistHolder holder) {
    super.bind(holder);

    holder.playlistTitle.setText(playlistTitle);



    Context context = holder.importStatus.getContext();
    if(importEvent.action != null && importEvent.action == IMPORT_PROGRESS_CHANGED) {
      holder.importStatus.setText(String.format(context.getString(R.string.import_status_importing), importEvent.progress));
    } else {
      holder.importStatus.setText(R.string.import_status_queued);
    }
  }

  class ImportingPlaylistHolder extends EpoxyHolder {

    View itemView;
    TextView playlistTitle, importStatus;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      playlistTitle = itemView.findViewById(R.id.playlist_title);
      importStatus = itemView.findViewById(R.id.import_status);
    }
  }

}
