package com.batache.songs.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.batache.songs.R;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.Playlist;
import com.batache.songs.ui.base.ListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.List;

public class MyMusicFragment extends ListFragment {

  private static final String TAG = "MyMusicFragment : ";

  private DatabaseHelper mDatabaseHelper;

  private SwipeRefreshLayout mSwipeRefreshLayout;
  private FloatingActionButton mNewPlaylistBtn;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mDatabaseHelper = DatabaseHelper.getInstance(getContext());

    setHasOptionsMenu(true);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_my_music;
  }

  @Override
  public void onCreateView() {
    mSwipeRefreshLayout = mRootView.findViewById(R.id.swipe_refresh_layout);
    mSwipeRefreshLayout.setOnRefreshListener(() -> loadMyMusicPlaylists());

    mNewPlaylistBtn = mRootView.findViewById(R.id.new_playlist_btn);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mDatabaseHelper.getPlaylistsListener().observe(this, playlists -> loadMyMusicPlaylists());
    mDatabaseHelper.getPlaylistSongInsertionListener().observe(this, playlistSongJoins -> {
      loadMyMusicPlaylists();
    });

    mNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        MaterialDialog.Builder newPlaylistDialog = new MaterialDialog.Builder(getContext());
        newPlaylistDialog.title(R.string.new_playlist);
        newPlaylistDialog.inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        newPlaylistDialog.alwaysCallInputCallback();
        newPlaylistDialog.input(getString(R.string.playlist_name), null, false, new MaterialDialog.InputCallback() {
          @Override
          public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim().equals("")) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            }
          }
        });
        newPlaylistDialog.cancelable(true);
        newPlaylistDialog.positiveText(R.string.create);
        newPlaylistDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            List<Playlist> playlists = mDatabaseHelper.getAllPlaylists();
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
            mDatabaseHelper.insertPlaylist(new Playlist(String.valueOf(Integer.valueOf(playlists.get(playlists.size() - 1).getId()) + 1), dialog.getInputEditText().getText().toString().trim(), new Date()));
            Toast.makeText(getContext(), R.string.playlist_created_successfully, Toast.LENGTH_SHORT).show();
          }
        });
        newPlaylistDialog.negativeText(R.string.cancel);
        newPlaylistDialog.show();

      }
    });

    loadMyMusicPlaylists();
  }

  private void loadMyMusicPlaylists() {
    mController.reset();

    mController.addPlaylistsSection(getContext(), getString(R.string.playlists), mDatabaseHelper.getAllPlaylists());

    mSwipeRefreshLayout.setRefreshing(false);
  }

}
