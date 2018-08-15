package ml.toufic.songs.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.File;

@Entity(tableName = "downloaded_songs_table")
public class PlayableSong extends Song {

  @ColumnInfo(name = "song")
  private Song song;

  @ColumnInfo(name = "path")
  private File path;

  //REPLACEMENT WILL BE DONE BASED ON ID: IF IT'S THE SAME, REPLACE
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "index")
  private int indexInQueue = 0;

  @ColumnInfo(name = "lastposition")
  private int lastPosition = 0;

  @ColumnInfo(name = "looping")
  private boolean looping = false;

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

  public void setIndexInQueue(int index) {
    indexInQueue = index;
  }

  public int getIndexInQueue() {
    return indexInQueue;
  }

  @Override
  public String toString() {
    return "{\"id\":" + getId() + ", \"title\":" + getTitle() + ", \"artist\":" + getArtist() + ", \"coverArt\":" + getCoverArt() + ", \"path\":" + path.toString() + "}";
  }
}
