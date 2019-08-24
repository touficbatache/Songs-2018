package com.batache.songs.util;

import android.content.Context;

import java.text.DecimalFormat;

import com.batache.songs.R;

public class TextUtils {

  public static String formatMilliseconds(Context context, int milliseconds) {
    DecimalFormat format = new DecimalFormat("00");
    String minutes = format.format(milliseconds / 1000 / 60);
    String seconds = format.format(milliseconds / 1000 % 60);
    return String.format(context.getResources().getString(R.string.player_progress_text), minutes, seconds);
  }

  public static String formatIntoTwoNumbers(int number) {
    DecimalFormat format = new DecimalFormat("00");
    return format.format(number);
  }

  public static String removeParenthesis(String string) {
    return string.replaceAll("\\(.*\\)", "");
  }

  public static String removeBrackets(String string) {
    return string.replaceAll("\\[.*\\]", "");
  }

}
