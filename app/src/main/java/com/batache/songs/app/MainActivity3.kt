package com.batache.songs.app

import android.animation.StateListAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.batache.songs.R
import com.batache.songs.api.SongsAPI
import com.batache.songs.app.base.BaseActivity
import com.batache.songs.app.settings.SettingsActivity
import com.batache.songs.player.MiniPlayerFragment
import com.batache.songs.player.PlayerFragment
import com.batache.songs.ui.controller.SearchController
import com.batache.songs.util.CodeUtils
import com.batache.songs.util.DeviceUtils
import com.batache.songs.util.DimensionUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.android.synthetic.main.activity_main3.*
import kotlinx.android.synthetic.main.floating_search_bar.*
import java.util.*

class MainActivity3 : BaseActivity() {

  val appBarHeight: Int
    get() = appBar.measuredHeight - DimensionUtils.TOP_INSET
  private var appBarStateListAnimator: StateListAnimator? = null

  private var navController: NavController? = null
  private var appBarConfiguration: AppBarConfiguration? = null

  private var isSearchOpen = false
  private var searchController: SearchController? = null

  private var suggestedQueries: List<String>? = null
  val searchHandler = Handler()
  var searchRunnable = Runnable {
    searchController?.addAutocompleteQueries(suggestedQueries, object : SearchController.OnAutocompleteQueryClickListener {
      override fun onClick(autocompleteQuery: String?) {
        autocompleteQuery?.let { query ->
          performSearch(query)
        }
      }
    })
  }

  private lateinit var sheetBehavior: BottomSheetBehavior<View>

  private var isShapeAdded: Boolean = false

  private lateinit var miniPlayerFragment: MiniPlayerFragment
  private lateinit var playerFragment: PlayerFragment

  private var bottomNavigationHeight = 0

  override fun getLayout(): Int {
    return R.layout.activity_main3
  }

  override fun getContext(): Context {
    return this
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(toolbar)
    appBarStateListAnimator = appBar.stateListAnimator

    miniPlayerFragment = supportFragmentManager.findFragmentById(R.id.mini_player) as MiniPlayerFragment
    playerFragment = supportFragmentManager.findFragmentById(R.id.player) as PlayerFragment

    setupSearch()
    setupNavigation()
    setupPlayerBottomSheet()
  }
  
  fun getAppBar(): AppBarLayout {
    return appBar
  }

  private fun setupSearch() {
    SearchController().let {
      searchController = it
      searchRecyclerView.layoutManager = LinearLayoutManager(context)
      searchRecyclerView.setController(it)
    }

    floatingSearchBarContent.setOnClickListener { view: View? -> openSearch() }

    settingsBtn.setOnClickListener { view: View? -> startActivity(Intent(this, SettingsActivity::class.java)) }

    searchBar.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun afterTextChanged(editable: Editable) {
        searchController?.clear()
        if (editable.toString().trim { it <= ' ' }.isEmpty()) {
          return
        }
        SongsAPI.getInstance().getAutocomplete(editable.toString(), object : SongsAPI.OnAutocompleteFetchedListener {
          override fun onSuccess(suggestedQueries: List<String>) {
            searchHandler.removeCallbacks(searchRunnable)
            this@MainActivity3.suggestedQueries = suggestedQueries
            searchHandler.postDelayed(searchRunnable, 300)
          }

          override fun onFailure(t: Throwable?) {}
        })
      }
    })

    searchBar.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
      searchBar.searchQuery.trim { it <= ' ' }.let { query ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH && query.isNotEmpty()) {
          searchBar.clearSearchFocus()
          DeviceUtils.hideKeyboardFrom(context, searchBar.searchEt)
          performSearch(query)
        }
      }
      true
    }

    searchBar.setBackIconClickListener { view: View? -> closeResetSearch() }
  }

  private fun openSearch() {
    isSearchOpen = true

    MaterialContainerTransform().apply {
      startView = floatingSearchBar
      endView = fullpageSearchView

      addTarget(endView!!)

      setPathMotion(MaterialArcMotion())
    }.let { transform ->
      TransitionManager.beginDelayedTransition(floatingSearchBar, transform)
      floatingSearchBar.visibility = View.INVISIBLE
      fullpageSearchView.visibility = View.VISIBLE
    }

    Handler().postDelayed({
      if (searchBar.requestSearchFocus()) {
        DeviceUtils.showKeyboardOn(context, searchBar.searchEt)
      }
    }, 250)
  }

  private fun closeResetSearch() {
    isSearchOpen = false

    MaterialContainerTransform().apply {
      startView = fullpageSearchView
      endView = floatingSearchBar

      addTarget(endView!!)

      setPathMotion(MaterialArcMotion())
    }.let { transform ->
      TransitionManager.beginDelayedTransition(fullpageSearchView, transform)
      fullpageSearchView.visibility = View.GONE
      floatingSearchBar.visibility = View.VISIBLE
    }

    DeviceUtils.hideKeyboardFrom(context, searchBar.searchEt)
    searchBar.clearSearchText()
    searchController?.clear()
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

  private fun setupNavigation() {
    HashSet<Int>().apply {
      add(R.id.exploreFragment)
      add(R.id.myMusicFragment)
    }.let { topLevelDestinations ->
      appBarConfiguration = AppBarConfiguration.Builder(topLevelDestinations).build()
    }

    navController = Navigation.findNavController(this, R.id.nav_host_fragment).apply {
      appBarConfiguration?.let { appBarConfiguration ->
        NavigationUI.setupActionBarWithNavController(this@MainActivity3, this, appBarConfiguration)
      }

      addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
        if (destination.id == R.id.exploreFragment || destination.id == R.id.myMusicFragment) {
          appBar.isLiftOnScroll = false
          appBar.stateListAnimator = null
          floatingSearchBar.visibility = View.VISIBLE
          toolbar.visibility = View.GONE
        } else {
          appBar.stateListAnimator = appBarStateListAnimator
          appBar.isLiftOnScroll = true
          toolbar.visibility = View.VISIBLE
          floatingSearchBar.visibility = View.GONE
        }
      }

      bottomNavigation.post {
        bottomNavigationHeight = bottomNavigation.measuredHeight
        onApplyAllWindowInsets()
      }
      NavigationUI.setupWithNavController(bottomNavigation, this)

      bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
        NavigationUI.onNavDestinationSelected(item, this)
//        if (isSearchOpen) {
//          closeResetSearch()
//        }
        true
      }
    }
  }

  private fun setupPlayerBottomSheet() {
    sheetBehavior = BottomSheetBehavior.from(playerBottomSheet)
    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//    sheetBehavior.halfExpandedRatio = 0.83f
//    sheetBehavior.isFitToContents = false

    val dialogBackground = MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, getPx(16f))
            .setTopRightCorner(CornerFamily.ROUNDED, getPx(16f))
            .build()
    ).apply {
      setTint(getColorAttr(R.attr.colorSurface))
    }

    sheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0 && touch_outside.visibility != View.VISIBLE) {
          touch_outside.visibility = View.VISIBLE
        } else if (slideOffset == 0f) {
          touch_outside.visibility = View.GONE
        }

        touch_outside.alpha = slideOffset * 0.5f

        val topOffsetMultiplier: Float = (slideOffset * 12).coerceAtLeast(0f).coerceAtMost(1f)
        bottomNavigation.translationY = bottomNavigation.measuredHeight * topOffsetMultiplier

        miniPlayerFragment.updateVisibility(slideOffset)

        playerFragment.updateVisibility(slideOffset)
      }

      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (!isShapeAdded) {
          ViewCompat.setBackground(bottomSheet, dialogBackground)
          isShapeAdded = true
        }
      }
    })

    mini_player_container.setOnClickListener {
      sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    touch_outside.setOnClickListener {
      sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
  }

  private fun getColorAttr(colorResId: Int): Int {
    var color: Int

    context.obtainStyledAttributes(TypedValue().data, intArrayOf(colorResId)).apply {
      color = getColor(0, 0)
      recycle()
    }

    return color
  }

  private fun getPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
  }

  override fun onResume() {
    super.onResume()

    registerMiniPlayer(true)
    registerPlayer(true)
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

  fun getPlayerState() = sheetBehavior.state

  fun collapsePlayer() {
    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
  }

  fun hidePlayer() {
    sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  override fun onApplyAllWindowInsets() {
    (findViewById<View>(R.id.nav_host_fragment).layoutParams as ViewGroup.MarginLayoutParams).let { params ->
      params.setMargins(
          params.leftMargin,
          appBarHeight * -1,
          params.rightMargin,  //notice only changing right margin
          bottomNavigationHeight + resources.getDimension(R.dimen.mini_player_height).toInt() + appBarHeight
      )
    }

    statusBarOverlay.layoutParams = statusBarOverlay.layoutParams.apply {
      height = DimensionUtils.TOP_INSET
    }

    appBar.setPadding(0, DimensionUtils.TOP_INSET, 0, 0)

    sheetBehavior.peekHeight = bottomNavigationHeight + resources.getDimension(R.dimen.mini_player_height).toInt()

    playerFragment.onApplyAllWindowInsets()
    miniPlayerFragment.onApplyAllWindowInsets()
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

    if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
      collapsePlayer()
      return
    }

    super.onBackPressed()
  }

  override fun onPause() {
    super.onPause()

    registerMiniPlayer(false)
    registerPlayer(false)
  }

}