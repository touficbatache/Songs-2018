package com.batache.songs.model.pojo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "playlist_song_join",
    primaryKeys = {"playlistId", "songId"},
    foreignKeys = {
        @ForeignKey(
            entity = Playlist.class,
            parentColumns = "id",
            childColumns = "playlistId",
            onDelete = CASCADE),
        @ForeignKey(
            entity = Song.class,
            parentColumns = "id",
            childColumns = "songId",
            onDelete = CASCADE)
    }
)
public class PlaylistSongJoin {
  @NonNull
  public String playlistId;
  @NonNull
  public String songId;

  public PlaylistSongJoin(@NonNull String playlistId, @NonNull String songId) {
    this.playlistId = playlistId;
    this.songId = songId;
  }
}
