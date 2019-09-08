package com.batache.songs;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.batache.songs.api.SongDownloader;
import com.batache.songs.app.player.PlayerManager;
import com.cyl.musicapi.BaseApiImpl;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

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
    PRDownloader.initialize(this, prDownloaderConfig);

    SongDownloader.initialize(this);
    PlayerManager.initialize(this);

    BaseApiImpl.INSTANCE.initWebView(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    PlayerManager.destroy();
    SongDownloader.destroy();
  }
}
