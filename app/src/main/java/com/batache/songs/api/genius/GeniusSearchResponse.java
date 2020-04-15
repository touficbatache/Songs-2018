package com.batache.songs.api.genius;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeniusSearchResponse {

  @SerializedName("data")
  @Expose
  public Data data = null;

  public class Data {

    @SerializedName("songs")
    @Expose
    public List<Song> songs;

  }

  public class Song {

    @SerializedName("song_id")
    @Expose
    public String id = null;

    @SerializedName("song_name")
    @Expose
    public String title = null;

    @SerializedName("artist_name")
    @Expose
    public String artistName = null;

    @SerializedName("album_logo")
    @Expose
    public String albumLogoUrl = null;

    @SerializedName("listen_file")
    @Expose
    public String url = null;

    @SerializedName("lyric")
    @Expose
    public String lyricsUrl = null;

  }

}
