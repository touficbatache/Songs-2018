package com.batache.songs.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.batache.songs.R;
import com.batache.songs.api.SongsAPI;
import com.batache.songs.app.base.BaseActivity;
import com.batache.songs.app.settings.SettingsActivity;
import com.batache.songs.model.pojo.Song;
import com.batache.songs.player.MiniPlayerFragment;
import com.batache.songs.player.PlayerFragment;
import com.batache.songs.ui.controller.SongsController;
import com.batache.songs.ui.view.SearchBar;
import com.batache.songs.util.CodeUtils;
import com.batache.songs.util.DimensionUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity implements SlidingUpPanelLayout.PanelSlideListener {

  private AppBarLayout appBar;
  private Toolbar toolbar;

  private NavController navController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavigationView bottomNavigationView;

  private boolean isSearchOpen = false;
  private CardView floatingSearchBar, fullpageSearchView;
  private SearchBar searchBar;
  private EpoxyRecyclerView searchRecyclerView;
  private SongsController controller;
  private boolean youtubeFailed, dndnhaFailed, xiamiFailed = false;
  private List<Song> searchResults = new ArrayList<>();

  public SlidingUpPanelLayout slidingPanelLayout;

  private PlayerFragment playerFragment;
  private MiniPlayerFragment miniPlayerFragment;

  private int bottomNavigationHeight = 0;
  private int panelHeight;

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

    setupFloatingSearchBar();
    setupNavigation();
    initializePlayer();
  }

  private void setupFloatingSearchBar() {
    floatingSearchBar = findViewById(R.id.floating_search_bar);
    fullpageSearchView = findViewById(R.id.fullpage_search_view);
    searchBar = findViewById(R.id.searchbar);
    searchBar.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        search(searchBar.getSearchQuery().trim());
        searchBar.clearSearchFocus();
        closeKeyboard();
        return true;
      }
      return false;
    });
    findViewById(R.id.action_settings).setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));

    findViewById(R.id.floating_search_bar_content).setOnClickListener(view -> {
      openSearch();
    });

    searchBar.setBackIconClickListener(view -> closeResetSearch());

    controller = new SongsController();
    searchRecyclerView = findViewById(R.id.search_recyclerview);
    searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    searchRecyclerView.setController(controller);
  }

  private void openSearch() {
    isSearchOpen = true;
    MaterialContainerTransform transform = new MaterialContainerTransform(MainActivity.this);
    transform.setStartView(floatingSearchBar);
    transform.setEndView(fullpageSearchView);
    transform.setPathMotion(new MaterialArcMotion());

    TransitionManager.beginDelayedTransition(floatingSearchBar, transform);
    floatingSearchBar.setVisibility(View.INVISIBLE);
    fullpageSearchView.setVisibility(View.VISIBLE);

    new Handler().postDelayed(() -> {
      if (searchBar.requestSearchFocus()) {
        openKeyboard();
      }
    }, 250);

//    slidingPanelLayout.setPanelHeight(0);
//    bottomNavigationView.setVisibility(View.GONE);
  }

  private void closeResetSearch() {
    isSearchOpen = false;
    MaterialContainerTransform transform = new MaterialContainerTransform(MainActivity.this);
    transform.setStartView(fullpageSearchView);
    transform.setEndView(floatingSearchBar);
    transform.setPathMotion(new MaterialArcMotion());

    TransitionManager.beginDelayedTransition(fullpageSearchView, transform);
    fullpageSearchView.setVisibility(View.GONE);
    floatingSearchBar.setVisibility(View.VISIBLE);

    closeKeyboard();

    searchBar.clearSearchText();
    controller.clear();

//    new Handler().postDelayed(() -> {
//      if (searchBar.requestSearchFocus()) {
//        slidingPanelLayout.setPanelHeight(panelHeight);
//        bottomNavigationView.setVisibility(View.VISIBLE);
//      }
//    }, 300);
  }

  private void search(String searchQuery) {
    controller.clear();

    searchResults = new ArrayList<>();

    searchYoutube(searchQuery, () -> searchDndnha(searchQuery, () -> searchXiami(searchQuery, () -> {
      if (youtubeFailed && dndnhaFailed && xiamiFailed) {
        Snackbar.make(findViewById(R.id.recyclerview), "No songs found", Snackbar.LENGTH_SHORT).show();
        return;
      }

      controller.addExploreSearchResultsSection(this, searchResults);
    })));
  }

  private void searchYoutube(String searchQuery, SearchListener searchListener) {
    SongsAPI.getInstance().searchYoutube(this, searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        youtubeFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(Throwable t) {
        youtubeFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private void searchDndnha(String searchQuery, SearchListener searchListener) {
    SongsAPI.getInstance().searchDndnha(searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        dndnhaFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(@Nullable Throwable t) {
        dndnhaFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private void searchXiami(String searchQuery, SearchListener searchListener) {
    SongsAPI.getInstance().searchXiami(this, searchQuery, new SongsAPI.OnSongsFetchedListener() {
      @Override
      public void onSuccess(List<Song> fetchedSongs) {
        xiamiFailed = false;
        searchResults.addAll(fetchedSongs);
        searchListener.onComplete();
      }

      @Override
      public void onFailure(Throwable t) {
        xiamiFailed = true;
        searchListener.onComplete();
      }
    });
  }

  private interface SearchListener {
    void onComplete();
  }

  private void setupNavigation() {
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);

    Set<Integer> topLevelDestinations = new HashSet<>();
    topLevelDestinations.add(R.id.exploreFragment);
    topLevelDestinations.add(R.id.myMusicFragment);
    appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
      if (destination.getId() == R.id.exploreFragment || destination.getId() == R.id.myMusicFragment) {
        appBar.setElevation(0);
        floatingSearchBar.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);
      } else {
        appBar.setElevation(10);
        toolbar.setVisibility(View.VISIBLE);
        floatingSearchBar.setVisibility(View.GONE);
      }
    });

    bottomNavigationView = findViewById(R.id.navigation);
    bottomNavigationView.post(() -> {
      bottomNavigationHeight = bottomNavigationView.getMeasuredHeight();
      onApplyAllWindowInsets();
    });
    NavigationUI.setupWithNavController(bottomNavigationView, navController);

    bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
      NavigationUI.onNavDestinationSelected(item, navController);
      if (isSearchOpen) {
        closeResetSearch();
      }
      return true;
    });
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
  public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState
      previousState, SlidingUpPanelLayout.PanelState newState) {
    if (playerFragment == null) {
      return;
    }
    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
      playerFragment.setClickable(true);
      if (playerFragment != null) {
        playerFragment.addListenerToRecyclerView();
      }
    } else {
      onApplyAllWindowInsets();
      playerFragment.setClickable(false);
      if (playerFragment != null) {
        playerFragment.clearListenerFromRecyclerView();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerPlayer(true);
    registerMiniPlayer(true);
    
    if(!isSearchOpen) {
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
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

    panelHeight = bottomNavigationHeight + (int) getResources().getDimension(R.dimen.mini_player_height);
    slidingPanelLayout.setPanelHeight(panelHeight);

    if (playerFragment != null) {
      playerFragment.onApplyAllWindowInsets();
    }

    if (miniPlayerFragment != null) {
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

  private void openKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(searchBar.getSearchEt(), InputMethodManager.SHOW_IMPLICIT);
  }

  private void closeKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(searchBar.getSearchEt().getWindowToken(), 0);
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration);
  }

  @Override
  public void onBackPressed() {
    if (isSearchOpen) {
      closeResetSearch();
      return;
    }

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
