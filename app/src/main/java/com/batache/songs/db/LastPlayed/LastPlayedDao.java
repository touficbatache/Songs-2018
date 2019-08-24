package com.batache.songs.db.LastPlayed;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.batache.songs.model.LastPlayed;

@Dao
public interface LastPlayedDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void setLastPlayed(LastPlayed lastPlayed);

  @Query("SELECT * FROM lastplayed WHERE id = '1'")
  LastPlayed getLastPlayed();

}