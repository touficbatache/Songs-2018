package com.batache.songs.ui.controller;

import com.batache.songs.model.Lyric;
import com.batache.songs.ui.base.BaseController;
import com.batache.songs.ui.model.LyricModel_;

import java.util.ArrayList;
import java.util.List;

public class LyricsController extends BaseController {

  private List<Lyric> lyrics = new ArrayList<>();

  public void addLyrics(List<Lyric> lyrics) {
    this.lyrics = lyrics;
    for (Lyric lyric : lyrics) {
      data.add(
          new LyricModel_()
              .lyricLine(lyric.getText())
      );
    }
    requestModelBuild();
  }

  public void setSelected(int index) {
    if(lyrics.isEmpty()) {
      return;
    }
    reset();
    for (int i = 0; i < lyrics.size(); i++) {
      data.add(
          new LyricModel_()
              .lyricLine(lyrics.get(i).getText())
              .selected(i == index)
      );
    }
    requestModelBuild();
  }

}
