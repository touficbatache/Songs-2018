package com.batache.songs.app.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.batache.songs.app.MainActivity;

public abstract class BaseFragment extends Fragment {

  protected View rootView;

  private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo ni = manager.getActiveNetworkInfo();
      onNetworkChanged(ni);
    }
  };

  private int originalRootViewHeight;

  public abstract int getLayout();

  public abstract void onCreateView();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(getLayout(), null);

//    if (requireActivity() instanceof MainActivity) {
//      ((MainActivity) requireActivity()).getAppBar().addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> fixRootViewHeight());
//    }

    onCreateView();

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    originalRootViewHeight = rootView.getMeasuredHeight();

//    if (requireActivity() instanceof MainActivity) {
//      fixRootViewHeight();
//    }
  }

  @Override
  public void onResume() {
    super.onResume();

//    if (requireActivity() instanceof MainActivity) {
//      fixRootViewHeight();
//    }

    onApplyAllWindowInsets();

    requireActivity().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
  }

  private void fixRootViewHeight() {
    rootView.setLayoutParams(
        new FrameLayout.LayoutParams(
            rootView.getMeasuredWidth(),
            rootView.getMeasuredHeight() - ((MainActivity) requireActivity()).getAppBarHeight()
        )
    );
  }

  public abstract void onApplyAllWindowInsets();

  public void onNetworkChanged(NetworkInfo networkInfo) {

  }

  @Override
  public void onPause() {
    super.onPause();

    requireActivity().unregisterReceiver(networkStateReceiver);
  }
}
