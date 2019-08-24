package com.batache.songs.ui.model;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;

@EpoxyModelClass(layout = R.layout.item_lyric_line)
public abstract class LyricModel extends EpoxyModelWithHolder<LyricModel.LyricHolder> {

  @EpoxyAttribute
  String lyricLine;

  @EpoxyAttribute
  boolean selected = false;

  @Override
  public void bind(@NonNull LyricHolder holder) {
    super.bind(holder);

    holder.lyricLineTv.setText(lyricLine);
    if (selected) {
      holder.lyricLineTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
      holder.lyricLineTv.setTypeface(Typeface.DEFAULT_BOLD);
    } else {
      holder.lyricLineTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
      holder.lyricLineTv.setTypeface(Typeface.DEFAULT);
    }
    holder.lyricLineTv.requestLayout();
  }

  class LyricHolder extends EpoxyHolder {

    private TextView lyricLineTv;

    @Override
    protected void bindView(View itemView) {
      lyricLineTv = itemView.findViewById(R.id.lyric_line_tv);
    }
  }
}