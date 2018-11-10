package ml.toufic.songs.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import ml.toufic.songs.R;
import ml.toufic.songs.network.HTMLDataFetcher;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.util.ImageLoader;

import static ml.toufic.songs.util.SongUtils.AS_API_KEY;

public class PlayerCoverModel extends EpoxyModelWithHolder<PlayerCoverModel.PlayerCoverHolder> {

  private Context mContext;
  private String id = "temp";
  private String coverArtUrl;

  private String ytSongTitle;
  private LyricScrollCallback lyricScrollCallback;

  private TextView lyricsTv;

  public PlayerCoverModel(Context context, PlayableSong playableSong, LyricScrollCallback lyricScrollCallback) {
    mContext = context;
    id = playableSong.getId();
    ytSongTitle = playableSong.getTitle();
    coverArtUrl = playableSong.getCoverArt();
    this.lyricScrollCallback = lyricScrollCallback;
  }

  public PlayerCoverModel(Context context, String coverArtUrl) {
    mContext = context;
    this.coverArtUrl = coverArtUrl;
  }

  @Override
  protected PlayerCoverHolder createNewHolder() {
    return new PlayerCoverHolder();
  }

  @Override
  protected int getDefaultLayout() {
    return R.layout.item_player_cover;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void bind(@NonNull PlayerCoverHolder holder) {
    super.bind(holder);
    ImageLoader.loadImage(mContext, holder.coverArt, new ImageLoader.ImageConfigurations(coverArtUrl, id, ImageLoader.ImageConfigurations.PNG));
    lyricsTv = holder.lyricsTv;
    lyricsTv.setMovementMethod(new ScrollingMovementMethod());
    lyricsTv.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        Log.d("tgblog me:", "onTouch: " + action);
        switch(action) {
          case MotionEvent.ACTION_DOWN:
            lyricScrollCallback.onScroll();
            break;

          case MotionEvent.ACTION_CANCEL:
          case MotionEvent.ACTION_UP:
            lyricScrollCallback.onScrollEnd();
            break;
        }

        return false;
      }
    });
    fetchSongInfo();
  }

  class PlayerCoverHolder extends EpoxyHolder {

    SimpleDraweeView coverArt;
    TextView lyricsTv;

    @Override
    protected void bindView(View itemView) {
      coverArt = itemView.findViewById(R.id.player_song_cover_art);
      lyricsTv = itemView.findViewById(R.id.player_song_lyrics);
    }
  }

  private void fetchSongInfo() {
    JSONObjectDataFetcher songInfoFetcher = new JSONObjectDataFetcher("http://ws.audioscrobbler.com/2.0/?method=track.search&track=" + removeParenthesis(ytSongTitle).replace("HD", "") + "&api_key=" + AS_API_KEY + "&format=json", new JSONObjectDataFetcher.JSONDataFetcherCallback() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          JSONObject track = result.getJSONObject("results").getJSONObject("trackmatches").getJSONArray("track").getJSONObject(0);
          Log.d("tgblog: ", "track results: " + track.toString());
          String trackName = removeParenthesis(track.get("name").toString()).replace(" " , "_").trim();
          String trackArtist = removeParenthesis(track.get("artist").toString()).split("ft")[0].trim().replace(" " , "_").trim(); //.split(" ")[0]
          fetchLyrics(trackArtist, trackName, new LyricFetcherCallback() {
            @Override
            public void onSuccess(String lyrics) {
              lyricsTv.setText(lyrics);
            }

            @Override
            public void onError(String error) {
              lyricsTv.setText("");
            }
          });
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError() {}
    });
    songInfoFetcher.execute();
  }

  private void fetchLyrics(String artist, String song, final LyricFetcherCallback lyricFetcherCallback) {
    String url = "http://lyrics.wikia.com/wiki/" + artist + ":" + song;

    HTMLDataFetcher lyricsHTMLFetch = new HTMLDataFetcher(mContext, url, new HTMLDataFetcher.HTMLDataFetcherCallback() {
      @Override
      public void onSuccess(Document result) {
        String lyrics = result.select(".lyricbox").html();

        // remove everything between brackets
        lyrics = lyrics.replaceAll("\\[[^\\]]*\\]", "");
        // remove html comments
        lyrics = lyrics.replaceAll("(<!--)[^-]*-->", "");
        // replace newlines
        lyrics = lyrics.replaceAll("<br>", "\n");
        // remove all tags
        lyrics = lyrics.replaceAll("<[^>]*>", "");

        if(!lyrics.equals("")){
          lyricFetcherCallback.onSuccess(lyrics);
        }
        else{
          lyricFetcherCallback.onError("Not found");
        }

      }

      @Override
      public void onError() {
        lyricFetcherCallback.onError("");
      }
    });
    lyricsHTMLFetch.execute();
  }

  interface LyricFetcherCallback {
    void onSuccess(String lyrics);
    void onError(String error);
  }

  private String removeParenthesis(String string) {
    return string.replaceAll("\\(.*\\)", "");
  }

  public interface LyricScrollCallback {
    void onScroll();
    void onScrollEnd();
  }
}