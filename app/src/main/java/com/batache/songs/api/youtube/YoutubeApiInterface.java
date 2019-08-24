package com.batache.songs.api.youtube;

import com.batache.songs.util.SongUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApiInterface {

  @GET("playlistItems?part=id, snippet&key=" + SongUtils.API_KEY_2)
  Call<YoutubePlaylistResponse> getSongsFromPlaylist(@Query("playlistId") String playlistId, @Query("maxResults") String maxResults);

  @GET("videos?part=id, snippet&key=" + SongUtils.API_KEY_2)
  Call<YoutubeVideosResponse> getSong(@Query("id") String videoId);

  @GET("search?part=snippet&type=video&key=" + SongUtils.API_KEY_2)
  Call<YoutubeSearchResponse> search(@Query("q") String searchQuery);

  @GET("captions?part=snippet&type=video&key=" + SongUtils.API_KEY_2)
  Call<YoutubeCaptionLanguagesResponse> getAvailableCaptionLanguages(@Query("videoId") String videoId);

}
