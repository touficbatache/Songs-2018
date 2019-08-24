package com.batache.songs.api.captions;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class CaptionsApiClient {

  private static final String BASE_URL = "http://video.google.com/";
  private static Retrofit retrofit;

  public static Retrofit getRetrofitClient() {
    //If condition to ensure we don't create multiple retrofit instances in a single application
    if (retrofit == null) {
      //Defining the Retrofit using Builder
      retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL) //This is the only mandatory call on Builder object.
        .addConverterFactory(SimpleXmlConverterFactory.create()) // Convertor library used to convert response into POJO
        .build();
    }
    return retrofit;
  }

}
