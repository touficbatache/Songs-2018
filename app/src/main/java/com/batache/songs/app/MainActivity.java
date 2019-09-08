package com.batache.songs.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.batache.songs.R;
import com.batache.songs.app.base.BaseActivity;
import com.batache.songs.app.player.MiniPlayerFragment;
import com.batache.songs.app.player.PlayerFragment;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity implements SlidingUpPanelLayout.PanelSlideListener {

  private AppBarLayout appBar;
  private Toolbar toolbar;

  private NavController navController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavigationView bottomNavigationView;

  public SlidingUpPanelLayout slidingPanelLayout;

  private PlayerFragment playerFragment;
  private MiniPlayerFragment miniPlayerFragment;

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
    appBar = findViewById(R.id.app_bar);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    setupNavigation();
    initializePlayer();
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

  private void initializePlayer() {
    slidingPanelLayout = findViewById(R.id.sliding_layout);
    slidingPanelLayout.addPanelSlideListener(this);

    playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_player);

    miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_mini_player);
  }

  @Override
  public void onPanelSlide(View panel, float slideOffset) {
    if (miniPlayerFragment == null) {
      return;
    }
    float alpha = 1 - slideOffset;
    miniPlayerFragment.updateVisibility(alpha);
    setBottomNavigationY(alpha);
  }

  @Override
  public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
    if (playerFragment == null) {
      return;
    }
    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
      playerFragment.setClickable(true);
      if(playerFragment != null) {
        playerFragment.addListenerToRecyclerView();
      }
    } else {
      onApplyAllWindowInsets();
      playerFragment.setClickable(false);
      if(playerFragment != null) {
        playerFragment.clearListenerFromRecyclerView();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerPlayer(true);
    registerMiniPlayer(true);
  }

  private void registerPlayer(boolean register) {//FIXME do not register to eventbuss if there is no playqueue
    if (playerFragment == null) {
      return;
    }
    if (register) {
      if (CodeUtils.registerToEventBus(playerFragment)) {
        playerFragment.update();
      }
    } else {
      CodeUtils.unregisterFromEventBus(playerFragment);
    }
  }

  private void registerMiniPlayer(boolean register) {//FIXME do not register to eventbuss if there is no playqueue
    if (register) {
      if (CodeUtils.registerToEventBus(miniPlayerFragment)) {
        miniPlayerFragment.update();
      }
    } else {
      CodeUtils.unregisterFromEventBus(miniPlayerFragment);
    }
  }

  @Override
  public void onApplyAllWindowInsets() {
    appBar.setPadding(0, DimensionUtils.TOP_INSET, 0, 0);
    slidingPanelLayout.setPanelHeight(
        bottomNavigationHeight + (int) getResources().getDimension(R.dimen.mini_player_height)
    );

    if(playerFragment != null) {
      playerFragment.onApplyAllWindowInsets();
    }

    if(miniPlayerFragment != null) {
      miniPlayerFragment.onApplyAllWindowInsets();
    }
  }

  public void setBottomNavigationY(float alpha) {
    if (bottomNavigationView == null) {
      return;
    }
    bottomNavigationView.setTranslationY(bottomNavigationView.getHeight() * (alpha * -1 + 1));
  }

  public void collapsePlayer() {
    slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
  }

  public void hidePlayer() {
    slidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
  }

  public SlidingUpPanelLayout.PanelState getPlayerState() {
    return slidingPanelLayout.getPanelState();
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration);
  }

  @Override
  public void onBackPressed() {
    if (getPlayerState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
      collapsePlayer();
      return;
    }

    super.onBackPressed();
  }

  @Override
  protected void onPause() {
    super.onPause();
    registerPlayer(false);
    registerMiniPlayer(false);
  }
}
