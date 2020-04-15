package com.batache.songs.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.batache.songs.R;
import com.batache.songs.app.MainActivity;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DeviceUtils;
import com.batache.songs.util.ImageLoader;
import com.batache.songs.util.PlayerActions;
import com.batache.songs.util.events.PlayerControllerEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.batache.songs.util.events.PlayerControllerEvent.Action.UPDATE_EVENT;

public class PlayerService extends Service {

  private static boolean isRunning;
  private static boolean isShowingNotification;

  private static final int NOTIFICATION_ID = 101;

  private static final String PLAYER_GROUP_CHANNEL_ID = "player_group_channel_id";
  private static String PLAYER_CHANNEL_ID = "player_channel_id";
  private static final String PLAYER_CHANNEL_NAME = "Player Controller";
  private static final String PLAYER_CHANNEL_DESCRIPTION = "This category shows you the player controller";

  private Context context;
  private PlayerNotificationManager playerNotificationManager;

  @Override
  public void onCreate() {
    super.onCreate();
    isRunning = true;
    context = this;
    CodeUtils.registerToEventBus(this);

    if (DeviceUtils.isOreo()) {
      PLAYER_CHANNEL_ID = createNotificationChannel(PLAYER_CHANNEL_ID, PLAYER_CHANNEL_NAME);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();

      if (action != null) {
        switch (action) {
          case PlayerActions.ACTION_STOP:
            smartStopForeground(false);
            stopSelf();
            return START_NOT_STICKY;
          case PlayerActions.ACTION_START:
            showPlayerNotification();
            return START_STICKY;
          default:
            return super.onStartCommand(intent, flags, startId);
        }
      }
    }
    return START_NOT_STICKY;
  }

  private void showPlayerNotification() {
    if (isShowingNotification) {
      return;
    }
    playerNotificationManager = new PlayerNotificationManager(
        context,
        PLAYER_CHANNEL_ID,
        NOTIFICATION_ID,
        new PlayerNotificationManager.MediaDescriptionAdapter() {
          @Override
          public String getCurrentContentTitle(Player player) {
            return getPlayingSong(player).getTitle();
          }

          @Nullable
          @Override
          public PendingIntent createCurrentContentIntent(Player player) {
            Intent intent = new Intent(context, MainActivity.class);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
          }

          @Nullable
          @Override
          public String getCurrentContentText(Player player) {
            return getPlayingSong(player).getArtist();
          }

          @Nullable
          @Override
          public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            ImageLoader.getBitmap(context, getPlayingSong(player).getId(), getPlayingSong(player).getCoverArt(), callback::onBitmap);
            return null;
          }
        },
        new PlayerNotificationManager.NotificationListener() {
          @Override
          public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            isShowingNotification = false;

            if (!dismissedByUser) {
              stopSelf();
            }
          }

          @Override
          public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            isShowingNotification = true;

            startForeground(notificationId, notification);
            if (!PlayerManager.getInstance().isPlaying()) {
              smartStopForeground(false);
            }
          }
        }
    );

    playerNotificationManager.setPlayer(PlayerManager.getInstance().getPlayer());
    playerNotificationManager.setSmallIcon(R.drawable.ic_music_note_outlined_black_24dp);
    playerNotificationManager.setUseNavigationActionsInCompactView(true);
    playerNotificationManager.setUseChronometer(false);

    MediaSessionCompat mediaSession = new MediaSessionCompat(this, PLAYER_CHANNEL_ID);

    MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
    TimelineQueueNavigator queueNavigator =
        new TimelineQueueNavigator(mediaSession) {
          @Override
          public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
            return getMediaDescriptionAtQueuePosition(windowIndex);
          }
        };
    mediaSessionConnector.setQueueNavigator(queueNavigator);
    mediaSessionConnector.setMediaMetadataProvider(new MediaSessionConnector.MediaMetadataProvider() {
      String metadataExtrasPrefix = null;

      @Override
      public MediaMetadataCompat getMetadata(Player player) {
        PlayerManager playerManager = PlayerManager.getInstance();
        Song song = playerManager.getCurrentSong();

        if (song == null) {
          return new MediaMetadataCompat.Builder().build();
        }

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            player.isCurrentWindowDynamic() || playerManager.getSongDuration() == C.TIME_UNSET
                ? -1
                : playerManager.getSongDuration());

        MediaDescriptionCompat description = getMediaDescriptionAtQueuePosition(playerManager.getCurrentSongIndex());
        Bundle extras = description.getExtras();
        if (extras != null) {
          for (String key : extras.keySet()) {
            Object value = extras.get(key);
            if (value instanceof String) {
              builder.putString(metadataExtrasPrefix + key, (String) value);
            } else if (value instanceof CharSequence) {
              builder.putText(metadataExtrasPrefix + key, (CharSequence) value);
            } else if (value instanceof Long) {
              builder.putLong(metadataExtrasPrefix + key, (Long) value);
            } else if (value instanceof Integer) {
              builder.putLong(metadataExtrasPrefix + key, (Integer) value);
            } else if (value instanceof Bitmap) {
              builder.putBitmap(metadataExtrasPrefix + key, (Bitmap) value);
            } else if (value instanceof RatingCompat) {
              builder.putRating(metadataExtrasPrefix + key, (RatingCompat) value);
            }
          }
        }
        if (description.getTitle() != null) {
          String title = String.valueOf(description.getTitle());
          builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
          builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title);
        }
        if (description.getSubtitle() != null) {
          builder.putString(
              MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
              String.valueOf(description.getSubtitle()));
          builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
              String.valueOf(description.getSubtitle()));
        }
        if (description.getDescription() != null) {
          builder.putString(
              MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
              String.valueOf(description.getDescription()));
        }
        if (description.getIconBitmap() != null) {
          builder.putBitmap(
              MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, description.getIconBitmap());
        }
        if (description.getIconUri() != null) {
          builder.putString(
              MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
              String.valueOf(description.getIconUri()));
        }
        if (description.getMediaId() != null) {
          builder.putString(
              MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
              String.valueOf(description.getMediaId()));
        }
        if (description.getMediaUri() != null) {
          builder.putString(
              MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
              String.valueOf(description.getMediaUri()));
        }
        return builder.build();
      }
    });

    mediaSessionConnector.setPlayer(PlayerManager.getInstance().getPlayer());
    playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
  }

  private MediaDescriptionCompat getMediaDescriptionAtQueuePosition(int windowIndex) {
    Song song = PlayerManager.getInstance().getPlayqueue().get(windowIndex);
    return new MediaDescriptionCompat.Builder()
        .setMediaId(song.getId())
        .setTitle(song.getTitle())
        .setSubtitle(song.getArtist())
        .setIconBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_note_outlined_black_24dp))
        .build();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private String createNotificationChannel(String channelId, String channelName) {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(PLAYER_GROUP_CHANNEL_ID, PLAYER_CHANNEL_NAME));

    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
    channel.setShowBadge(false);
    channel.setSound(null, null);
    channel.setGroup(PLAYER_GROUP_CHANNEL_ID);
    channel.setDescription(PLAYER_CHANNEL_DESCRIPTION);

    notificationManager.createNotificationChannel(channel);
    return channelId;
  }

  public static void startUpdate(Context context) {
    if (isRunning) {
      PlayerControllerEvent.postUpdatePlayerControllers();
      return;
    }
    Intent intent = new Intent(context, PlayerService.class)
        .setAction(PlayerActions.ACTION_START);
    startService(context, intent);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void handlePlayerControllerEvent(PlayerControllerEvent event) {
    if (event.action == UPDATE_EVENT) {
      showPlayerNotification();
    }
  }

  public static void stop(Context context) {
    if (isRunning) {
      Intent intent = new Intent(context, PlayerService.class)
          .setAction(PlayerActions.ACTION_STOP);
      startService(context, intent);
    }
  }

  private static void startService(Context context, Intent intent) {
    if (DeviceUtils.isOreo()) {
      context.startForegroundService(intent);
    } else {
      context.startService(intent);
    }
  }

  private void smartStopForeground(boolean removeNotification) {
    if (removeNotification || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      stopForeground(removeNotification);
      return;
    }
    stopForeground(STOP_FOREGROUND_DETACH);
  }

  private Song getPlayingSong(Player player) {
    return PlayerManager.getInstance().getPlayqueue().get(player.getCurrentWindowIndex());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    isRunning = false;
    CodeUtils.unregisterFromEventBus(this);
    if (playerNotificationManager != null) {
      playerNotificationManager.setPlayer(null);
    }
    smartStopForeground(false);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
