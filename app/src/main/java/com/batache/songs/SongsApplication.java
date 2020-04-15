package com.batache.songs;

import android.content.Intent;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.batache.songs.api.SongDownloader;
import com.batache.songs.player.PlayerManager;
import com.cyl.musicapi.BaseApiImpl;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.nativecode.ImagePipelineNativeLoader;

public class SongsApplication extends MultiDexApplication {

  private static SongsApplication instance;

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;

    ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(this)
        .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
        .setResizeAndRotateEnabledForNetwork(true)
        .setDownsampleEnabled(true);
    ImagePipelineConfig imagePipelineConfig = builder.build();
    Fresco.initialize(this, imagePipelineConfig);
    try {
      ImagePipelineNativeLoader.load();
    } catch (UnsatisfiedLinkError error) {
      Fresco.shutDown();
      builder.experiment().setNativeCodeDisabled(true);
      imagePipelineConfig = builder.build();
      Fresco.initialize(instance, imagePipelineConfig);
      error.printStackTrace();
    }

    PRDownloaderConfig prDownloaderConfig = PRDownloaderConfig.newBuilder()
        .setDatabaseEnabled(true)
        .build();
    PRDownloader.initialize(this, prDownloaderConfig);

    SongDownloader.initialize();

    PlayerManager.initialize();

    BaseApiImpl.INSTANCE.initWebView(this);
  }

  public static SongsApplication get() {
    if (instance == null) {
      throw new RuntimeException("SongsApp instance not set");
    }
    return instance;
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    PlayerManager.destroy();
    SongDownloader.destroy();
  }
}
