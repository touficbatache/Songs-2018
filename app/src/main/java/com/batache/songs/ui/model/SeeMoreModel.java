package com.batache.songs.ui.model;

import android.view.View;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import com.batache.songs.R;

@EpoxyModelClass(layout = R.layout.item_see_more)
public abstract class SeeMoreModel extends EpoxyModelWithHolder<SeeMoreModel.SeeMoreHolder> {

  @EpoxyAttribute
  View.OnClickListener onClickListener;

  @Override
  public void bind(@NonNull SeeMoreHolder holder) {
    super.bind(holder);

    holder.itemView.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(@NonNull SeeMoreHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class SeeMoreHolder extends EpoxyHolder {
    View itemView;

    @Override
    protected void bindView(View itemView) {
      this.itemView = itemView;
    }
  }
}
