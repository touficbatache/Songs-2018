package com.batache.songs.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batache.songs.api.captions.Caption;
import com.batache.songs.api.captions.CaptionsApiClient;
import com.batache.songs.api.captions.CaptionsApiInterface;
import com.batache.songs.api.captions.CaptionsResponse;
import com.batache.songs.api.song_info.SongInfoApiClient;
import com.batache.songs.api.song_info.SongInfoApiInterface;
import com.batache.songs.api.song_info.SongInfoResponse;
import com.batache.songs.api.xiami.XiamiApiClient;
import com.batache.songs.api.xiami.XiamiApiInterface;
import com.batache.songs.api.xiami.XiamiSearchResponse;
import com.batache.songs.api.youtube.YoutubeApiClient;
import com.batache.songs.api.youtube.YoutubeApiInterface;
import com.batache.songs.api.youtube.YoutubeCaptionLanguagesResponse;
import com.batache.songs.api.youtube.YoutubePlaylistResponse;
import com.batache.songs.api.youtube.YoutubeSearchResponse;
import com.batache.songs.api.youtube.YoutubeVideosResponse;
import com.batache.songs.model.pojo.Lyric;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.util.CodeUtils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.batache.songs.util.TextUtils.removeBrackets;
import static com.batache.songs.util.TextUtils.removeParenthesis;

public class SongsAPI {

  private static SongsAPI sInstance;

  private Retrofit youtubeApiRetrofit;
  private YoutubeApiInterface youtubeApiInterface;

  private Retrofit captionsApiRetrofit;
  private CaptionsApiInterface captionsApiInterface;

  private Retrofit xiamiApiRetrofit;
  private XiamiApiInterface xiamiApiInterface;

  private Retrofit songInfoApiRetrofit;
  private SongInfoApiInterface songInfoApiInterface;

  public static SongsAPI getInstance() {
    if (sInstance == null) {
      sInstance = new SongsAPI();
    }
    return sInstance;
  }

  private SongsAPI() {
    youtubeApiRetrofit = YoutubeApiClient.getRetrofitClient();
    youtubeApiInterface = youtubeApiRetrofit.create(YoutubeApiInterface.class);
    captionsApiRetrofit = CaptionsApiClient.getRetrofitClient();
    captionsApiInterface = captionsApiRetrofit.create(CaptionsApiInterface.class);
    xiamiApiRetrofit = XiamiApiClient.getRetrofitClient();
    xiamiApiInterface = xiamiApiRetrofit.create(XiamiApiInterface.class);
    songInfoApiRetrofit = SongInfoApiClient.getRetrofitClient();
    songInfoApiInterface = songInfoApiRetrofit.create(SongInfoApiInterface.class);
  }

  public void getSongsFromPlaylist(String playlistId, String maxResults, OnSongsFetchedListener onSongsFetchedListener) {
    Call<YoutubePlaylistResponse> call = youtubeApiInterface.getSongsFromPlaylist(playlistId, maxResults);
    call.enqueue(new Callback<YoutubePlaylistResponse>() {
      @Override
      public void onResponse(@NonNull Call<YoutubePlaylistResponse> call, @NonNull Response<YoutubePlaylistResponse> response) {
        if (response.body() != null) {
          List<YoutubePlaylistResponse.Item> playlistItems = response.body().items;
          List<Song> fetchedSongs = new ArrayList<>();

          for (YoutubePlaylistResponse.Item playlistItem : playlistItems) {
            YoutubePlaylistResponse.Snippet snippet = playlistItem.snippet;
            YoutubePlaylistResponse.ResourceId resourceId = snippet.resourceId;
            YoutubePlaylistResponse.Thumbnails thumbnails = snippet.thumbnails;

            String id = resourceId.videoId;
            String title = snippet.title;
            String author = snippet.channelTitle;
            String thumbnail = thumbnails.thumbnail_high.url; // TODO: Add thumbnail size in settings

            fetchedSongs.add(new Song(id, title, author, thumbnail));
          }

          onSongsFetchedListener.onSuccess(fetchedSongs);
        }
      }

      @Override
      public void onFailure(@NonNull Call<YoutubePlaylistResponse> call, @NonNull Throwable t) {
        onSongsFetchedListener.onFailure(t);
      }
    });
  }

  public void getSong(String videoId, OnSongsFetchedListener onSongsFetchedListener) {
    Call<YoutubeVideosResponse> call = youtubeApiInterface.getSong(videoId);
    call.enqueue(new Callback<YoutubeVideosResponse>() {
      @Override
      public void onResponse(@NonNull Call<YoutubeVideosResponse> call, @NonNull Response<YoutubeVideosResponse> response) {
        if (response.body() != null) {
          List<YoutubeVideosResponse.Item> playlistItems = response.body().items;
          List<Song> fetchedSongs = new ArrayList<>();

          for (YoutubeVideosResponse.Item playlistItem : playlistItems) {
            YoutubeVideosResponse.Snippet snippet = playlistItem.snippet;
            YoutubeVideosResponse.Thumbnails thumbnails = snippet.thumbnails;

            String id = playlistItem.id;
            String title = snippet.title;
            String author = snippet.channelTitle;
            String thumbnail = thumbnails.thumbnail_high.url; // TODO: Add thumbnail size in settings

            fetchedSongs.add(new Song(id, title, author, thumbnail));
          }

          if (!fetchedSongs.isEmpty()) {
            onSongsFetchedListener.onSuccess(fetchedSongs);
          } else {
            onSongsFetchedListener.onFailure(null);
          }
        }
      }

      @Override
      public void onFailure(@NonNull Call<YoutubeVideosResponse> call, @NonNull Throwable t) {
        onSongsFetchedListener.onFailure(t);
      }
    });
  }

  public List<Song> getSongs(List<String> videoIds) throws IOException {
    List<Song> fetchedSongs = new ArrayList<>();

    for (String videoId : videoIds) {
      Call<YoutubeVideosResponse> call = youtubeApiInterface.getSong(videoId);
      List<YoutubeVideosResponse.Item> playlistItems = call.execute().body().items;
      if (playlistItems != null && !playlistItems.isEmpty()) {
        YoutubeVideosResponse.Item playlistItem = playlistItems.get(0);

        YoutubeVideosResponse.Snippet snippet = playlistItem.snippet;
        YoutubeVideosResponse.Thumbnails thumbnails = snippet.thumbnails;

        String id = playlistItem.id;
        String title = snippet.title;
        String author = snippet.channelTitle;
        String thumbnail = thumbnails.thumbnail_high.url; // TODO: Add thumbnail size in settings

        fetchedSongs.add(new Song(id, title, author, thumbnail));
      }
    }

    return fetchedSongs;
  }

  public void searchYoutube(String searchQuery, OnSongsFetchedListener onSongsFetchedListener) {
    Call<YoutubeSearchResponse> call = youtubeApiInterface.search(searchQuery);
    call.enqueue(new Callback<YoutubeSearchResponse>() {
      @Override
      public void onResponse(@NonNull Call<YoutubeSearchResponse> call, @NonNull Response<YoutubeSearchResponse> response) {
        if (response.body() != null) {
          List<YoutubeSearchResponse.Item> playlistItems = response.body().items;
          List<Song> fetchedSongs = new ArrayList<>();

          for (YoutubeSearchResponse.Item playlistItem : playlistItems) {
            YoutubeSearchResponse.Id vidId = playlistItem.id;
            YoutubeSearchResponse.Snippet snippet = playlistItem.snippet;
            YoutubeSearchResponse.Thumbnails thumbnails = snippet.thumbnails;

            String id = vidId.videoId;
            // unescape html: replace characters like &amp; with &
            String title = StringEscapeUtils.unescapeHtml4(snippet.title);
            String author = snippet.channelTitle;
            String thumbnail = thumbnails.thumbnail_high.url; // TODO: Add thumbnail size in settings

            fetchedSongs.add(new Song(id, title, author, thumbnail));
          }

          onSongsFetchedListener.onSuccess(fetchedSongs);
        }
      }

      @Override
      public void onFailure(@NonNull Call<YoutubeSearchResponse> call, @NonNull Throwable t) {
        onSongsFetchedListener.onFailure(t);
      }
    });
  }

  public void getAvailableCaptionLanguages(String videoId, OnCaptionLanguagesFetchedListener onCaptionLanguagesFetchedListener) {
    Call<YoutubeCaptionLanguagesResponse> call = youtubeApiInterface.getAvailableCaptionLanguages(videoId);
    call.enqueue(new Callback<YoutubeCaptionLanguagesResponse>() {
      @Override
      public void onResponse(@NonNull Call<YoutubeCaptionLanguagesResponse> call, @NonNull Response<YoutubeCaptionLanguagesResponse> response) {
        if (response.body() != null) {
          List<YoutubeCaptionLanguagesResponse.Item> captions = response.body().items;
          Map<String, String> captionLanguages = new HashMap<>();

          for (YoutubeCaptionLanguagesResponse.Item caption : captions) {
            if (!caption.snippet.trackKind.equals("ASR")) {
              captionLanguages.put(new Locale(caption.snippet.language).getDisplayLanguage(), caption.snippet.language);
            }
          }

          onCaptionLanguagesFetchedListener.onSuccess(captionLanguages);
        }
      }

      @Override
      public void onFailure(@NonNull Call<YoutubeCaptionLanguagesResponse> call, @NonNull Throwable t) {
        onCaptionLanguagesFetchedListener.onFailure(t);
      }
    });
  }

  public void getCaptions(String songId, String language, OnLyricsFetchedListener onCaptionsFetchedListener) {
    Call<CaptionsResponse> call = captionsApiInterface.getCaptions(songId, language);
    call.enqueue(new Callback<CaptionsResponse>() {
      @Override
      public void onResponse(@NonNull Call<CaptionsResponse> call, @NonNull Response<CaptionsResponse> response) {
        if (response.body() != null) {
          List<Lyric> lyrics = new ArrayList<>();
          for (Caption caption : response.body().captions) {
            int seconds = caption.start.split("\\.").length > 0 ? Integer.parseInt(caption.start.split("\\.")[0]) : 0;
            int millis = caption.start.split("\\.").length > 1 ? Integer.parseInt(caption.start.split("\\.")[1].concat("0")) : 0;
            lyrics.add(new Lyric((int) TimeUnit.SECONDS.toMillis(seconds) + millis, caption.text));
          }
          onCaptionsFetchedListener.onSuccess(lyrics);
        }
      }

      @Override
      public void onFailure(@NonNull Call<CaptionsResponse> call, @NonNull Throwable t) {
        onCaptionsFetchedListener.onFailure(t);
      }
    });
  }

  public void searchXiami(String searchQuery, OnSongsFetchedListener onSongsFetchedListener) {
    Call<XiamiSearchResponse> call = xiamiApiInterface.search(searchQuery);
    call.enqueue(new Callback<XiamiSearchResponse>() {
      @Override
      public void onResponse(@NonNull Call<XiamiSearchResponse> call, @NonNull Response<XiamiSearchResponse> response) {
        if (response.body() != null) {
          XiamiSearchResponse.Data data = response.body().data;
          List<XiamiSearchResponse.Song> songs = data.songs;
          List<Song> fetchedSongs = new ArrayList<>();

          for (XiamiSearchResponse.Song song : songs) {
            Song xiamiSong = new Song(song.id, song.title, song.artistName, song.albumLogoUrl);
            xiamiSong.setUrl(song.url);
            xiamiSong.setLyricsUrl(song.lyricsUrl);
            xiamiSong.setSongSource(Song.SongSource.xiami);
            fetchedSongs.add(xiamiSong);
          }

          onSongsFetchedListener.onSuccess(fetchedSongs);
        }
      }

      @Override
      public void onFailure(@NonNull Call<XiamiSearchResponse> call, @NonNull Throwable t) {
        onSongsFetchedListener.onFailure(t);
      }
    });
  }

  public void getSongLyrics(Context context, String songTitle, OnLyricsFetchedListener onLyricsFetchedListener) {
    String searchTitle = removeBrackets(removeParenthesis(songTitle)).replace("HD", "").replace("-", "");
    Call<SongInfoResponse> call = songInfoApiInterface.getSongInfo(searchTitle);
    call.enqueue(new Callback<SongInfoResponse>() {
      @Override
      public void onResponse(@NonNull Call<SongInfoResponse> call, @NonNull Response<SongInfoResponse> response) {
        if (response.body() != null) {
          List<SongInfoResponse.Track> allTracks = response.body().results.trackMatches.tracks;

          if (allTracks.isEmpty()) {
            onLyricsFetchedListener.onFailure(new Throwable("No lyrics found"));
            return;
          }

          List<Double> matches = new ArrayList<>();
          for (SongInfoResponse.Track track : allTracks) {
            int nameMatch = findMatchInPercent(searchTitle, track.name);
            int artistMatch = findMatchInPercent(searchTitle, track.artist);
            double finalResult = (nameMatch + (artistMatch * 0.5)) / 1.5;
            matches.add(finalResult);
          }

          SongInfoResponse.Track track = allTracks.get(matches.indexOf(Collections.max(matches)));
          final String trackName = removeParenthesis(track.name).split("ft")[0].replace(" ", "_").trim();
          final String trackArtist = removeParenthesis(track.artist).split("ft")[0].trim().replace(" ", "_").trim(); //.split(" ")[0]

          String url = "http://lyrics.wikia.com/wiki/" + trackArtist + ":" + trackName;

          HTMLDataFetcher lyricsHTMLFetch = new HTMLDataFetcher(context, url, new HTMLDataFetcher.HTMLDataFetcherCallback() {
            @Override
            public void onSuccess(Document result) {
              // unescape html: replace characters like &amp; with &
              String lyricsStr = StringEscapeUtils.unescapeHtml4(
                  result.select(".lyricbox").html()
                      // remove everything between brackets
                      .replaceAll("\\[[^\\]]*\\]", "")
                      // remove html comments
                      .replaceAll("(<!--)[^-]*-->", "")
                      // replace newlines
                      .replaceAll("<br>", "\n")
                      // replace two lines with one
                      .replaceAll("\n\n", "\n")
                      // remove all tags
                      .replaceAll("<[^>]*>", "")
              );

              if (!lyricsStr.equals("")) {
                List<Lyric> lyrics = new ArrayList<>();
                for (String lyricLine : new ArrayList<>(Arrays.asList(lyricsStr.split("\\n")))) {
                  lyrics.add(new Lyric(lyricLine));
                }
                onLyricsFetchedListener.onSuccess(lyrics);
              } else {
                onLyricsFetchedListener.onFailure(null);
              }
            }

            @Override
            public void onFailure() {
              onLyricsFetchedListener.onFailure(null);
            }
          });
          lyricsHTMLFetch.execute();
        }
      }

      @Override
      public void onFailure(@NonNull Call<SongInfoResponse> call, @NonNull Throwable t) {
        onLyricsFetchedListener.onFailure(t);
      }
    });
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

  public void getXiamiSongLyrics(String lyricsUrl, OnXiamiLyricsFetchedListener onLyricsFetchedListener) {
    Call<ResponseBody> call = xiamiApiInterface.getLyrics(lyricsUrl);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        if (response.body() != null) {
          String artist = null;
          List<Lyric> lyrics = new ArrayList<>();
          try {
            String responseRecieved = response.body().string();

            if (responseRecieved.split("\\[ar:").length > 1) {
              artist = responseRecieved.split("\\[ar:")[1].split("]")[0];
            }

            String lyricsStr = responseRecieved
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\[x-trans][^\\r\\n]*\\r\\n", "");

            if (lyricsStr.split("\\[offset:0]").length > 1) {
              lyricsStr = lyricsStr.split("\\[offset:0]")[1];
            }

            List<String> lyricLines = new ArrayList<>(Arrays.asList(lyricsStr.split("\\n")));
            int index = 0;
            for (String lyricLine : lyricLines) {
              if (lyricLine.contains("[") && lyricLine.contains("]")) {
                String timecode = lyricLine.split("\\[")[1].split("]")[0];
                int minutes = Integer.parseInt(timecode.split("\\.")[0].split(":")[0]);
                int seconds = Integer.parseInt(timecode.split("\\.")[0].split(":")[1]);
                int millis = Integer.parseInt(timecode.split("\\.")[1]);
                String lyricText = lyricLine.split("]").length > 1 ? lyricLine.split("]")[1] : "";

                if (!CodeUtils.isEmpty(lyricText) || (index != 0 && index != lyricLines.size() - 1)) {
                  lyrics.add(new Lyric((int) TimeUnit.MINUTES.toMillis(minutes) + (int) TimeUnit.SECONDS.toMillis(seconds) + millis, lyricText));
                }

                index++;
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          onLyricsFetchedListener.onSuccess(artist, lyrics);
        }
      }

      @Override
      public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        onLyricsFetchedListener.onFailure(t);
      }
    });
  }

  public void getGeniusLyrics() {
//    NOTE: JS working example below: will return a list of lyric links, to be run on a search results page
//
//    var links = new Array();
//    var nodes = document.querySelectorAll("mini-song-card[object='$ctrl.hit.result']");
//    for(i=0; i<nodes.length; i++) {
//      links[i] = nodes[i].querySelector("a").getAttribute("ng-href");
//    }
//    console.log(links);
  }

  public interface BaseApiListener {
    void onFailure(@Nullable Throwable t);
  }

  public interface OnSongsFetchedListener extends BaseApiListener {
    void onSuccess(List<Song> fetchedSongs);
  }

  public interface OnLyricsFetchedListener extends BaseApiListener {
    void onSuccess(List<Lyric> lyrics);
  }

  public interface OnXiamiLyricsFetchedListener extends BaseApiListener {
    void onSuccess(String artist, List<Lyric> lyrics);
  }

  public interface OnCaptionLanguagesFetchedListener extends BaseApiListener {
    void onSuccess(Map<String, String> captionLanguages);
  }
}
