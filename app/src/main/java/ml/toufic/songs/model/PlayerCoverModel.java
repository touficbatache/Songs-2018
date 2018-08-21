package ml.toufic.songs.model;

import android.support.annotation.NonNull;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import ml.toufic.songs.R;
import ml.toufic.songs.util.ImageLoader;

public class PlayerCoverModel extends EpoxyModelWithHolder<PlayerCoverModel.PlayerCoverHolder> {

  private String id = "temp";
  private String coverArtUrl;

  public PlayerCoverModel(PlayableSong playableSong) {
    id = playableSong.getId();
    coverArtUrl = playableSong.getCoverArt();
  }

  public PlayerCoverModel(String coverArtUrl) {
    this.coverArtUrl = coverArtUrl;
  }

  @Override
  protected PlayerCoverHolder createNewHolder() {
    return new PlayerCoverHolder();
  }

  @Override
  protected int getDefaultLayout() {
    return R.layout.item_player_cover;
  }

  @Override
  public void bind(@NonNull PlayerCoverHolder holder) {
    super.bind(holder);
    ImageLoader.loadImage(holder.coverArt, new ImageLoader.ImageConfigurations(coverArtUrl, id, ImageLoader.ImageConfigurations.PNG));
//    holder.coverArt.setImageURI(Uri.parse(coverArtUrl));
  }

  class PlayerCoverHolder extends EpoxyHolder {

    SimpleDraweeView coverArt;

    @Override
    protected void bindView(View itemView) {
      coverArt = (SimpleDraweeView) itemView;
    }
  }
}