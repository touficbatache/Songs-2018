package com.batache.songs.app.my_music;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.batache.songs.R;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.model.pojo.Playlist;
import com.batache.songs.app.base.ListFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

  private String dialogPlaylistName;

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

        AlertDialog dialog = new MaterialAlertDialogBuilder(getActivity())
            .setTitle(R.string.new_playlist)
            .setView(R.layout.layout_outlined_text_field)
            .setCancelable(false)
            .setPositiveButton(R.string.create, (dialogInterface, which) -> {
              List<Playlist> playlists = mDatabaseHelper.getAllPlaylists();
              mDatabaseHelper.insertPlaylist(new Playlist(String.valueOf(Integer.valueOf(playlists.get(playlists.size() - 1).getId()) + 1), dialogPlaylistName, new Date()));
              Toast.makeText(getContext(), R.string.playlist_created_successfully, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();

        TextInputLayout playlistNameHint = dialog.findViewById(R.id.input_layout);
        playlistNameHint.setHint(getResources().getString(R.string.playlist_name));
        TextInputEditText playlistNameEt = dialog.findViewById(R.id.edit_text);
        playlistNameEt.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

          }

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            dialogPlaylistName = charSequence.toString().trim();
            if (dialogPlaylistName.equals("")) {
              dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
          }

          @Override
          public void afterTextChanged(Editable editable) {

          }
        });
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
