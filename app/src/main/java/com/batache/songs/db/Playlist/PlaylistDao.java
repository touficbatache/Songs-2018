package com.batache.songs.db.Playlist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.batache.songs.model.pojo.Playlist;

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

  @Query("SELECT * FROM playlists ORDER BY creationdate DESC")
  List<Playlist> getAllPlaylistsDesc();

  @Query("SELECT * FROM playlists ORDER BY creationdate ASC")
  LiveData<List<Playlist>> getAllPlaylistsLive();

  @Delete
  void delete(Playlist playlist);

  @Query("DELETE FROM playlists WHERE id = :playlistId")
  void delete(String playlistId);

  @Query("DELETE FROM playlists")
  void deleteAll();
}