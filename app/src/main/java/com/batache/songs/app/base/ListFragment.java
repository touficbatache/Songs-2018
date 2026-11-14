package com.batache.songs.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.epoxy.EpoxyController;
import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.app.MainActivity;

public abstract class ListFragment<T extends EpoxyController> extends BaseFragment {

  protected T mController;
  private EpoxyRecyclerView recyclerView;

  private int originalTopSpaceableViewPaddingTop;

  public abstract T getController();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(getLayout(), null);

    mController = getController();
    recyclerView = rootView.findViewById(R.id.recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setController(mController);

    if (requireActivity() instanceof MainActivity) {
      MainActivity activity = (MainActivity) requireActivity();

      activity.getAppBar().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
          onApplyTopAppBarSpacing(activity.getAppBarHeight());
          activity.getAppBar().removeOnLayoutChangeListener(this);
        }
      });
    }

    onCreateView();

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    originalTopSpaceableViewPaddingTop = getTopSpaceableView().getPaddingTop();
  }

  @Override
  public void onApplyAllWindowInsets() {
    if (requireActivity() instanceof MainActivity) {
      onApplyTopAppBarSpacing(((MainActivity) requireActivity()).getAppBarHeight());
    }
  }

  private void onApplyTopAppBarSpacing(int topSpacing) {
    View spaceableView = getTopSpaceableView();

    if (spaceableView == null) {
      return;
    }

    spaceableView.setPadding(
        spaceableView.getPaddingLeft(),
        originalTopSpaceableViewPaddingTop + topSpacing,
        spaceableView.getPaddingRight(),
        spaceableView.getPaddingBottom()
    );
  }

  public View getTopSpaceableView() {
    return recyclerView;
  }
}
