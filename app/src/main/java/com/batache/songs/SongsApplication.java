package com.batache.songs;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Playlist;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import java.util.Date;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class SongsApplication extends MultiDexApplication {

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
        .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
        .setResizeAndRotateEnabledForNetwork(true)
        .setDownsampleEnabled(true)
        .build();
    Fresco.initialize(this, imagePipelineConfig);

    PRDownloaderConfig prDownloaderConfig = PRDownloaderConfig.newBuilder()
        .setDatabaseEnabled(true)
        .build();
    PRDownloader.initialize(getApplicationContext(), prDownloaderConfig);

    DatabaseHelper mDatabaseHelper = DatabaseHelper.getInstance(getApplicationContext());
    if (mDatabaseHelper.getPlaylist(PLAYLIST_LIKES_ID) == null) {
      mDatabaseHelper.insertPlaylist(new Playlist(PLAYLIST_LIKES_ID, getString(R.string.favorites), new Date()));
    }
    if (mDatabaseHelper.getPlaylist(PLAYLIST_DOWNLOADS_ID) == null) {
      mDatabaseHelper.insertPlaylist(new Playlist(PLAYLIST_DOWNLOADS_ID, getString(R.string.downloads), new Date()));
    }
  }
}
