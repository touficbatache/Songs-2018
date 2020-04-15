package com.batache.songs.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.batache.songs.app.MainActivity;

public abstract class BaseFragment extends Fragment {

  protected View rootView;

  public abstract int getLayout();

  public abstract void onCreateView();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(getLayout(), null);

    onCreateView();

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    onApplyAllWindowInsets();
  }

  public abstract void onApplyAllWindowInsets();
}
