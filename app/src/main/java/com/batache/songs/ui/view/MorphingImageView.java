package com.batache.songs.ui.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.batache.songs.R;

public class MorphingImageView extends AppCompatImageView {

  private AnimatedVectorDrawableCompat mFirstToSecondAVD, mSecondToFirstAVD;
  int mCurrentFillColor;
  private int mFirstStateColor;
  private int mSecondStateColor;
  private boolean mIsShowingSecondState;

  public MorphingImageView(Context context) {
    this(context, null);
  }

  public MorphingImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MorphingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if (attrs != null) {
      init(attrs);
    }
  }

  private void init(AttributeSet attrs) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MorphingImageView);

    mFirstToSecondAVD = AnimatedVectorDrawableCompat.create(getContext(), ta.getResourceId(R.styleable.MorphingImageView_fromIconAVD, R.drawable.play_to_pause));
    mSecondToFirstAVD = AnimatedVectorDrawableCompat.create(getContext(), ta.getResourceId(R.styleable.MorphingImageView_toIconAVD, R.drawable.pause_to_play));

    mFirstStateColor = ta.getColor(R.styleable.MorphingImageView_fromColor, getResources().getColor(R.color.colorOnBackground));
    mSecondStateColor = ta.getColor(R.styleable.MorphingImageView_toColor, getResources().getColor(R.color.colorSecondary));

    if (!ta.getBoolean(R.styleable.MorphingImageView_isColorChanging, false)) {
      int[] androidSet = {
          android.R.attr.tint, // index 0
      };
      TypedArray a = getContext().obtainStyledAttributes(attrs, androidSet);
      mFirstStateColor = a.getColor(0, getResources().getColor(R.color.colorOnBackground));
      mSecondStateColor = a.getColor(0, getResources().getColor(R.color.colorOnBackground));
      a.recycle();
    }

    applyFillColor(mFirstStateColor);

    ta.recycle();

    mIsShowingSecondState = false;
    setImageDrawable(mFirstToSecondAVD);
  }

  public void switchState() {
    if (mIsShowingSecondState) {
      animateToFirstState();
    } else {
      animateToSecondState();
    }
  }

  public void animateToSecondState() {
    setImageDrawable(mFirstToSecondAVD);
    mFirstToSecondAVD.start();
    // from -> to. It's now showing to
    mIsShowingSecondState = true;

    animateFromColorToColor(mFirstStateColor, mSecondStateColor);
  }

  public void animateToFirstState() {
    setImageDrawable(mSecondToFirstAVD);
    mSecondToFirstAVD.start();
    // to -> from. It's now showing from
    mIsShowingSecondState = false;

    animateFromColorToColor(mSecondStateColor, mFirstStateColor);
  }

  private void animateFromColorToColor(int fromColor, int toColor) {
    if (!isEnabled()) {
      return;
    }
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
    applyFillColor(mCurrentFillColor);
  }

  private void applyFillColor(@ColorRes int color) {
    mCurrentFillColor = color;
    if(isEnabled()) {
      setColorFilter(mCurrentFillColor);
    } else {
      setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }
}