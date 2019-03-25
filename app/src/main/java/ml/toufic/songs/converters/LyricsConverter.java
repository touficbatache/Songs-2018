package ml.toufic.songs.converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;
import ml.toufic.songs.model.Lyrics;

public class LyricsConverter {
  private static Gson gson = new Gson();

  @TypeConverter
  public static List<String> toStringList(String json) {
    return json == null ? Collections.emptyList() : gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
  }

  @TypeConverter
  public static String toString(List<String> list) {
    return gson.toJson(list);
  }
}
