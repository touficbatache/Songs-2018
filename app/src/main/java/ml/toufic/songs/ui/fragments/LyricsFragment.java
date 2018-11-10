package ml.toufic.songs.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ml.toufic.songs.R;
import ml.toufic.songs.network.HTMLDataFetcher;
import ml.toufic.songs.network.JSONObjectDataFetcher;
import ml.toufic.songs.ui.activities.MainActivity;

import static ml.toufic.songs.util.SongUtils.AS_API_KEY;

public class LyricsFragment extends Fragment {

  private static String songTitleKey = "songtitlekey";

  private MainActivity mActivity;
  private Context mContext;
  private View mRootView;
  private LyricAdapter mLyricAdapter;
  private RecyclerView mLyricsRecyclerView;
  private String songTitle;

  public static LyricsFragment newInstance(String songTitle) {
    LyricsFragment fragment = new LyricsFragment();
    Bundle args = new Bundle();
    args.putString(songTitleKey, songTitle);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();
    mContext = getContext();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_lyrics, null);

    Bundle args = getArguments();
    if(args != null) {
      songTitle = args.getString(songTitleKey);
    }

    mRootView.findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mActivity.finishLyricsFragment();
      }
    });
    ((TextView) mRootView.findViewById(R.id.song_title)).setText(songTitle);

    mLyricAdapter = new LyricAdapter();
    mLyricsRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mLyricsRecyclerView.setAdapter(mLyricAdapter);
    mLyricsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

    fetchSongInfo();

    return mRootView;
  }

  private void fetchSongInfo() {
    JSONObjectDataFetcher songInfoFetcher = new JSONObjectDataFetcher("http://ws.audioscrobbler.com/2.0/?method=track.search&track=" + removeParenthesis(songTitle).replace("HD", "") + "&api_key=" + AS_API_KEY + "&format=json", new JSONObjectDataFetcher.JSONDataFetcherCallback() {
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
              List<String> arrayLyrics = new ArrayList<>();
              arrayLyrics.addAll(Arrays.asList(lyrics.split("\\n")));
              for(int i=0; i<arrayLyrics.size(); i++) {
                mLyricAdapter.addLyricLine(arrayLyrics.get(i));
              }
//              lyricsTv.setText(lyrics);
            }

            @Override
            public void onError(String error) {
//              lyricsTv.setText("");
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

}
