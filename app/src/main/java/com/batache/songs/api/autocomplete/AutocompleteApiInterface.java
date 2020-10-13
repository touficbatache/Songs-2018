package com.batache.songs.api.autocomplete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AutocompleteApiInterface {

  @GET("search?client=firefox&ds=yt")
  Call<List<Object>> getAutocomplete(@Query("q") String query);

}
