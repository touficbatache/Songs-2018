package com.batache.songs.converters;

import com.batache.songs.model.Lyric;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;

public class LyricsConverter {
  private static Gson gson = new Gson();

  @TypeConverter
  public static List<Lyric> toStringList(String json) {
    return json == null ? Collections.emptyList() : gson.fromJson(json, new TypeToken<List<Lyric>>() {}.getType());
  }

  @TypeConverter
  public static String toString(List<Lyric> list) {
    return gson.toJson(list);
  }
}
