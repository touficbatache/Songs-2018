package com.batache.songs.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.batache.songs.db.LastPlayed.LastPlayedDao;
import com.batache.songs.db.Playlist.PlaylistDao;
import com.batache.songs.db.PlaylistSongJoin.PlaylistSongJoinDao;
import com.batache.songs.db.Song.SongDao;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.model.pojo.PlaylistSongJoin;

@Database(entities = {Playlist.class, Song.class, PlaylistSongJoin.class, LastPlayed.class}, version = 1, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class SongsDatabase extends RoomDatabase {

  public abstract PlaylistDao playlistDao();

  public abstract SongDao songDao();

  public abstract PlaylistSongJoinDao playlistSongJoinDao();

  public abstract LastPlayedDao lastPlayedDao();

  // This is migration from version 1 to version 2, for future versions, we'll need to create a new Migration
  private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      // Since we didn't alter the table, there's nothing else to do here.
      // However, if we added a column called "last_update" here's what should be done:
      // database.execSQL("ALTER TABLE users ADD COLUMN last_update INTEGER");
      // Refer to this article for more: https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
    }
  };

  private static SongsDatabase INSTANCE;

  public static SongsDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (SongsDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
            SongsDatabase.class, "songs_database")
            .allowMainThreadQueries()
//            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)
            .build();
        }
      }
    }
    return INSTANCE;
  }

}