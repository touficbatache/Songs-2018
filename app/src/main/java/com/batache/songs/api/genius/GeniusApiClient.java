package com.batache.songs.api.genius;

import com.github.slashrootv200.retrofithtmlconverter.HtmlConverterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class GeniusApiClient {

  private static final String BASE_URL = "https://genius.com/";

  private static Retrofit retrofit;

  public static Retrofit getRetrofitClient() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(HtmlConverterFactory.create(BASE_URL))
          .build();
    }
    return retrofit;
  }

}
