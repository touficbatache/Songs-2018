package com.batache.songs.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class ImageLoader {

  public static void loadImage(Context context, final SimpleDraweeView imageContainer, ImageConfigurations imageConfigurations) {
    new DownloadImageAsyncTask(context, new DownloadImageCallback() {
      @Override
      public void onCompletion(Uri imageFileUri) {
        imageContainer.setImageURI(imageFileUri);
      }
    }).execute(imageConfigurations);
  }

  private static class DownloadImageAsyncTask extends AsyncTask<ImageConfigurations, Void, Uri> {

    private WeakReference<Context> mContext;
    private DownloadImageCallback completionCallback;

    DownloadImageAsyncTask(Context context, DownloadImageCallback completionCallback) {
      mContext = new WeakReference<>(context);
      this.completionCallback = completionCallback;
    }

    @Override
    protected Uri doInBackground(final ImageConfigurations... imageConfigurationsList) {
      ImageConfigurations imageConfigurations = imageConfigurationsList[0];

      String imageUrl = imageConfigurations.getImageUrl();
      File imagesStorageDir = DeviceUtils.getInternalCacheDir(mContext.get());
      String imageFileName = imageConfigurations.getImageFileName();
      File imageFile = DeviceUtils.getInternalCacheDirFilePath(mContext.get(), imageFileName);

      if(!imageFile.exists() || imageFileName.equals("temp.png")) {
        try {
          // Get the image from the url
          InputStream inputStream = new URL(imageUrl).openStream();
          Bitmap image = BitmapFactory.decodeStream(inputStream);

          // Create the storage directory if it does not exist
          if (!imagesStorageDir.exists()) {
            imagesStorageDir.mkdirs();
          }

          if(imageFile.exists() && imageFileName.equals("temp.png")) {
            imageFile.delete();
          }

          // Save the image to media storage directory
          FileOutputStream fos = new FileOutputStream(imageFile);
          image.compress(Bitmap.CompressFormat.PNG, 100, fos);
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return Uri.fromFile(imageFile);
    }

    @Override
    protected void onPostExecute(Uri imageFileUri) {
      super.onPostExecute(imageFileUri);
      completionCallback.onCompletion(imageFileUri);
    }
  }

  private interface DownloadImageCallback {
    void onCompletion(Uri imageFileUri);
  }

  public static class ImageConfigurations {
    public static String PNG = ".png";

    private String imageUrl;
    private String imageFileName;

    public ImageConfigurations(String imageUrl, String imageName, String imageFileExtension) {
      this.imageUrl = imageUrl;
      this.imageFileName = imageName + imageFileExtension;
    }

    public String getImageUrl() {
      return imageUrl;
    }

    public String getImageFileName() {
      return imageFileName;
    }
  }
}
