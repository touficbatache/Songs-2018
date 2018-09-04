package ml.toufic.songs.db.Song;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

@Dao
public interface SongDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PlayableSong playableSong);

  @Update
  void update(PlayableSong playableSong);

  @Query("SELECT * FROM songs WHERE id = :songId")
  PlayableSong getSong(String songId);

  @Query("SELECT * FROM songs")
  List<PlayableSong> getAllSongs();

  @Query("SELECT * FROM songs ORDER BY `index` ASC")
  LiveData<List<PlayableSong>> getAllSongsLive();

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