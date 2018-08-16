package ml.toufic.songs.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

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
  public static void downloadSong(final Context context, final Song song, final SongDownloaderCallback callback) {
    fetchSongURL(context, song, new SongURLFetcherCallback() {
      @Override
      public void onSuccess(YtFile songFile) {
        boolean isSongInDownloadsDir = SongUtils.isSongInDownloadsDir(song.getId());
        boolean isSongInMyMusic = SongUtils.isSongInMyMusic(context, song.getId());

        if (isSongInDownloadsDir && isSongInMyMusic) {
          Toast.makeText(context, "Song already exists in \"My Music\"!", Toast.LENGTH_LONG).show();
        } else {
          startDownload(context, song, songFile.getUrl(), callback);
        }
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

    String dirPath = DeviceUtils.getDownloadStorageDirectoryPath();
    final String fileName = SongUtils.getSongFileName(song.getId());
    PRDownloader.download(url, dirPath, fileName)
      .build()
      .setOnStartOrResumeListener(new OnStartOrResumeListener() {
        @Override
        public void onStartOrResume() {
          callback.onStartOrResume();
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
          final PlayableSong playableSong = new PlayableSong(song, SongUtils.getSongPath(fileName));
          if(playableSong.getPath().exists()) {
            List<PlayableSong> playableSongs = SongUtils.getCachedSongs(context);
            playableSongs.add(playableSong);
            SongUtils.cacheSongs(context, playableSongs);
          }
          callback.onDownloadComplete(playableSong.getId());
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
    void onStartOrResume();

    void onProgress(Progress progress);

    void onDownloadComplete(String songId);

    void onError(Error error);
  }

}
