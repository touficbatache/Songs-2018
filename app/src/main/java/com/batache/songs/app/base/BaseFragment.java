package com.batache.songs.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

  protected View mRootView;

  public abstract int getLayout();

  public abstract void onCreateView();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(getLayout(), null);

    onCreateView();

    return mRootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    onApplyAllWindowInsets();
  }

  public abstract void onApplyAllWindowInsets();
}
