package ml.toufic.songs.db.Playlist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ml.toufic.songs.model.Playlist;

@Dao
public interface PlaylistDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Playlist playlist);

  @Update
  void update(Playlist playlist);

  @Query("SELECT * FROM playlists WHERE id = :playlistId")
  Playlist getPlaylist(String playlistId);

  @Query("SELECT * FROM playlists ORDER BY creationdate ASC")
  List<Playlist> getAllPlaylists();

  @Query("SELECT * FROM playlists ORDER BY creationdate ASC")
  LiveData<List<Playlist>> getAllPlaylistsLive();

  @Query("UPDATE playlists SET lastOpened = 0 WHERE id != :playlistId")
  void resetAllPlaylistsExcept(String playlistId);

  @Query("SELECT * FROM playlists WHERE lastOpened = 1")
  Playlist getLastOpenedPlaylist();

  @Delete
  void delete(Playlist playlist);

  @Query("DELETE FROM playlists WHERE id = :playlistId")
  void delete(String playlistId);

  @Query("DELETE FROM playlists")
  void deleteAll();
}