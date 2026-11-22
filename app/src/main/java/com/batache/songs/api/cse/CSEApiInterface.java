package com.batache.songs.api.cse;

import com.batache.songs.BuildConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CSEApiInterface {

  @GET("?key=" + BuildConfig.CSE_API_KEY + "&alt=json&cx=" + BuildConfig.CSE_DNDNHA_CX)
  Call<CSESearchResponse> searchDndnha(@Query("q") String searchQuery);

  @GET("?key=" + BuildConfig.CSE_API_KEY + "&alt=json&cx=" + BuildConfig.CSE_GENIUS_CX)
  Call<CSESearchResponse> searchGenius(@Query("q") String searchQuery);

}
