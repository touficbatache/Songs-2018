package ml.toufic.songs.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

  private PlayerCoverLyricsCallback playerCoverLyricsCallback;

  public PlayerCoverModel(Context context, PlayableSong playableSong, PlayerCoverLyricsCallback playerCoverLyricsCallback) {
    mContext = context;
    id = playableSong.getId();
    coverArtUrl = playableSong.getCoverArt();
    this.playerCoverLyricsCallback = playerCoverLyricsCallback;
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

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void bind(@NonNull PlayerCoverHolder holder) {
    super.bind(holder);
    ImageLoader.loadImage(mContext, holder.coverArt, new ImageLoader.ImageConfigurations(coverArtUrl, id, ImageLoader.ImageConfigurations.PNG));

    holder.lyricsBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        playerCoverLyricsCallback.onLyricsBtnClick();
      }
    });
  }

  @Override
  public void unbind(@NonNull PlayerCoverHolder holder) {
    super.unbind(holder);
    holder.lyricsBtn.setOnClickListener(null);
  }

  class PlayerCoverHolder extends EpoxyHolder {

    SimpleDraweeView coverArt;
    FloatingActionButton lyricsBtn;

    @Override
    protected void bindView(View itemView) {
      coverArt = itemView.findViewById(R.id.player_song_cover_art);
      lyricsBtn = itemView.findViewById(R.id.lyrics_btn);
    }
  }

  public interface PlayerCoverLyricsCallback {
    void onLyricsBtnClick();
  }
}