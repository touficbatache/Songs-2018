package ml.toufic.songs.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;

@Dao
public interface DownloadedSongsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(PlayableSong playableSong);

  @Query("DELETE FROM downloaded_songs_table")
  void deleteAll();

//  @Query("SELECT * from downloaded_songs_table")
//  List<PlayableSong> getAllSongs();

  @Query("SELECT * from downloaded_songs_table ORDER BY `index` ASC")
  LiveData<List<PlayableSong>> getAllSongs();
}