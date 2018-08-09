package ml.toufic.songs.model;

public class Song {

  private String id;
  private String link;
  private String title;
  private String artist;
  private String coverArt;

  public Song(String id, String title, String artist, String coverArt) {
    this.id = id;
    this.link = "https://www.youtube.com/watch?v=" + id;
    this.title = title;
    this.artist = artist;
    this.coverArt = coverArt;
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
