package ml.toufic.songs.model;

import java.io.File;

public class PlayableSong {

  private int index;
  private File path;
  private String id;
  private String link;
  private String title;
  private String artist;
  private String coverArt;
  private int lastPosition = 0;
  private boolean looping = false;

  public PlayableSong(Song song) {
    //this.path = song.getPath;
    this.id = song.getId();
    this.link = "https://www.youtube.com/watch?v=" + id;
    this.title = song.getTitle();
    this.artist = song.getArtist();
    this.coverArt = song.getCoverArt();
  }

  public PlayableSong(DownloadedSong downloadedSong, int index) {
    this.index = index;
    this.path = downloadedSong.getPath();
    this.id = downloadedSong.getId();
    this.link = "https://www.youtube.com/watch?v=" + id;
    this.title = downloadedSong.getTitle();
    this.artist = downloadedSong.getArtist();
    this.coverArt = downloadedSong.getCoverArt();
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

  public void setLastPosition(int lastPosition) {
    this.lastPosition = lastPosition;
  }

  public int getLastPosition() {
    return lastPosition;
  }

  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  public boolean isLooping() {
    return looping;
  }

  public int getIndex() {
    return index;
  }
}
