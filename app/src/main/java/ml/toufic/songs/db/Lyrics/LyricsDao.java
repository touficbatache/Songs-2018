package ml.toufic.songs.db.Lyrics;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import ml.toufic.songs.model.Lyrics;

@Dao
public interface LyricsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Lyrics lyrics);

  @Query("SELECT * FROM lyrics WHERE songId = :songId")
  Lyrics getLyrics(String songId);

  @Query("DELETE FROM lyrics WHERE songId = :songId")
  void delete(String songId);

}