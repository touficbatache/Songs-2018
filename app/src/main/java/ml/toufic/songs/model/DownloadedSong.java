package ml.toufic.songs.model;

import java.io.File;

public class DownloadedSong {

  private File path;

  private String id;
  private String link;
  private String title;
  private String artist;
  private String coverArt;

  public DownloadedSong(File songPath, Song song) {
    this.path = songPath;
    this.id = song.getId();
    this.link = "https://www.youtube.com/watch?v=" + id;
    this.title = song.getTitle();
    this.artist = song.getArtist();
    this.coverArt = song.getCoverArt();
  }

  public void setPath(File songPath) {
    this.path = songPath;
  }

  public File getPath() {
    return path;
  }

  public void setId(String id) {
    this.id = id;
  }

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
