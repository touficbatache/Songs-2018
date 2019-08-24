package com.batache.songs.api.xiami;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface XiamiApiInterface {

  @Headers("referer: http://m.xiami.com")
  @GET("web?v=2.0&app_key=1&r=search%2Fsongs&page=1&limit=5")
  Call<XiamiSearchResponse> search(@Query("key") String searchQuery);

  @GET
  Call<ResponseBody> getLyrics(@Url String url);

}
