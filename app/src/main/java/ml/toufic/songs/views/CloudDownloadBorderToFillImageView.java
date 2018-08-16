package ml.toufic.songs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import ml.toufic.songs.R;

public class CloudDownloadBorderToFillImageView extends AppCompatImageView {

  private Drawable mCloudDownloadBorder;
  private Drawable mCloudDownloadFill;
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
      int fill = a.getColor(0,
        getResources().getColor(R.color.grey_67));
      setColorFilter(fill);
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
    Drawable drawable = mIsShowingFill ? mCloudDownloadBorder : mCloudDownloadFill;
    setImageDrawable(drawable);
    mIsShowingFill = !mIsShowingFill;
  }

  public void showFill() {
    if(!mIsShowingFill) {
      Drawable drawable = mCloudDownloadFill;
      setImageDrawable(drawable);
      // border -> fill. It's now showing fill
      mIsShowingFill = true;
    }
  }

  public void showBorder() {
    Drawable drawable = mCloudDownloadBorder;
    setImageDrawable(drawable);
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