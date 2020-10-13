package com.batache.songs.app.explore

import android.view.View
import androidx.navigation.Navigation
import com.airbnb.epoxy.EpoxyController
import com.batache.songs.model.adapter.NoConnectionModel_
import com.batache.songs.model.adapter.SongModel_
import com.batache.songs.model.adapter.SubheaderModel_
import com.batache.songs.model.pojo.Song
import java.util.*

class ExploreController : EpoxyController() {

  private var data: List<SongsSection> = ArrayList()
  private var showNoConnection: Boolean = false
  private var myMusicBtnListener: View.OnClickListener? = null

  fun setData(sections: List<SongsSection>) {
    this.data = sections
    requestModelBuild()
  }

  fun showNoConnection(showNoConnection: Boolean, myMusicBtnListener: View.OnClickListener?) {
    this.showNoConnection = showNoConnection
    this.myMusicBtnListener = myMusicBtnListener
    requestModelBuild()
  }

  override fun buildModels() {
    if (showNoConnection) {
      NoConnectionModel_().apply {
        id("no_connection")
        onClickListener(myMusicBtnListener)
        addTo(this@ExploreController)
      }
      return
    }

    var sectionIndice = 0
    data.forEach { section ->
      section.title?.let { sectionTitle ->
        SubheaderModel_().apply {
          id("section_${sectionIndice}")
          title(section.title)
          onClickListener { view ->
            val action = ExploreFragmentDirections.playlistAction(sectionTitle, section.playlistId)
            Navigation.findNavController(view).navigate(action)
          }
          addTo(this@ExploreController)
        }
      }

      var songIndice = 0
      section.songs.forEach { song ->
        SongModel_().apply {
          id("section_${sectionIndice}_song_${songIndice}")
          song(song)
          showArtist(false)
          addTo(this@ExploreController)
        }
        songIndice++
      }

      sectionIndice++
    }
  }
}

data class SongsSection(
    val playlistId: String,
    val title: String?,
    val songs: List<Song>
)