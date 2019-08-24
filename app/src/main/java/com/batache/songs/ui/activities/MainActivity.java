package com.batache.songs.ui.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.batache.songs.R;
import com.batache.songs.ui.base.BaseActivity;
import com.batache.songs.ui.fragments.PlayerFragment;
import com.batache.songs.util.DimensionUtils;
import com.batache.songs.util.PlayerHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity {

  private AppBarLayout appBar;

  private NavController navController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavigationView bottomNavigationView;

  public SlidingUpPanelLayout slidingPanelLayout;
  private int bottomNavigationHeight = 0;

  @Override
  public Context getContext() {
    return MainActivity.this;
  }

  @Override
  public int getLayout() {
    return R.layout.activity_main;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setSupportActionBar(findViewById(R.id.toolbar));
    appBar = findViewById(R.id.app_bar);

    setupNavigation();
    initPlayer();
  }

  private void setupNavigation() {
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);

    Set<Integer> topLevelDestinations = new HashSet<>();
    topLevelDestinations.add(R.id.exploreFragment);
    topLevelDestinations.add(R.id.myMusicFragment);
    appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    bottomNavigationView = findViewById(R.id.navigation);
    bottomNavigationView.post(() -> {
      bottomNavigationHeight = bottomNavigationView.getMeasuredHeight();
      onApplyAllWindowInsets();
    });
    NavigationUI.setupWithNavController(bottomNavigationView, navController);
  }

  private void initPlayer() {
    slidingPanelLayout = findViewById(R.id.sliding_layout);

    getSupportFragmentManager().popBackStack();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.player_fragment_container, new PlayerFragment())
        .commitAllowingStateLoss();
  }

  @Override
  public void onApplyAllWindowInsets() {
    appBar.setPadding(0, DimensionUtils.TOP_INSET, 0, 0);
    slidingPanelLayout.setPanelHeight(
        bottomNavigationHeight + (int) getResources().getDimension(R.dimen.mini_player_height)
    );
  }

  public void setBottomNavigationY(float alpha) {
    if (bottomNavigationView == null) {
      return;
    }
    bottomNavigationView.setTranslationY(bottomNavigationView.getHeight() * (alpha * -1 + 1));
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration);
  }

  @Override
  public void onBackPressed() {
    if (slidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      return;
    }

    super.onBackPressed();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    PlayerHelper.destroy();
  }
}
