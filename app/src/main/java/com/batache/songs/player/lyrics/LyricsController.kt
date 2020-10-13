package com.batache.songs.player.lyrics

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.songs.model.adapter.LyricModel
import com.batache.songs.model.adapter.LyricModel_
import com.batache.songs.model.pojo.Lyric

class LyricsController : EpoxyController() {
  private var data: MutableList<ControllerLyric> = ArrayList()

  fun addLyrics(lyrics: List<Lyric>) {
    for (lyric in lyrics) {
      data.add(ControllerLyric(lyric, false))
    }
//    for (i in lyrics.indices) {
//      data.add(
//          LyricModel_()
//              .id("lyric_line_$i")
//              .lyricLine(lyrics[i].text)
//      )
//    }
    requestModelBuild()
  }

  fun setSelected(index: Int) {
    if (data.isNullOrEmpty()) {
      return
    }
    data.find { it.isSelected }?.isSelected = false
//    data.filter { it.isSelected }.forEach { it.isSelected = false }
    data[index].isSelected = true
    requestModelBuild()
  }

  fun reset() {
    data = ArrayList()
  }

  fun clear() {
    reset()
    requestModelBuild()
  }

  override fun buildModels() {
    for (i in data.indices) {
      LyricModel_().apply {
        id("lyric_line_$i")
        lyricLine(data[i].lyric.text)
        selected(data[i].isSelected)
        addTo(this@LyricsController)
      }
    }
  }
}

data class ControllerLyric (
  var lyric: Lyric,
  var isSelected: Boolean = false
)