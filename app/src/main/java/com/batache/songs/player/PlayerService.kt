package com.batache.songs.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.batache.songs.R
import com.batache.songs.app.MainActivity
import com.batache.songs.model.pojo.Song
import com.batache.songs.util.CodeUtils
import com.batache.songs.util.DeviceUtils
import com.batache.songs.util.ImageLoader
import com.batache.songs.util.PlayerActions
import com.batache.songs.util.events.PlayerControllerEvent
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.MediaMetadataProvider
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlayerService : Service() {

  private var context: Context? = null
  private var playerNotificationManager: PlayerNotificationManager? = null

  private var currentSongId = ""
  private var currentBitmap: Bitmap? = null

  override fun onCreate() {
    super.onCreate()
    isRunning = true
    context = this
    CodeUtils.registerToEventBus(this)

    if (DeviceUtils.isOreo()) {
      PLAYER_CHANNEL_ID = createNotificationChannel(PLAYER_CHANNEL_ID, PLAYER_CHANNEL_NAME)
    }
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val action = intent.action

    if (action != null) {
      return when (action) {
        PlayerActions.ACTION_START -> {
          showPlayerNotification()
          START_STICKY
        }
        PlayerActions.ACTION_STOP -> {
          smartStopForeground(false)
          stopSelf()
          START_NOT_STICKY
        }
        else -> super.onStartCommand(intent, flags, startId)
      }
    }
    return START_NOT_STICKY
  }

  private fun showPlayerNotification() {
    if (isShowingNotification) {
      return
    }
    playerNotificationManager = PlayerNotificationManager(
        context,
        PLAYER_CHANNEL_ID,
        NOTIFICATION_ID,
        object : MediaDescriptionAdapter {
          override fun getCurrentContentTitle(player: Player): String? {
            return getPlayingSong(player)?.title
          }

          override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
          }

          override fun getCurrentContentText(player: Player): String? {
            return getPlayingSong(player)?.artist
          }

          override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
            val songId = getPlayingSong(player)?.id

            if (currentSongId == songId) {
              return currentBitmap
            }

            songId?.let { id ->
              currentSongId = id
              currentBitmap = null
              ImageLoader.getBitmap(context, id, getPlayingSong(player)?.coverArt) { bitmap ->
                currentBitmap = bitmap
                callback.onBitmap(bitmap)
              }
            }
            return null
          }
        },
        object : PlayerNotificationManager.NotificationListener {
          override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            isShowingNotification = false

            if (!dismissedByUser) {
              stopSelf()
            }
          }

          override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            isShowingNotification = true

            startForeground(notificationId, notification)
            if (!PlayerManager.getInstance().isPlaying) {
              smartStopForeground(false)
            }
          }
        }
    )

    playerNotificationManager?.setPlayer(PlayerManager.getInstance().player)
    playerNotificationManager?.setSmallIcon(R.drawable.ic_music_note_outlined_black_24dp)
    playerNotificationManager?.setUseNavigationActionsInCompactView(true)
    playerNotificationManager?.setUseChronometer(false)

    val mediaSession = MediaSessionCompat(this, PLAYER_CHANNEL_ID)

    val mediaSessionConnector = MediaSessionConnector(mediaSession)
    val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
      override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        return getMediaDescriptionAtQueuePosition(windowIndex)
      }
    }
    mediaSessionConnector.setQueueNavigator(queueNavigator)
    mediaSessionConnector.setMediaMetadataProvider(object : MediaMetadataProvider {
      var metadataExtrasPrefix: String? = null

      override fun getMetadata(player: Player): MediaMetadataCompat {
        val playerManager = PlayerManager.getInstance()

        if (playerManager.currentSong == null) {
          return MediaMetadataCompat.Builder().build()
        }

        return MediaMetadataCompat.Builder().apply {
          putLong(
              MediaMetadataCompat.METADATA_KEY_DURATION,
              if (player.isCurrentWindowDynamic || playerManager.songDuration == C.TIME_UNSET) -1 else playerManager.songDuration)

          val description = getMediaDescriptionAtQueuePosition(playerManager.currentSongIndex)
          val extras = description.extras
          if (extras != null) {
            for (key in extras.keySet()) {
              when (val value = extras[key]) {
                is String -> {
                  putString(metadataExtrasPrefix + key, value)
                }
                is CharSequence -> {
                  putText(metadataExtrasPrefix + key, value)
                }
                is Long -> {
                  putLong(metadataExtrasPrefix + key, value)
                }
                is Int -> {
                  putLong(metadataExtrasPrefix + key, value as Long)
                }
                is Bitmap -> {
                  putBitmap(metadataExtrasPrefix + key, value)
                }
                is RatingCompat -> {
                  putRating(metadataExtrasPrefix + key, value)
                }
              }
            }
          }

          description.title?.toString().let { title ->
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
          }

          description.subtitle?.toString().let { subtitle ->
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
          }

          description.description?.toString().let { desc ->
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, desc)
          }

          description.iconBitmap?.let { iconBitmap ->
            putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconBitmap)
          }

          description.iconUri?.toString().let { iconUri ->
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, iconUri)
          }

          description.mediaId?.let { mediaId ->
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
          }

          description.mediaUri?.toString().let { mediaUri ->
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
          }
        }.build()
      }
    })

    mediaSessionConnector.setPlayer(PlayerManager.getInstance().player)
    playerNotificationManager?.setMediaSessionToken(mediaSession.sessionToken)
  }

  private fun getMediaDescriptionAtQueuePosition(windowIndex: Int): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder().apply {
      PlayerManager.getInstance().playqueue.let { playqueue ->
        if (playqueue.isNotEmpty()) {
          playqueue[windowIndex].let { song ->
            setMediaId(song.id)
            setTitle(song.title)
            setSubtitle(song.artist)
            setIconBitmap(BitmapFactory.decodeResource(context!!.resources, R.drawable.ic_music_note_outlined_black_24dp))
          }
        }
      }
    }.build()
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private fun createNotificationChannel(channelId: String, channelName: String): String {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannelGroup(NotificationChannelGroup(PLAYER_GROUP_CHANNEL_ID, PLAYER_CHANNEL_NAME))

    val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
    channel.setShowBadge(false)
    channel.setSound(null, null)
    channel.group = PLAYER_GROUP_CHANNEL_ID
    channel.description = PLAYER_CHANNEL_DESCRIPTION
    notificationManager.createNotificationChannel(channel)
    return channelId
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun handlePlayerControllerEvent(event: PlayerControllerEvent) {
    if (event.action == PlayerControllerEvent.Action.UPDATE_EVENT) {
      showPlayerNotification()
    }
  }

  private fun smartStopForeground(removeNotification: Boolean) {
    if (removeNotification || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      stopForeground(removeNotification)
      return
    }
    stopForeground(STOP_FOREGROUND_DETACH)
  }

  private fun getPlayingSong(player: Player): Song? {
    return if (PlayerManager.getInstance().playqueue.isEmpty()) {
      null
    } else {
      PlayerManager.getInstance().playqueue[player.currentWindowIndex]
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    isRunning = false
    CodeUtils.unregisterFromEventBus(this)
    if (playerNotificationManager != null) {
      playerNotificationManager!!.setPlayer(null)
    }
    smartStopForeground(false)
  }

  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  companion object {
    private var isRunning = false
    private var isShowingNotification = false

    private const val NOTIFICATION_ID = 101

    private const val PLAYER_GROUP_CHANNEL_ID = "player_group_channel_id"
    private var PLAYER_CHANNEL_ID = "player_channel_id"
    private const val PLAYER_CHANNEL_NAME = "Player Controller"
    private const val PLAYER_CHANNEL_DESCRIPTION = "This category shows you the player controller"

    @JvmStatic
    fun startUpdate(context: Context) {
      if (isRunning) {
        PlayerControllerEvent.postUpdatePlayerControllers()
        return
      }
      val intent = Intent(context, PlayerService::class.java)
          .setAction(PlayerActions.ACTION_START)
      startService(context, intent)
    }

    @JvmStatic
    fun stop(context: Context) {
      if (isRunning) {
        val intent = Intent(context, PlayerService::class.java)
            .setAction(PlayerActions.ACTION_STOP)
        startService(context, intent)
      }
    }

    private fun startService(context: Context, intent: Intent) {
      if (DeviceUtils.isOreo()) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }
  }
}