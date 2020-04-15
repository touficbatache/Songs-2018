package com.batache.songs.api.cse;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CSEApiClient {

  private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1/";

  private static Retrofit retrofit;

  public static Retrofit getRetrofitClient() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
    }
    return retrofit;
  }

}
