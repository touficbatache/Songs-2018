package ml.toufic.songs.network;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import java.io.File;
import java.util.List;

import at.huber.youtubeExtractor.YtFile;
import ml.toufic.songs.model.DownloadedSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

public class SongDownloader {

  public static void startDownload(Context context, final Song song, final YtFile ytFile, final SongDownloaderCallback callback) {

    Uri uri = Uri.parse(ytFile.getUrl());
    DownloadManager.Request request = new DownloadManager.Request(uri);
    request.setTitle(song.getTitle());
    request.setVisibleInDownloadsUi(false);
    request.allowScanningByMediaScanner();
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationInExternalPublicDir(DeviceUtils.getDownloadStorageDirectorySmallPath(), SongUtils.getSongFileName(song.getId()));

    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    manager.enqueue(request);

    context.registerReceiver(new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        callback.onSuccess(new DownloadedSong(new File(DeviceUtils.getDownloadStorageDirectoryPath(), SongUtils.getSongFileName(song.getId())), song));
        context.unregisterReceiver(this);
      }
    }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  public interface SongDownloaderCallback {
    // method called when server's data get fetched
    void onSuccess(DownloadedSong downloadedSong);
  }

}
