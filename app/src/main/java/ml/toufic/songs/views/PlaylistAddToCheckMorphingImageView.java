package ml.toufic.songs.views;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ml.toufic.songs.R;

public class PlaylistAddToCheckMorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawable mPlaylistAddToCheck;
  private AnimatedVectorDrawable mPlaylistCheckToAdd;
  private boolean mIsShowingCheck;
  public PlaylistAddToCheckMorphingImageView(Context context) {
    super(context);
    init();
  }

  public PlaylistAddToCheckMorphingImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PlaylistAddToCheckMorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init(){
    mIsShowingCheck = false;
    mPlaylistAddToCheck =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.playlist_add_to_check);
    mPlaylistCheckToAdd =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.playlist_check_to_add);
    setImageDrawable(mPlaylistAddToCheck);
    setColorFilter(getResources().getColor(R.color.grey_67));
  }

  public void switchState(){
    AnimatedVectorDrawable drawable = mIsShowingCheck ? mPlaylistCheckToAdd : mPlaylistAddToCheck;
    setImageDrawable(drawable);
    drawable.start();
    mIsShowingCheck = !mIsShowingCheck;
  }

  public void showCheck() {
    if(!mIsShowingCheck) {
      AnimatedVectorDrawable drawable = mPlaylistAddToCheck;
      setImageDrawable(drawable);
      drawable.start();
      // add -> check. It's now showing check
      mIsShowingCheck = true;
    }
  }

  public void showAdd() {
    AnimatedVectorDrawable drawable = mPlaylistCheckToAdd;
    setImageDrawable(drawable);
    drawable.start();
    // check -> add. It's now showing add
    mIsShowingCheck = false;
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