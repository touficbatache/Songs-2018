package com.batache.songs.views;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.batache.songs.R;

public class IconToggleView extends AppCompatImageView {

  private int mFirstIcon, mSecondIcon;
  int mCurrentFillColor;
  private int mFirstIconColor;
  private int mSecondIconColor;
  private boolean mIsShowingSecondIcon;

  public IconToggleView(Context context) {
    this(context, null);
  }

  public IconToggleView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public IconToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if(attrs != null) {
      init(attrs);
    }
  }

  private void init(AttributeSet attrs){
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.IconToggleView);

    mFirstIcon = ta.getResourceId(R.styleable.IconToggleView_firstIcon, R.drawable.ic_favorite_outlined_black_24dp);
    mSecondIcon = ta.getResourceId(R.styleable.IconToggleView_secondIcon, R.drawable.ic_favorite_24dp);

    mFirstIconColor = ta.getColor(R.styleable.IconToggleView_firstIconColor, getResources().getColor(R.color.grey_67));
    mSecondIconColor = ta.getColor(R.styleable.IconToggleView_secondIconColor, getResources().getColor(R.color.grey_67));

    mCurrentFillColor = mFirstIconColor;

    ta.recycle();

    mIsShowingSecondIcon = false;
    setImageResource(mFirstIcon);
  }

  public void switchState(){
    if(mIsShowingSecondIcon) {
      showFirstState();
    } else {
      showSecondState();
    }
  }

  public void showFirstState() {
    if(mFirstIcon != -1) {
      setImageResource(mFirstIcon);
      mIsShowingSecondIcon = false;
      setColorFilter(mFirstIconColor);
      mCurrentFillColor = mFirstIconColor;
    }
  }

  public void showSecondState() {
    if(mSecondIcon !=- 1) {
      setImageResource(mSecondIcon);
      mIsShowingSecondIcon = true;
      setColorFilter(mSecondIconColor);
      mCurrentFillColor = mSecondIconColor;
    }
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