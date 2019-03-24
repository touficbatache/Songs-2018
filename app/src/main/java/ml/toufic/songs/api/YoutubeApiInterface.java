package ml.toufic.songs.api;

import ml.toufic.songs.model.YoutubePlaylistResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApiInterface {

  @GET("playlistItems?part=id, snippet&key=YOUR_YOUTUBE_API_KEY")
  Call<YoutubePlaylistResponse> getPlaylist(@Query("playlistId") String playlistId, @Query("maxResults") String maxResults);

}
