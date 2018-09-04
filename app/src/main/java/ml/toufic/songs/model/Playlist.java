package ml.toufic.songs.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "playlists")
public class Playlist {

  //REPLACEMENT WILL BE DONE BASED ON ID: IF IT'S THE SAME, REPLACE
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "id")
  private String id;

  @ColumnInfo(name = "title")
  private String title;

  @ColumnInfo(name = "creationdate")
  private Date creationDate;

  @ColumnInfo(name = "lastopened")
  private boolean lastOpened;

  public Playlist(@NonNull String id, String title, Date creationDate) {
    this.id = id;
    this.title = title;
    this.creationDate = creationDate;
  }

  public void setId(@NonNull String id) {
    this.id = id;
  }

  @NonNull
  public String getId() {
    return id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setLastOpened(boolean lastOpened) {
    this.lastOpened = lastOpened;
  }

  public boolean getLastOpened() {
    return lastOpened;
  }

}
