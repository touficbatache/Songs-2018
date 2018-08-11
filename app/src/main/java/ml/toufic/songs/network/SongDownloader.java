package ml.toufic.songs.network;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

public class SongDownloader {

  @SuppressLint("StaticFieldLeak")
  public static void downloadSong(final Context context, final Song song) {
    new YouTubeExtractor(context) {
      @Override
      public void onExtractionComplete(SparseArray<YtFile> ytFiles, final VideoMeta vMeta) {
        String toastText = "Oops, it looks like this song isn't currently available. Try again later";
        if (ytFiles != null) {
          for (int i = 0, itag; i < ytFiles.size(); i++) {
            itag = ytFiles.keyAt(i);
            final YtFile ytFile = ytFiles.get(itag);

            // We only want audio: height == -1 :)
            if (ytFile.getFormat().getHeight() == -1) {
              boolean isSongInDownloadsDir = SongUtils.isSongInDownloadsDir(song.getId());
              boolean isSongInMyMusic = SongUtils.isSongInMyMusic(context, song.getId());

              if (isSongInDownloadsDir && isSongInMyMusic) {
                toastText = "Song already exists!";
              } else {
                toastText = "Download started";
                startDownload(context, song, ytFile);
              }
            }
          }
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
      }
    }.extract(song.getLink(), true, false);
  }

  private static void startDownload(Context context, final Song song, final YtFile ytFile) {

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
        // Add the song to cached list:
        PlayableSong playableSong = new PlayableSong(song, new File(DeviceUtils.getDownloadStorageDirectoryPath(), SongUtils.getSongFileName(song.getId())));
        if (playableSong.getPath().exists()) {
          List<PlayableSong> playableSongs = SongUtils.getCachedSongs(context);
          playableSongs.add(playableSong);
          SongUtils.cacheSongs(context, playableSongs);
        }
//        callback.onSuccess(new PlayableSong(song, new File(DeviceUtils.getDownloadStorageDirectoryPath(), SongUtils.getSongFileName(song.getId()))));
        context.unregisterReceiver(this);
      }
    }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

//  public interface SongDownloaderCallback {
//    // method called when server's data get fetched
//    void onSuccess(PlayableSong playableSong);
//  }

}
