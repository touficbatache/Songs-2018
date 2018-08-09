package ml.toufic.songs.views;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

import ml.toufic.songs.R;

public class PlayToPauseMorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawable mPlayToPause;
  private AnimatedVectorDrawable mPauseToPlay;
  private boolean mIsShowingPause;
  public PlayToPauseMorphingImageView(Context context) {
    super(context);
    init();
  }

  public PlayToPauseMorphingImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PlayToPauseMorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init(){
    mIsShowingPause = false;
    mPlayToPause =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.play_to_pause);
    mPauseToPlay =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.pause_to_play);
    setImageDrawable(mPlayToPause);
    setColorFilter(getResources().getColor(R.color.grey_67));
  }

  public void morph(){
    AnimatedVectorDrawable drawable = mIsShowingPause ? mPauseToPlay : mPlayToPause;
    setImageDrawable(drawable);
    drawable.start();
    mIsShowingPause = !mIsShowingPause;
  }

  public void play() {
    if(!mIsShowingPause) {
      AnimatedVectorDrawable drawable = mPlayToPause;
      setImageDrawable(drawable);
      drawable.start();
      // play -> pause. It's now showing pause
      mIsShowingPause = true;
    }
  }

  public void pause() {
    AnimatedVectorDrawable drawable = mPauseToPlay;
    setImageDrawable(drawable);
    drawable.start();
    // pause -> play. It's now showing play
    mIsShowingPause = false;
  }

}