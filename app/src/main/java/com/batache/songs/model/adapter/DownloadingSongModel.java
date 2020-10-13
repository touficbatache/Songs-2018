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
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.SongUtils;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Status;

@EpoxyModelClass(layout = R.layout.item_song_downloading)
public abstract class DownloadingSongModel extends EpoxyModelWithHolder<DownloadingSongModel.DownloadingSongHolder> {

  @EpoxyAttribute
  Download download;

  @Override
  public void bind(@NonNull DownloadingSongHolder holder) {
    super.bind(holder);

    Song song = SongUtils.getSong(download);
    holder.songTitle.setText(song.getTitle());

    Context context = holder.downloadStatus.getContext();
    if (download.getStatus() == Status.DOWNLOADING) {
      holder.downloadStatus.setText(String.format(context.getString(R.string.download_status_downloading), download.getProgress()));
    } else {
      holder.downloadStatus.setText(context.getString(R.string.download_status_queued));
    }
  }

  class DownloadingSongHolder extends EpoxyHolder {

    View itemView;
    TextView songTitle, downloadStatus;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      songTitle = itemView.findViewById(R.id.song_title);
      downloadStatus = itemView.findViewById(R.id.download_status);
    }
  }

}
