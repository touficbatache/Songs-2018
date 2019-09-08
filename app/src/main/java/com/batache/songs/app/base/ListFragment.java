package com.batache.songs.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.ui.controller.SongsController;

public abstract class ListFragment extends BaseFragment {

  protected SongsController mController;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(getLayout(), null);

    mController = new SongsController();
    EpoxyRecyclerView mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mRecyclerView.setController(mController);

    onCreateView();

    return mRootView;
  }

  @Override
  public void onApplyAllWindowInsets() {

  }
}
