package com.batache.songs.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.batache.songs.model.Playlist;
import com.batache.songs.model.Song;

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

  @TypeConverter
  public static Playlist playlistFromString(String value) {
    return new Gson().fromJson(value, new TypeToken<Playlist>(){}.getType());
  }
  @TypeConverter
  public static String fromPlaylist(Playlist playlist) {
    return new Gson().toJson(playlist);
  }

  @TypeConverter
  public static Date dateFromString(String value) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    try {
      date = format.parse(value);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return date;
  }
  @TypeConverter
  public static String fromDate(Date date) {
    String dateTime;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateTime = dateFormat.format(date);
    return dateTime;
  }
}