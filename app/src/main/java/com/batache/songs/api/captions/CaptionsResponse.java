package com.batache.songs.api.captions;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "transcript", strict = false)
public class CaptionsResponse {

  @ElementList(inline = true, required = false)
  public List<Caption> captions;

}
