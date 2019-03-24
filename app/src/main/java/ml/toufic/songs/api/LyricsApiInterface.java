package ml.toufic.songs.api;

import ml.toufic.songs.model.SongLyricsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LyricsApiInterface {

  @GET("?method=track.search&api_key=YOUR_LASTFM_API_KEY&format=json")
  Call<SongLyricsResponse> getSongInfo(@Query("track") String query);

}
