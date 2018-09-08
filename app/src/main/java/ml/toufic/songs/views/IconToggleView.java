package ml.toufic.songs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ml.toufic.songs.R;

public class IconToggleView extends AppCompatImageView {

  private Drawable mFirstIcon;
  private Drawable mSecondIcon;
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

    mFirstIcon = ta.getDrawable(R.styleable.IconToggleView_firstIcon);
    mSecondIcon = ta.getDrawable(R.styleable.IconToggleView_secondIcon);

    mFirstIconColor = ta.getColor(R.styleable.IconToggleView_firstIconColor, getResources().getColor(R.color.grey_67));
    mSecondIconColor = ta.getColor(R.styleable.IconToggleView_secondIconColor, getResources().getColor(R.color.grey_67));

    mCurrentFillColor = mFirstIconColor;

    ta.recycle();

    mIsShowingSecondIcon = false;
    setImageDrawable(mFirstIcon);
  }

  public void switchState(){
    if(mIsShowingSecondIcon) {
      showFirstState();
    } else {
      showSecondState();
    }
  }

  public void showFirstState() {
    if(mFirstIcon != null) {
      setImageDrawable(mFirstIcon);
      mIsShowingSecondIcon = false;
      setColorFilter(mFirstIconColor);
      mCurrentFillColor = mFirstIconColor;
    }
  }

  public void showSecondState() {
    if(mSecondIcon != null) {
      setImageDrawable(mSecondIcon);
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