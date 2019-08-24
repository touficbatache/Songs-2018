package com.batache.songs.util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class CodeUtils {

  public static boolean registerToEventBus(Object object) {
    if (!EventBus.getDefault().isRegistered(object)) {
      EventBus.getDefault().register(object);
      return true;
    }
    return false;
  }

  public static void unregisterFromEventBus(Object object) {
    if (EventBus.getDefault().isRegistered(object)) {
      EventBus.getDefault().unregister(object);
    }
  }

  public static boolean isEmpty(String string) {
    if(string == null) {
      return true;
    }
    return string.isEmpty();
  }

  public static boolean isEmpty(int integer) {
    return integer == -1;
  }

  public static boolean isEmpty(List<?> arrayList) {
    if(arrayList == null) {
      return true;
    }
    return arrayList.isEmpty();
  }

}
