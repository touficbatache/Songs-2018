package ml.toufic.songs.db;

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
public interface DownloadedSongsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PlayableSong playableSong);

  @Query("SELECT * FROM downloaded_songs_table WHERE id = :songId")
  PlayableSong getSong(String songId);

  @Query("UPDATE downloaded_songs_table SET lastplayed = 0 WHERE id != :songId")
  void resetAllLastPlayedSongs(String songId);

  @Update
  void setLastPlayedSong(PlayableSong playableSong);

  @Query("SELECT * FROM downloaded_songs_table WHERE lastPlayed = 1")
  PlayableSong getLastPlayedSong();

  @Update
  void update(PlayableSong playableSong);

  @Query("SELECT * FROM downloaded_songs_table ORDER BY `index` ASC")
  LiveData<List<PlayableSong>> getAllSongs();

  @Query("DELETE FROM downloaded_songs_table WHERE id = :songId")
  void delete(String songId);

  @Delete
  void delete(PlayableSong playableSong);

  @Query("DELETE FROM downloaded_songs_table")
  void deleteAll();
}