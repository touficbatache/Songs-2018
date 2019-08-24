package com.batache.songs.db.Song;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.batache.songs.model.Song;

@Dao
public interface SongDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Song song);

  @Update
  void update(Song song);

  @Query("SELECT * FROM songs WHERE id = :songId")
  Song getSong(String songId);

  @Query("SELECT * FROM songs")
  List<Song> getAllSongs();

  @Delete
  void delete(Song song);

  @Query("DELETE FROM songs WHERE id = :songId")
  void delete(String songId);

  @Query("DELETE FROM songs")
  void deleteAll();
}