package com.batache.songs.app.my_music;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.batache.songs.R;
import com.batache.songs.app.base.ListFragment;
import com.batache.songs.app.settings.SettingsActivity;
import com.batache.songs.db.DatabaseHelper;
import com.batache.songs.player.PlayerManager;
import com.batache.songs.ui.controller.SongsController;
import com.batache.songs.util.DeviceUtils;
import com.batache.songs.util.PlaylistImporter;
import com.batache.songs.util.SongUtils;
import com.batache.songs.util.events.ImportEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MyMusicFragment extends ListFragment<SongsController> implements View.OnClickListener {

  private DatabaseHelper databaseHelper;

  private SwipeRefreshLayout swipeRefreshLayout;
  private FloatingActionButton newPlaylistBtn;

  private AlertDialog newPlaylistDialog;

  private ImportEvent importEvent;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    setEnterTransition(new MaterialFadeThrough());

    databaseHelper = DatabaseHelper.getInstance(getContext());

    setHasOptionsMenu(true);
  }

  @Override
  public int getLayout() {
    return R.layout.fragment_my_music;
  }

  @Override
  public void onCreateView() {
    swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(() -> loadMyMusicPlaylists());

    newPlaylistBtn = rootView.findViewById(R.id.new_playlist_btn);
  }

  private String dialogPlaylistName;

  @Override
  public SongsController getController() {
    return new SongsController();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    databaseHelper.getPlaylistsListener().observe(getViewLifecycleOwner(), playlists -> loadMyMusicPlaylists());
    databaseHelper.getPlaylistSongInsertionListener().observe(getViewLifecycleOwner(), playlistSongJoins -> {
      loadMyMusicPlaylists();
    });

    newPlaylistBtn.setOnClickListener(this);

    loadMyMusicPlaylists();
  }

  private void loadMyMusicPlaylists() {
    mController.reset();

    List<SongUtils.ExportedPlaylist> importingPlaylists = PlaylistImporter.getInstance().getImportingPlaylists();

    mController.addPlaylistsSection(getString(R.string.playlists), databaseHelper.getAllPlaylistsDesc(), importingPlaylists, importEvent);

    swipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onClick(View view) {
    newPlaylistDialog = new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(R.string.create_new_playlist)
        .setView(R.layout.layout_outlined_text_field)
        .setCancelable(false)
        .setPositiveButton(R.string.create, (dialogInterface, which) -> {
          PlayerManager.getInstance().createPlaylist(dialogPlaylistName);
          Toast.makeText(getContext(), R.string.playlist_created_successfully, Toast.LENGTH_SHORT).show();
        })
        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> DeviceUtils.hideKeyboardFrom(getContext(), newPlaylistDialog.findViewById(R.id.edit_text)))
        .show();

    newPlaylistDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

    TextInputLayout playlistNameHint = newPlaylistDialog.findViewById(R.id.input_layout);
    playlistNameHint.setHint(getResources().getString(R.string.title));
    TextInputEditText playlistNameEt = newPlaylistDialog.findViewById(R.id.edit_text);
    playlistNameEt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        dialogPlaylistName = charSequence.toString().trim();
        newPlaylistDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!dialogPlaylistName.equals(""));
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_toolbar_settings, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.settingsBtn) {
      startActivity(new Intent(getContext(), SettingsActivity.class));
      return true;
    }

    return false;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onImportEvent(ImportEvent importEvent) {
    this.importEvent = importEvent;
    loadMyMusicPlaylists();
  }

  @Override
  public FABData getFAB() {
    return new FABData("New playlist", R.drawable.ic_playlist_add_black_24dp, this);
  }
}
