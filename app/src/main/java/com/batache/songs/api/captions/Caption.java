package com.batache.songs.api.captions;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "text", strict = false)
public class Caption {

  @Attribute(name = "start")
  public String start;

  @Text
  public String text;

}
