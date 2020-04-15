package com.batache.songs.player.playqueue;

import com.batache.songs.model.pojo.Song;

import java.util.ArrayList;
import java.util.List;

public class Playqueue {

  private List<Song> songs = new ArrayList<>();

  private String playlistId;
  private int index;

  private boolean isStreaming = false;
  private Song streamSong;

}
