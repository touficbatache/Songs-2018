package ml.toufic.songs.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "playlist_song_join",
        primaryKeys = { "playlistId", "songId" },
        foreignKeys = {
            @ForeignKey(entity = Playlist.class,
              parentColumns = "id",
              childColumns = "playlistId"),
            @ForeignKey(entity = PlayableSong.class,
              parentColumns = "id",
              childColumns = "songId",
              onDelete = CASCADE)
          }
        )
public class PlaylistSongJoin {
  @NonNull public String playlistId;
  @NonNull public String songId;

  public PlaylistSongJoin(String playlistId, String songId) {
    this.playlistId = playlistId;
    this.songId = songId;
  }
}
