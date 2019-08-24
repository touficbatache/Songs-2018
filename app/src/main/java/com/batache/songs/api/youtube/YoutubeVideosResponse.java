package com.batache.songs.api.youtube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YoutubeVideosResponse {

  @SerializedName("items")
  @Expose
  public List<Item> items = null;

  public class Item {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("snippet")
    @Expose
    public Snippet snippet = null;

  }

  public class Snippet {

    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("thumbnails")
    @Expose
    public Thumbnails thumbnails = null;
    @SerializedName("channelTitle")
    @Expose
    public String channelTitle;

  }

  public class Thumbnails {

    @SerializedName("default")
    @Expose
    public Thumbnail thumbnail_default = null;

    @SerializedName("medium")
    @Expose
    public Thumbnail thumbnail_medium = null;

    @SerializedName("high")
    @Expose
    public Thumbnail thumbnail_high = null;

    @SerializedName("standard")
    @Expose
    public Thumbnail thumbnail_standard = null;

    @SerializedName("maxres")
    @Expose
    public Thumbnail thumbnail_maxres = null;

  }

  public class Thumbnail {

    @SerializedName("url")
    @Expose
    public String url = null;

  }

}
