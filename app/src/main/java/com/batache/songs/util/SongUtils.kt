package com.batache.songs.util

import android.net.Uri
import com.batache.songs.api.youtube.YoutubePlaylistResponse
import com.batache.songs.db.Deserializers.UriInOut
import com.batache.songs.model.pojo.Song
import com.batache.songs.model.pojo.Song.SongSource
import com.cyl.musicapi.playlist.MusicInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.Download
import java.util.*

object SongUtils {
  const val SONG_EXTRA = "SONG_EXTRA"

  const val PLAYLIST_LIKES_ID = "1"
  const val PLAYLIST_DOWNLOADS_ID = "2"

  const val NEW_THIS_WEEK_PLAYLIST_ID = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth"
  const val POPULAR_PLAYLIST_ID = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI"
  const val API_KEY = "YOUR_YOUTUBE_API_KEY"
  const val API_KEY_2 = "YOUR_YOUTUBE_API_KEY_2"
  const val AS_API_KEY = "YOUR_LASTFM_API_KEY" //AudioScrobbler API Key

  const val SEARCH_KEY_QQ = "QQ"
  const val SEARCH_KEY_XIAMI = "XIAMI"
  const val SEARCH_KEY_NETEASE = "NETEASE"

  const val SONG_KEY_QQ = "qq"
  const val SONG_KEY_XIAMI = "xiami"
  const val SONG_KEY_BAIDU = "baidu"
  const val SONG_KEY_NETEASE = "netease"

  private const val PIC_SIZE_SMALL = "90"
  private const val PIC_SIZE_NORMAL = "150"
  private const val PIC_SIZE_BIG = "300"

  @JvmStatic
  fun toString(song: Song?): String {
    return GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriInOut())
        .create()
        .toJson(song)
  }

  @JvmStatic
  fun getSong(download: Download): Song {
    return GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriInOut())
        .create()
        .fromJson(download.extras.map[SONG_EXTRA], Song::class.java)
  }

  @JvmStatic
  fun getSongFileName(songId: String, songSource: SongSource): String {
    return "${songId}-${songSource.name}.m4a"
  }

  @JvmStatic
  fun getSongId(fileName: String): String {
    return fileName.split(".m4a").toTypedArray()[0]
  }

  fun getSong(musicInfo: MusicInfo): Song {
    val song = Song()
    if (musicInfo.songId != null) {
      song.id = musicInfo.songId!!
    } else if (musicInfo.id != null) {
      song.id = musicInfo.id!!
    }
    song.title = musicInfo.name
//    song.type = musicInfo.vendor
//    song.album = musicInfo.album.name
//    song.albumId = musicInfo.album.id
//    song.isCp = musicInfo.cp
//    song.isDl = musicInfo.dl
//    musicInfo.quality?.let {
//      song.sq = it.sq
//      song.hq = it.hq
//      song.high = it.high
//    }
    if (musicInfo.artists != null) {
      val artistIds = StringBuilder(musicInfo.artists!![0].id)
      val artistNames = StringBuilder(musicInfo.artists!![0].name)

      for (i in 1 until musicInfo.artists!!.size) {
        artistIds.append(", ").append(musicInfo.artists!![i].id)
        artistNames.append(", ").append(musicInfo.artists!![i].name)
      }

      song.artist = artistNames.toString()
      //      song.artistId = artistIds.toString()
    }
    musicInfo.album.cover?.let { coverUrl ->
      musicInfo.vendor?.let { musicInfoVendor ->
        song.thumbnails = Song.Thumbnails(
            getAlbumPic(coverUrl, musicInfoVendor, PIC_SIZE_NORMAL),
            getAlbumPic(coverUrl, musicInfoVendor, PIC_SIZE_SMALL),
            getAlbumPic(coverUrl, musicInfoVendor, PIC_SIZE_NORMAL),
            getAlbumPic(coverUrl, musicInfoVendor, PIC_SIZE_BIG),
            getAlbumPic(coverUrl, musicInfoVendor, PIC_SIZE_BIG)
        )
      }
    }
//    song.coverBig = getAlbumPic(musicInfo.album.cover, musicInfo.vendor, PIC_SIZE_NORMAL);
//    song.coverSmall = getAlbumPic(musicInfo.album.cover, musicInfo.vendor, PIC_SIZE_SMALL);

    // TODO: set song uri
    return song
  }

  @JvmStatic
  fun getThumbnail(thumbnails: YoutubePlaylistResponse.Thumbnails): String {
    return when {
      thumbnails.thumbnail_default != null -> thumbnails.thumbnail_default.url
      thumbnails.thumbnail_standard != null -> thumbnails.thumbnail_standard.url
      thumbnails.thumbnail_medium != null -> thumbnails.thumbnail_medium.url
      thumbnails.thumbnail_high != null -> thumbnails.thumbnail_high.url
      thumbnails.thumbnail_maxres != null -> thumbnails.thumbnail_maxres.url
      else -> ""
    }
  }

  private fun getAlbumPic(url: String, type: String, size: String): String {
    val urlHost: String

    return when (type) {
      SONG_KEY_QQ -> url.replace("150x150", "${size}x${size}")

      SONG_KEY_XIAMI -> {
        urlHost = url.split("@").toTypedArray()[0]
        "$urlHost@1e_1c_100Q_${size}w_${size}h"
      }

      SONG_KEY_NETEASE -> {
        urlHost = url.split("\\?").toTypedArray()[0]
        "$urlHost?param=${size}y${size}"
      }

      SONG_KEY_BAIDU -> {
        urlHost = url.split("@").toTypedArray()[0]
        "$urlHost@s_1,w_${size},h_${size}"
      }

      else -> ""
    }
  }

  @JvmStatic
  fun generateExportedPlaylist(playlistTitle: String?, playlistSongs: List<Song>): String {
    val exportedPlaylist = ExportedPlaylist(title = playlistTitle)

    for (song in playlistSongs) {
      exportedPlaylist.songs.add(
          ExportedSong(
              id = song.id,
              title = song.title,
              artist = song.artist,
              coverArt = song.getCoverArt(),
              lyricsUrl = song.lyricsUrl,
              areLyricsSynced = song.areLyricsSynced,
              creationDate = song.creationDate,
              songSource = song.songSource
          )
      )
    }

    return Gson().toJson(exportedPlaylist)
  }

  @JvmStatic
  fun getExportedPlaylist(exportedPlaylistJson: String?): ExportedPlaylist {
    return Gson().fromJson(exportedPlaylistJson, ExportedPlaylist::class.java)
  }

  data class ExportedPlaylist(
      var title: String? = null,
      var songs: MutableList<ExportedSong> = ArrayList()
  )

  data class ExportedSong(
      var id: String? = null,
      var title: String? = null,
      var artist: String? = null,
      var coverArt: String? = null,
      var lyricsUrl: String? = null,
      var areLyricsSynced: Boolean = false,
      var creationDate: Date? = null,
      var songSource: SongSource = SongSource.youtube
  )
}