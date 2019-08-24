package com.batache.songs.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.batache.songs.converters.LyricsConverter;
import com.batache.songs.converters.XiamiPropertiesConverter;

@Entity(tableName = "songs")
public class Song {

  //REPLACEMENT WILL BE DONE BASED ON ID: IF IT'S THE SAME, REPLACE
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "id")
  private String id;

  @ColumnInfo(name = "link")
  private String link;

  @ColumnInfo(name = "title")
  private String title;

  @ColumnInfo(name = "artist")
  private String artist;

  @ColumnInfo(name = "coverart")
  private String coverArt;

  @ColumnInfo(name = "xiamiproperties")
  @TypeConverters(XiamiPropertiesConverter.class)
  private XiamiProperties xiamiProperties;

  @ColumnInfo(name = "path")
  private File path;

  @ColumnInfo(name = "duration")
  private int duration;

  @ColumnInfo(name = "lyrics")
  @TypeConverters(LyricsConverter.class)
  private List<Lyric> lyrics = new ArrayList<>();

  @ColumnInfo(name = "creationdate")
  private Date creationDate;

  public Song() {

  }

  public Song(@NonNull String id, String title, String artist, String coverArt) {
    this.id = id;
    this.link = String.format("https://www.youtube.com/watch?v=%s", id);
    this.title = title;
    this.artist = artist;
    this.coverArt = coverArt;
    creationDate = new Date();
  }

  @Ignore
  protected Song(@NonNull String id, String link, String title, String artist, String coverArt, XiamiProperties xiamiProperties) {
    this.id = id;
    this.link = link;
    this.title = title;
    this.artist = artist;
    this.coverArt = coverArt;
    this.xiamiProperties = xiamiProperties;
    creationDate = new Date();
  }

  public void setId(@NonNull String id) {
    this.id = id;
  }

  @NonNull
  public String getId() {
    return id;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getLink() {
    return link;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getArtist() {
    return artist;
  }

  public void setCoverArt(String coverArt) {
    this.coverArt = coverArt;
  }

  public String getCoverArt() {
    return coverArt;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public static class XiamiProperties {

    @ColumnInfo(name = "xiamisongurl")
    private String songUrl;

    @ColumnInfo(name = "xiamilyricsurl")
    private String lyricsUrl;

    public XiamiProperties(String songUrl, String lyricsUrl) {
      this.songUrl = songUrl;
      this.lyricsUrl = lyricsUrl;
    }

    public void setSongUrl(String songUrl) {
      this.songUrl = songUrl;
    }

    public String getSongUrl() {
      return songUrl;
    }

    public void setLyricsUrl(String lyricsUrl) {
      this.lyricsUrl = lyricsUrl;
    }

    public String getLyricsUrl() {
      return lyricsUrl;
    }
  }

  public void setXiamiProperties(XiamiProperties xiamiProperties) {
    this.xiamiProperties = xiamiProperties;
  }

  public XiamiProperties getXiamiProperties() {
    return xiamiProperties;
  }

  public SongSource getSongSource() {
    if (xiamiProperties == null) {
      return SongSource.youtube;
    } else {
      return SongSource.xiami;
    }
  }

  public enum SongSource {
    youtube(0),
    xiami(1);

    private int code;

    SongSource(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  public void setPath(File songPath) {
    this.path = songPath;
  }

  public File getPath() {
    return path;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }

  public void setLyrics(List<Lyric> lyrics) {
    if(lyrics == null) {
      this.lyrics = new ArrayList<>();
      return;
    }
    this.lyrics = lyrics;
  }

  public List<Lyric> getLyrics() {
    return lyrics;
  }
}
