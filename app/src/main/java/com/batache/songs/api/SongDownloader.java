package com.batache.songs.api;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.batache.songs.R;
import com.batache.songs.SongsApplication;
import com.batache.songs.SongsService;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.Lyric;
import com.batache.songs.model.pojo.PlaylistSongJoin;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.PlayerManager;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DeviceUtils;
import com.batache.songs.util.PrefUtils;
import com.batache.songs.util.SongUtils;
import com.batache.songs.util.events.DownloadEvent;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Extras;
import com.tonyodev.fetch2core.Func;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;
import static com.batache.songs.util.SongUtils.SONG_EXTRA;

public class SongDownloader implements FetchListener {

  private static SongDownloader sInstance;

  public static void initialize() {
    if (sInstance == null) {
      sInstance = new SongDownloader(SongsApplication.get());
    }
  }

  public static SongDownloader getInstance() {
    if (sInstance == null) {
      throw new RuntimeException("Song Downloader not initialized");
    }
    return sInstance;
  }

  private SongsApplication context;
  private Fetch fetch;

  private NotificationCompat.Builder builder;
  private NotificationManagerCompat manager;

  private SongDownloader(SongsApplication songsApplication) {
    // we should always use the application context to avoid memory leaks
    context = songsApplication;

    fetch = Fetch.Impl.getInstance(new FetchConfiguration.Builder(context).build());
    fetch.addListener(this);
  }

  public void downloadSong(Song song) {
    if (song.getSongSource() == Song.SongSource.youtube || song.getSongSource() == Song.SongSource.dndnha) {
      fetchSongURL(context, song, new SongURLFetcherCallback() {
        @Override
        public void onSuccess(String playableUrl) {
          download(song, playableUrl);
        }

        @Override
        public void onError() {
          DownloadEvent.postDownloadCancelled(song);
        }
      });
    } else if (song.getSongSource() == Song.SongSource.xiami) {
      download(song, song.getUri().toString());
    }
  }

  public void fetchSongURL(final Context context, final Song song, final SongURLFetcherCallback callback) {
    if(song.getSongSource() == Song.SongSource.youtube) {
      new YouTubeExtractor(context) {
        @Override
        public void onExtractionComplete(SparseArray<YtFile> ytFiles, final VideoMeta vMeta) {
          if (ytFiles != null) {
            for (int i = 0, itag; i < ytFiles.size(); i++) {
              itag = ytFiles.keyAt(i);
              final YtFile ytFile = ytFiles.get(itag);

              // We only want audio: height == -1 :)
              if (ytFile.getFormat().getHeight() == -1) {
                callback.onSuccess(ytFile.getUrl());
              }
            }
          } else {
            callback.onError();
          }
        }
      }.extract(song.getLink(), true, false);
    } else if(song.getSongSource() == Song.SongSource.dndnha) {
      String url = song.getLink();

      HTMLDataFetcher lyricsHTMLFetch = new HTMLDataFetcher(context, url, new HTMLDataFetcher.HTMLDataFetcherCallback() {
        @Override
        public void onSuccess(Document result) {
          // unescape html: replace characters like &amp; with &
          String songUrl = StringEscapeUtils.unescapeHtml4(
              result.select("audio").attr("src")
          );

          if (!songUrl.equals("")) {
            callback.onSuccess(songUrl);
          }
        }

        @Override
        public void onFailure() {
        }
      });
      lyricsHTMLFetch.execute();
    }
  }

  public void fetchSongURLByUrl(final Context context, final String url, final SongURLFetcherCallback callback) {
    new YouTubeExtractor(context) {
      @Override
      public void onExtractionComplete(SparseArray<YtFile> ytFiles, final VideoMeta vMeta) {
        if (ytFiles != null) {
          for (int i = 0, itag; i < ytFiles.size(); i++) {
            itag = ytFiles.keyAt(i);
            final YtFile ytFile = ytFiles.get(itag);

            // We only want audio: height == -1 :)
            if (ytFile.getFormat().getHeight() == -1) {
              callback.onSuccess(ytFile.getUrl());
            }
          }
        }
      }
    }.extract(url, true, false);
  }

  public interface SongURLFetcherCallback {
    void onSuccess(String playableUrl);
    void onError();
  }

  private void download(Song song, String url) {
    String fileName = SongUtils.getSongFileName(song.getId(), song.getSongSource());
    Request request = new Request(url, DeviceUtils.getDownloadSongsDirFilePath(context, fileName).getPath());
    request.setPriority(Priority.HIGH);
    boolean downloadOverWifiOnly = PrefUtils.getInstance().getBoolean(PrefUtils.SETTINGS_DOWNLOAD_OVER_WIFI_ONLY_PREF, PrefUtils.SETTINGS_DOWNLOAD_OVER_WIFI_ONLY_DEFAULT_VALUE);
    request.setNetworkType(downloadOverWifiOnly ? NetworkType.WIFI_ONLY : NetworkType.ALL);
    request.setExtras(new Extras(new HashMap<String, String>() {{
      put(SONG_EXTRA, SongUtils.toString(song));
    }}));

    fetch.enqueue(request, result -> {
    }, result -> {
    });
  }

  private void showNotification(Download download) {
    Song song = SongUtils.getSong(download);

    manager = NotificationManagerCompat.from(context);

    builder = new NotificationCompat.Builder(context, initChannelId())
        .setContentTitle(song.getTitle())
        .setPriority(Notification.PRIORITY_LOW)
        .setSmallIcon(R.drawable.ic_music_note_outlined_black_24dp)
        .setAutoCancel(false)
        .setProgress(100, 0, false)
        .setWhen(System.currentTimeMillis())
        .setOngoing(true);
    builder.mActions.clear();

    Intent cancelIntent = new Intent(context, SongsService.class);
    cancelIntent.setAction(SongsService.ACTION_CANCEL_DOWNLOAD);
    cancelIntent.putExtra(SongsService.EXTRA_SONG_ID, song.getId());
    PendingIntent cancelPendingIntent = PendingIntent.getService(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.addAction(R.drawable.ic_music_note_outlined_black_24dp, "Cancel download", cancelPendingIntent);

    manager.notify(download.getId(), builder.build());
  }

  private String initChannelId() {
    String id = "songs_downloads_channel";

    if (DeviceUtils.isOreo()) {
      NotificationChannel notificationChannel = new NotificationChannel(id, "Download notifications", NotificationManager.IMPORTANCE_LOW);
      manager.createNotificationChannel(notificationChannel);
    }

    return id;
  }

  private void updateNotification(int id, int progress) {
    if (builder == null) {
      return;
    }

    builder.setProgress(100, progress, false);

    manager.notify(id, builder.build());
  }

  private void cancelNotification(int id) {
    manager.cancel(id);
  }

  public void cancelDownload(String songId) {
    getDownloadIdBySongId(songId, downloadId -> {
      if (downloadId == -1) {
        return;
      }
      fetch.cancel(downloadId);
    });
  }

  private void downloadCompleted(Download download, Context context, Song song) {
    boolean laterAddToFavorites = PlayerManager.getInstance().isSongInFavorites(song);

    if (song.getPath() != null && song.getPath().exists()) {
      PlayerManager playerManager = PlayerManager.getInstance();
      playerManager.insertSong(song);
      playerManager.insertSongInPlaylist(PLAYLIST_DOWNLOADS_ID, song.getId());
      if (laterAddToFavorites) {
        playerManager.insertSongInPlaylist(PLAYLIST_LIKES_ID, song.getId());
      }
    }

    cancelNotification(download.getId());

    DownloadEvent.postDownloadComplete(song);
  }

  @Override
  public void onAdded(@NotNull Download download) {

  }

  @Override
  public void onQueued(@NotNull Download download, boolean b) {

  }

  @Override
  public void onWaitingNetwork(@NotNull Download download) {

  }

  @Override
  public void onCompleted(@NotNull Download download) {
    Song song = SongUtils.getSong(download);

    song.setPath(new File(download.getFile()));

    if (song.getSongSource() == Song.SongSource.youtube || song.getSongSource() == Song.SongSource.dndnha) {
      SongsAPI.INSTANCE.getSongLyrics(song.getTitle(), song.getArtist(), new SongsAPI.OnLyricsFetchedListener() {
        @Override
        public void onSuccess(List<? extends Lyric> lyrics) {
          song.setLyrics((List<Lyric>) lyrics);
          downloadCompleted(download, context, song);
        }

        @Override
        public void onFailure(Throwable t) {
          downloadCompleted(download, context, song);
        }
      });
    } else if (song.getSongSource() == Song.SongSource.xiami) {
      if (CodeUtils.isEmpty(song.getLyricsUrl())) {
        downloadCompleted(download, context, song);
        return;
      }
      SongsAPI.INSTANCE.getXiamiSongLyrics(song.getLyricsUrl(), new SongsAPI.OnXiamiLyricsFetchedListener() {
        @Override
        public void onSuccess(String artist, List<? extends Lyric> lyrics) {
          if (!CodeUtils.isEmpty(artist)) {
            song.setArtist(artist);
          }
          song.setLyrics((List<Lyric>) lyrics);
          song.setAreLyricsSynced(true);
          downloadCompleted(download, context, song);
        }

        @Override
        public void onFailure(Throwable t) {
          downloadCompleted(download, context, song);
        }
      });
    }
  }

  @Override
  public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {

  }

  @Override
  public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

  }

  @Override
  public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
    showNotification(download);
    DownloadEvent.postDownloadStarted(SongUtils.getSong(download));
  }

  @Override
  public void onProgress(@NotNull Download download, long l, long l1) {
    updateNotification(download.getId(), download.getProgress());
    DownloadEvent.postDownloadProgressChanged(SongUtils.getSong(download), download.getProgress());
  }

  @Override
  public void onPaused(@NotNull Download download) {

  }

  @Override
  public void onResumed(@NotNull Download download) {

  }

  @Override
  public void onCancelled(@NotNull Download download) {
    DownloadEvent.postDownloadCancelled(SongUtils.getSong(download));
    manager.cancel(download.getId());
  }

  @Override
  public void onRemoved(@NotNull Download download) {

  }

  @Override
  public void onDeleted(@NotNull Download download) {

  }

  public void getDownloadIdBySongId(String songId, Func<Integer> callback) {
    getDownloadingSongs(result -> {
      for (Download download : result) {
        if (SongUtils.getSong(download).getId().equals(songId)) {
          callback.call(download.getId());
        }
      }
    });
  }

  public void getDownloadingSongs(Func<List<Download>> callback) {
    fetch.getDownloadsWithStatus(Status.DOWNLOADING, callback);
  }

  public void getQueuedSongs(Func<List<Download>> callback) {
    fetch.getDownloadsWithStatus(Status.QUEUED, callback);
  }

  public void getDownloadingAndQueuedSongs(Func<List<Download>> callback) {
    fetch.getDownloadsWithStatus(new ArrayList<>(Arrays.asList(Status.DOWNLOADING, Status.QUEUED)), callback);
  }

  public static void destroy() {
    if (sInstance == null) {
      return;
    }
    if (sInstance.fetch != null) {
      sInstance.fetch.close();
    }
    sInstance = null;
  }

}
