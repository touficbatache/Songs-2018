package ml.toufic.songs.views;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ml.toufic.songs.R;

public class FavoriteBorderToFillMorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawable mFavoriteBorderToFill;
  private AnimatedVectorDrawable mFavoriteFillToBorder;
  private boolean mIsShowingFill;
  public FavoriteBorderToFillMorphingImageView(Context context) {
    super(context);
    init();
  }

  public FavoriteBorderToFillMorphingImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FavoriteBorderToFillMorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init(){
    mIsShowingFill = false;
    mFavoriteBorderToFill =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.favorite_border_to_fill);
    mFavoriteFillToBorder =
      (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.favorite_fill_to_border);
    setImageDrawable(mFavoriteBorderToFill);
    setColorFilter(getResources().getColor(R.color.grey_67));
  }

  public void switchState(){
    AnimatedVectorDrawable drawable = mIsShowingFill ? mFavoriteFillToBorder : mFavoriteBorderToFill;
    setImageDrawable(drawable);
    drawable.start();
    mIsShowingFill = !mIsShowingFill;
  }

  public void showFill() {
    if(!mIsShowingFill) {
      AnimatedVectorDrawable drawable = mFavoriteBorderToFill;
      setImageDrawable(drawable);
      drawable.start();
      // border -> fill. It's now showing fill
      mIsShowingFill = true;
    }
  }

  public void showBorder() {
    AnimatedVectorDrawable drawable = mFavoriteFillToBorder;
    setImageDrawable(drawable);
    drawable.start();
    // fill -> border. It's now showing border
    mIsShowingFill = false;
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