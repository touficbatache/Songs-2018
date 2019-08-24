package com.batache.songs.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Lyric;
import com.batache.songs.model.Song;
import com.batache.songs.ui.base.BaseActivity;
import com.batache.songs.ui.controller.LyricsController;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.batache.songs.util.PlayerHelper;
import com.batache.songs.util.events.PlayerEvent;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LyricsActivity extends BaseActivity {

  private Song song;

  private ExtendedFloatingActionButton scrollToPositionFAB;

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
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
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

  @Override
  public void onApplyAllWindowInsets() {
    findViewById(R.id.toolbar).setPadding(0, DimensionUtils.TOP_INSET, 0, 0);
    recyclerView.setPadding(0, 0, 0, DimensionUtils.BOTTOM_INSET);
  }

  private void initLyricsSong() {
    deleteButton.setVisibility(View.GONE);
    lyricsController.clear();

    song = PlayerHelper.getInstance().getCurrentSong(getContext());

    ((TextView) findViewById(R.id.song_title)).setText(song.getTitle());

    if (song.getSongSource() == Song.SongSource.youtube) {
      captionsButton.setVisibility(View.VISIBLE);
      captionsButton.setOnClickListener(view -> fetchYoutubeCaptions());
    }
    editButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MaterialDialog.Builder editLyricsDialog = new MaterialDialog.Builder(getContext());
        editLyricsDialog.title("Edit lyrics");
        editLyricsDialog.inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editLyricsDialog.alwaysCallInputCallback();
        editLyricsDialog.input("Lyrics", null, false, new MaterialDialog.InputCallback() {
          @Override
          public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim().equals("")) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            }
          }
        });
        editLyricsDialog.cancelable(true);
        editLyricsDialog.positiveText(R.string.save);
        editLyricsDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            List<Lyric> lyrics = new ArrayList<>();
            for (String lyricLine : new ArrayList<>(Arrays.asList(dialog.getInputEditText().getText().toString().split("\\n")))) {
              lyrics.add(new Lyric(lyricLine));
            }
            setLyrics(lyrics);
          }
        });
        editLyricsDialog.negativeText(R.string.cancel);
        MaterialDialog lyricsAdditionDialog = editLyricsDialog.build();
        EditText editLyricsInput = lyricsAdditionDialog.getInputEditText();
        StringBuilder currentLyrics = new StringBuilder();
        for (Lyric lyric : song.getLyrics()) {
          currentLyrics.append(lyric.getText() + "\n");
        }
        editLyricsInput.setText(currentLyrics);
        editLyricsInput.setSingleLine(false);
        editLyricsInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        lyricsAdditionDialog.show();
      }
    });
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (song.getLyrics() != null) {
          song.setLyrics(null);
          DatabaseHelper.getInstance(getContext()).updateSong(song);
          finish();
        }
      }
    });
    scrollToPositionFAB.setOnClickListener(view -> {
      userScrolled = false;
      scrollToPositionFAB.hide();
    });

    if (!song.getLyrics().isEmpty()) {
      setLyrics(song.getLyrics());
    } else if (!song.getXiamiProperties().getLyricsUrl().isEmpty()) {
      SongsAPI.getInstance().getXiamiSongLyrics(song.getXiamiProperties().getLyricsUrl(), new SongsAPI.OnXiamiLyricsFetchedListener() {
        @Override
        public void onSuccess(String artist, List<Lyric> lyrics) {
          setLyrics(lyrics);
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
          setLyrics(lyrics);
        }

        @Override
        public void onFailure(Throwable t) {
          showSnackbar(getString(R.string.failed_fetching_lyrics));
        }
      });
    }
  }

  private void fetchYoutubeCaptions() {
    SongsAPI.getInstance().getAvailableCaptionLanguages(song.getId(), new SongsAPI.OnCaptionLanguagesFetchedListener() {
      @Override
      public void onSuccess(List<String> captionLanguages) {
        MaterialDialog.Builder youtubeCaptionsLanguagesDialog = new MaterialDialog.Builder(getContext())
            .title("Choose lyrics language")
            .items(captionLanguages)
            .alwaysCallSingleChoiceCallback()
            .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> true)
            .cancelable(true)
            .positiveText(R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override
              public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Toast.makeText(getContext(), captionLanguages.get(dialog.getSelectedIndex()), Toast.LENGTH_LONG).show();
                SongsAPI.getInstance().getCaptions(song.getId(), captionLanguages.get(dialog.getSelectedIndex()), new SongsAPI.OnLyricsFetchedListener() {
                  @Override
                  public void onSuccess(List<Lyric> captions) {
                    setLyrics(captions);
                  }

                  @Override
                  public void onFailure(Throwable t) {
                    showSnackbar("Failed fetching captions");
                  }
                });
              }
            })
            .negativeText(R.string.cancel);
        youtubeCaptionsLanguagesDialog.show();
      }

      @Override
      public void onFailure(Throwable t) {
        showSnackbar("Failed fetching captions");
      }
    });
  }

  private void setLyrics(List<Lyric> lyrics) {
    this.lyrics = lyrics;
    song.setLyrics(lyrics);
    DatabaseHelper.getInstance(getContext()).updateSong(song);
    lyricsController.reset();
    lyricsController.addLyrics(lyrics);
    deleteButton.setVisibility(View.VISIBLE);
  }

  private void showSnackbar(String message) {
    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onPlayerEvent(PlayerEvent playerEvent) {
    if (playerEvent.action == PlayerEvent.Action.SONG_CHANGED) {
      if (!PlayerHelper.getInstance().isStreaming()) {
        lastIndex = 0;
        initLyricsSong();
      }
    }
    if(playerEvent.action == PlayerEvent.Action.PROGRESS_CHANGED) {
      goToCurrentLine();
    }
  }

  private int lastIndex = 0;

  private void goToCurrentLine() {
//    if (!isSynced) {
//      return;
//    }
    int progress = (int) PlayerHelper.getInstance().getCurrentPosition() + 1000;

    for (int i = lastIndex; i < lyrics.size(); i++) {
      Lyric line = lyrics.get(i);
      Lyric nextLine = (i + 1 < lyrics.size()) ? lyrics.get(i + 1) : lyrics.get(i);

      if (progress == line.milliseconds || (progress > line.milliseconds && progress < nextLine.milliseconds)) {
        lastIndex = lyrics.indexOf(line);
        scrollToLineAtIndex(lastIndex);
        break;
      } else if (line == nextLine) {
        lastIndex = lyrics.indexOf(line);
        scrollToLineAtIndex(lastIndex);
        break;
      }
    }
  }

  private void scrollToLineAtIndex(final int index) {
//    if (!isSynced) {
//      return;
//    }
    lyricsController.setSelected(index);
    if (!userScrolled) {
      linearLayoutManager.scrollToPositionWithOffset(index, recyclerView.getHeight() / 3);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    CodeUtils.unregisterFromEventBus(this);
  }
}
