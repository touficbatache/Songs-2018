package ml.toufic.songs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

import ml.toufic.songs.R;

public class PlayToPauseMorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawable mPlayToPause;
  private AnimatedVectorDrawable mPauseToPlay;
  private boolean mIsShowingPause;
  public PlayToPauseMorphingImageView(Context context) {
    this(context, null);
  }

  public PlayToPauseMorphingImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayToPauseMorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();

    if(attrs != null) {
      int[] set = {
        android.R.attr.tint, // index 0
      };
      TypedArray a = context.obtainStyledAttributes(attrs, set);
      int fill = a.getColor(0,
        getResources().getColor(R.color.grey_67));
      setColorFilter(fill);
      a.recycle();
    }
  }

  private void init(){
    mIsShowingPause = false;
    mPlayToPause =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.play_to_pause);
    mPauseToPlay =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.pause_to_play);
    setImageDrawable(mPlayToPause);
  }

  public void switchState(){
    AnimatedVectorDrawable drawable = mIsShowingPause ? mPauseToPlay : mPlayToPause;
    setImageDrawable(drawable);
    drawable.start();
    mIsShowingPause = !mIsShowingPause;
  }

  public void play() {
    AnimatedVectorDrawable drawable = mPlayToPause;
    setImageDrawable(drawable);
    drawable.start();
    // play -> pause. It's now showing pause
    mIsShowingPause = true;
  }

  public void pause() {
    AnimatedVectorDrawable drawable = mPauseToPlay;
    setImageDrawable(drawable);
    drawable.start();
    // pause -> play. It's now showing play
    mIsShowingPause = false;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if(enabled) {
      setColorFilter(getResources().getColor(R.color.grey_67));
    } else {
      setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }
}