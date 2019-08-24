package com.batache.songs.db.PlaylistSongJoin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.batache.songs.model.Playlist;
import com.batache.songs.model.PlaylistSongJoin;
import com.batache.songs.model.Song;

@Dao
public interface PlaylistSongJoinDao {

  @Insert
  void insert(PlaylistSongJoin playlistSongJoin);

  @Query("SELECT * FROM playlists INNER JOIN playlist_song_join ON playlists.id=playlist_song_join.playlistId WHERE playlist_song_join.songId = :songId")
  List<Playlist> getPlaylistsForSong(String songId);

  @Query("SELECT * FROM songs INNER JOIN playlist_song_join ON songs.id=playlist_song_join.songId WHERE playlist_song_join.playlistId = :playlistId ORDER BY creationdate DESC")
  List<Song> getSongsForPlaylist(String playlistId);

  @Query("SELECT * FROM playlist_song_join")
  LiveData<List<PlaylistSongJoin>> getAllPlaylistSongJoinLive();

  @Query("DELETE FROM playlist_song_join WHERE playlist_song_join.playlistId = :playlistId AND playlist_song_join.songId = :songId")
  void remove(String playlistId, String songId);
}