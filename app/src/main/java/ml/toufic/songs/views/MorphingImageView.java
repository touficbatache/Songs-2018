package ml.toufic.songs.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ml.toufic.songs.R;

public class MorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawable mFromIconAVD;
  private AnimatedVectorDrawable mToIconAVD;
  int mCurrentFillColor;
  private int mFromColor;
  private int mToColor;
  private boolean mIsShowingToIcon;

  public MorphingImageView(Context context) {
    this(context, null);
  }

  public MorphingImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if(attrs != null) {
      init(attrs);
    }
  }

  private void init(AttributeSet attrs){
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MorphingImageView);

    mFromIconAVD = (AnimatedVectorDrawable) ta.getDrawable(R.styleable.MorphingImageView_fromIconAVD);
    mToIconAVD = (AnimatedVectorDrawable) ta.getDrawable(R.styleable.MorphingImageView_toIconAVD);

    mFromColor = ta.getColor(R.styleable.MorphingImageView_fromColor, getResources().getColor(R.color.grey_67));
    mToColor = ta.getColor(R.styleable.MorphingImageView_fromColor, getResources().getColor(R.color.colorAccent));

    if(!ta.getBoolean(R.styleable.MorphingImageView_isColorChanging, false)) {
      int[] androidSet = {
        android.R.attr.tint, // index 0
      };
      TypedArray a = getContext().obtainStyledAttributes(attrs, androidSet);
      mFromColor = a.getColor(0, getResources().getColor(R.color.grey_67));
      mToColor = a.getColor(0, getResources().getColor(R.color.grey_67));
      a.recycle();
    }
    mCurrentFillColor = mFromColor;

    ta.recycle();

    mIsShowingToIcon = false;
    setImageDrawable(mFromIconAVD);
  }

  public void switchState(){
    if(mIsShowingToIcon) {
      showFromIcon();
    } else {
      showToIcon();
    }
  }

  public void showToIcon() {
    AnimatedVectorDrawable drawable = mFromIconAVD;
    setImageDrawable(drawable);
    drawable.start();
    // from -> to. It's now showing to
    mIsShowingToIcon = true;

    animateFromColorToColor(mFromColor, mToColor);
  }

  public void showFromIcon() {
    AnimatedVectorDrawable drawable = mToIconAVD;
    setImageDrawable(drawable);
    drawable.start();
    // to -> from. It's now showing from
    mIsShowingToIcon = false;

    animateFromColorToColor(mToColor, mFromColor);
  }

  private void animateFromColorToColor(int fromColor, int toColor) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
    colorAnimation.setDuration(300); // milliseconds
    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        setColorFilter((int) animator.getAnimatedValue());
        mCurrentFillColor = (int) animator.getAnimatedValue();
      }
    });
    colorAnimation.start();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if(enabled) {
      setColorFilter(mCurrentFillColor);
    } else {
      setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }
}