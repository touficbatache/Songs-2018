package com.batache.songs.app

import android.animation.StateListAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.batache.songs.R
import com.batache.songs.api.SongsAPI
import com.batache.songs.api.SongsAPI.OnAutocompleteFetchedListener
import com.batache.songs.app.base.BaseActivity
import com.batache.songs.app.settings.SettingsActivity
import com.batache.songs.player.MiniPlayerFragment
import com.batache.songs.player.PlayerFragment
import com.batache.songs.ui.controller.SearchController
import com.batache.songs.ui.controller.SongsController
import com.batache.songs.ui.view.SearchBar
import com.batache.songs.util.CodeUtils
import com.batache.songs.util.DeviceUtils
import com.batache.songs.util.DimensionUtils
import com.batache.songs.util.PlaylistImporter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.android.synthetic.main.floating_search_bar.*
import java.util.*

class MainActivity : BaseActivity(), SlidingUpPanelLayout.PanelSlideListener {

  private lateinit var statusBarOverlay: View
  private var stateListAnimator: StateListAnimator? = null
  lateinit var appBar: AppBarLayout
  private lateinit var toolbar: MaterialToolbar

  private var navController: NavController? = null
  private var appBarConfiguration: AppBarConfiguration? = null
  private lateinit var bottomNavigationView: BottomNavigationView

  private var isSearchOpen = false
  private lateinit var floatingSearchBar: CardView
  private lateinit var fullpageSearchView: CardView
  private lateinit var searchBar: SearchBar
  private lateinit var searchRecyclerView: EpoxyRecyclerView
  private var controller: SearchController? = null

  private var suggestedQueries: List<String>? = null

  val handler = Handler()
  var searchRunnable = Runnable {
    controller?.addAutocompleteQueries(suggestedQueries, object : SearchController.OnAutocompleteQueryClickListener {
      override fun onClick(autocompleteQuery: String?) {
        autocompleteQuery?.let { query ->
          performSearch(query)
        }
      }
    })
  }

  private lateinit var slidingPanelLayout: SlidingUpPanelLayout

  private var playerFragment: PlayerFragment? = null
  private var miniPlayerFragment: MiniPlayerFragment? = null

  private var bottomNavigationHeight = 0

  override fun getContext(): Context {
    return this@MainActivity
  }

  override fun getLayout(): Int {
    return R.layout.activity_main
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    statusBarOverlay = findViewById(R.id.status_bar_overlay)

    appBar = findViewById(R.id.appBar)
    appBar.addOnLayoutChangeListener { view, i, i1, i2, i3, i4, i5, i6, i7 ->
      val marginParams = findViewById<View>(R.id.nav_host_fragment).layoutParams as MarginLayoutParams
      marginParams.setMargins(marginParams.leftMargin,
          appBarHeight * -1,
          marginParams.rightMargin,  //notice only changing right margin
          marginParams.bottomMargin)
    }
    stateListAnimator = appBar.stateListAnimator

    toolbar = findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)

    setupFloatingSearchBar()
    setupNavigation()
    initializePlayer()
    initializePlaylistImporter()
  }

  private fun setupFloatingSearchBar() {
    floatingSearchBar = findViewById(R.id.floatingSearchBar)
    fullpageSearchView = findViewById(R.id.fullpage_search_view)

    searchBar = findViewById(R.id.searchBar)
    searchBar.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
      val query = searchBar.getSearchQuery().trim { it <= ' ' }
      if (actionId == EditorInfo.IME_ACTION_SEARCH && !query.isEmpty()) {
        searchBar.clearSearchFocus()
        DeviceUtils.hideKeyboardFrom(context, searchBar.getSearchEt())
        performSearch(query)
      }
      true
    }
    searchBar.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun afterTextChanged(editable: Editable) {
        controller?.clear()
        if (editable.toString().trim { it <= ' ' }.isEmpty()) {
          return
        }
        SongsAPI.getInstance().getAutocomplete(editable.toString(), object : OnAutocompleteFetchedListener {
          override fun onSuccess(suggestedQueries: List<String>) {
            handler.removeCallbacks(searchRunnable)
            this@MainActivity.suggestedQueries = suggestedQueries
            handler.postDelayed(searchRunnable, 300)
          }

          override fun onFailure(t: Throwable?) {}
        })
      }
    })

    settingsBtn.setOnClickListener { view: View? -> startActivity(Intent(this, SettingsActivity::class.java)) }

    floatingSearchBarContent.setOnClickListener { view: View? -> openSearch() }

    searchBar.setBackIconClickListener { view: View? -> closeResetSearch() }

    controller = SearchController()
    searchRecyclerView = findViewById(R.id.search_recyclerview)
    searchRecyclerView.layoutManager = LinearLayoutManager(context)
    controller?.let {
      searchRecyclerView.setController(it)
    }
  }

  private fun openSearch() {
    isSearchOpen = true

    val transform = MaterialContainerTransform().apply {
      startView = floatingSearchBar
      endView = fullpageSearchView

      addTarget(endView!!)

      setPathMotion(MaterialArcMotion())
    }

    TransitionManager.beginDelayedTransition(floatingSearchBar, transform)
    floatingSearchBar.visibility = View.INVISIBLE
    fullpageSearchView.visibility = View.VISIBLE

    Handler().postDelayed({
      if (searchBar.requestSearchFocus()) {
        DeviceUtils.showKeyboardOn(context, searchBar.searchEt)
      }
    }, 250)
  }

  private fun closeResetSearch() {
    isSearchOpen = false

    val transform = MaterialContainerTransform().apply {
      startView = fullpageSearchView
      endView = floatingSearchBar

      addTarget(endView!!)

      setPathMotion(MaterialArcMotion())
    }

    TransitionManager.beginDelayedTransition(fullpageSearchView, transform)
    fullpageSearchView.visibility = View.GONE
    floatingSearchBar.visibility = View.VISIBLE

    DeviceUtils.hideKeyboardFrom(context, searchBar.searchEt)
    searchBar.clearSearchText()
    controller?.clear()
  }

  private fun performSearch(query: String) {
    closeResetSearch()
    Handler().postDelayed({
      val bundle = Bundle()
      bundle.putString("title", query)
      bundle.putString("query", query)
      navController?.navigate(R.id.searchFragment, bundle)
    }, 300)
  }

  val appBarHeight: Int
    get() = appBar.measuredHeight - DimensionUtils.TOP_INSET

  private fun setupNavigation() {
    val topLevelDestinations: MutableSet<Int> = HashSet()
    topLevelDestinations.apply {
      add(R.id.exploreFragment)
      add(R.id.myMusicFragment)
    }
    appBarConfiguration = AppBarConfiguration.Builder(topLevelDestinations).build()

    navController = Navigation.findNavController(this, R.id.nav_host_fragment).apply {
      appBarConfiguration?.let { appBarConfiguration ->
        NavigationUI.setupActionBarWithNavController(this@MainActivity, this, appBarConfiguration)
      }

      addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
        if (destination.id == R.id.exploreFragment || destination.id == R.id.myMusicFragment) {
          appBar.isLiftOnScroll = false
          appBar.stateListAnimator = null
          floatingSearchBar.visibility = View.VISIBLE
          toolbar.visibility = View.GONE
        } else {
          appBar.stateListAnimator = stateListAnimator
          appBar.isLiftOnScroll = true
          toolbar.visibility = View.VISIBLE
          floatingSearchBar.visibility = View.GONE
        }
      }

      bottomNavigationView = findViewById(R.id.navigation)
      bottomNavigationView.post {
        bottomNavigationHeight = bottomNavigationView.measuredHeight
        onApplyAllWindowInsets()
      }
      NavigationUI.setupWithNavController(bottomNavigationView, this)

      bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
        NavigationUI.onNavDestinationSelected(item, this)
        if (isSearchOpen) {
          closeResetSearch()
        }
        true
      }
    }
  }

  fun goToMyMusic() {
    navController?.navigate(R.id.myMusicFragment)
  }

  private fun initializePlayer() {
    slidingPanelLayout = findViewById(R.id.sliding_layout)
    slidingPanelLayout.addPanelSlideListener(this)

    playerFragment = supportFragmentManager.findFragmentById(R.id.fragment_player) as PlayerFragment?

    miniPlayerFragment = supportFragmentManager.findFragmentById(R.id.fragment_mini_player) as MiniPlayerFragment?
  }

  override fun onPanelSlide(panel: View, slideOffset: Float) {
    miniPlayerFragment?.updateVisibility(slideOffset)

    playerFragment?.updateVisibility(slideOffset)

    setBottomNavigationY(slideOffset)
  }

  override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
    playerFragment?.let {
      if (newState == PanelState.EXPANDED) {
        it.setClickable(true)
        it.addListenerToRecyclerView()
      } else {
        onApplyAllWindowInsets()
        it.setClickable(false)
        it.clearListenerFromRecyclerView()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    registerPlayer(true)
    registerMiniPlayer(true)

    if (!isSearchOpen) {
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }
  }

  private fun registerPlayer(register: Boolean) { //FIXME do not register to eventbuss if there is no playqueue
    playerFragment?.let {
      if (register) {
        if (CodeUtils.registerToEventBus(playerFragment)) {
          it.update()
        }
      } else {
        CodeUtils.unregisterFromEventBus(playerFragment)
      }
    }
  }

  private fun registerMiniPlayer(register: Boolean) { //FIXME do not register to eventbuss if there is no playqueue
    miniPlayerFragment?.let {
      if (register) {
        if (CodeUtils.registerToEventBus(miniPlayerFragment)) {
          it.update()
        }
      } else {
        CodeUtils.unregisterFromEventBus(miniPlayerFragment)
      }
    }
  }

  fun setBottomNavigationY(slideOffset: Float) {
    val topOffsetMultiplier: Float = (slideOffset * 12).coerceAtLeast(0f).coerceAtMost(1f)
    bottomNavigationView.translationY = bottomNavigationView.height * topOffsetMultiplier
  }

  fun collapsePlayer() {
    slidingPanelLayout.panelState = PanelState.COLLAPSED
  }

  fun hidePlayer() {
    slidingPanelLayout.panelState = PanelState.HIDDEN
  }

  val playerState: PanelState
    get() = slidingPanelLayout.panelState

  private fun initializePlaylistImporter() {
    if (TextUtils.equals(intent.action, Intent.ACTION_VIEW)) {
      val data = intent.data
      if (data != null && TextUtils.equals(data.scheme, "content")
          && intent.type == "application/octet-stream") {
        PlaylistImporter.getInstance().importPlaylist(data //            , () -> Snackbar.make(findViewById(R.id.nav_host_fragment), "Playlist imported successfully", Snackbar.LENGTH_SHORT)
        )
      }
    }
  }

  override fun onApplyAllWindowInsets() {
//    findViewById(R.id.status_bar_overlay).setLayoutParams(new ViewGroup.LayoutParams(findViewById(R.id.status_bar_overlay).getMeasuredWidth(), DimensionUtils.TOP_INSET));
//    findViewById(R.id.status_bar_overlay).setMinimumHeight(DimensionUtils.TOP_INSET);
    statusBarOverlay.layoutParams = statusBarOverlay.layoutParams.apply {
      height = DimensionUtils.TOP_INSET
    }
    appBar.setPadding(0, DimensionUtils.TOP_INSET, 0, 0)

    slidingPanelLayout.panelHeight = bottomNavigationHeight + resources.getDimension(R.dimen.mini_player_height).toInt()

    playerFragment?.onApplyAllWindowInsets()
    miniPlayerFragment?.onApplyAllWindowInsets()
  }

  override fun onSupportNavigateUp(): Boolean {
    navController?.let { navController ->
      appBarConfiguration?.let { appBarConfiguration ->
        return NavigationUI.navigateUp(navController, appBarConfiguration)
      }
    }

    return false
  }

  override fun onBackPressed() {
    if (isSearchOpen) {
      closeResetSearch()
      return
    }

    if (playerState == PanelState.EXPANDED) {
      collapsePlayer()
      return
    }

    super.onBackPressed()
  }

  override fun onPause() {
    super.onPause()
    registerPlayer(false)
    registerMiniPlayer(false)
  }
}