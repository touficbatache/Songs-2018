package ml.toufic.songs.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.util.DeviceUtils;
import ml.toufic.songs.util.SongUtils;

import static ml.toufic.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;

public class SongDownloader {

  @SuppressLint("StaticFieldLeak")
  public static void downloadSong(final Context context, final Song song, final SongDownloaderCallback callback) {
    fetchSongURL(context, song, new SongURLFetcherCallback() {
      @Override
      public void onSuccess(YtFile songFile) {
        startDownload(context, song, songFile.getUrl(), callback);
      }
    });
  }

  @SuppressLint("StaticFieldLeak")
  public static void fetchSongURL(final Context context, final Song song, final SongURLFetcherCallback callback) {
    new YouTubeExtractor(context) {
      @Override
      public void onExtractionComplete(SparseArray<YtFile> ytFiles, final VideoMeta vMeta) {
        if (ytFiles != null) {
          for (int i = 0, itag; i < ytFiles.size(); i++) {
            itag = ytFiles.keyAt(i);
            final YtFile ytFile = ytFiles.get(itag);

            // We only want audio: height == -1 :)
            if (ytFile.getFormat().getHeight() == -1) {
              callback.onSuccess(ytFile);
            }
          }
        }
      }
    }.extract(song.getLink(), true, false);
  }

  private static void startDownload(final Context context, final Song song, final String url, final SongDownloaderCallback callback) {

    String dirPath = DeviceUtils.getInternalFilesSongsDir(context).getPath();
    final String fileName = SongUtils.getSongFileName(song.getId());
    PRDownloader.download(url, dirPath, fileName)
      .build()
      .setOnStartOrResumeListener(new OnStartOrResumeListener() {
        @Override
        public void onStartOrResume() {
          callback.onStartOrResume(song.getTitle());
        }
      })
      .setOnProgressListener(new OnProgressListener() {
        @Override
        public void onProgress(Progress progress) {
          callback.onProgress(progress);
        }
      })
      .start(new OnDownloadListener() {
        @Override
        public void onDownloadComplete() {
          PlayableSong playableSong = new PlayableSong(song, DeviceUtils.getInternalFilesSongsDirFilePath(context, fileName));
          callback.onDownloadComplete(playableSong);
        }

        @Override
        public void onError(Error error) {
          callback.onError(error);
        }
      });
  }

  public interface SongURLFetcherCallback {
    // method called when server's data get fetched
    void onSuccess(YtFile songFile);
  }

  public interface SongDownloaderCallback {
    void onStartOrResume(String songTitle);

    void onProgress(Progress progress);

    void onDownloadComplete(PlayableSong playableSong);

    void onError(Error error);
  }

}
