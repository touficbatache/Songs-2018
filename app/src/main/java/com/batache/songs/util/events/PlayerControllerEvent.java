package com.batache.songs.util.events;

import org.greenrobot.eventbus.EventBus;

public class PlayerControllerEvent {

  public Action action;

  public PlayerControllerEvent(Action action) {
    this.action = action;
  }

  public enum Action {
    UPDATE_EVENT
  }

  public static void postUpdatePlayerControllers() {
    EventBus.getDefault().post(new PlayerControllerEvent(Action.UPDATE_EVENT));
  }

}
