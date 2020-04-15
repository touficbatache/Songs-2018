package com.batache.songs.api.cse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CSESearchResponse {

  @SerializedName("items")
  @Expose
  public List<Item> items = null;

  public class Item {

    @SerializedName("link")
    @Expose
    public String link = null;

    @SerializedName("title")
    @Expose
    public String title = null;

    @SerializedName("pagemap")
    @Expose
    public Pagemap pagemap = null;

  }

  public class Pagemap {

    @SerializedName("person")
    @Expose
    public List<Person> people = null;

    @SerializedName("cse_image")
    @Expose
    public List<CseImage> cseImages = null;

  }

  public class CseImage {

    @SerializedName("src")
    @Expose
    public String src = null;

  }

  public class Person {

    @SerializedName("name")
    @Expose
    public String name = null;

  }

}
