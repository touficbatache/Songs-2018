package com.batache.songs.app

import android.content.Context
import android.os.Bundle
import com.batache.songs.R
import com.batache.songs.app.base.BaseActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class MainActivityWithMotionLayout : BaseActivity() {

  val playerState: SlidingUpPanelLayout.PanelState = SlidingUpPanelLayout.PanelState.HIDDEN

  override fun getLayout(): Int {
    return R.layout.activity_main_with_motion_layout
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun getContext(): Context {
    return this
  }

  override fun onApplyAllWindowInsets() {

  }

  fun collapsePlayer() {

  }

  fun hidePlayer() {

  }
}
