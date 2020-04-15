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
    rootView = inflater.inflate(getLayout(), null);

    mController = new SongsController();
    EpoxyRecyclerView recyclerView = rootView.findViewById(R.id.recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setController(mController);

    onCreateView();

    return rootView;
  }

  @Override
  public void onApplyAllWindowInsets() {

  }
}
