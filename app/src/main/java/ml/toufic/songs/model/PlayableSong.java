package ml.toufic.songs.model;

import java.io.File;

public class PlayableSong extends Song {

  private File path;
  private int indexInQueue = 0;
  private int lastPosition = 0;
  private boolean looping = false;

  public PlayableSong(Song song, File songPath) {
    super(song.getId(), song.getTitle(), song.getArtist(), song.getCoverArt());

    this.path = songPath;
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
}
