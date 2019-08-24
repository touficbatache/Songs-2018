package com.batache.songs.model;

import org.apache.commons.text.StringEscapeUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "text", strict = false)
public class Lyric {

  @Attribute(name = "milliseconds")
  public int milliseconds;

  @Text
  private String text;

  public Lyric() {

  }

  public Lyric(int milliseconds, String lyric) {
    this.milliseconds = milliseconds;
    text = lyric;
  }

  public Lyric(String lyric) {
    text = lyric;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return StringEscapeUtils.unescapeHtml4(text);
  }
}