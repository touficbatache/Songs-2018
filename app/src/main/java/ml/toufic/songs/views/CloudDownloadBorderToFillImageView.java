package ml.toufic.songs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import ml.toufic.songs.R;

public class CloudDownloadBorderToFillImageView extends AppCompatImageView {

  private Drawable mCloudDownloadBorder;
  private Drawable mCloudDownloadFill;
  private int fillColor;
  private int currentFillColor;
  private boolean mIsShowingFill;

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
    mCloudDownloadBorder =
      getContext().getDrawable(R.drawable.ic_cloud_download_border_24dp);
    mCloudDownloadFill =
      getContext().getDrawable(R.drawable.ic_cloud_download_24dp);
    setImageDrawable(mCloudDownloadBorder);
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
    setImageDrawable(mCloudDownloadFill);
    // border -> fill. It's now showing fill
    mIsShowingFill = true;
    currentFillColor = getResources().getColor(R.color.colorAccent);

    setColorFilter(currentFillColor);
  }

  public void showBorder() {
    clearAnimation();
    setImageDrawable(mCloudDownloadBorder);
    // fill -> border. It's now showing border
    mIsShowingFill = false;
    currentFillColor = getResources().getColor(R.color.grey_67);

    setColorFilter(currentFillColor);
  }

  public void showBlinkingAnimation() {
    Animation myFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_fade_animation);
    startAnimation(myFadeInAnimation);
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
}