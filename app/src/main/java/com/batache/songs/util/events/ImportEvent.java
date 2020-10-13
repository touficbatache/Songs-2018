package com.batache.songs.util.events;

import org.greenrobot.eventbus.EventBus;

public class ImportEvent {

  public Action action;
  public int progress = 0;

  public ImportEvent(Action action) {
    this.action = action;
  }

  public ImportEvent(Action action, int progress) {
    this.action = action;
    this.progress = progress;
  }

  public enum Action {
    IMPORT_QUEUED, IMPORT_PROGRESS_CHANGED, IMPORT_CANCELLED, IMPORT_COMPLETE
  }

  public static void postImportQueued() {
    EventBus.getDefault().post(new ImportEvent(ImportEvent.Action.IMPORT_QUEUED));
  }

  public static void postImportProgressChanged(int progress) {
    EventBus.getDefault().post(new ImportEvent(ImportEvent.Action.IMPORT_PROGRESS_CHANGED, progress));
  }

  public static void postImportCancelled() {
    EventBus.getDefault().post(new ImportEvent(ImportEvent.Action.IMPORT_CANCELLED));
  }

  public static void postImportComplete() {
    EventBus.getDefault().post(new ImportEvent(Action.IMPORT_COMPLETE));
  }

}
