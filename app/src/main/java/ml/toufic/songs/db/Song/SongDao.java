package ml.toufic.songs.db.Song;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Song;

@Dao
public interface SongDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PlayableSong playableSong);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Song song);

  @Update
  void update(PlayableSong playableSong);

  @Query("SELECT * FROM songs WHERE id = :songId")
  PlayableSong getSong(String songId);

  @Query("SELECT * FROM songs")
  List<PlayableSong> getAllSongs();

  @Query("UPDATE songs SET lastplayed = 0 AND lastposition = 0 WHERE id != :songId")
  void resetAllSongsExcept(String songId);

  @Query("SELECT * FROM songs WHERE lastPlayed = 1")
  PlayableSong getLastPlayedSong();

  @Delete
  void delete(PlayableSong playableSong);

  @Query("DELETE FROM songs WHERE id = :songId")
  void delete(String songId);

  @Query("DELETE FROM songs")
  void deleteAll();
}