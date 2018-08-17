package ml.toufic.songs.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import ml.toufic.songs.model.PlayableSong;

@Database(entities = {PlayableSong.class}, version = 1, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class DownloadedSongsDatabase extends RoomDatabase {

  public abstract DownloadedSongsDao downloadedSongsDao();

  private static DownloadedSongsDatabase INSTANCE;

  public static DownloadedSongsDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (DownloadedSongsDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
            DownloadedSongsDatabase.class, "downloaded_songs_database")
            .allowMainThreadQueries()
            .build();
        }
      }
    }
    return INSTANCE;
  }

}