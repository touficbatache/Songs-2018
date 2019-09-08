package com.batache.songs.app.lyrics;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.Lyric;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.app.base.BaseActivity;
import com.batache.songs.app.player.PlayerManager;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.batache.songs.util.events.PlayerEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LyricsActivity extends BaseActivity {

  private Song song;

  private ExtendedFloatingActionButton scrollToPositionFAB;
  private int fabBottomMargin = 0;

  private LyricsController lyricsController;
  private LinearLayoutManager linearLayoutManager;
  private EpoxyRecyclerView recyclerView;
  private ImageButton captionsButton, editButton, deleteButton;

  private List<Lyric> lyrics;

  private boolean userScrolled = false;

  @Override
  public Context getContext() {
    return LyricsActivity.this;
  }

  @Override
  public int getLayout() {
    return R.layout.activity_lyrics;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    findViewById(R.id.back_arrow).setOnClickListener(view -> finish());

    scrollToPositionFAB = findViewById(R.id.scroll_to_position_fab);
    fabBottomMargin = ((ViewGroup.MarginLayoutParams) scrollToPositionFAB.getLayoutParams()).bottomMargin;
    scrollToPositionFAB.hide();

    recyclerView = findViewById(R.id.recyclerview);

    lyricsController = new LyricsController();
    recyclerView.setController(lyricsController);

    linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
    recyclerView.setLayoutManager(linearLayoutManager);

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (song.getAreLyricsSynced() && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
          userScrolled = true;
          scrollToPositionFAB.show();
        }
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
      }
    });

    captionsButton = findViewById(R.id.captions_btn);
    editButton = findViewById(R.id.edit_btn);
    deleteButton = findViewById(R.id.delete_btn);

    initLyricsSong();

    CodeUtils.registerToEventBus(this);
  }

  private String dialogLyrics;

  private void initLyricsSong() {
    deleteButton.setVisibility(View.GONE);
    lyricsController.clear();

    song = PlayerManager.getInstance().getCurrentSong();

    ((TextView) findViewById(R.id.song_title)).setText(song.getTitle());

    if (song.getSongSource() == Song.SongSource.youtube) {
      captionsButton.setVisibility(View.VISIBLE);
      captionsButton.setOnClickListener(view -> fetchYoutubeCaptions());
    }
    editButton.setOnClickListener(view -> {
      AlertDialog dialog = new MaterialAlertDialogBuilder(LyricsActivity.this)
          .setTitle("Edit lyrics")
          .setView(R.layout.layout_outlined_text_field)
          .setCancelable(false)
          .setPositiveButton(R.string.save, (dialogInterface, which) -> {
            List<Lyric> lyrics = new ArrayList<>();
            for (String lyricLine : new ArrayList<>(Arrays.asList(dialogLyrics.split("\\n")))) {
              lyrics.add(new Lyric(lyricLine));
            }
            setLyrics(lyrics, false);
          })
          .setNegativeButton(R.string.cancel, null)
          .show();

      TextInputLayout editLyricsHint = dialog.findViewById(R.id.input_layout);
      editLyricsHint.setHint("Lyrics");

      TextInputEditText editLyricsEt = dialog.findViewById(R.id.edit_text);
      StringBuilder currentLyrics = new StringBuilder();
      for (Lyric lyric : song.getLyrics()) {
        currentLyrics.append(lyric.getText() + "\n");
      }
      editLyricsEt.setText(currentLyrics);
      editLyricsEt.setSingleLine(false);
      editLyricsEt.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
          dialogLyrics = charSequence.toString().trim();
          if (dialogLyrics.equals("")) {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
          }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
      });
    });
    deleteButton.setOnClickListener(view -> {
      if (song.getLyrics() != null) {
        song.setLyrics(null);
        DatabaseHelper.getInstance(getContext()).updateSong(song);
        finish();
      }
    });
    scrollToPositionFAB.setOnClickListener(view -> {
      userScrolled = false;
      scrollToPositionFAB.hide();
    });

    if (!CodeUtils.isEmpty(song.getLyrics())) {
      setLyrics(song.getLyrics(), song.getAreLyricsSynced());
    } else if (!CodeUtils.isEmpty(song.getLyricsUrl())) {
      SongsAPI.getInstance().getXiamiSongLyrics(song.getLyricsUrl(), new SongsAPI.OnXiamiLyricsFetchedListener() {
        @Override
        public void onSuccess(String artist, List<Lyric> lyrics) {
          setLyrics(lyrics, true);
        }

        @Override
        public void onFailure(Throwable t) {
          showSnackbar(getString(R.string.failed_fetching_lyrics));
        }
      });
    } else {
      SongsAPI.getInstance().getSongLyrics(getContext(), song.getTitle(), new SongsAPI.OnLyricsFetchedListener() {
        @Override
        public void onSuccess(List<Lyric> lyrics) {
          setLyrics(lyrics, false);
        }

        @Override
        public void onFailure(Throwable t) {
          showSnackbar(getString(R.string.failed_fetching_lyrics));
        }
      });
    }
  }

  private int selectedLanguageIndex = 0;

  private void fetchYoutubeCaptions() {
    SongsAPI.getInstance().getAvailableCaptionLanguages(song.getId(), new SongsAPI.OnCaptionLanguagesFetchedListener() {
      @Override
      public void onSuccess(Map<String, String> captionLanguages) {
        new MaterialAlertDialogBuilder(LyricsActivity.this)
            .setTitle("Choose lyrics language")
            .setSingleChoiceItems(captionLanguages.keySet().toArray(new CharSequence[captionLanguages.keySet().size()]), selectedLanguageIndex, (dialog, which) -> {
              selectedLanguageIndex = which;
            })
            .setCancelable(true)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
              SongsAPI.getInstance().getCaptions(song.getId(), captionLanguages.values().toArray()[selectedLanguageIndex].toString(), new SongsAPI.OnLyricsFetchedListener() {
                @Override
                public void onSuccess(List<Lyric> captions) {
                  setLyrics(captions, true);
                }

                @Override
                public void onFailure(Throwable t) {
                  showSnackbar("Failed fetching captions");
                }
              });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
      }

      @Override
      public void onFailure(Throwable t) {
        showSnackbar("Failed fetching captions");
      }
    });
  }

  private void setLyrics(List<Lyric> lyrics, boolean areSynced) {
    this.lyrics = lyrics;
    song.setLyrics(lyrics);
    song.setAreLyricsSynced(areSynced);
    DatabaseHelper.getInstance(getContext()).updateSong(song);
    lyricsController.reset();
    lyricsController.addLyrics(lyrics);
    deleteButton.setVisibility(View.VISIBLE);
    linearLayoutManager.scrollToPositionWithOffset(0, recyclerView.getHeight() / 3);
  }

  private void showSnackbar(String message) {
    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onPlayerEvent(PlayerEvent playerEvent) {
    if (playerEvent.action == PlayerEvent.Action.SONG_CHANGED) {
      if (PlayerManager.getInstance().isCurrentSongLocallyStored()) {
        initLyricsSong();
      }
    }
    if (song.getAreLyricsSynced() && playerEvent.action == PlayerEvent.Action.PROGRESS_CHANGED) {
      goToCurrentLine();
    }
  }

  private void goToCurrentLine() {
    int progress = (int) PlayerManager.getInstance().getCurrentPosition() + 500;

    for (int i = 0; i < lyrics.size(); i++) {
      Lyric line = lyrics.get(i);
      Lyric nextLine = (i + 1 < lyrics.size()) ? lyrics.get(i + 1) : lyrics.get(i);

      if (progress < line.milliseconds || progress == line.milliseconds || progress < nextLine.milliseconds || line == nextLine) {
        scrollToLineAtIndex(lyrics.indexOf(line));
        break;
      }
    }
  }

  private void scrollToLineAtIndex(final int index) {
    lyricsController.setSelected(index);
    if (!userScrolled) {
      linearLayoutManager.scrollToPositionWithOffset(index, recyclerView.getHeight() / 3);
    }
  }

  @Override
  public void onApplyAllWindowInsets() {
    findViewById(R.id.toolbar).setPadding(0, DimensionUtils.TOP_INSET, 0, 0);
    recyclerView.setPadding(0, 0, 0, DimensionUtils.BOTTOM_INSET);
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) scrollToPositionFAB.getLayoutParams();
    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, fabBottomMargin + DimensionUtils.BOTTOM_INSET);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    CodeUtils.unregisterFromEventBus(this);
  }
}
