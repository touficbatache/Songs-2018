package com.batache.songs.app.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.batache.songs.api.SongsAPI
import com.batache.songs.model.pojo.Song
import com.batache.songs.util.SongUtils

class ExploreViewModel : ViewModel() {

  private val _maxResults = "5"

  val exploreLiveData: MediatorLiveData<ExploreLiveData> = MediatorLiveData()

  init {
    initializeState()
  }

  private fun initializeState() {
    exploreLiveData.value = ExploreLiveData(
        newThisWeekSongs = listOf(),
        popularSongs = listOf(),
        isNewThisWeekCallCompleted = false,
        isPopularCallCompleted = false
    )
  }

  fun fetchPlaylists() {
    SongsAPI.getInstance().getSongsFromPlaylist(SongUtils.NEW_THIS_WEEK_PLAYLIST_ID, _maxResults, object : SongsAPI.OnSongsFetchedListener {
      override fun onSuccess(fetchedSongs: MutableList<Song>?) {
        fetchedSongs?.let { newThisWeekSongs ->
          exploreLiveData.value = exploreLiveData.value?.copy(
              newThisWeekSongs = newThisWeekSongs,
              isNewThisWeekCallCompleted = true
          )
        }
      }

      override fun onFailure(t: Throwable?) {
        exploreLiveData.value = exploreLiveData.value?.copy(
            isNewThisWeekCallCompleted = true
        )
      }

    })

    SongsAPI.getInstance().getSongsFromPlaylist(SongUtils.POPULAR_PLAYLIST_ID, _maxResults, object : SongsAPI.OnSongsFetchedListener {
      override fun onSuccess(fetchedSongs: MutableList<Song>?) {
        fetchedSongs?.let { popularSongs ->
          exploreLiveData.value = exploreLiveData.value?.copy(
              popularSongs = popularSongs,
              isPopularCallCompleted = true
          )
        }
      }

      override fun onFailure(t: Throwable?) {
        exploreLiveData.value = exploreLiveData.value?.copy(
            isPopularCallCompleted = true
        )
      }

    })
  }
}

data class ExploreLiveData(
    val newThisWeekSongs: List<Song>,
    val popularSongs: List<Song>,
    val isNewThisWeekCallCompleted: Boolean,
    val isPopularCallCompleted: Boolean
)