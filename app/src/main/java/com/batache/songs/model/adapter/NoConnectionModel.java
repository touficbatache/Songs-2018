package com.batache.songs.model.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.batache.songs.R;
import com.google.android.material.button.MaterialButton;

@EpoxyModelClass(layout = R.layout.layout_no_connection)
public abstract class NoConnectionModel extends EpoxyModelWithHolder<NoConnectionModel.NoConnectionHolder> {

  @EpoxyAttribute
  View.OnClickListener onClickListener;

  @Override
  public void bind(@NonNull NoConnectionHolder holder) {
    super.bind(holder);

    holder.myMusicBtn.setOnClickListener(onClickListener);
  }

  @Override
  public void unbind(@NonNull NoConnectionHolder holder) {
    super.unbind(holder);

    holder.myMusicBtn.setOnClickListener(null);
  }

  static class NoConnectionHolder extends EpoxyHolder {
    View itemView;
    MaterialButton myMusicBtn;

    @Override
    protected void bindView(@NonNull View itemView) {
      this.itemView = itemView;
      myMusicBtn = itemView.findViewById(R.id.btn_my_music);
    }
  }
}
