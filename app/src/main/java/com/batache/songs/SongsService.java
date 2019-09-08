package com.batache.songs;

import android.app.IntentService;
import android.content.Intent;

import com.batache.songs.api.SongDownloader;
import com.batache.songs.util.CodeUtils;

public class SongsService extends IntentService {
  public static final String ACTION_CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD";

  public static final String EXTRA_SONG_ID = "song_id";

  public SongsService() {
    super("SongsService");
  }

  @Override
  public void onHandleIntent(Intent intent) {
    final String action = intent.getAction();
    if (ACTION_CANCEL_DOWNLOAD.equals(action)) {
      String songId = intent.getStringExtra(EXTRA_SONG_ID);
      if(CodeUtils.isEmpty(songId)) {
        return;
      }
      SongDownloader.getInstance().cancelDownload(songId);
    } else {
      throw new IllegalArgumentException("Unsupported action: " + action);
    }
  }
}
