package com.batache.songs.views;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.batache.songs.R;

public class CloudDownloadBorderToFillImageView extends AppCompatImageView {

  private int mCloudDownloadBorder, mCloudDownloadFill;
  private int fillColor;
  private int currentFillColor;
  private boolean mIsShowingFill;
  private boolean mIsBlinking;

  public CloudDownloadBorderToFillImageView(Context context) {
    this(context, null);
  }

  public CloudDownloadBorderToFillImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CloudDownloadBorderToFillImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();

    if(attrs != null) {
      int[] set = {
        android.R.attr.tint, // index 0
      };
      TypedArray a = context.obtainStyledAttributes(attrs, set);
      fillColor = a.getColor(0,
        getResources().getColor(R.color.grey_67));
      setColorFilter(fillColor);
      a.recycle();
    }
  }

  private void init(){
    mIsShowingFill = false;
    mCloudDownloadBorder = R.drawable.ic_cloud_download_outlined_black_24dp;
    mCloudDownloadFill = R.drawable.ic_cloud_download_24dp;
    setImageResource(mCloudDownloadBorder);
  }

  public void switchState(){
    if(mIsShowingFill) {
      showBorder();
    } else {
      showFill();
    }
  }

  public void showFill() {
    clearAnimation();
    mIsBlinking = false;
    setImageResource(mCloudDownloadFill);
    // border -> fill. It's now showing fill
    mIsShowingFill = true;
    currentFillColor = getResources().getColor(R.color.player_action_other);

    setColorFilter(currentFillColor);
  }

  public void showBorder() {
    clearAnimation();
    mIsBlinking = false;
    setImageResource(mCloudDownloadBorder);
    // fill -> border. It's now showing border
    mIsShowingFill = false;
    currentFillColor = getResources().getColor(R.color.grey_67);

    setColorFilter(currentFillColor);
  }

  public void showBlinkingAnimation() {
    mIsBlinking = true;
    Animation myFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_fade_animation);
    startAnimation(myFadeInAnimation);
  }

  public boolean isShowingFill() {
    return mIsShowingFill;
  }

  public boolean isShowingBorder() {
    return !mIsShowingFill;
  }

  public boolean isBlinking() {
    return mIsBlinking;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if(enabled) {
      setColorFilter(currentFillColor);
    } else {
      setColorFilter(getResources().getColor(R.color.grey_c0));
    }
  }

  public void setOnDownloadClickedListener(OnDownloadClickListener onDownloadClickedListener) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if(isShowingBorder() && !isBlinking()) {
          onDownloadClickedListener.onDownloadStart();
        } else if (isBlinking()) {
          onDownloadClickedListener.onDownloadCancel();
        } else if (isShowingFill()) {
          onDownloadClickedListener.onDownloadDelete();
        }
      }
    });
  }

  public interface OnDownloadClickListener {
    void onDownloadStart();
    void onDownloadCancel();
    void onDownloadDelete();
  }
}