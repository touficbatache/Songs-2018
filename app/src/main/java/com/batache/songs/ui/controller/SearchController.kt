package com.batache.songs.ui.controller

import com.batache.songs.app.base.BaseController
import com.batache.songs.model.adapter.AutocompleteQueryModel_

class SearchController : BaseController() {

  fun addAutocompleteQueries(suggestedQueries: List<String>?, onClickListener: OnAutocompleteQueryClickListener?) {
    suggestedQueries?.let { queries ->
      for (suggestedQuery in queries) {
        data.add(AutocompleteQueryModel_()
            .id("suggested_query_$suggestedQuery")
            .autocompleteQuery(suggestedQuery)
            .onClickListener(onClickListener)
        )
      }
      requestModelBuild()
    }
  }

  interface OnAutocompleteQueryClickListener {
    fun onClick(autocompleteQuery: String?)
  }

}