package com.batache.songs.db;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.batache.songs.db.LastPlayed.LastPlayedRepository;
import com.batache.songs.db.Playlist.PlaylistRepository;
import com.batache.songs.db.PlaylistSongJoin.PlaylistSongJoinRepository;
import com.batache.songs.db.Song.SongRepository;
import com.batache.songs.model.pojo.LastPlayed;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.model.pojo.PlaylistSongJoin;
import com.batache.songs.model.pojo.Song;

import java.util.List;

import static com.batache.songs.util.SongUtils.PLAYLIST_DOWNLOADS_ID;
import static com.batache.songs.util.SongUtils.PLAYLIST_LIKES_ID;

public class DatabaseHelper {

  private SongRepository mSongRepository;
  private PlaylistRepository mPlaylistRepository;
  private PlaylistSongJoinRepository mPlaylistSongJoinRepository;
  private LastPlayedRepository mLastPlayedRepository;

  private static DatabaseHelper sInstance;

  public static DatabaseHelper getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new DatabaseHelper(context);
    }
    return sInstance;
  }

  private DatabaseHelper(Context context) {
    mSongRepository = new SongRepository(context);
    mPlaylistRepository = new PlaylistRepository(context);
    mPlaylistSongJoinRepository = new PlaylistSongJoinRepository(context);
    mLastPlayedRepository = new LastPlayedRepository(context);
  }

  // Songs DB --------------------------------------------------------------------------------------------------

  public void insertSong(Song song) {
    mSongRepository.insert(song);
  }

  public void updateSong(Song song) {
    mSongRepository.update(song);
  }

  public Song getSong(String songId) {
    return mSongRepository.getSong(songId);
  }

  public boolean hasSong(String songId) {
    return mSongRepository.hasSong(songId);
  }

  public List<Song> getAllSongs() {
    return mSongRepository.getAllSongs();
  }

  public LiveData<List<Song>> getSongsListener() {
    return mSongRepository.getAllSongsLive();
  }

  public void deleteSong(Song song) {
    mSongRepository.delete(song);
  }

  public void deleteSong(String songId) {
    mSongRepository.delete(songId);
  }

  public void deleteAllSongs() {
    mSongRepository.deleteAll();
  }

  // Playlists DB --------------------------------------------------------------------------------------------------

  public void insertPlaylist(Playlist playlist) {
    mPlaylistRepository.insert(playlist);
  }

  public void Playlist(Playlist playlist) {
    mPlaylistRepository.update(playlist);
  }

  public Playlist getPlaylist(String playlistId) {
    return mPlaylistRepository.getPlaylist(playlistId);
  }

  public boolean hasPlaylist(String playlistId) {
    return mPlaylistRepository.hasPlaylist(playlistId);
  }

  public List<Playlist> getAllPlaylists() {
    return mPlaylistRepository.getAllPlaylists();
  }

  public LiveData<List<Playlist>> getPlaylistsListener() {
    return mPlaylistRepository.getAllPlaylistsLive();
  }

  public void deletePlaylist(Playlist playlist) {
    mPlaylistRepository.delete(playlist);
  }

  public void deletePlaylist(String playlistId) {
    mPlaylistRepository.delete(playlistId);
  }

  public void deleteAllPlaylists() {
    mPlaylistRepository.deleteAll();
  }

  // PlaylistSongJoin DB --------------------------------------------------------------------------------------------------

  public void insertSongInPlaylist(PlaylistSongJoin playlistSongJoin) {
    mPlaylistSongJoinRepository.insert(playlistSongJoin);
  }

  public List<Playlist> getPlaylistsForSong(String songId) {
    return mPlaylistSongJoinRepository.getPlaylistsForSong(songId);
  }

  public List<Song> getSongsForPlaylist(String playlistId) {
    return mPlaylistSongJoinRepository.getSongsForPlaylist(playlistId);
  }

  public LiveData<List<PlaylistSongJoin>> getPlaylistSongInsertionListener() {
    return mPlaylistSongJoinRepository.getAllPlaylistSongJoinLive();
  }

  public boolean isSongInPlaylist(String playlistId, String songId) {
    return mPlaylistSongJoinRepository.isSongInPlaylist(playlistId, songId);
  }

  public boolean isSongInCustomPlaylist(String songId) {
    boolean isInCustomPlaylist = false;
    for (Playlist playlist : getPlaylistsForSong(songId)) {
      if (!playlist.getId().equals(PLAYLIST_LIKES_ID) && !playlist.getId().equals(PLAYLIST_DOWNLOADS_ID)) {
        isInCustomPlaylist = true;
      }
    }
    return isInCustomPlaylist;
  }

  public void removeSongFromPlaylist(String playlistId, String songId) {
    mPlaylistSongJoinRepository.remove(playlistId, songId);
  }

  // Last Played DB --------------------------------------------------------------------------------------------------

  public void setLastPlayed(LastPlayed lastPlayed) {
    mLastPlayedRepository.setLastPlayed(lastPlayed);
  }

  public LastPlayed getLastPlayed() {
    return mLastPlayedRepository.getLastPlayed();
  }

}
