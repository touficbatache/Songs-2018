package ml.toufic.songs.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import ml.toufic.songs.R;
import ml.toufic.songs.util.ImageLoader;

public class PlayerCoverModel extends EpoxyModelWithHolder<PlayerCoverModel.PlayerCoverHolder> {

  private Context mContext;
  private String id = "temp";
  private String coverArtUrl;

  public PlayerCoverModel(Context context, PlayableSong playableSong) {
    mContext = context;
    id = playableSong.getId();
    coverArtUrl = playableSong.getCoverArt();
  }

  public PlayerCoverModel(Context context, String coverArtUrl) {
    mContext = context;
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
    ImageLoader.loadImage(mContext, holder.coverArt, new ImageLoader.ImageConfigurations(coverArtUrl, id, ImageLoader.ImageConfigurations.PNG));
  }

  class PlayerCoverHolder extends EpoxyHolder {

    SimpleDraweeView coverArt;

    @Override
    protected void bindView(View itemView) {
      coverArt = (SimpleDraweeView) itemView;
    }
  }
}