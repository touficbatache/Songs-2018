package com.batache.songs.api.captions;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CaptionsApiInterface {

  @GET("timedtext")
  Call<CaptionsResponse> getCaptions(@Query("v") String songId, @Query("lang") String language);

}
