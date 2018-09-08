package ml.toufic.songs.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import ml.toufic.songs.db.Playlist.PlaylistDao;
import ml.toufic.songs.db.PlaylistSongJoin.PlaylistSongJoinDao;
import ml.toufic.songs.db.Song.SongDao;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.Song;
import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.PlaylistSongJoin;

@Database(entities = {Playlist.class, Song.class, PlayableSong.class, PlaylistSongJoin.class}, version = 2, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class SongsDatabase extends RoomDatabase {

  public abstract PlaylistDao playlistDao();

  public abstract SongDao songDao();

  public abstract PlaylistSongJoinDao playlistSongJoinDao();

  private static SongsDatabase INSTANCE;

  public static SongsDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (SongsDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
            SongsDatabase.class, "songs_database")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build();
        }
      }
    }
    return INSTANCE;
  }

}