package com.batache.songs.network;

import android.content.Context;
import android.util.SparseArray;

import com.batache.songs.api.SongsAPI;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Lyric;
import com.batache.songs.model.PlaylistSongJoin;
import com.batache.songs.model.Song;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DeviceUtils;
import com.batache.songs.util.SongUtils;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.utils.Utils;

import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;

public class SongDownloader {

  private static SongDownloader sInstance;

  public static SongDownloader getInstance() {
    if(sInstance == null) {
      sInstance = new SongDownloader();
    }
    return sInstance;
  }

  public void downloadSong(final Context context, final Song song, final SongDownloaderCallback callback) {
    if (song.getSongSource() == Song.SongSource.youtube) {
      fetchSongURL(context, song, new SongURLFetcherCallback() {
        @Override
        public void onSuccess(YtFile songFile) {
          startDownload(context, song, songFile.getUrl(), callback);
        }
      });
    } else if (song.getSongSource() == Song.SongSource.xiami) {
      startDownload(context, song, song.getXiamiProperties().getSongUrl(), callback);
    }
  }

  public void fetchSongURL(final Context context, final Song song, final SongURLFetcherCallback callback) {
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

  private void startDownload(final Context context, final Song song, final String url, final SongDownloaderCallback callback) {
    String dirPath = DeviceUtils.getInternalFilesSongsDir(context).getPath();
    final String fileName = SongUtils.getSongFileName(song.getId(), song.getSongSource());
    PRDownloader.download(url, dirPath, fileName)
        .build()
        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
          @Override
          public void onStartOrResume() {
            callback.onStartOrResume(song, Utils.getUniqueId(url, dirPath, fileName));
          }
        })
        .setOnProgressListener(new OnProgressListener() {
          @Override
          public void onProgress(Progress progress) {
            callback.onProgress(song, progress);
          }
        })
        .start(new OnDownloadListener() {
          @Override
          public void onDownloadComplete() {
            song.setPath(DeviceUtils.getInternalFilesSongsDirFilePath(context, fileName));
            if (song.getSongSource() == Song.SongSource.youtube) {
              SongsAPI.getInstance().getSongLyrics(context, song.getTitle(), new SongsAPI.OnLyricsFetchedListener() {
                @Override
                public void onSuccess(List<Lyric> lyrics) {
                  song.setLyrics(lyrics);
                  downloadCompleted(context, song, callback);
                }

                @Override
                public void onFailure(Throwable t) {
                  downloadCompleted(context, song, callback);
                }
              });
            } else if (song.getSongSource() == Song.SongSource.xiami) {
              SongsAPI.getInstance().getXiamiSongLyrics(song.getXiamiProperties().getLyricsUrl(), new SongsAPI.OnXiamiLyricsFetchedListener() {
                @Override
                public void onSuccess(String artist, List<Lyric> lyrics) {
                  if (!CodeUtils.isEmpty(artist)) {
                    song.setArtist(artist);
                  }
                  song.setLyrics(lyrics);
                  downloadCompleted(context, song, callback);
                }

                @Override
                public void onFailure(Throwable t) {
                  downloadCompleted(context, song, callback);
                }
              });
            }
          }

          @Override
          public void onError(Error error) {
            callback.onFailure(song, error);
          }
        });
  }

  private void downloadCompleted(Context context, Song downloadedSong, SongDownloaderCallback callback) {
    if (downloadedSong.getPath().exists()) {
      DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
      databaseHelper.insertSong(downloadedSong);
      databaseHelper.insertSongInPlaylist(new PlaylistSongJoin(PLAYLIST_DOWNLOADS_ID, downloadedSong.getId()));
      callback.onDownloadComplete(downloadedSong);
      return;
    }

    Error error = new Error();
    error.setConnectionError(true);
    callback.onFailure(downloadedSong, error);
  }

  public void cancelDownload(int downloadId) {
    PRDownloader.cancel(downloadId);
  }

  public interface SongURLFetcherCallback {
    // method called when server's data get fetched
    void onSuccess(YtFile songFile);
  }

  public interface SongDownloaderCallback {
    void onStartOrResume(Song song, int downloadId);

    void onProgress(Song song, Progress progress);

    void onDownloadComplete(Song downloadedSong);

    void onFailure(Song song, Error error);
  }

}
