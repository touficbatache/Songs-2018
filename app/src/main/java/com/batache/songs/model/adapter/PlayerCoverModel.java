package com.batache.songs.model.adapter;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.util.CodeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import com.batache.songs.R;
import com.batache.songs.util.ImageLoader;

@EpoxyModelClass(layout = R.layout.item_player_cover)
public abstract class PlayerCoverModel extends EpoxyModelWithHolder<PlayerCoverModel.PlayerCoverHolder> {

  @EpoxyAttribute
  Context context;

  @EpoxyAttribute
  String songId;

  @EpoxyAttribute
  String coverArtUrl;

  @EpoxyAttribute
  PlayerCoverCallback callback;

  private LinearLayout seekPreviousBtn, seekPreviousText, seekNextBtn, seekNextText;

  private final Handler handler = new Handler();
  private Runnable seekPrevInvisible = new Runnable() {
    public void run() {
      seekPreviousBtn.setAlpha(0);
    }
  };
  private Runnable seekPrevTextInvisible = new Runnable() {
    public void run() {
      seekPreviousText
        .animate()
        .alpha(0)
        .setDuration(260)
        .setListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animator) {

          }

          @Override
          public void onAnimationEnd(Animator animator) {
            seekPreviousText.setAlpha(1);
          }

          @Override
          public void onAnimationCancel(Animator animator) {

          }

          @Override
          public void onAnimationRepeat(Animator animator) {

          }
        });
    }
  };
  private Runnable seekNextInvisible = new Runnable() {
    public void run() {
      seekNextBtn.setAlpha(0);
    }
  };
  private Runnable seekNextTextInvisible = new Runnable() {
    public void run() {
      seekNextText
        .animate()
        .alpha(0)
        .setDuration(260)
        .setListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
          seekNextText.setAlpha(1);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
      });
    }
  };

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void bind(@NonNull PlayerCoverHolder holder) {
    super.bind(holder);
    String imageName = CodeUtils.isEmpty(songId) ? "temp" : songId;
    ImageLoader.loadImage(context, holder.coverArt, new ImageLoader.ImageConfigurations(coverArtUrl, imageName, ImageLoader.ImageConfigurations.PNG));

    holder.lyricsBtn.setOnClickListener(view -> callback.onLyricsBtnClick());

    seekPreviousBtn = holder.seekPreviousBtn;
    seekPreviousText = holder.seekPreviousText;
    seekNextText = holder.seekNextText;
    seekNextBtn = holder.seekNextBtn;

    seekPreviousBtn.setOnTouchListener(new View.OnTouchListener() {
      private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
          handler.removeCallbacks(seekPrevTextInvisible);
          handler.removeCallbacks(seekPrevInvisible);
          callback.onSeekPrevious(seekPreviousBtn);
          seekPreviousBtn.setAlpha(1);
          handler.postDelayed(seekPrevTextInvisible, 250);
          handler.postDelayed(seekPrevInvisible, 500);
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
      private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
          handler.removeCallbacks(seekNextTextInvisible);
          handler.removeCallbacks(seekNextInvisible);
          callback.onSeekNext(seekNextBtn);
          seekNextBtn.setAlpha(1);
          handler.postDelayed(seekNextTextInvisible, 250);
          handler.postDelayed(seekNextInvisible, 500);
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
    LinearLayout seekPreviousBtn, seekPreviousText, seekNextBtn, seekNextText;

    @Override
    protected void bindView(View itemView) {
      coverArt = itemView.findViewById(R.id.player_song_cover_art);
      lyricsBtn = itemView.findViewById(R.id.lyrics_btn);
      seekPreviousBtn = itemView.findViewById(R.id.seek_previous);
      seekPreviousText = itemView.findViewById(R.id.seek_previous_text);
      seekNextBtn = itemView.findViewById(R.id.seek_next);
      seekNextText = itemView.findViewById(R.id.seek_next_text);
    }
  }

  public interface PlayerCoverCallback {
    void onLyricsBtnClick();

    void onSeekPrevious(LinearLayout view);

    void onSeekNext(LinearLayout view);
  }
}