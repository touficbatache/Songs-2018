package com.batache.songs.api.youtube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YoutubeCaptionLanguagesResponse {

  @SerializedName("items")
  @Expose
  public List<Item> items = null;

  public class Item {

    @SerializedName("snippet")
    @Expose
    public Snippet snippet = null;

  }

  public class Snippet {

    @SerializedName("trackKind")
    @Expose
    public String trackKind;

    @SerializedName("language")
    @Expose
    public String language;

  }

}
