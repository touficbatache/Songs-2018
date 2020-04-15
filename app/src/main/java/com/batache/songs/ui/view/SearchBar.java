package com.batache.songs.ui.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.batache.songs.R;

public class SearchBar extends LinearLayoutCompat {

  private ImageView backIcon, clearIcon;
  private EditText searchEt;

  public SearchBar(Context context) {
    this(context, null);
  }

  public SearchBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(getContext(), R.layout.search_bar, this);

    backIcon = findViewById(R.id.close_search);
    clearIcon = findViewById(R.id.clear_text);
    searchEt = findViewById(R.id.et_search);

    clearIcon.setOnClickListener(view -> clearSearchText());
    searchEt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence != null && !charSequence.toString().isEmpty()) {
          clearIcon.setVisibility(VISIBLE);
        } else {
          clearIcon.setVisibility(GONE);
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
  }

  public void setBackIconClickListener(OnClickListener onClickListener) {
    backIcon.setOnClickListener(onClickListener);
  }

  public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
    searchEt.setOnEditorActionListener(onEditorActionListener);
  }

  public boolean requestSearchFocus() {
    return searchEt.requestFocus();
  }

  public void clearSearchFocus() {
    searchEt.clearFocus();
  }

  public String getSearchQuery() {
    return searchEt.getText().toString();
  }

  public void clearSearchText() {
    searchEt.getText().clear();
  }

  public EditText getSearchEt() {
    return searchEt;
  }
}
