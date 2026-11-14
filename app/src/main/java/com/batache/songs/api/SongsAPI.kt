package com.batache.songs.api

import com.batache.songs.R
import com.batache.songs.SongsApplication
import com.batache.songs.api.autocomplete.AutocompleteApiClient
import com.batache.songs.api.autocomplete.AutocompleteApiInterface
import com.batache.songs.api.captions.CaptionsApiClient
import com.batache.songs.api.captions.CaptionsApiInterface
import com.batache.songs.api.captions.CaptionsResponse
import com.batache.songs.api.cse.CSEApiClient
import com.batache.songs.api.cse.CSEApiInterface
import com.batache.songs.api.cse.CSESearchResponse
import com.batache.songs.api.genius.GeniusApiClient
import com.batache.songs.api.genius.GeniusApiInterface
import com.batache.songs.api.xiami.XiamiApiClient
import com.batache.songs.api.xiami.XiamiApiInterface
import com.batache.songs.api.xiami.XiamiSearchResponse
import com.batache.songs.api.youtube.*
import com.batache.songs.model.pojo.Lyric
import com.batache.songs.model.pojo.Song
import com.batache.songs.util.CodeUtils
import com.batache.songs.util.PrefUtils
import com.batache.songs.util.TextUtils
import okhttp3.ResponseBody
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

// we should always use the application context to avoid memory leaks
object SongsAPI {

  private val context: SongsApplication

  private val youtubeApiInterface: YoutubeApiInterface
  private val captionsApiInterface: CaptionsApiInterface
  private val xiamiApiInterface: XiamiApiInterface
  private val cseApiInterface: CSEApiInterface
  private val geniusApiInterface: GeniusApiInterface
  private val autocompleteApiInterface: AutocompleteApiInterface

  init {
    context = SongsApplication.get()

    youtubeApiInterface = YoutubeApiClient.getRetrofitClient().create(YoutubeApiInterface::class.java)
    captionsApiInterface = CaptionsApiClient.getRetrofitClient().create(CaptionsApiInterface::class.java)
    xiamiApiInterface = XiamiApiClient.getRetrofitClient().create(XiamiApiInterface::class.java)
    cseApiInterface = CSEApiClient.getRetrofitClient().create(CSEApiInterface::class.java)
    geniusApiInterface = GeniusApiClient.getRetrofitClient().create(GeniusApiInterface::class.java)
    autocompleteApiInterface = AutocompleteApiClient.getRetrofitClient().create(AutocompleteApiInterface::class.java)
  }

  fun getSongsFromPlaylist(playlistId: String?, maxResults: String?, onSongsFetchedListener: OnSongsFetchedListener) {
    val call = youtubeApiInterface.getSongsFromPlaylist(playlistId, maxResults)
    call.enqueue(object : Callback<YoutubePlaylistResponse?> {
      override fun onResponse(call: Call<YoutubePlaylistResponse?>, response: Response<YoutubePlaylistResponse?>) {
        if (response.body() != null) {
          val playlistItems = response.body()!!.items
          val fetchedSongs: MutableList<Song> = ArrayList()

          for (playlistItem in playlistItems) {
            val snippet = playlistItem.snippet
            val resourceId = snippet.resourceId
            val responseThumbnails = snippet.thumbnails

            val id = resourceId.videoId
            val title = snippet.title
            val author = snippet.channelTitle
            val thumbnails = Song.Thumbnails(
                responseThumbnails.thumbnail_default?.url,
                responseThumbnails.thumbnail_standard?.url,
                responseThumbnails.thumbnail_medium?.url,
                responseThumbnails.thumbnail_high?.url,
                responseThumbnails.thumbnail_maxres?.url
            )

            fetchedSongs.add(Song(id, title, author, thumbnails))
          }

          onSongsFetchedListener.onSuccess(fetchedSongs)
        }
      }

      override fun onFailure(call: Call<YoutubePlaylistResponse?>, t: Throwable) {
        onSongsFetchedListener.onFailure(t)
      }
    })
  }

  fun getSong(videoId: String?, onSongsFetchedListener: OnSongsFetchedListener) {
    val call = youtubeApiInterface.getSong(videoId)
    call.enqueue(object : Callback<YoutubeVideosResponse?> {
      override fun onResponse(call: Call<YoutubeVideosResponse?>, response: Response<YoutubeVideosResponse?>) {
        if (response.body() != null) {
          val playlistItems = response.body()!!.items
          val fetchedSongs: MutableList<Song> = ArrayList()

          for (playlistItem in playlistItems) {
            val snippet = playlistItem.snippet
            val responseThumbnails = snippet.thumbnails

            val id = playlistItem.id
            val title = snippet.title
            val author = snippet.channelTitle
            val thumbnails = Song.Thumbnails(
                responseThumbnails.thumbnail_default?.url,
                responseThumbnails.thumbnail_standard?.url,
                responseThumbnails.thumbnail_medium?.url,
                responseThumbnails.thumbnail_high?.url,
                responseThumbnails.thumbnail_maxres?.url
            )
            //            responseThumbnails.thumbnail_high?.url; // TODO: Add thumbnail size in settings

            fetchedSongs.add(Song(id, title, author, thumbnails))
          }

          if (!fetchedSongs.isEmpty()) {
            onSongsFetchedListener.onSuccess(fetchedSongs)
          } else {
            onSongsFetchedListener.onFailure(null)
          }
        }
      }

      override fun onFailure(call: Call<YoutubeVideosResponse?>, t: Throwable) {
        onSongsFetchedListener.onFailure(t)
      }
    })
  }

  @Throws(IOException::class)
  fun getSongs(videoIds: List<String?>): List<Song> {
    val fetchedSongs: MutableList<Song> = ArrayList()

    for (videoId in videoIds) {
      val call = youtubeApiInterface.getSong(videoId)
      val playlistItems = call.execute().body()!!.items
      if (playlistItems != null && !playlistItems.isEmpty()) {
        val playlistItem = playlistItems[0]

        val snippet = playlistItem.snippet
        val responseThumbnails = snippet.thumbnails

        val id = playlistItem.id
        val title = snippet.title
        val author = snippet.channelTitle
        val thumbnails = Song.Thumbnails(
            responseThumbnails.thumbnail_default?.url,
            responseThumbnails.thumbnail_standard?.url,
            responseThumbnails.thumbnail_medium?.url,
            responseThumbnails.thumbnail_high?.url,
            responseThumbnails.thumbnail_maxres?.url
        )

        fetchedSongs.add(Song(id, title, author, thumbnails))
      }
    }

    return fetchedSongs
  }

  fun searchYoutube(searchQuery: String, onSongsFetchedListener: OnSongsFetchedListener) {
    val call =
        if (PrefUtils.getInstance().getBoolean(PrefUtils.SEARCH_SHOW_MUSIC_ONLY_PREF, PrefUtils.SEARCH_SHOW_MUSIC_ONLY_DEFAULT_VALUE))
          youtubeApiInterface.searchMusicOnly("$searchQuery - Topic")
        else
          youtubeApiInterface.search(searchQuery)
    call.enqueue(object : Callback<YoutubeSearchResponse?> {
      override fun onResponse(call: Call<YoutubeSearchResponse?>, response: Response<YoutubeSearchResponse?>) {
        if (response.body() != null) {
          val playlistItems = response.body()!!.items

          if (CodeUtils.isEmpty(playlistItems)) {
            onSongsFetchedListener.onFailure(Throwable(context.getString(R.string.search_failed)))
            return
          }

          val fetchedSongs: MutableList<Song> = ArrayList()

          for (playlistItem in playlistItems) {
            val vidId = playlistItem!!.id
            val snippet = playlistItem.snippet
            val responseThumbnails = snippet.thumbnails

            val id = vidId.videoId
            // unescape html: replace characters like &amp; with &
            val title = StringEscapeUtils.unescapeHtml4(snippet.title)
            val author = if (snippet.channelTitle.toLowerCase().contains("- topic")) snippet.channelTitle.replace("- (?i)Topic".toRegex(), "").trim { it <= ' ' } else snippet.channelTitle
            val thumbnails = Song.Thumbnails(
                responseThumbnails.thumbnail_default?.url,
                responseThumbnails.thumbnail_standard?.url,
                responseThumbnails.thumbnail_medium?.url,
                responseThumbnails.thumbnail_high?.url,
                responseThumbnails.thumbnail_maxres?.url
            )

            fetchedSongs.add(Song(id, title, author, thumbnails))
          }

          onSongsFetchedListener.onSuccess(fetchedSongs)
        }
      }

      override fun onFailure(call: Call<YoutubeSearchResponse?>, t: Throwable) {
        onSongsFetchedListener.onFailure(t)
      }
    })
  }

  fun getAvailableCaptionLanguages(videoId: String?, onCaptionLanguagesFetchedListener: OnCaptionLanguagesFetchedListener) {
    val call = youtubeApiInterface.getAvailableCaptionLanguages(videoId)
    call.enqueue(object : Callback<YoutubeCaptionLanguagesResponse?> {
      override fun onResponse(call: Call<YoutubeCaptionLanguagesResponse?>, response: Response<YoutubeCaptionLanguagesResponse?>) {
        if (response.body() != null) {
          val captions = response.body()!!.items
          val captionLanguages: MutableMap<String, String> = HashMap()

          for (caption in captions) {
            if (caption.snippet.trackKind != "ASR") {
              captionLanguages[Locale(caption.snippet.language).displayLanguage] = caption.snippet.language
            }
          }

          onCaptionLanguagesFetchedListener.onSuccess(captionLanguages)
        }
      }

      override fun onFailure(call: Call<YoutubeCaptionLanguagesResponse?>, t: Throwable) {
        onCaptionLanguagesFetchedListener.onFailure(t)
      }
    })
  }

  fun getCaptions(songId: String?, language: String?, onCaptionsFetchedListener: OnLyricsFetchedListener) {
    val call = captionsApiInterface.getCaptions(songId, language)
    call.enqueue(object : Callback<CaptionsResponse?> {
      override fun onResponse(call: Call<CaptionsResponse?>, response: Response<CaptionsResponse?>) {
        if (response.body() != null) {
          val lyrics: MutableList<Lyric> = ArrayList()
          for (caption in response.body()!!.captions) {
            val seconds = if (caption.start.split("\\.".toRegex()).toTypedArray().isNotEmpty()) caption.start.split("\\.".toRegex()).toTypedArray()[0].toInt() else 0
            val millis: Int = if (caption.start.split("\\.".toRegex()).toTypedArray().size > 1) (caption.start.split("\\.".toRegex()).toTypedArray()[1] + "0").toInt() else 0
            lyrics.add(Lyric(TimeUnit.SECONDS.toMillis(seconds.toLong()).toInt() + millis, caption.text))
          }
          onCaptionsFetchedListener.onSuccess(lyrics)
        }
      }

      override fun onFailure(call: Call<CaptionsResponse?>, t: Throwable) {
        onCaptionsFetchedListener.onFailure(t)
      }
    })
  }

  fun searchXiami(searchQuery: String?, onSongsFetchedListener: OnSongsFetchedListener) {
    val call = xiamiApiInterface.search(searchQuery)
    call.enqueue(object : Callback<XiamiSearchResponse?> {
      override fun onResponse(call: Call<XiamiSearchResponse?>, response: Response<XiamiSearchResponse?>) {
        if (response.body() == null) {
          onSongsFetchedListener.onFailure(Throwable(context.getString(R.string.search_failed)))
          return
        }

        val data = response.body()!!.data
        val songs = data.songs

        if (CodeUtils.isEmpty(songs)) {
          onSongsFetchedListener.onFailure(Throwable(context.getString(R.string.search_failed)))
          return
        }

        val fetchedSongs: MutableList<Song> = ArrayList()

        for (song in songs) {
          val thumbnails = Song.Thumbnails(
              song!!.albumLogoUrl,
              song.albumLogoUrl,
              song.albumLogoUrl,
              song.albumLogoUrl,
              song.albumLogoUrl
          )
          val xiamiSong = Song(song.id, song.title, song.artistName, thumbnails)
          xiamiSong.setUrl(song.url)
          xiamiSong.lyricsUrl = song.lyricsUrl
          xiamiSong.songSource = Song.SongSource.xiami
          fetchedSongs.add(xiamiSong)
        }

        onSongsFetchedListener.onSuccess(fetchedSongs)
      }

      override fun onFailure(call: Call<XiamiSearchResponse?>, t: Throwable) {
        onSongsFetchedListener.onFailure(t)
      }
    })
  }

  fun getSongLyrics(songTitle: String?, songArtist: String, onLyricsFetchedListener: OnLyricsFetchedListener) {
    val searchTitle = TextUtils.removeBrackets(TextUtils.removeParenthesis(songTitle)).replace("HD", "").replace("-", "")

    val searchQuery = "$searchTitle $songArtist"

    val call = cseApiInterface.searchGenius(searchQuery)
    call.enqueue(object : Callback<CSESearchResponse?> {
      override fun onResponse(call: Call<CSESearchResponse?>, response: Response<CSESearchResponse?>) {
        if (response.body() == null || response.body()!!.items == null || response.body()!!.items.size <= 0) {
          onLyricsFetchedListener.onFailure(null)
          return
        }

        val path = response.body()!!.items[0].link
            .split("\\b(genius.com/)\\b".toRegex()).toTypedArray()[1]

        val call2 = geniusApiInterface.getLyrics(path)
        call2.enqueue(object : Callback<Document> {
          override fun onResponse(call: Call<Document>, response: Response<Document>) {
            val lyricsStr = StringEscapeUtils.unescapeHtml4(
                response.body()!!.select(".lyrics p").html() // remove everything between brackets
                    .replace("\\[[^\\]]*\\]".toRegex(), "") // remove html comments
                    .replace("(<!--)[^-]*-->".toRegex(), "") // replace newlines
                    .replace("<br>".toRegex(), "\n") // replace two lines with one
                    .replace("\n\n".toRegex(), "\n") // remove all tags
                    .replace("<[^>]*>".toRegex(), "")
            )

            if (lyricsStr == "") {
              onLyricsFetchedListener.onFailure(null)
              return
            }

            val lyrics: MutableList<Lyric> = ArrayList()
            for (lyricLine in ArrayList(Arrays.asList(*lyricsStr.split("\\n".toRegex()).toTypedArray()))) {
              lyrics.add(Lyric(lyricLine))
            }
            onLyricsFetchedListener.onSuccess(lyrics)
          }

          override fun onFailure(call: Call<Document>, t: Throwable) {
            onLyricsFetchedListener.onFailure(t)
          }
        })
      }

      override fun onFailure(call: Call<CSESearchResponse?>, t: Throwable) {
        onLyricsFetchedListener.onFailure(t)
      }
    })
  }

  fun getXiamiSongLyrics(lyricsUrl: String?, onLyricsFetchedListener: OnXiamiLyricsFetchedListener) {
    val call = xiamiApiInterface.getLyrics(lyricsUrl)
    call.enqueue(object : Callback<ResponseBody?> {
      override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
        if (response.body() != null) {
          var artist: String? = null
          val lyrics: MutableList<Lyric> = ArrayList()
          try {
            val responseRecieved = response.body()!!.string()

            if (responseRecieved.split("\\[ar:".toRegex()).toTypedArray().size > 1) {
              artist = responseRecieved.split("\\[ar:".toRegex()).toTypedArray()[1].split("]".toRegex()).toTypedArray()[0]
            }

            var lyricsStr = responseRecieved
                .replace("<[^>]*>".toRegex(), "")
                .replace("\\[x-trans][^\\r\\n]*\\r\\n".toRegex(), "")

            if (lyricsStr.split("\\[offset:0]".toRegex()).toTypedArray().size > 1) {
              lyricsStr = lyricsStr.split("\\[offset:0]".toRegex()).toTypedArray()[1]
            }

            val lyricLines: List<String> = ArrayList(Arrays.asList(*lyricsStr.split("\\n".toRegex()).toTypedArray()))
            var index = 0
            for (lyricLine in lyricLines) {
              val numberPat = Pattern.compile("\\d+")
              val matcher = numberPat.matcher(lyricLine)
              if (lyricLine.contains("[") && lyricLine.contains("]") && matcher.find()) {
                val timecode = lyricLine.split("\\[".toRegex()).toTypedArray()[1].split("]".toRegex()).toTypedArray()[0]
                val minutes = timecode.split("\\.".toRegex()).toTypedArray()[0].split(":".toRegex()).toTypedArray()[0].toInt()
                val seconds = timecode.split("\\.".toRegex()).toTypedArray()[0].split(":".toRegex()).toTypedArray()[1].toInt()
                val millis = timecode.split("\\.".toRegex()).toTypedArray()[1].toInt()
                val lyricText = if (lyricLine.split("]".toRegex()).toTypedArray().size > 1) lyricLine.split("]".toRegex()).toTypedArray()[1] else ""

                if (!CodeUtils.isEmpty(lyricText) || index != 0 && index != lyricLines.size - 1) {
                  lyrics.add(Lyric(TimeUnit.MINUTES.toMillis(minutes.toLong()).toInt() + TimeUnit.SECONDS.toMillis(seconds.toLong()).toInt() + millis, lyricText))
                }

                index++
              }
            }
          } catch (e: IOException) {
            e.printStackTrace()
          }
          onLyricsFetchedListener.onSuccess(artist, lyrics)
        }
      }

      override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
        onLyricsFetchedListener.onFailure(t)
      }
    })
  }

  fun searchDndnha(searchStr: String, onSongsFetchedListener: OnSongsFetchedListener) {
    val searchStringNoSpaces = searchStr.replace(" ", "+")

    val call = cseApiInterface.searchDndnha(searchStringNoSpaces)
    call.enqueue(object : Callback<CSESearchResponse?> {
      override fun onResponse(call: Call<CSESearchResponse?>, response: Response<CSESearchResponse?>) {
        val dndnhaSearchResult = response.body()

        if (dndnhaSearchResult == null || CodeUtils.isEmpty(dndnhaSearchResult.items)) {
          onSongsFetchedListener.onFailure(null)
          return
        }

        val songsResult: MutableList<Song> = ArrayList()
        for (i in 0 until Math.min(dndnhaSearchResult.items.size, 5)) {
          val item = dndnhaSearchResult.items[i]
          if (item.link.contains("listen")) {
//          String artist = item.pagemap.people != null && !item.pagemap.people.isEmpty() ? item.pagemap.people.get(0).name : "Unknown artist";

            val thumbnails = Song.Thumbnails(
                item.pagemap.cseImages[0].src,
                item.pagemap.cseImages[0].src,
                item.pagemap.cseImages[0].src,
                item.pagemap.cseImages[0].src,
                item.pagemap.cseImages[0].src
            )
            val song = Song(item.link.split("listen_".toRegex()).toTypedArray()[1].split("_".toRegex()).toTypedArray()[0], item.link, item.title.split("-".toRegex()).toTypedArray()[1].split("-".toRegex()).toTypedArray()[0], item.title.split("-".toRegex()).toTypedArray()[0].split("-".toRegex()).toTypedArray()[0], thumbnails)
            song.songSource = Song.SongSource.dndnha
            songsResult.add(song)
          }
        }

        onSongsFetchedListener.onSuccess(songsResult)
      }

      override fun onFailure(call: Call<CSESearchResponse?>, t: Throwable) {
        onSongsFetchedListener.onFailure(t)
      }
    })
  }

  fun getAutocomplete(query: String?, onAutocompleteFetchedListener: OnAutocompleteFetchedListener) {
    val call = autocompleteApiInterface.getAutocomplete(query)
    call.enqueue(object : Callback<List<Any>> {
      override fun onResponse(call: Call<List<Any>>, response: Response<List<Any>>) {
        val responseArray = response.body()!!

        if (responseArray.size <= 1) {
          onAutocompleteFetchedListener.onFailure(Throwable("Response size too small"))
          return
        }

        val suggestedQueries: List<String> = ArrayList(Arrays.asList(*responseArray[1].toString().replace("[", "").replace("]", "").split(",".toRegex()).toTypedArray()))

        onAutocompleteFetchedListener.onSuccess(suggestedQueries)
      }

      override fun onFailure(call: Call<List<Any>>, t: Throwable) {
        onAutocompleteFetchedListener.onFailure(t)
      }
    })
  }

  interface BaseApiListener {
    fun onFailure(t: Throwable?)
  }

  interface OnSongsFetchedListener : BaseApiListener {
    fun onSuccess(fetchedSongs: MutableList<Song>?)
  }

  interface OnLyricsFetchedListener : BaseApiListener {
    fun onSuccess(lyrics: List<Lyric>)
  }

  interface OnXiamiLyricsFetchedListener : BaseApiListener {
    fun onSuccess(artist: String?, lyrics: List<Lyric>)
  }

  interface OnCaptionLanguagesFetchedListener : BaseApiListener {
    fun onSuccess(captionLanguages: Map<String, String>)
  }

  interface OnAutocompleteFetchedListener : BaseApiListener {
    fun onSuccess(suggestedQueries: List<String>)
  }
}