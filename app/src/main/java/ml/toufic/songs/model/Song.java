package ml.toufic.songs.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
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

  public Song(@NonNull String id, String title, String artist, String coverArt) {
    this.id = id;
    this.link = "https://www.youtube.com/watch?v=" + id;
    this.title = title;
    this.artist = artist;
    this.coverArt = coverArt;
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
}
