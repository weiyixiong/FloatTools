package com.wyx.flex.Collection;

import android.app.Activity;
import android.view.MotionEvent;

public interface AppModel {
  Activity getCurrentActivity();

  void dispatchTouchEvents(MotionEvent event);

  boolean isCollectingTouch();

  void completeInput(boolean b, boolean b1);
}