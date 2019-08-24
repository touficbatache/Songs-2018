package com.batache.songs.ui.model;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;

@EpoxyModelClass(layout = R.layout.item_subheader)
public abstract class SubheaderModel extends EpoxyModelWithHolder<SubheaderModel.SubheaderHolder> {

  @EpoxyAttribute
  Context context;

  @EpoxyAttribute
  String title;

  @EpoxyAttribute
  View.OnClickListener onClickListener;

  @Override
  public void bind(@NonNull SubheaderHolder holder) {
    super.bind(holder);

    if (onClickListener != null) {
      TypedValue outValue = new TypedValue();
      context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
      holder.itemView.setBackgroundResource(outValue.resourceId);
      holder.itemView.setOnClickListener(onClickListener);
      holder.more.setVisibility(View.VISIBLE);
    } else {
      holder.itemView.setBackground(null);
      holder.itemView.setOnClickListener(null);
      holder.more.setVisibility(View.GONE);
    }

    holder.title.setText(title);
  }

  @Override
  public void unbind(@NonNull SubheaderHolder holder) {
    super.unbind(holder);

    holder.itemView.setOnClickListener(null);
  }

  class SubheaderHolder extends EpoxyHolder {

    View itemView;
    TextView title;
    ImageView more;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      title = itemView.findViewById(R.id.title);
      more = itemView.findViewById(R.id.more);
    }
  }

}
