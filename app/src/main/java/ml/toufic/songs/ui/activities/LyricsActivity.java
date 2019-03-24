package ml.toufic.songs.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ml.toufic.songs.R;
import ml.toufic.songs.api.LyricsApiClient;
import ml.toufic.songs.api.LyricsApiInterface;
import ml.toufic.songs.model.SongLyricsResponse;
import ml.toufic.songs.network.HTMLDataFetcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LyricsActivity extends AppCompatActivity {

  public static String songTitleKey = "songtitlekey";

  private Context mContext;
  private LyricAdapter mLyricAdapter;
  private RecyclerView mLyricsRecyclerView;
  private String songTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lyrics);

    mContext = LyricsActivity.this;

    songTitle = getIntent().getStringExtra(songTitleKey);

    findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
    ((TextView) findViewById(R.id.song_title)).setText(songTitle);

    mLyricAdapter = new LyricAdapter();
    mLyricsRecyclerView = findViewById(R.id.recyclerview);
    mLyricsRecyclerView.setAdapter(mLyricAdapter);
    mLyricsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));

    fetchSongInfo();
  }

  private void fetchSongInfo() {
    Retrofit retrofit = LyricsApiClient.getRetrofitClient();
    LyricsApiInterface apiInterface = retrofit.create(LyricsApiInterface.class);

    String searchTitle = removeParenthesis(songTitle).replace("HD", "").replace("-", "");
    Call<SongLyricsResponse> call = apiInterface.getSongInfo(searchTitle);
    call.enqueue(new Callback<SongLyricsResponse>() {
      @Override
      public void onResponse(@NonNull Call<SongLyricsResponse> call, @NonNull Response<SongLyricsResponse> response) {
        if (response.body() != null) {
          List<SongLyricsResponse.Track> allTracks = response.body().results.trackMatches.tracks;

          List<Double> matches = new ArrayList<>();
          for (SongLyricsResponse.Track track : allTracks) {
            int nameMatch = findMatchInPercent(searchTitle, track.name);
            int artistMatch = findMatchInPercent(searchTitle, track.artist);
            double finalResult = (nameMatch + (artistMatch * 0.5)) / 1.5;
            matches.add(finalResult);
          }

          SongLyricsResponse.Track track = allTracks.get(matches.indexOf(Collections.max(matches)));
          final String trackName = removeParenthesis(track.name).split("ft")[0].replace(" ", "_").trim();
          final String trackArtist = removeParenthesis(track.artist).split("ft")[0].trim().replace(" ", "_").trim(); //.split(" ")[0]
          fetchLyrics(trackArtist, trackName);
        }
      }

      @Override
      public void onFailure(@NonNull Call<SongLyricsResponse> call, @NonNull Throwable t) {
        Toast.makeText(mContext, "Failed fetching lyrics", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void fetchLyrics(String artist, String song) {
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

        if (!lyrics.equals("")) {
          List<String> arrayLyrics = new ArrayList<>(Arrays.asList(lyrics.split("\\n")));
          for (int i = 0; i < arrayLyrics.size(); i++) {
            mLyricAdapter.addLyricLine(arrayLyrics.get(i));
          }
        } else {
          Toast.makeText(mContext, "No lyrics found", Toast.LENGTH_SHORT).show();
        }

      }

      @Override
      public void onError() {
        Toast.makeText(mContext, "Failed fetching lyrics", Toast.LENGTH_SHORT).show();
      }
    });
    lyricsHTMLFetch.execute();
  }

  private String removeParenthesis(String string) {
    return string.replaceAll("\\(.*\\)", "");
  }

  class LyricAdapter extends EpoxyAdapter {
    void addLyricLine(String lyric) {
      addModel(new LyricModel(lyric));
    }
  }

  class LyricModel extends EpoxyModelWithHolder<LyricHolder> {

    private String lyricLine;

    LyricModel(String lyricLine) {
      this.lyricLine = lyricLine;
    }

    @Override
    protected LyricHolder createNewHolder() {
      return new LyricHolder();
    }

    @Override
    protected int getDefaultLayout() {
      return R.layout.item_lyric_line;
    }

    @Override
    public void bind(@NonNull LyricHolder holder) {
      super.bind(holder);

      holder.lyricLineTv.setText(lyricLine);
    }
  }

  class LyricHolder extends EpoxyHolder {

    private TextView lyricLineTv;

    @Override
    protected void bindView(View itemView) {
      lyricLineTv = itemView.findViewById(R.id.lyric_line_tv);
    }
  }

  private int findMatchInPercent(String source, String subject) {
    int wordsInCommon = 0;
    int wordsLength = subject.split("\\s+").length;

    List<String> subjectKeywords = new ArrayList<>(Arrays.asList(subject.split("\\s+")));
    for (int i = 0; i < subjectKeywords.size(); i++) {
      if (source.contains(subjectKeywords.get(i))) {
        wordsInCommon++;
      }
    }

//    Old algo
//    int wordsLength = source.split("\\s+").length;
//    List<String> sourceKeywords = new ArrayList<>(Arrays.asList(source.split("\\s+")));
//    for(int i=0; i<sourceKeywords.size(); i++) {
//      if(subject.contains(sourceKeywords.get(i))) {
//        wordsInCommon++;
//      }
//    }
//    List<String> subjectKeywords = new ArrayList<>(Arrays.asList(subject.split("\\s+")));
//    for(int i=0; i<subjectKeywords.size(); i++) {
//      if(!source.contains(subjectKeywords.get(i))) {
//        wordsLength++;
//      }
//    }

    return wordsInCommon * 100 / wordsLength;
  }
}
