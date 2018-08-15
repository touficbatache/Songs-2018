package ml.toufic.songs.db;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;

import ml.toufic.songs.model.Song;

public class Converters {
  @TypeConverter
  public static File fileFromString(String value) {
    return new File(value);
  }
  @TypeConverter
  public static String fromFile(File file) {
    return file.toString();
  }

  @TypeConverter
  public static Song songFromString(String value) {
    return new Gson().fromJson(value, new TypeToken<Song>(){}.getType());
  }
  @TypeConverter
  public static String fromSong(Song song) {
    return new Gson().toJson(song);
  }
}