package com.batache.songs.model.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.batache.songs.ui.controller.SearchController;
import com.batache.songs.ui.controller.SongsController;

@EpoxyModelClass(layout = R.layout.item_autocomplete_query)
public abstract class AutocompleteQueryModel extends EpoxyModelWithHolder<AutocompleteQueryModel.LyricHolder> {

  @EpoxyAttribute
  String autocompleteQuery;

  @EpoxyAttribute
  SearchController.OnAutocompleteQueryClickListener onClickListener;

  @Override
  public void bind(@NonNull LyricHolder holder) {
    super.bind(holder);

    holder.suggestedQueryTv.setText(autocompleteQuery);
    holder.itemView.setOnClickListener(view -> onClickListener.onClick(autocompleteQuery));
  }

  class LyricHolder extends EpoxyHolder {

    private View itemView;
    private TextView suggestedQueryTv;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
      suggestedQueryTv = itemView.findViewById(R.id.suggested_query_tv);
    }
  }
}