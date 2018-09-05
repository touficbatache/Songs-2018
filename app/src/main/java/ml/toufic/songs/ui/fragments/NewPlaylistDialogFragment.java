package ml.toufic.songs.ui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.Objects;

import ml.toufic.songs.R;
import ml.toufic.songs.model.Playlist;
import ml.toufic.songs.ui.activities.MainActivity;

public class NewPlaylistDialogFragment extends DialogFragment {

  private static final String TAG = "NewPlaylistDialogFragment : ";
  private MainActivity mActivity;
  private Context mContext;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (MainActivity) getActivity();

    mContext = getContext();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
    mBuilder.setTitle("New playlist");

    View mView = mActivity.getLayoutInflater().inflate(R.layout.dialog_new_playlist, null);
    final EditText playlistTitleTv = mView.findViewById(R.id.playlist_title);
    mBuilder.setView(mView);

    // Set up the buttons
    mBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if(!Objects.equals(playlistTitleTv.getText().toString().trim(), "")) {
          mActivity.insertPlaylist(new Playlist(String.valueOf(mActivity.getAllPlaylists().size() + 1), playlistTitleTv.getText().toString().trim(), new Date()));
          Toast.makeText(mContext, "Playlist created successfully!", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(mContext, "Playlist name is empty!", Toast.LENGTH_SHORT).show();
        }
      }
    });
    mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    AlertDialog dialog = mBuilder.create();
    //set to adjust screen height automatically, when soft keyboard appears on screen
    mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    return dialog;
  }
}
