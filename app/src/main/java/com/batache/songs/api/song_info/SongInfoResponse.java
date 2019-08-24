package com.batache.songs.api.song_info;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongInfoResponse {

  @SerializedName("results")
  @Expose
  public Results results = null;

  public class Results {

    @SerializedName("trackmatches")
    @Expose
    public TrackMatches trackMatches = null;

  }

  public class TrackMatches {

    @SerializedName("track")
    @Expose
    public List<Track> tracks = null;

  }

  public class Track {

    @SerializedName("name")
    @Expose
    public String name = null;
    @SerializedName("artist")
    @Expose
    public String artist = null;

  }


}
