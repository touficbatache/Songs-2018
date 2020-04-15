package com.batache.songs.api.cse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CSEApiInterface {

  @GET("?key=YOUR_CSE_API_KEY&alt=json&cx=YOUR_CSE_DNDNHA_CX")
  Call<CSESearchResponse> searchDndnha(@Query("q") String searchQuery);

  @GET("?key=YOUR_CSE_API_KEY&alt=json&cx=YOUR_CSE_GENIUS_CX")
  Call<CSESearchResponse> searchGenius(@Query("q") String searchQuery);

}
