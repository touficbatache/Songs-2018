package com.batache.songs.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.batache.songs.model.Song;

public class XiamiPropertiesConverter {

  private static Gson gson = new Gson();
  @TypeConverter
  public static Song.XiamiProperties stringToXproperties(String data) {
    if (data == null) {
      return null;
    }

    return gson.fromJson(data, new TypeToken<Song.XiamiProperties>() {}.getType());
  }

  @TypeConverter
  public static String toString(Song.XiamiProperties someObjects) {
    return gson.toJson(someObjects);
  }
}
