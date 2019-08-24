package com.batache.songs.api.song_info;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SongInfoApiInterface {

  @GET("?method=track.search&api_key=YOUR_LASTFM_API_KEY&format=json")
  Call<SongInfoResponse> getSongInfo(@Query("track") String query);

}
