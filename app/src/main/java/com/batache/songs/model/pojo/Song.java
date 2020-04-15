package com.batache.songs.model.pojo;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  @ColumnInfo(name = "uri")
  private Uri uri;

  @ColumnInfo(name = "lyricsurl")
  private String lyricsUrl;

  @ColumnInfo(name = "lyrics")
  private List<Lyric> lyrics = new ArrayList<>();

  @ColumnInfo(name = "lyricssynced")
  private boolean areLyricsSynced = false;

  @ColumnInfo(name = "creationdate")
  private Date creationDate;

  @ColumnInfo(name = "songsource")
  private SongSource songSource = SongSource.youtube;

  public Song() {

  }

  public Song(@NonNull String id, String title, String artist, String coverArt) {
    this.id = id;
    this.link = String.format("https://www.youtube.com/watch?v=%s", id);
    this.title = title != null ? title.trim() : "Unknown song";
    this.artist = artist != null ? artist.trim() : "Unknown artist";
    this.coverArt = coverArt;
    creationDate = new Date();
  }

  public Song(@NonNull String id, String link, String title, String artist, String coverArt) {
    this.id = id;
    this.link = link;
    this.title = title != null ? title.trim() : "Unknown song";
    this.artist = artist != null ? artist.trim() : "Unknown artist";
    this.coverArt = coverArt;
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

  public void setUri(Uri uri) {
    this.uri = uri;
  }

  public Uri getUri() {
    return uri;
  }

  public void setPath(File file) {
    this.uri = Uri.parse(file.getPath());
  }

  public File getPath() {
    if(isLocallyStored()) {
      return new File(uri.getPath());
    }
    return null;
  }

  public void setUrl(String url) {
    this.uri = Uri.parse(url);
    this.link = url;
  }

  public boolean isLocallyStored() {
    return uri != null && uri.getPath() != null && !uri.toString().contains("http://") && !uri.toString().contains("https://");
  }

  public void setLyricsUrl(String lyricsUrl) {
    this.lyricsUrl = lyricsUrl;
  }

  public String getLyricsUrl() {
    return lyricsUrl;
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

  public void setAreLyricsSynced(boolean areLyricsSynced) {
    this.areLyricsSynced = areLyricsSynced;
  }

  public boolean getAreLyricsSynced() {
    return areLyricsSynced;
  }

  public void setSongSource(SongSource songSource) {
    this.songSource = songSource;
  }

  public SongSource getSongSource() {
    return songSource;
  }

  public enum SongSource {
    youtube(0),
    xiami(1),
    dndnha(2);

    private int code;

    SongSource(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

}
