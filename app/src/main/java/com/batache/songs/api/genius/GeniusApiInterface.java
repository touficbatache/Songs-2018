package com.batache.songs.api.genius;

import org.jsoup.nodes.Document;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeniusApiInterface {

  @GET("search")
  Call<Document> search(@Query("q") String searchQuery);

  @GET("{song_id}")
  Call<Document> getLyrics(@Path(value = "song_id", encoded = true) String songId);

}
