package ml.toufic.songs.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.io.File;

@Entity(tableName = "songs")
public class PlayableSong extends Song {

  @ColumnInfo(name = "song")
  private Song song;

  @ColumnInfo(name = "path")
  private File path;

  @ColumnInfo(name = "durationinms")
  private int durationInMs;

  @ColumnInfo(name = "lastposition")
  private int lastPosition = 0;

  @ColumnInfo(name = "lastplayed")
  private boolean lastPlayed = false;

  public PlayableSong(Song song, File path) {
    super(song.getId(), song.getTitle(), song.getArtist(), song.getCoverArt());

    this.song = song;
    this.path = path;
  }

  public Song getSong() {
    return song;
  }

  public void setPath(File songPath) {
    this.path = songPath;
  }

  public File getPath() {
    return path;
  }

  public void setDurationInMs(int durationInMs) {
    this.durationInMs = durationInMs;
  }

  public int getDurationInMs() {
    return durationInMs;
  }

  public void setLastPosition(int lastPosition) {
    this.lastPosition = lastPosition;
  }

  public int getLastPosition() {
    return lastPosition;
  }

  public void setLastPlayed(boolean lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  public boolean getLastPlayed() {
    return lastPlayed;
  }
}
