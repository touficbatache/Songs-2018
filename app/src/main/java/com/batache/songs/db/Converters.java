package com.batache.songs.db;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.batache.songs.model.pojo.Lyric;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.batache.songs.model.pojo.Song;

public class Converters {
  private static Gson gson = new Gson();

  @TypeConverter
  public static Uri uriFromString(String value) {
    if(value == null) {
      return null;
    }
    return Uri.parse(value);
  }
  @TypeConverter
  public static String fromUri(Uri uri) {
    if(uri == null) {
      return null;
    }
    return uri.toString();
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

  @TypeConverter
  public static List<Lyric> toStringList(String json) {
    return json == null ? Collections.emptyList() : gson.fromJson(json, new TypeToken<List<Lyric>>() {}.getType());
  }

  @TypeConverter
  public static String toString(List<Lyric> list) {
    return gson.toJson(list);
  }

  @TypeConverter
  public static Song.SongSource songSourceFromString(String value) {
    return value == null ? Song.SongSource.youtube : new Gson().fromJson(value, new TypeToken<Song.SongSource>(){}.getType());
  }
  @TypeConverter
  public static String fromSongSource(Song.SongSource songSource) {
    return new Gson().toJson(songSource);
  }
}