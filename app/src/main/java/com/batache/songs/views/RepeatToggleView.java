package com.batache.songs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import com.batache.songs.R;

public class RepeatToggleView extends AppCompatImageView {

  public RepeatToggleView(Context context) {
    this(context, null);
  }

  public RepeatToggleView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RepeatToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if(attrs != null) {
      init(attrs);
    }
  }

  private void init(AttributeSet attrs){
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.IconToggleView);

    ta.recycle();

    showState(0);
  }

  public void showState(int state) {
    if (state == 0) {
      setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_24dp));
      setColorFilter(getResources().getColor(R.color.grey_c0));
    } else if (state == 2) {
      setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_24dp));
      setColorFilter(getResources().getColor(R.color.grey_67));
    } else if (state == 1) {
      setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_24dp));
      setColorFilter(getResources().getColor(R.color.grey_67));
    }
  }
}