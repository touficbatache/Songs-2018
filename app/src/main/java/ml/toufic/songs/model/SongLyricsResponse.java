package ml.toufic.songs.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongLyricsResponse {

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
