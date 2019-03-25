package ml.toufic.songs.model;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import ml.toufic.songs.converters.LyricsConverter;

@Entity(tableName = "lyrics")
public class Lyrics {

  //REPLACEMENT WILL BE DONE BASED ON ID: IF IT'S THE SAME, REPLACE
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "songId")
  private String songId;

  @ColumnInfo(name = "lyrics")
  @TypeConverters(LyricsConverter.class)
  private List<String> lyrics;

  public Lyrics(String songId, List<String> lyrics) {
    this.songId = songId;
    this.lyrics = lyrics;
  }

  public void setSongId(@NonNull String songId) {
    this.songId = songId;
  }

  @NonNull
  public String getSongId() {
    return songId;
  }

  public void setLyrics(List<String> lyrics) {
    this.lyrics = lyrics;
  }

  public List<String> getLyrics() {
    return lyrics;
  }
}
