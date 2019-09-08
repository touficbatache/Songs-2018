package com.batache.songs.model.pojo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lastplayed")
public class LastPlayed {

  //REPLACEMENT WILL BE DONE BASED ON ID: IF IT'S THE SAME, REPLACE
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "id")
  private String id = "1";

  @ColumnInfo(name = "songIndex")
  private int songIndex;

  @ColumnInfo(name = "playlistId")
  private String playlistId;

  @ColumnInfo(name = "position")
  private long position;

  public LastPlayed(String playlistId, int songIndex, long position) {
    this.playlistId = playlistId;
    this.songIndex = songIndex;
    this.position = position;
  }

  @NonNull
  public String getId() {
    return id;
  }

  public void setId(@NonNull String id) {
    this.id = id;
  }

  public void setSongIndex(int songIndex) {
    this.songIndex = songIndex;
  }

  public int getSongIndex() {
    return songIndex;
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }

  public String getPlaylistId() {
    return playlistId;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public long getPosition() {
    return position;
  }
}
