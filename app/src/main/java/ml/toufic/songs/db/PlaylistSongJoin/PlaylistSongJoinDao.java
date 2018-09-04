package ml.toufic.songs.db.PlaylistSongJoin;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ml.toufic.songs.model.PlayableSong;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.model.PlaylistSongJoin;

@Dao
public interface PlaylistSongJoinDao {

  @Insert
  void insert(PlaylistSongJoin playlistSongJoin);

  @Query("SELECT * FROM playlists INNER JOIN playlist_song_join ON playlists.id=playlist_song_join.playlistId WHERE playlist_song_join.songId = :songId")
  List<Playlist> getPlaylistsForSong(String songId);

  @Query("SELECT * FROM songs INNER JOIN playlist_song_join ON songs.id=playlist_song_join.songId WHERE playlist_song_join.playlistId = :playlistId ORDER BY `index` ASC")
  List<PlayableSong> getSongsForPlaylist(String playlistId);

  @Query("SELECT * FROM playlist_song_join")
  LiveData<List<PlaylistSongJoin>> getAllPlaylistSongJoinLive();

  @Query("DELETE FROM playlist_song_join WHERE playlist_song_join.playlistId = :playlistId AND playlist_song_join.songId = :songId")
  void remove(String playlistId, String songId);
}