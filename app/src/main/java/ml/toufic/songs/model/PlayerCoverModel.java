package ml.toufic.songs.model;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.graphics.drawable.RippleDrawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import ml.toufic.songs.R;
import ml.toufic.songs.util.ImageLoader;

public class PlayerCoverModel extends EpoxyModelWithHolder<PlayerCoverModel.PlayerCoverHolder> {

  private Context mContext;
  private String id = "temp";
  private String coverArtUrl;

  private PlayerCoverCallback playerCoverCallback;

  private LinearLayout seekPreviousBtn, seekNextBtn;

  private boolean isStreaming = false;

  public PlayerCoverModel(Context context, PlayableSong playableSong, PlayerCoverCallback playerCoverCallback) {
    mContext = context;
    id = playableSong.getId();
    coverArtUrl = playableSong.getCoverArt();
    this.playerCoverCallback = playerCoverCallback;
  }

  public PlayerCoverModel(Context context, String coverArtUrl, PlayerCoverCallback playerCoverCallback) {
    mContext = context;
    this.coverArtUrl = coverArtUrl;
    this.playerCoverCallback = playerCoverCallback;
    isStreaming = true;
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

    if(!isStreaming) {
      holder.lyricsBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          playerCoverCallback.onLyricsBtnClick();
        }
      });
    } else {
      holder.lyricsBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          MaterialDialog.Builder lyricsNotAvailable = new MaterialDialog.Builder(mContext);
          lyricsNotAvailable.title("Lyrics not available");
          lyricsNotAvailable.content("Lyrics are not available un-downloaded songs. Download this song to see its lyrics.");
          lyricsNotAvailable.cancelable(true);
          lyricsNotAvailable.positiveText("Ok");
          lyricsNotAvailable.show();
        }
      });
    }

    seekPreviousBtn = holder.seekPreviousBtn;
    seekNextBtn = holder.seekNextBtn;

    seekPreviousBtn.setOnTouchListener(new View.OnTouchListener() {
      private GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
          playerCoverCallback.onSeekPrevious(seekPreviousBtn);
          seekPreviousBtn.setAlpha(1);
          new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                seekPreviousBtn.setAlpha(0);
              }
            }, 700);
          return super.onDoubleTap(e);
        }
      });

      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return true;
      }
    });

    seekNextBtn.setOnTouchListener(new View.OnTouchListener() {
      private GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
          playerCoverCallback.onSeekNext(seekNextBtn);
          seekNextBtn.setAlpha(1);
          new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                seekNextBtn.setAlpha(0);
              }
            }, 700);
          return super.onDoubleTap(e);
        }
      });

      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return true;
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
    LinearLayout seekPreviousBtn, seekNextBtn;

    @Override
    protected void bindView(View itemView) {
      coverArt = itemView.findViewById(R.id.player_song_cover_art);
      lyricsBtn = itemView.findViewById(R.id.lyrics_btn);
      seekPreviousBtn = itemView.findViewById(R.id.seek_previous);
      seekNextBtn = itemView.findViewById(R.id.seek_next);
    }
  }

  public interface PlayerCoverCallback {
    void onLyricsBtnClick();

    void onSeekPrevious(LinearLayout view);

    void onSeekNext(LinearLayout view);
  }
}